package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.enums.StateEnum;
import com.example.demo.repository.*;
import com.example.demo.utils.AppConstant;
import com.example.demo.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
public class MessageServiceImpl implements MessageService {

    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final BotSender botSender;
    private final DontPayPermissionRepository dontPayPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final StopManageBotRepository stopManageBotRepository;

    @Override
    public void process(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            User user = commonUtils.getUser(message.getChatId());
            if (user.getState().equals(StateEnum.ACTIVATE_BOT)) {
                checkCode(message);
            }
            if (text.equals("/start"))
                start(message);
            else if (user.getState().equals(StateEnum.START)) {
                switch (text) {
                    case AppConstant.BUY_PERMISSION:
                        buyPermission(message);
                        break;
                    case AppConstant.ACTIVATION_CODE:
                        activateBot(message);
                        break;
                    case AppConstant.CHANNEL_LIST:
                        showRequestList(message);
                        break;
                    case AppConstant.MY_GROUPS:
                        myGroup(message);
                        break;
                }
            }


        }

    }

    private void myGroup(Message message) {
        botSender.exe(getUserGroups(message.getChatId()));
    }

    private void showRequestList(Message message) {
        SendMessage sendMessage = showUserRequests(message.getChatId());
        botSender.exe(sendMessage);
    }

    private void checkCode(Message message) {
        String text = message.getText();
        Optional<DontPayPermission> optional = dontPayPermissionRepository.findById(message.getChatId());
        if (optional.isEmpty()) {
            botSender.exe(AppConstant.DONT_HAVE_BOT_PASS, message.getChatId(), null);
            commonUtils.setState(message.getChatId(), StateEnum.START);
            return;
        }
        DontPayPermission dontPayPermission = optional.get();
        if (!dontPayPermission.getInvoiceNumber().equals(text)) {
            if (dontPayPermission.getAttempt() == 1) {
                dontPayPermissionRepository.delete(dontPayPermission);
                botSender.exe(AppConstant.DONT_HAVE_BOT_PASS, message.getChatId(), start(message.getChatId()));
                commonUtils.setState(message.getChatId(), StateEnum.START);
                return;
            }
            dontPayPermission.setAttempt(dontPayPermission.getAttempt() - 1);
            dontPayPermissionRepository.save(dontPayPermission);
            commonUtils.setState(message.getChatId(), StateEnum.START);
            botSender.exe(AppConstant.DONT_EQUALS_PASS, message.getChatId(), start(message.getChatId()));
            return;
        }
        Optional<Permission> optionalPermission = permissionRepository.findById(message.getChatId());
        if (optionalPermission.isEmpty()) {
            Timestamp expire = Timestamp.valueOf(LocalDateTime.now().plusMonths(dontPayPermission.getMonth()));
            Permission permission = new Permission(message.getChatId(), expire);
            permissionRepository.save(permission);
            commonUtils.setState(message.getChatId(), StateEnum.START);
            botSender.exe(AppConstant.SUCCESSFULLY_BUY, message.getChatId(), start(message.getChatId()));
            return;
        }
        Permission permission = optionalPermission.get();
        Timestamp expire = Timestamp.valueOf(permission.getExpire().toLocalDateTime().plusMonths(dontPayPermission.getMonth()));
        permission.setExpire(expire);
        permissionRepository.save(permission);
        dontPayPermissionRepository.delete(dontPayPermission);
        commonUtils.setState(message.getChatId(), StateEnum.START);
        botSender.exe(AppConstant.SUCCESSFULLY_LENGTHENED, message.getChatId(), start(message.getChatId()));

    }

    private void activateBot(Message message) {
        commonUtils.setState(message.getChatId(), StateEnum.ACTIVATE_BOT);
        botSender.exe(AppConstant.SEND_CODE, message.getChatId(), null);
    }

    private void buyPermission(Message message) {
        ReplyKeyboard replyKeyboard = buttonService.withData(List.of(AppConstant.ONE_MONTH, AppConstant.SIX_MONTH, AppConstant.ONE_YEAR), 2);
        botSender.exe(AppConstant.BUY_PERMISSION_DESCRIPTION, message.getChatId(), replyKeyboard);
    }

    private void start(Message message) {
        commonUtils.setState(message.getChatId(), StateEnum.START);
        botSender.exe(AppConstant.START_MESSAGE, message.getChatId(), start(message.getChatId()));
    }

    @Override
    public ReplyKeyboard start(Long chatId) {
        List<String> buttons = new ArrayList<>();
        if (!joinRequestRepository.findAllByUserId(chatId).isEmpty()) {
            buttons.add(AppConstant.CHANNEL_LIST);
        }
        if (!groupRepository.findAllByUserId(chatId).isEmpty()) {
            buttons.add(AppConstant.MY_GROUPS);
        }
        if (dontPayPermissionRepository.findById(chatId).isPresent()) {
            buttons.add(AppConstant.ACTIVATION_CODE);
        }
        buttons.add(AppConstant.BUY_PERMISSION);
        return buttonService.withString(buttons, 1);
    }

    @Override
    public SendMessage showUserRequests(Long chatId) {
        List<JoinRequest> requests = joinRequestRepository.findAllByUserId(chatId);
        if (requests.isEmpty()) {
            throw new RuntimeException();
        }
        StringBuilder sb = new StringBuilder();
        List<Map<String, String>> list = new ArrayList<>();
        AtomicInteger i = new AtomicInteger(1);
        requests.forEach(r -> {
            if (stopManageBotRepository.findByGroupId(r.getGroupId()).isPresent()) {
                return;
            }
            sb.append(i.getAndIncrement()).append(". ");
            sb.append(botSender.getChatName(r.getGroupId()).getTitle()).append("\n-----------------\n");

            list.add(Map.of(
                    AppConstant.TEXT_BUY, AppConstant.DATA_BUY + r.getGroupId(),
                    AppConstant.TEXT_ABOUT_PRICE, AppConstant.DATA_ABOUT_PRICE + r.getGroupId(),
                    AppConstant.TEXT_REMOVE, AppConstant.DATA_REMOVE + r.getGroupId()));
        });
        ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(list, 1, true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(AppConstant.CHANNEL_LIST_SHOW_TEXT + "\n\n" + sb);
        sendMessage.setReplyMarkup(replyKeyboard);
        return sendMessage;
    }

    @Override
    public SendMessage getUserGroups(Long chatId) {
        List<Group> groups = groupRepository.findAllByUserId(chatId);
        if (groups.isEmpty()) {
            botSender.exe(AppConstant.DONT_HAVE_GROUPS, chatId, null);
            throw new RuntimeException();
        }
        AtomicInteger i = new AtomicInteger(1);
        StringBuilder sb = new StringBuilder();
        List<Map<String, String>> list = new ArrayList<>();
        for (Group group : groups) {
            Map<String, String> map = new HashMap<>();
            Optional<StopManageBot> optionalStopManageBot = stopManageBotRepository.findByGroupId(group.getGroupId());
            sb.append(i.getAndIncrement()).append(". ").append(botSender.getChatName(group.getGroupId()).getTitle());

            map.put(AppConstant.TEXT_CHANGE_PRICE,
                    AppConstant.DATA_CHANGE_PRICE + group.getGroupId());

            if (optionalStopManageBot.isEmpty()) {
                map.put(
                        AppConstant.TEXT_STOP_MANAGE_GROUP,
                        AppConstant.DATA_STOP_MANAGE_GROUP + group.getGroupId());
                list.add(map);
                sb.append("\n");
                continue;
            }
            sb.append(" ").append(AppConstant.STOPED_MANAGE_BOT).append("\n");
            map.put(
                    AppConstant.TEXT_START_MANAGE_GROUP,
                    AppConstant.DATA_START_MANAGE_GROUP + group.getGroupId());

            list.add(map);
        }
        ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(list, 1, false);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(sb.toString());
        sendMessage.setReplyMarkup(replyKeyboard);
        return sendMessage;
    }

}

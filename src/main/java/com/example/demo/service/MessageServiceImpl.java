package com.example.demo.service;

import com.example.demo.entity.DontPayPermission;
import com.example.demo.entity.Permission;
import com.example.demo.entity.User;
import com.example.demo.enums.StateEnum;
import com.example.demo.repository.DontPayPermissionRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.utils.AppConstant;
import com.example.demo.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MessageServiceImpl implements MessageService {

    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final BotSender botSender;
    private final DontPayPermissionRepository dontPayPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;

    @Override
    public void process(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            User user = commonUtils.getUser(message.getChatId());
            if (user.getState().equals(StateEnum.ACTIVATE_BOT)) {
                checkCode(message);
            }
            switch (text) {
                case "/start":
                    start(message);
                    break;
                case AppConstant.BUY_PERMISSION:
                    if (user.getState().equals(StateEnum.START))
                        buyPermission(message);
                    break;
                case AppConstant.ACTIVATION_CODE:
                    if (user.getState().equals(StateEnum.START))
                        activateBot(message);
                    break;
            }

        }

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
        commonUtils.setState(message.getChatId(), StateEnum.BUY_PERMISSION);
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
        if (groupRepository.findByUserId(chatId).isPresent()) {
            buttons.add(AppConstant.MY_GROUPS);
        }
        if (dontPayPermissionRepository.findById(chatId).isPresent()) {
            buttons.add(AppConstant.ACTIVATION_CODE);
        }
        buttons.add(AppConstant.BUY_PERMISSION);
        return buttonService.withString(buttons, 1);
    }
}

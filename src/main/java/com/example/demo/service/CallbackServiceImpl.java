package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.enums.StateEnum;
import com.example.demo.repository.*;
import com.example.demo.utils.AppConstant;
import com.example.demo.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.demo.utils.CommonUtils.setPrice;

@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final DontPayPermissionRepository dontPayPermissionRepository;
    private final InvoiceService invoiceService;
    private final BotSender botSender;
    private final MessageService messageService;
    private final GroupRepository groupRepository;
    private final ButtonService buttonService;
    private final PermissionRepository permissionRepository;
    private final UserOrderRepository userOrderRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final StopManageBotRepository stopManageBotRepository;
    private final DontPayUserOrderRepository dontPayUserOrderRepository;
    private final ChatJoinServiceImpl chatJoinServiceImpl;

    @Override
    public void process(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        if (List.of(AppConstant.ONE_MONTH, AppConstant.SIX_MONTH, AppConstant.ONE_YEAR).contains(data))
            buyPermission(callbackQuery);
        else if (data.startsWith(AppConstant.DATA_ABOUT_PRICE)) {
            showPrice(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_BUY)) {
            buyJoinReq(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_REMOVE)) {
            removeJoinReq(callbackQuery);
        } else if (data.startsWith(AppConstant.BACK_DATA)) {
            back(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_CHANGE_PRICE)) {
            changePrice(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_START_MANAGE_GROUP)) {
            startMangeBot(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_STOP_MANAGE_GROUP)) {
            stopManageBot(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_BUY_JOIN_REQ)) {
            sendInvoice(callbackQuery);
        }
    }

    private void sendInvoice(CallbackQuery callbackQuery) {
        String[] split = callbackQuery.getData().split(":");
        Long month = Long.parseLong(split[1]);
        Long groupId = Long.parseLong(split[split.length - 1]);
        Long userId = callbackQuery.getFrom().getId();
        String invoice = invoiceService.generateInvoice();
        dontPayUserOrderRepository.save(new DontPayUserOrder(null, userId, groupId, month, invoice, 5));
        botSender.exe(AppConstant.YOUR_CODE_FOR_JOIN + invoice, userId, messageService.start(userId));
        botSender.deleteMessage(userId, callbackQuery.getMessage().getMessageId());
    }

    private void stopManageBot(CallbackQuery callbackQuery) {
        Long groupId = Long.parseLong(callbackQuery.getData().split(AppConstant.DATA_STOP_MANAGE_GROUP)[1]);
        stopManageBotRepository.save(new StopManageBot(null, groupId));
        sendStartStopMessage(callbackQuery);
    }

    private void sendStartStopMessage(CallbackQuery callbackQuery) {
        SendMessage userGroups = messageService.getUserGroups(callbackQuery.getFrom().getId());
        botSender.changeText(userGroups.getText(), callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
        botSender.changeReplyKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), userGroups.getReplyMarkup());
    }

    private void startMangeBot(CallbackQuery callbackQuery) {
        Long groupId = Long.parseLong(callbackQuery.getData().split(AppConstant.DATA_START_MANAGE_GROUP)[1]);
        stopManageBotRepository.findByGroupId(groupId).ifPresent(stopManageBotRepository::delete);
        sendStartStopMessage(callbackQuery);
    }

    private void changePrice(CallbackQuery callbackQuery) {
        Long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        setPrice.put(callbackQuery.getFrom().getId(), groupId);

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        commonUtils.setState(callbackQuery.getFrom().getId(), StateEnum.CHANGE_PRICE);
        Group group = groupRepository.findByGroupId(groupId).orElseThrow();
        String append = " сум";
        ;
        if (group.getPriceForMonth() == 0.0) {
            append = " это означаеть что бесплатно";
        }
        String text = AppConstant.SHOW_GROUP_PRICE + decimalFormat.format(group.getPriceForMonth()) + append + "\n\n" + AppConstant.SEND_PRICE_FOR_CHANGE;

        botSender.changeText(text, callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());

        ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(List.of(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + AppConstant.DATA_SHOW_USER_GROUPS)), 1, false);
        botSender.changeReplyKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), replyKeyboard);

    }

    private void back(CallbackQuery callbackQuery) {
        String str = callbackQuery.getData().split(":")[1];
        switch (str) {
            case AppConstant.DATA_BACK_SHOW_REQUESTS -> {
                backToListRequests(callbackQuery);
            }
            case AppConstant.DATA_SHOW_USER_GROUPS -> {
                backToListGroups(callbackQuery);
            }
        }
    }

    private void backToListGroups(CallbackQuery callbackQuery) {
        Long userId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        SendMessage userGroups = messageService.getUserGroups(userId);
        botSender.changeText(userGroups.getText(), userId, messageId);
        botSender.changeReplyKeyboard(userId, messageId, userGroups.getReplyMarkup());

    }

    private void backToListRequests(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getFrom().getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        SendMessage sendMessage = messageService.showUserRequests(chatId);
        botSender.changeText(sendMessage.getText(), chatId, messageId);
        botSender.changeReplyKeyboard(chatId, messageId, sendMessage.getReplyMarkup());

    }

    private void removeJoinReq(CallbackQuery callbackQuery) {
        Long groupId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        List<JoinRequest> requests = joinRequestRepository.findAllByUserId(callbackQuery.getFrom().getId());
        Optional<JoinRequest> first = requests.stream().filter(r -> r.getGroupId().equals(groupId)).findFirst();
        first.ifPresent(r -> {
            joinRequestRepository.delete(r);
            botSender.revokeJoinRequest(r);
            if (callbackQuery.getMessage().getReplyMarkup().getKeyboard().size() == 1) {
                botSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            }

        });
    }

    private void buyJoinReq(CallbackQuery callbackQuery) {
        Long id = Long.parseLong(callbackQuery.getData().split(":")[1]);

        if (stopManageBotRepository.findByGroupId(id).isPresent()) {
            return;
        }

        Group group = groupRepository.findByGroupId(id).orElseThrow();
        if (group.getPriceForMonth() == null || group.getPriceForMonth() == 0.0d) {
            UserOrder userOrder = new UserOrder(null,
                    callbackQuery.getFrom().getId(),
                    group.getGroupId(),
                    Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
            userOrderRepository.save(userOrder);
            JoinRequest joinRequest = joinRequestRepository.findAllByUserId(callbackQuery.getFrom().getId()).stream().filter(r -> r.getGroupId().equals(group.getGroupId())).findFirst().orElseThrow();
            botSender.acceptJoinRequest(joinRequest);
            joinRequestRepository.delete(joinRequest);
            botSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            return;
        }
        Long owner = group.getUserId();
        Optional<Permission> optional = permissionRepository.findById(owner);
        if (optional.isEmpty()) {
            return;
        }
        Permission permission = optional.get();
        List<Map<String, String>> list = new ArrayList<>();
        if (permission.getExpire().after(new Date(System.currentTimeMillis() + 1 * 31 * 24 * 60 * 60))) {
            list.add(
                    Map.of(AppConstant.ONE_MONTH,
                            AppConstant.DATA_BUY_JOIN_REQ + 1 + ":" + AppConstant.DATA_BUY + id));
        } else return;
        if (permission.getExpire().after(new Date(System.currentTimeMillis() + 6 * 31 * 24 * 60 * 60))) {
            list.add(
                    Map.of(AppConstant.SIX_MONTH,
                            AppConstant.DATA_BUY_JOIN_REQ + 6 + ":" + AppConstant.DATA_BUY + id));
        }
        if (permission.getExpire().after(new Date(System.currentTimeMillis() + 12 * 31 * 24 * 60 * 60))) {
            list.add(
                    Map.of(AppConstant.ONE_YEAR,
                            AppConstant.DATA_BUY_JOIN_REQ + 12 + ":" + AppConstant.DATA_BUY + id));
        }
        list.add(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + AppConstant.DATA_BACK_SHOW_REQUESTS));
        ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(list, 1, false);
        botSender.changeText(AppConstant.BUY_PERMISSION_DESCRIPTION, callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
        botSender.changeReplyKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), replyKeyboard);
    }

    private void showPrice(CallbackQuery callbackQuery) {
        Long id = Long.parseLong(callbackQuery.getData().split(":")[1]);
        Group group = groupRepository.findByGroupId(id).orElseThrow();
        if (group.getPriceForMonth() == null) {
            botSender.changeText(AppConstant.PRICE_NULL, callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(List.of(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA + AppConstant.DATA_BACK_SHOW_REQUESTS)), 1, false);
            botSender.changeReplyKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), replyKeyboard);
            return;
        }
        Long userId = group.getUserId();
        Permission permission = permissionRepository.findById(userId).orElseThrow();
        if (permission.getExpire().getTime() < System.currentTimeMillis()) {
            permissionRepository.delete(permission);
            botSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
            botSender.exe(AppConstant.MY_MISTAKE, callbackQuery.getFrom().getId(), null);
            return;
        }
        botSender.changeText(AppConstant.WITH_PRICE, callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
        ReplyKeyboard replyKeyboard = buttonService.callbackKeyboard(List.of(Map.of(AppConstant.BACK_TEXT, AppConstant.BACK_DATA)), 1, false);
        botSender.changeReplyKeyboard(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId(), replyKeyboard);
    }

    private void buyPermission(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getFrom().getId();
        String pass = null;
        if (data.equals(AppConstant.ONE_MONTH)) {
            pass = addToPaymentPermission((short) 1, chatId);
        } else if (data.equals(AppConstant.SIX_MONTH)) {
            pass = addToPaymentPermission((short) 6, chatId);
        } else if (data.equals(AppConstant.ONE_YEAR)) {
            pass = addToPaymentPermission((short) 12, chatId);
        }
        commonUtils.setState(chatId, StateEnum.START);

        botSender.deleteMessage(chatId, callbackQuery.getMessage().getMessageId());
        botSender.exe(AppConstant.AFTER_BUYING_REQUEST + " " + pass, chatId, messageService.start(chatId));
    }

    private String addToPaymentPermission(short month, long chatId) {
        String invoice = invoiceService.generateInvoice();
        dontPayPermissionRepository.save(new DontPayPermission(chatId, month, invoice, 5));
        return invoice;
    }

}

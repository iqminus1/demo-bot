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
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        } else if (data.startsWith(AppConstant.TEXT_CHANGE_PRICE)) {
            changePrice(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_START_MANAGE_GROUP)) {
            startMangeBot(callbackQuery);
        } else if (data.startsWith(AppConstant.DATA_STOP_MANAGE_GROUP)) {
            stopManageBot(callbackQuery);
        }
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

    }

    private void back(CallbackQuery callbackQuery) {
        String str = callbackQuery.getData().split(":")[1];
        switch (str) {
            case AppConstant.DATA_BACK_SHOW_REQUESTS -> {
                backToListRequests(callbackQuery);
            }
        }
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
        Group group = groupRepository.findByGroupId(id).orElseThrow();
        if (group.getPriceForMonth() == null) {
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
        botSender.deleteMessage(callbackQuery.getFrom().getId(), callbackQuery.getMessage().getMessageId());
        botSender.exe(AppConstant.DONT_FREE, callbackQuery.getFrom().getId(), null);
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
        if (permission.getExpire().after(new Date())) {
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

package com.example.demo.service;

import com.example.demo.entity.DontPayPermission;
import com.example.demo.entity.User;
import com.example.demo.enums.StateEnum;
import com.example.demo.repository.DontPayPermissionRepository;
import com.example.demo.utils.AppConstant;
import com.example.demo.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class CallbackServiceImpl implements CallbackService {
    private final CommonUtils commonUtils;
    private final DontPayPermissionRepository dontPayPermissionRepository;
    private final Random random;
    private final BotSender botSender;
    private final MessageService messageService;

    @Override
    public void process(CallbackQuery callbackQuery) {
        User user = commonUtils.getUser(callbackQuery.getFrom().getId());
        StateEnum state = user.getState();
        switch (state) {
            case BUY_PERMISSION -> buyPermission(callbackQuery);
        }
    }

    private void buyPermission(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getFrom().getId();
        if (data.equals(AppConstant.ONE_MONTH)) {
            addToPaymentPermission((short) 1, chatId);
        } else if (data.equals(AppConstant.SIX_MONTH)) {
            addToPaymentPermission((short) 6, chatId);
        } else if (data.equals(AppConstant.ONE_YEAR)) {
            addToPaymentPermission((short) 12, chatId);
        }
        commonUtils.setState(chatId, StateEnum.START);

        botSender.delete(chatId, callbackQuery.getMessage().getMessageId());
        botSender.exe(AppConstant.AFTER_BUYING_REQUEST, chatId, messageService.start(chatId));
    }

    private void addToPaymentPermission(short month, long chatId) {
        String invoice = generateInvoice();
        dontPayPermissionRepository.save(new DontPayPermission(chatId, month, invoice, 5));
    }

    private String generateInvoice() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (random.nextBoolean()) {
                sb.append(random.nextInt(0, 9));
            } else {
                if (random.nextBoolean()) {
                    sb.append((char) random.nextInt(65, 90));
                } else {
                    sb.append((char) random.nextInt(97, 122));
                }
            }
        }
        return sb.toString();
    }
}

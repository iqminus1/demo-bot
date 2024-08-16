package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.enums.StateEnum;
import com.example.demo.utils.AppConstant;
import com.example.demo.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;

@RequiredArgsConstructor
@Service
public class MessageServiceImpl implements MessageService {

    private final CommonUtils commonUtils;
    private final ButtonService buttonService;
    private final BotSender botSender;

    @Override
    public void process(Message message) {
        if (message.hasText()) {
            String text = message.getText();
            User user = commonUtils.getUser(message.getChatId());
            switch (text) {
                case "/start":
                    start(message);
                    break;
                case AppConstant.BUY_PERMISSION:
                    if (user.getState().equals(StateEnum.START))
                        buyPermission(message);
                    break;
            }
        }
    }

    private void buyPermission(Message message) {
        commonUtils.setState(message.getChatId(), StateEnum.BUY_PERMISSION);
        ReplyKeyboard replyKeyboard = buttonService.withData(List.of(AppConstant.ONE_MONTH, AppConstant.SIX_MONTH, AppConstant.ONE_YEAR),2);
        botSender.exe(AppConstant.BUY_PERMISSION_DESCRIPTION,message.getChatId(),replyKeyboard);
    }

    private void start(Message message) {
        commonUtils.setState(message.getChatId(), StateEnum.START);
        ReplyKeyboard replyKeyboard = buttonService.withString(List.of(AppConstant.BUY_PERMISSION), 1);
        botSender.exe(AppConstant.START_MESSAGE, message.getChatId(), replyKeyboard);
    }
}

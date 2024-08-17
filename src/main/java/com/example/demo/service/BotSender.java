package com.example.demo.service;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotSender extends DefaultAbsSender {
    public BotSender(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }

    public void exe(String text, Long chatId, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(replyKeyboard);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getOwner(Long chatId) {
        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(chatId);

        try {
            return execute(getChatAdministrators).stream().filter(a -> a.getStatus().equals("creator")).map(a -> a.getUser().getId()).findFirst().orElseThrow();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


}

package com.example.demo.service;

import com.example.demo.entity.JoinRequest;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotSender extends DefaultAbsSender {
    public BotSender(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }

    public void kickUser(Long userId, Long chatId) {
        BanChatMember banChatMember = new BanChatMember();
        banChatMember.setChatId(chatId);
        banChatMember.setUserId(userId);

        UnbanChatMember unbanChatMember = new UnbanChatMember();
        unbanChatMember.setChatId(chatId);
        unbanChatMember.setUserId(userId);
        try {
            execute(banChatMember);

            execute(unbanChatMember);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void exe(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
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

    public Long getOwner(Long chatId) {
        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(chatId);

        try {
            return execute(getChatAdministrators).stream().filter(a -> a.getStatus().equals("creator")).map(a -> a.getUser().getId()).findFirst().orElseThrow();
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptJoinRequest(JoinRequest joinRequest) {
        ApproveChatJoinRequest acceptJoinReq = new ApproveChatJoinRequest();
        acceptJoinReq.setUserId(joinRequest.getUserId());
        acceptJoinReq.setChatId(joinRequest.getGroupId());
        try {
            execute(acceptJoinReq);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void revokeJoinRequest(JoinRequest joinRequest) {
        DeclineChatJoinRequest declineChatJoinRequest = new DeclineChatJoinRequest();
        declineChatJoinRequest.setChatId(joinRequest.getGroupId());
        declineChatJoinRequest.setUserId(joinRequest.getUserId());
        try {
            execute(declineChatJoinRequest);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeText(String text, long chatId, int messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setText(text);
        editMessageText.setMessageId(messageId);
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeReplyKeyboard(long chatId, int messageId, ReplyKeyboard replyKeyboard) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup((InlineKeyboardMarkup) replyKeyboard);
        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(chatId);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Chat getChatName(Long chatId) {
        GetChat getChat = new GetChat();
        getChat.setChatId(chatId);
        try {
            return execute(getChat);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

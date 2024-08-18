package com.example.demo.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public interface MessageService {
    void process(Message message);

    ReplyKeyboard start(Long chatId);

    SendMessage showUserRequests(Long chatId);
    SendMessage getUserGroups(Long chatId);
}

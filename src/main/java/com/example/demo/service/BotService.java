package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class BotService extends TelegramLongPollingBot {
    private final MessageService messageService;

    public BotService() {
        super(new DefaultBotOptions(), "7320858493:AAFtIOr8bofMTKFuMjegu8SVuxxrdTMYagI");
        messageService = new MessageServiceImpl();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            messageService.process(update.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return "";
    }
}

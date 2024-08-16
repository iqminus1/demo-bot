package com.example.demo.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Service
public class BotService extends TelegramLongPollingBot {
    private final MessageService messageService;


    @SneakyThrows
    public BotService(MessageService messageService) {
        super(new DefaultBotOptions(), "7320858493:AAFtIOr8bofMTKFuMjegu8SVuxxrdTMYagI");
        this.messageService = messageService;
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(this);

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            messageService.process(update.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return "manager_groups_v1_bot";
    }
}

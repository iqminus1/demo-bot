package com.example.demo.service;

import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class BotSender extends DefaultAbsSender {
    public BotSender(DefaultBotOptions options, String botToken) {
        super(options, botToken);
    }
}

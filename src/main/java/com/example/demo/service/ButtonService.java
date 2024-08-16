package com.example.demo.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.List;
import java.util.Map;

public interface ButtonService {
    ReplyKeyboard withString(List<String> buttons, int rowSize);

    ReplyKeyboard callbackKeyboard(Map<String, String> textData, int rowSize);

    ReplyKeyboard withData(List<String> buttons, int rowSize);
}

package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ButtonServiceImpl implements ButtonService {
    @Override
    public ReplyKeyboard withString(List<String> buttons, int rowSize) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        int i = 1;
        for (String button : buttons) {
            row.add(new KeyboardButton(button));
            if (rowSize % i == 0) {
                rows.add(row);
                row = new KeyboardRow();
                i = 0;
            }
            i++;
        }
        if (rowSize > buttons.size() || rowSize % buttons.size() != 0)
            rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard callbackKeyboard(List<Map<String, String>> textData, int rowSize, boolean incremented) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int i = 1;
        for (Map<String, String> map : textData) {

            for (String text : map.keySet()) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setCallbackData(map.get(text));
                if (incremented) text = i + ". " + text;
                button.setText(text);
                row.add(button);
            }

            if (rowSize % i == 0) {
                rows.add(row);
                row = new ArrayList<>();
                i = 0;
            }
            i++;

        }
        markup.setKeyboard(rows);
        return markup;
    }

    @Override
    public ReplyKeyboard withData(List<String> buttons, int rowSize) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int i = 1;
        for (String text : buttons) {
            InlineKeyboardButton button = new InlineKeyboardButton(text);
            button.setCallbackData(text);
            row.add(button);
            if (rowSize % i == 0) {
                rows.add(row);
                row = new ArrayList<>();
                i = 0;
            }
            i++;
        }
        markup.setKeyboard(rows);
        return markup;
    }
}

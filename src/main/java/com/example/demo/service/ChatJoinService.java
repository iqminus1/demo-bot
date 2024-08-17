package com.example.demo.service;

import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;

public interface ChatJoinService {
    void process(ChatJoinRequest chatJoinRequest);
}

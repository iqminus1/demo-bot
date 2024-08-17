package com.example.demo.service;

import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;

public interface ChatMemberService {
    void process(ChatMemberUpdated chatMember);
}

package com.example.demo.service;

import com.example.demo.entity.Group;
import com.example.demo.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChatMemberServiceImpl implements ChatMemberService {

    private final GroupRepository groupRepository;
    private final BotSender botSender;

    public ChatMemberServiceImpl(GroupRepository groupRepository, BotSender botSender) {
        this.groupRepository = groupRepository;
        this.botSender = botSender;
    }

    @Override
    public void process(ChatMemberUpdated chatMember) {
        ChatMember newChatMember = chatMember.getNewChatMember();
        if (newChatMember != null) {
            if (Objects.equals(newChatMember.getUser().getUserName(), "manager_groups_v1_bot")) {
                if (newChatMember.getStatus().equals("left"))
                    deleteFromUserGroup(chatMember);
                else if (List.of("member", "administrator").contains(newChatMember.getStatus()))
                    addToUserGroup(chatMember);
            }
        }


    }

    private void deleteFromUserGroup(ChatMemberUpdated chatMember) {
        Long groupId = chatMember.getChat().getId();
        Optional<Group> optional = groupRepository.findByGroupId(groupId);
        if (optional.isPresent()) {
            Group group = optional.get();
            groupRepository.delete(group);
        }
    }

    private void addToUserGroup(ChatMemberUpdated chatMember) {
        Long creator = botSender.getOwner(chatMember.getChat().getId());
        Group group = new Group(null, chatMember.getChat().getId(), creator, null);
        groupRepository.save(group);
    }


}

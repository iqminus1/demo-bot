package com.example.demo.service;

import com.example.demo.entity.Group;
import com.example.demo.entity.JoinRequest;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.JoinRequestRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.utils.AppConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatJoinServiceImpl implements ChatJoinService {

    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final BotSender botSender;
    private final JoinRequestRepository joinRequestRepository;

    @Override
    public void process(ChatJoinRequest chatJoinRequest) {
        Long groupId = chatJoinRequest.getChat().getId();
        Group group = groupRepository.findByGroupId(groupId).orElseThrow();
        Long owner = group.getUserId();
        if (permissionRepository.findById(owner).isEmpty()) {
            botSender.exe(AppConstant.OWNER_NOT_BUY_PERMISSION, owner, null);
            return;
        }
        Long userId = chatJoinRequest.getUser().getId();
        botSender.exe(AppConstant.USER_MUST_BUY, userId, null);
        List<JoinRequest> list = joinRequestRepository.findAllByUserId(userId).stream().filter(r -> r.getUserId().equals(userId) && r.getGroupId().equals(groupId)).toList();
        if (list.isEmpty()) {
            JoinRequest joinRequest = new JoinRequest(null, userId, groupId, null);
            joinRequestRepository.save(joinRequest);
        }
    }
}

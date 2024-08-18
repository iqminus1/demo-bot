package com.example.demo.service;

import com.example.demo.entity.Group;
import com.example.demo.entity.JoinRequest;
import com.example.demo.entity.UserOrder;
import com.example.demo.repository.*;
import com.example.demo.utils.AppConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.ChatJoinRequest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatJoinServiceImpl implements ChatJoinService {

    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final BotSender botSender;
    private final JoinRequestRepository joinRequestRepository;
    private final StopManageBotRepository stopManageBotRepository;
    private final UserOrderRepository userOrderRepository;

    @Override
    public void process(ChatJoinRequest chatJoinRequest) {
        Long groupId = chatJoinRequest.getChat().getId();
        if (stopManageBotRepository.findByGroupId(groupId).isPresent()) {
            return;
        }
        Group group = groupRepository.findByGroupId(groupId).orElseThrow();
        Long owner = group.getUserId();
        if (permissionRepository.findById(owner).isEmpty()) {
            botSender.exe(AppConstant.OWNER_NOT_BUY_PERMISSION, owner, null);
            return;
        }
        Optional<UserOrder> first = userOrderRepository.findAllByUserId(chatJoinRequest.getUser().getId()).stream().filter(or -> or.getChatId().equals(groupId) && or.getExpire().after(new Date())).findFirst();
        if (first.isEmpty()) {

            Long userId = chatJoinRequest.getUser().getId();
            try {
                SendMessage sendMessage = new SendMessage(userId.toString(), AppConstant.USER_MUST_BUY);
                botSender.execute(sendMessage);
                List<JoinRequest> list = joinRequestRepository.findAllByUserId(userId).stream().filter(r -> r.getGroupId().equals(groupId)).toList();
                if (list.isEmpty()) {
                    JoinRequest joinRequest = new JoinRequest(null, userId, groupId, null);
                    joinRequestRepository.save(joinRequest);
                }
            } catch (Exception e) {
                JoinRequest req = new JoinRequest(null, userId, groupId, null);
                botSender.acceptJoinRequest(req);
                botSender.revokeJoinRequest(req);

                botSender.exe(AppConstant.RESEND_LINK + chatJoinRequest.getInviteLink().getInviteLink(), userId, null);
            }
        }
        botSender.acceptJoinRequest(new JoinRequest(null, chatJoinRequest.getUser().getId(), groupId, null));
    }
}

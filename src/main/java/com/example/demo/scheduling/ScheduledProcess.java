package com.example.demo.scheduling;

import com.example.demo.entity.Permission;
import com.example.demo.entity.UserOrder;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.UserOrderRepository;
import com.example.demo.service.BotSender;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@EnableScheduling
@Component
public class ScheduledProcess {
    private final PermissionRepository permissionRepository;
    private final GroupRepository groupRepository;
    private final UserOrderRepository userOrderRepository;
    private final BotSender botSender;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    private void clearPermissionsFromUser() {
        List<Permission> endedPermissions = permissionRepository.findAll().stream()
                .filter(p -> p.getExpire().getTime() < System.currentTimeMillis())
                .toList();
        permissionRepository.deleteAll(endedPermissions);
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    public void clearOrders() {
        List<UserOrder> expiredOrders = userOrderRepository.findAll().stream().filter(r -> r.getExpire().getTime() < System.currentTimeMillis()).toList();
        expiredOrders.forEach(order -> {
            botSender.kickUser(order.getUserId(), order.getChatId());
            userOrderRepository.delete(order);
        });
    }

    @Scheduled(fixedDelay = 45, timeUnit = TimeUnit.MINUTES)
    private void checkCreators() {
        groupRepository.findAll().stream()
                .peek(g -> {
                    Long owner = botSender.getOwner(g.getGroupId());
                    if (!owner.equals(g.getUserId())) {
                        g.setUserId(owner);
                        groupRepository.save(g);
                    }
                });
    }
}

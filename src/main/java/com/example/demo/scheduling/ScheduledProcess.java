package com.example.demo.scheduling;

import com.example.demo.entity.Permission;
import com.example.demo.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@EnableScheduling
public class ScheduledProcess {
    private final PermissionRepository permissionRepository;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.HOURS)
    private void clearPermissionsFromUser() {
        List<Permission> endedPermissions = permissionRepository.findAll().stream()
                .filter(p -> p.getExpire().after(new Date()))
                .toList();
        permissionRepository.deleteAll(endedPermissions);
    }
}

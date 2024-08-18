package com.example.demo.utils;

import com.example.demo.entity.User;
import com.example.demo.enums.StateEnum;
import com.example.demo.repository.UserRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@EnableScheduling
public class CommonUtils {
    public static Map<Long, Long> setPrice = new HashMap<>();
    private final UserRepository userRepository;

    private final List<User> users = Collections.synchronizedList(new ArrayList<>());

    public void setState(Long chatId, StateEnum newState) {
        User user = getUser(chatId);
        user.setState(newState);
    }

    public User getUser(Long chatId) {
        User user = users.stream()
                .filter(u -> u.getId().equals(chatId))
                .findFirst()
                .orElse(null);

        if (user == null) {
            user = userRepository.findById(chatId)
                    .orElseGet(() -> {
                        User newUser = new User(chatId, StateEnum.START);
                        userRepository.save(newUser);
                        users.add(newUser);
                        return newUser;
                    });
        }
        return user;
    }

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.MINUTES)
    public void save() {
        userRepository.saveAll(users);
        users.clear();
    }

    @PreDestroy
    public void destroy() {
        save();
    }

}

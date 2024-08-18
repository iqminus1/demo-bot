package com.example.demo.repository;

import com.example.demo.entity.StopManageBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StopManageBotRepository extends JpaRepository<StopManageBot, Long> {
    Optional<StopManageBot> findByGroupId(Long chatId);
}
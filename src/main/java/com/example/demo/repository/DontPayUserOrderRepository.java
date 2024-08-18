package com.example.demo.repository;

import com.example.demo.entity.DontPayUserOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DontPayUserOrderRepository extends JpaRepository<DontPayUserOrder, Long> {
    List<DontPayUserOrder> findAllByUserId(Long chatId);
}
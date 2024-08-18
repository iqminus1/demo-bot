package com.example.demo.repository;

import com.example.demo.entity.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {
    Optional<UserOrder> findByUserId(Long userId);
    List<UserOrder> findAllByUserId(Long userId);
}
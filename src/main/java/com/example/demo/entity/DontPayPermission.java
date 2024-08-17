package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class DontPayPermission {
    @Id
    private Long chatId;

    private short month;

    private String invoiceNumber;

    private Integer attempt;
}

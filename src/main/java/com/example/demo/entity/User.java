package com.example.demo.entity;

import com.example.demo.enums.StateEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity(name = "users")
public class User {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private StateEnum state;

}

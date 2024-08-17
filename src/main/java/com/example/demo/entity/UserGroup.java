package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
public class UserGroup {
    @Id
    private Long chatId;

    @OneToMany
    @ToString.Exclude
    private List<Group> groupIds;


}

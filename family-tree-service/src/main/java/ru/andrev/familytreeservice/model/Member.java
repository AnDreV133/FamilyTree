package ru.andrev.familytreeservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@IdClass(MemberId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    private Integer id;

    @Id
    private Long familyTreeId;

    private String fullname;
}
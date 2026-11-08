package ru.andrev.familytreeservice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "familyTree")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyTree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String treeJson;
}
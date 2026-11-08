package ru.andrev.familytreeservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyTreeView {
    private Integer id;
    private String fullname;
    private List<FamilyTreeView> parents;
}
package ru.andrev.familytreeservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FamilyTreeNodeDto {
    private Integer id;
    private String fullname;
    private List<Integer> parents;
    private List<Integer> childs;
}
package ru.andrev.familytreeservice.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RelationNode {
    private Integer id;
    private List<Integer> parents = new ArrayList<>();
    private List<Integer> childs = new ArrayList<>();
}
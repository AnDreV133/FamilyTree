package ru.andrev.familytreeservice.model;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MemberId implements Serializable {
    private Integer id;
    private Long familyTreeId;
}
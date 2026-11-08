package ru.andrev.familytreeservice.repository;

import ru.andrev.familytreeservice.model.FamilyTree;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyTreeRepository extends JpaRepository<FamilyTree, Long> {
}
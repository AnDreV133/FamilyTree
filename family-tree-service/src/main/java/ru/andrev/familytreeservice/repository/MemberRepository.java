package ru.andrev.familytreeservice.repository;

import ru.andrev.familytreeservice.model.Member;
import ru.andrev.familytreeservice.model.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, MemberId> {

    List<Member> findByFamilyTreeId(Long familyTreeId);

    Optional<Member> findByIdAndFamilyTreeId(Integer id, Long familyTreeId);

    @Query("select coalesce(max(m.id), 0) from Member m where m.familyTreeId = :familyTreeId")
    Integer findMaxIdByFamilyTreeId(@Param("familyTreeId") Long familyTreeId);
}
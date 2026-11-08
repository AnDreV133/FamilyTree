package ru.andrev.familytreeservice.controller;

import ru.andrev.familytreeservice.dto.FamilyTreeNodeDto;
import ru.andrev.familytreeservice.dto.FamilyTreeView;
import ru.andrev.familytreeservice.model.FamilyTree;
import ru.andrev.familytreeservice.model.Member;
import ru.andrev.familytreeservice.service.FamilyTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/familytree")
public class FamilyTreeController {

    @Autowired
    private FamilyTreeService familyTreeService;

    @PostMapping
    public FamilyTree createTree(
            @RequestParam Long userId,
            @RequestParam String name) {
        return familyTreeService.createTree(userId, name);
    }

    @PostMapping("/{memberId}/parent")
    public Member createParent(
            @PathVariable Integer memberId,
            @RequestParam String fullname,
            @RequestParam Long familyTreeId) {
        return familyTreeService.addParent(familyTreeId, memberId, fullname);
    }

    @PostMapping("/{memberId}/child")
    public Member createChild(
            @PathVariable Integer memberId,
            @RequestParam String fullname,
            @RequestParam Long familyTreeId) {
        return familyTreeService.addChild(familyTreeId, memberId, fullname);
    }

    @DeleteMapping("/{memberId}")
    public void deleteMember(
            @PathVariable Integer memberId,
            @RequestParam Long familyTreeId) {
        familyTreeService.deleteMember(familyTreeId, memberId);
    }

    @PutMapping("/{memberId}")
    public Member updateMember(
            @PathVariable Integer memberId,
            @RequestParam String fullname,
            @RequestParam Long familyTreeId) {
        return familyTreeService.updateMember(familyTreeId, memberId, fullname);
    }

    @GetMapping
    public List<FamilyTreeNodeDto> getRelations(@RequestParam Long familyTreeId) {
        return familyTreeService.getRelations(familyTreeId);
    }

    @GetMapping("/{memberId}")
    public FamilyTreeView getTreeFrom(
            @PathVariable Integer memberId,
            @RequestParam Long familyTreeId) {
        return familyTreeService.getTreeFrom(memberId, familyTreeId);
    }
}
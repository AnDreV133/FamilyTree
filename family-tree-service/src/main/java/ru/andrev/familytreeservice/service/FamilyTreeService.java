package ru.andrev.familytreeservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.andrev.familytreeservice.dto.FamilyTreeNodeDto;
import ru.andrev.familytreeservice.dto.FamilyTreeView;
import ru.andrev.familytreeservice.dto.RelationNode;
import ru.andrev.familytreeservice.model.FamilyTree;
import ru.andrev.familytreeservice.model.Member;
import ru.andrev.familytreeservice.model.MemberId;
import ru.andrev.familytreeservice.repository.FamilyTreeRepository;
import ru.andrev.familytreeservice.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FamilyTreeService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FamilyTreeRepository familyTreeRepository;

    public FamilyTree createTree(Long userId, String name) {
        FamilyTree tree = new FamilyTree();
        tree.setUserId(userId);
        tree.setName(name);
        tree.setTreeJson("[]");
        tree = familyTreeRepository.save(tree);

        Integer rootId = generateMemberId(tree.getId());
        memberRepository.save(new Member(rootId, tree.getId(), name));

        List<RelationNode> relations = new ArrayList<>();
        relations.add(new RelationNode(rootId, new ArrayList<>(), new ArrayList<>()));
        saveRelations(tree, relations);
        return tree;
    }

    public Integer generateMemberId(Long familyTreeId) {
        return memberRepository.findMaxIdByFamilyTreeId(familyTreeId) + 1;
    }

    public Member addParent(Long familyTreeId, Integer memberId, String fullname) {
        FamilyTree tree = getTree(familyTreeId);
        requireMember(familyTreeId, memberId);
        List<RelationNode> relations = readRelations(tree);

        Integer newId = generateMemberId(familyTreeId);
        Member parent = memberRepository.save(new Member(newId, familyTreeId, fullname));

        findRelation(relations, memberId).getParents().add(newId);
        relations.add(new RelationNode(newId, new ArrayList<>(), new ArrayList<>(List.of(memberId))));
        saveRelations(tree, relations);
        return parent;
    }

    public Member addChild(Long familyTreeId, Integer memberId, String fullname) {
        FamilyTree tree = getTree(familyTreeId);
        requireMember(familyTreeId, memberId);
        List<RelationNode> relations = readRelations(tree);

        Integer newId = generateMemberId(familyTreeId);
        Member child = memberRepository.save(new Member(newId, familyTreeId, fullname));

        findRelation(relations, memberId).getChilds().add(newId);
        relations.add(new RelationNode(newId, new ArrayList<>(List.of(memberId)), new ArrayList<>()));
        saveRelations(tree, relations);
        return child;
    }

    public void deleteMember(Long familyTreeId, Integer memberId) {
        FamilyTree tree = getTree(familyTreeId);
        List<RelationNode> relations = readRelations(tree);

        relations.removeIf(node -> node.getId().equals(memberId));
        for (RelationNode node : relations) {
            node.getParents().remove(memberId);
            node.getChilds().remove(memberId);
        }
        saveRelations(tree, relations);
        memberRepository.deleteById(new MemberId(memberId, familyTreeId));
    }

    public Member updateMember(Long familyTreeId, Integer memberId, String fullname) {
        Member member = requireMember(familyTreeId, memberId);
        member.setFullname(fullname);
        return memberRepository.save(member);
    }

    public List<FamilyTreeNodeDto> getRelations(Long familyTreeId) {
        FamilyTree tree = getTree(familyTreeId);
        List<RelationNode> relations = readRelations(tree);
        Map<Integer, String> names = namesByMemberId(familyTreeId);

        return relations.stream()
                .sorted(Comparator.comparing(RelationNode::getId))
                .map(node -> new FamilyTreeNodeDto(
                        node.getId(),
                        names.getOrDefault(node.getId(), ""),
                        node.getParents(),
                        node.getChilds()))
                .collect(Collectors.toList());
    }

    public FamilyTreeView getTreeFrom(Integer memberId, Long familyTreeId) {
        FamilyTree tree = getTree(familyTreeId);
        List<RelationNode> relations = readRelations(tree);
        Map<Integer, RelationNode> byId = relations.stream()
                .collect(Collectors.toMap(RelationNode::getId, node -> node));

        if (!byId.containsKey(memberId)) {
            throw new RuntimeException("Member not found");
        }
        return buildView(memberId, byId, namesByMemberId(familyTreeId), new HashSet<>());
    }

    private FamilyTreeView buildView(Integer id,
                                     Map<Integer, RelationNode> byId,
                                     Map<Integer, String> names,
                                     Set<Integer> path) {
        FamilyTreeView view = new FamilyTreeView(id, names.getOrDefault(id, ""), new ArrayList<>());
        path.add(id);
        RelationNode node = byId.get(id);
        if (node != null) {
            for (Integer parentId : node.getParents()) {
                if (!path.contains(parentId) && byId.containsKey(parentId)) {
                    view.getParents().add(buildView(parentId, byId, names, path));
                }
            }
        }
        path.remove(id);
        return view;
    }

    private Map<Integer, String> namesByMemberId(Long familyTreeId) {
        return memberRepository.findByFamilyTreeId(familyTreeId).stream()
                .collect(Collectors.toMap(Member::getId, Member::getFullname));
    }

    private FamilyTree getTree(Long familyTreeId) {
        return familyTreeRepository.findById(familyTreeId)
                .orElseThrow(() -> new RuntimeException("Family tree not found"));
    }

    private Member requireMember(Long familyTreeId, Integer memberId) {
        return memberRepository.findByIdAndFamilyTreeId(memberId, familyTreeId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    private RelationNode findRelation(List<RelationNode> relations, Integer memberId) {
        return relations.stream()
                .filter(node -> node.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Member not found in tree"));
    }

    private List<RelationNode> readRelations(FamilyTree tree) {
        String json = tree.getTreeJson();
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<RelationNode>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse treeJson", e);
        }
    }

    private void saveRelations(FamilyTree tree, List<RelationNode> relations) {
        try {
            tree.setTreeJson(objectMapper.writeValueAsString(relations));
        } catch (Exception e) {
            throw new RuntimeException("Cannot serialize treeJson", e);
        }
        familyTreeRepository.save(tree);
    }
}
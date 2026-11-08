package ru.andrev.familytreeservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.andrev.familytreeservice.model.User;
import ru.andrev.familytreeservice.repository.FamilyTreeRepository;
import ru.andrev.familytreeservice.repository.MemberRepository;
import ru.andrev.familytreeservice.repository.UserRepository;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class FamilyTreeGetIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FamilyTreeRepository familyTreeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        memberRepository.deleteAll();
        familyTreeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getRelationsAfterCreateReturnsSingleRoot() throws Exception {
        long treeId = createTree("Я");

        mockMvc.perform(get("/familytree").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullname").value("Я"))
                .andExpect(jsonPath("$[0].parents").isEmpty())
                .andExpect(jsonPath("$[0].childs").isEmpty());
    }

    @Test
    void getRelationsAfterAddingParentContainsBothLinks() throws Exception {
        long treeId = createTree("Я");
        addParent(treeId, 1, "Мама");

        mockMvc.perform(get("/familytree").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].parents[0]").value(2))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fullname").value("Мама"))
                .andExpect(jsonPath("$[1].childs[0]").value(1));
    }

    @Test
    void getRelationsAfterAddingChildContainsBothLinks() throws Exception {
        long treeId = createTree("Я");
        addChild(treeId, 1, "Сын");

        mockMvc.perform(get("/familytree").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].childs[0]").value(2))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fullname").value("Сын"))
                .andExpect(jsonPath("$[1].parents[0]").value(1));
    }

    @Test
    void getRelationsAfterUpdateReturnsNewFullname() throws Exception {
        long treeId = createTree("Я");

        mockMvc.perform(put("/familytree/1")
                        .param("fullname", "Иван")
                        .param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/familytree").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullname").value("Иван"));
    }

    @Test
    void getRelationsAfterDeleteRemovesNodeAndLinks() throws Exception {
        long treeId = createTree("Я");
        addChild(treeId, 1, "Сын");

        mockMvc.perform(delete("/familytree/2")
                        .param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/familytree").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].childs").isEmpty());
    }

    @Test
    void getTreeFromRootReturnsRootWithoutParents() throws Exception {
        long treeId = createTree("Я");
        addParent(treeId, 1, "Мама");

        mockMvc.perform(get("/familytree/1").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullname").value("Я"))
                .andExpect(jsonPath("$.parents[0].id").value(2))
                .andExpect(jsonPath("$.parents[0].parents").isEmpty());
    }

    @Test
    void getTreeFromMemberReturnsParentChain() throws Exception {
        long treeId = createTree("Я");
        addParent(treeId, 1, "Мама");
        addParent(treeId, 2, "Бабка");

        mockMvc.perform(get("/familytree/1").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parents[0].id").value(2))
                .andExpect(jsonPath("$.parents[0].fullname").value("Мама"))
                .andExpect(jsonPath("$.parents[0].parents[0].id").value(3))
                .andExpect(jsonPath("$.parents[0].parents[0].fullname").value("Бабка"));
    }

    @Test
    void getTreeFromMiddleMemberReturnsItsParents() throws Exception {
        long treeId = createTree("Я");
        addParent(treeId, 1, "Мама");
        addParent(treeId, 2, "Бабка");

        mockMvc.perform(get("/familytree/2").param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.fullname").value("Мама"))
                .andExpect(jsonPath("$.parents[0].id").value(3))
                .andExpect(jsonPath("$.parents[0].fullname").value("Бабка"));
    }

    @Test
    void getUsersReturnsAllUsers() throws Exception {
        userRepository.save(new User(null, "Иван", "ivan", "x"));
        userRepository.save(new User(null, "Пётр", "petr", "x"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name", hasItem("Иван")))
                .andExpect(jsonPath("$[*].name", hasItem("Пётр")));
    }

    @Test
    void getUsersTreeCompareReturnsUsersWithTrees() throws Exception {
        userRepository.save(new User(null, "Иван", "ivan", "x"));
        userRepository.save(new User(null, "Пётр", "petr", "x"));
        long firstTree = createTree("Я");
        long secondTree = createTree("Он");

        mockMvc.perform(get("/users/treecompare")
                        .param("id", String.valueOf(firstTree))
                        .param("id", String.valueOf(secondTree)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private long createTree(String name) throws Exception {
        String response = mockMvc.perform(post("/familytree")
                        .param("userId", "1")
                        .param("name", name))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void addParent(long treeId, int memberId, String fullname) throws Exception {
        mockMvc.perform(post("/familytree/" + memberId + "/parent")
                        .param("fullname", fullname)
                        .param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk());
    }

    private void addChild(long treeId, int memberId, String fullname) throws Exception {
        mockMvc.perform(post("/familytree/" + memberId + "/child")
                        .param("fullname", fullname)
                        .param("familyTreeId", String.valueOf(treeId)))
                .andExpect(status().isOk());
    }
}
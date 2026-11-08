package ru.andrev.authservice;

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
import ru.andrev.authservice.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

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
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void registerReturnsCreatedUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("name", "Иван")
                        .param("login", "ivan")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.login").value("ivan"));
    }

    @Test
    void loginWithCorrectPasswordReturnsUser() throws Exception {
        register("Иван", "ivan", "123");

        mockMvc.perform(post("/auth/login")
                        .param("login", "ivan")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иван"))
                .andExpect(jsonPath("$.login").value("ivan"));
    }

    @Test
    void loginWithWrongPasswordReturnsEmptyBody() throws Exception {
        register("Иван", "ivan", "123");

        mockMvc.perform(post("/auth/login")
                        .param("login", "ivan")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    private void register(String name, String login, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .param("name", name)
                        .param("login", login)
                        .param("password", password))
                .andExpect(status().isOk());
    }
}
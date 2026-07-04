package com.nikhil.multitenant.it;


import com.nikhil.multitenant.dto.LoginRequestDto;
import com.nikhil.multitenant.dto.LoginResponseDto;
import com.nikhil.multitenant.dto.ProjectResponseDto;
import com.nikhil.multitenant.model.Project;
import com.nikhil.multitenant.model.Role;
import com.nikhil.multitenant.model.Tenant;
import com.nikhil.multitenant.model.User;
import com.nikhil.multitenant.repository.ProjectRepository;
import com.nikhil.multitenant.repository.TenantRepository;
import com.nikhil.multitenant.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = "server.port=8080"
)
@ActiveProfiles("test")
@Testcontainers
public class AuthIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("multitenant")
            .withUsername("user")
            .withPassword("user");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private static final Logger logger = LoggerFactory.getLogger(AuthIntegrationTest.class);
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        projectRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();

        Tenant tenant1 = new Tenant();
        tenant1.setName("Test Tenant1");
        tenantRepository.save(tenant1);

        Tenant tenant2 = new Tenant();
        tenant2.setName("Test Tenant2");
        tenantRepository.save(tenant2);

        User user1 = new User();
        user1.setEmail("testuser1@gmail.com");
        user1.setPassword(passwordEncoder.encode("testuser1password"));
        user1.setTenant(tenant1);
        user1.setRole(Role.ADMIN);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("testuser2@gmail.com");
        user2.setPassword(passwordEncoder.encode("testuser2password"));
        user2.setTenant(tenant2);
        user2.setRole(Role.MANAGER);
        userRepository.save(user2);

        User user3 = new User();
        user3.setEmail("testuser3@gmail.com");
        user3.setPassword(passwordEncoder.encode("testuser3password"));
        user3.setTenant(tenant2);
        user3.setRole(Role.USER);
        userRepository.save(user3);

        Project project1 = new Project();
        project1.setName("Test Project 1");
        project1.setTenant(tenant1);
        projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("Test Project 2");
        project2.setTenant(tenant2);
        projectRepository.save(project2);
    }

    private String loginAndGetToken(String email, String password) {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);
        dto.setPassword(password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
                "http://localhost:8080/api/auth/login",
                new HttpEntity<>(dto, headers),
                LoginResponseDto.class
        );
        return response.getBody().getToken();
    }


    @Test
    public void shouldLogin() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("testuser1@gmail.com");
        loginRequestDto.setPassword("testuser1password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequestDto, headers);

        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
                "http://localhost:8080/api/auth/login",
                request,
                LoginResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    public void shouldReturnUnauthorized() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("testuser1@gmail.com");
        loginRequestDto.setPassword("testuser1pass");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDto> request = new HttpEntity<>(loginRequestDto, headers);

        ResponseEntity<LoginResponseDto> response = restTemplate.postForEntity(
                "http://localhost:8080/api/auth/login",
                request,
                LoginResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldReturnAllocatedProjectsForAdmin() {
        String jwtToken = loginAndGetToken("testuser1@gmail.com", "testuser1password");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth(jwtToken);
        HttpEntity<String> request2 = new HttpEntity<>(headers2);

        ResponseEntity<ProjectResponseDto> response2 = restTemplate.exchange(
                "http://localhost:8080/api/projects",
                HttpMethod.GET,
                request2,
                ProjectResponseDto.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getProjects())
                .extracting(Project::getName)
                .containsOnly("Test Project 1")
                .doesNotContain("Test Project 2");
    }

    @Test
    public void shouldReturnAllocatedProjectsForManager() {
        String jwtToken = loginAndGetToken("testuser2@gmail.com", "testuser2password");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth(jwtToken);
        HttpEntity<String> request2 = new HttpEntity<>(headers2);

        ResponseEntity<ProjectResponseDto> response2 = restTemplate.exchange(
                "http://localhost:8080/api/projects",
                HttpMethod.GET,
                request2,
                ProjectResponseDto.class
        );
        logger.info("Status code: {}", response2.getStatusCode());
        logger.info("Response Body: {}", response2.getBody());
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isNotNull();
        assertThat(response2.getBody().getProjects())
                .extracting(Project::getName)
                .containsOnly("Test Project 2")
                .doesNotContain("Test Project 1");
    }

    @Test
    public void returnOkForAdminForAdminDashboard(){
        String jwtToken = loginAndGetToken("testuser1@gmail.com", "testuser1password");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth(jwtToken);
        HttpEntity<String> request2 = new HttpEntity<>(headers2);

        ResponseEntity<String> response2 = restTemplate.exchange(
                "http://localhost:8080/api/admin/dashboard",
                HttpMethod.GET,
                request2,
                String.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isNotNull();
    }

    @Test
    public void returnUnauthorizedForUserForAdminDashboard(){
        String jwtToken = loginAndGetToken("testuser3@gmail.com", "testuser3password");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth(jwtToken);
        HttpEntity<String> request2 = new HttpEntity<>(headers2);

        ResponseEntity<String> response2 = restTemplate.exchange(
                "http://localhost:8080/api/admin/dashboard",
                HttpMethod.GET,
                request2,
                String.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response2.getBody()).isNull();
    }

    @Test
    public void returnUnauthorizedForManagerForAdminDashboard(){
        String jwtToken = loginAndGetToken("testuser2@gmail.com", "testuser2password");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth(jwtToken);
        HttpEntity<String> request2 = new HttpEntity<>(headers2);

        ResponseEntity<String> response2 = restTemplate.exchange(
                "http://localhost:8080/api/admin/dashboard",
                HttpMethod.GET,
                request2,
                String.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response2.getBody()).isNull();
    }

    @Test
    public void returnOkForManagerForManagerProjects(){
        String jwtToken = loginAndGetToken("testuser2@gmail.com", "testuser2password");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth(jwtToken);
        HttpEntity<String> request2 = new HttpEntity<>(headers2);

        ResponseEntity<String> response2 = restTemplate.exchange(
                "http://localhost:8080/api/manager/projects",
                HttpMethod.GET,
                request2,
                String.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isNotNull();
    }

    @Test
    public void returnUnauthorizedForUserForManagerProjects(){
        String jwtToken = loginAndGetToken("testuser3@gmail.com", "testuser3password");

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setBearerAuth(jwtToken);
        HttpEntity<String> request2 = new HttpEntity<>(headers2);

        ResponseEntity<String> response2 = restTemplate.exchange(
                "http://localhost:8080/api/manager/projects",
                HttpMethod.GET,
                request2,
                String.class
        );
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response2.getBody()).isNull();
    }
}

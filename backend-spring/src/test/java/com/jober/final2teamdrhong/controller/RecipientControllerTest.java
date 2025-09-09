package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.recipient.RecipientRequest;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "test@example.com", roles = "USER") // 인증된 목 유저 설정
class RecipientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private Workspace testWorkspace;

    @BeforeEach
    void setUp() {
        // 1. 각 테스트 실행 전, 데이터베이스를 깨끗한 상태로 만들기 위해 모든 관련 테이블의 데이터를 삭제합니다.
        recipientRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
        // 2. H2 DB의 ID 시퀀스를 초기화하여 항상 일관된 ID로 테스트를 시작하도록 보장합니다.
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN users_id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE workspace ALTER COLUMN workspace_id RESTART WITH 1");

        // 3. 테스트에서 사용할 기본 사용자(ID=1)와 워크스페이스(ID=1)를 생성하고 DB에 저장합니다.
        //    이는 컨트롤러의 하드코딩된 currentUserId = 1 과 일치시키기 위함입니다.
        testUser = User.builder()
                .userName("테스트유저")
                .userEmail("test@example.com")
                .userNumber("010-1234-5678")
                .build();
        userRepository.save(testUser);

        testWorkspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url")
                .representerName("대표")
                .representerPhoneNumber("010-1111-2222")
                .companyName("테스트 회사")
                .user(testUser)
                .build();
        workspaceRepository.save(testWorkspace);
    }

    @Test
    @DisplayName("수신자 생성 성공 테스트")
    void createRecipient_Success() throws Exception {
        // given (테스트 준비)
        // 1. API 요청 본문에 담아 보낼 DTO 객체를 생성합니다.
        RecipientRequest.CreateDTO createDTO = RecipientRequest.CreateDTO.builder()
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .recipientMemo("VIP 고객")
                .build();

        // 2. DTO 객체를 JSON 문자열로 변환합니다.
        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when (테스트 실행)
        // 1. MockMvc를 사용하여 POST /workspaces/{workspaceId}/recipients 엔드포인트로 API 요청을 보냅니다.
        //    - contentType을 application/json으로 설정합니다.
        //    - content에 위에서 만든 JSON 문자열을 담습니다.
        //    - with(csrf())를 통해 CSRF 보호를 통과시킵니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/" + testWorkspace.getWorkspaceId() + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf())
        );

        // then (결과 검증)
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 201 Created 인지 확인합니다.
                .andExpect(status().isCreated())
                // 1-2. 응답 JSON 본문에 recipientId 필드가 존재하는지 확인합니다.
                .andExpect(jsonPath("$.recipientId").exists())
                // 1-3. recipientName 필드의 값이 요청한 데이터와 일치하는지 확인합니다.
                .andExpect(jsonPath("$.recipientName").value("홍길동"))
                // 1-4. recipientPhoneNumber 필드의 값이 요청한 데이터와 일치하는지 확인합니다.
                .andExpect(jsonPath("$.recipientPhoneNumber").value("010-1234-5678"))
                // 1-5. createdAt 필드가 null이 아닌 값으로 채워져 있는지 확인합니다.
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("수신자 생성 실패 테스트 - 유효성 검사 실패 (이름 누락)")
    void createRecipient_Fail_Validation() throws Exception {
        // given
        // 1. DTO의 @NotBlank 제약조건을 위반하는, 비어있는 recipientName을 가진 DTO를 준비합니다.
        RecipientRequest.CreateDTO createDTO = RecipientRequest.CreateDTO.builder()
                .recipientName("") // @NotBlank 위반
                .recipientPhoneNumber("010-1234-5678")
                .build();

        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when
        // 1. 유효하지 않은 데이터로 생성 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/" + testWorkspace.getWorkspaceId() + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf())
        );

        // then
        // 1. 컨트롤러의 @Valid 어노테이션에 의해 요청이 서비스 계층으로 전달되기 전에 차단되고,
        //    HTTP 상태 코드 400 Bad Request가 반환되는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수신자 생성 실패 테스트 - 권한 없는 워크스페이스")
    void createRecipient_Fail_UnauthorizedWorkspace() throws Exception {
        // given
        // 1. 존재하지 않거나 내 소유가 아닌 워크스페이스 ID를 임의로 준비합니다.
        Integer unauthorizedWorkspaceId = 999;

        // 2. 요청 본문에 담길 DTO를 준비합니다.
        RecipientRequest.CreateDTO createDTO = RecipientRequest.CreateDTO.builder()
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1234-5678")
                .build();

        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when
        // 1. 다른 사람의 워크스페이스에 수신자 추가를 시도하는 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/" + unauthorizedWorkspaceId + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf())
        );

        // then
        // 1. 서비스 계층의 인가 로직에서 예외를 던지고, GlobalExceptionHandler에 의해
        //    최종적으로 HTTP 상태 코드 400 Bad Request가 반환되는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }
}

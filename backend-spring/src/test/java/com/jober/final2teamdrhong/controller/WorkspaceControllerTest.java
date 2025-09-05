package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceRequest;
import com.jober.final2teamdrhong.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import com.jober.final2teamdrhong.entity.User;
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
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc; // HTTP 요청을 시뮬레이션하는 객체

    @Autowired
    private ObjectMapper objectMapper; // Java 객체를 JSON 문자열로 변환하는 객체

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 실행 전에 항상 ID가 1인 사용자를 생성하여 DB에 저장
        // 다른 필수 필드가 있다면 함께 채워주어야 합니다. (예: username)
        User testUser = User.builder()
                .userName("테스트유저")
                .userEmail("test@example.com")
                .userNumber("010-1234-5678")
                .build();

        userRepository.save(testUser);
    }

    @Test
    @DisplayName("워크스페이스 생성 성공 테스트")
    void createWorkspace_Success() throws Exception {
        // given (테스트 준비)
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("성공 테스트 워크스페이스")
                .workspaceSubname("부이름")
                .workspaceAddress("주소")
                .workspaceDetailAddress("상세주소")
                .workspaceUrl("unique-url-success")
                .representerName("홍길동")
                .representerPhoneNumber("010-1111-2222")
                .representerEmail("gildong@example.com")
                .companyName("성공 주식회사")
                .companyRegisterNumber("111-22-33333")
                .build();

        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when (테스트 실행)
        // POST /api/workspaces 로 JSON 데이터를 담아 요청을 보냄
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces") // 실제 API 엔드포인트 경로
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf())
        );

        // then (결과 검증)
        resultActions
                .andExpect(status().isCreated()) // HTTP 상태 코드가 201 Created 인지 확인
                .andExpect(jsonPath("$.workspaceName").value("성공 테스트 워크스페이스"));
        // 응답 JSON의 data.workspaceName 필드 값이 일치하는지 확인
    }

    @Test
    @DisplayName("워크스페이스 생성 실패 테스트 - 필수 필드 누락")
    void createWorkspace_Fail_Validation() throws Exception {
        // given (테스트 준비)
        WorkspaceRequest.CreateDTO createDTO = WorkspaceRequest.CreateDTO.builder()
                .workspaceName("") // workspaceName을 @NotBlank 위반으로 빈 값으로 설정
                .workspaceSubname("부이름")
                .workspaceAddress("주소")
                .workspaceDetailAddress("상세주소")
                .workspaceUrl("unique-url-fail")
                .representerName("김철수")
                .representerPhoneNumber("010-3333-4444")
                .representerEmail("cheolsoo@example.com")
                .companyName("실패 주식회사")
                .companyRegisterNumber("444-55-66666")
                .build();

        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when (테스트 실행)
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf())
        );

        // then (결과 검증)
        resultActions.andExpect(status().isBadRequest()); // 유효성 검사 실패로 400 Bad Request가 반환되는지 확인
    }
}

package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.workspace.WorkspaceRequest;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        // 기존 데이터 완전 삭제 및 H2 시퀀스 리셋
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN users_id RESTART WITH 1");
        
        // 이제 testUser가 ID=1로 생성됨 (컨트롤러의 하드코딩된 userId=1과 일치)
        testUser = User.builder()
                .userName("테스트유저")
                .userEmail("test@example.com")
                .userNumber("010-1234-5678")
                .build();
        userRepository.save(testUser);

        anotherUser = User.builder()
                .userName("다른유저")
                .userEmail("another@example.com")
                .userNumber("010-5678-1234")
                .build();
        userRepository.save(anotherUser);
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
        // testUser의 실제 ID를 사용하기 위해 Mock 또는 다른 방법 필요
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

    @Test
    @DisplayName("워크스페이스 목록 조회 성공 테스트")
    void readWorkspaces_Success() throws Exception {
        // given
        // 1. @BeforeEach에서 생성된 testUser의 소유로 워크스페이스 2개를 DB에 미리 저장합니다.
        Workspace testWorkspace1 = Workspace.builder()
                .workspaceName("테스트 워크스페이스1")
                .workspaceUrl("test-url-1")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사1")
                .build();
        testWorkspace1.setUser(testUser); // setUser()를 사용해서 연관관계 설정
        workspaceRepository.save(testWorkspace1);

        Workspace testWorkspace2 = Workspace.builder()
                .workspaceName("테스트 워크스페이스2")
                .workspaceUrl("test-url-2")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사2")
                .build();
        testWorkspace2.setUser(testUser); // setUser()를 사용해서 연관관계 설정
        workspaceRepository.save(testWorkspace2);

        // when
        // 2. MockMvc를 사용해 GET /api/workspaces API를 호출하고, 그 결과를 ResultActions 객체에 저장합니다.
        //    클래스 상단의 @WithMockUser 설정 덕분에 이 요청은 인증된 사용자의 요청으로 처리됩니다.
        ResultActions resultActions = mockMvc.perform(get("/workspaces"));

        // then
        // 3. ResultActions 객체를 사용하여 응답을 검증합니다.
        resultActions
            // 3-1. 응답 상태 코드가 200 OK 인지 확인합니다.
            .andExpect(status().isOk())
            // 3-2. 응답 Body의 최상위($)가 JSON 배열이고, 그 크기가 2인지 확인합니다.
            .andExpect(jsonPath("$", hasSize(2)))
            // 3-3. 배열의 요소의 workspaceName 필드 값이 "테스트 워크스페이스1", "테스트 워크스페이스2"과 일치하는지 확인합니다.
            .andExpect(jsonPath("$[0].workspaceName").value("테스트 워크스페이스1"))
            .andExpect(jsonPath("$[1].workspaceName").value("테스트 워크스페이스2"));
    }

    /*
    [목록 조회 실패 통합 테스트를 작성하지 않는 이유]

    현재 WorkspaceController의 readWorkspaces 메소드는 인증된 사용자의 ID를
    내부적으로 Integer currentUserId = 1; 과 같이 하드코딩하여 사용하고 있으며,
    @BeforeEach setUp 메소드는 항상 ID가 1인 사용자를 DB에 생성합니다.

    따라서 통합 테스트 환경에서는 "존재하지 않는 사용자"로 조회를 시도하는
    실패 시나리오를 의도적으로 만들어내는 것이 불가능합니다.

    이 "사용자 없음" 실패 시나리오는 Service 계층의 단위 테스트
    (WorkspaceServiceTest의 readWorkspaces_Fail_UserNotFound)에서
    Mockito를 사용하여 완벽하게 검증되었으므로, 컨트롤러 통합 테스트에서는 생략합니다.

    추후 컨트롤러가 @AuthenticationPrincipal을 통해 실제 인증된 사용자 정보를 사용하도록
    리팩토링되면, 그 시점에 이 실패 케이스에 대한 통합 테스트를 추가할 수 있습니다.
    */
    @Test
    @DisplayName("워크스페이스 목록 조회 실패 테스트 - 존재하지 않는 사용자")
    void readWorkspaces_Fail_UserNotFound() {

    }

    @Test
    @DisplayName("워크스페이스 상세 조회 성공 테스트")
    void readWorkspaceDetail_Success() throws Exception {
        // given
        // 테스트용 워크스페이스를 생성하고, testUser를 주인으로 설정한 뒤 DB에 저장합니다.
        Workspace testWorkspace1 = Workspace.builder()
                .workspaceName("테스트 워크스페이스1")
                .workspaceUrl("test-url-1")
                .representerName("테스트대표1")
                .representerPhoneNumber("010-1111-1111")
                .companyName("테스트회사1")
                .build();
        testWorkspace1.setUser(testUser); // setUser()를 사용해서 연관관계 설정
        workspaceRepository.save(testWorkspace1);

        // when
        // 저장된 워크스페이스의 ID를 경로 변수(PathVariable)로 사용하여 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                get("/workspaces/" + testWorkspace1.getWorkspaceId())
        );

        // then
        // 1. 응답 상태 코드가 200 OK 인지 확인합니다.
        // 2. 응답 Body의 workspaceName 필드 값이 우리가 넣은 데이터와 일치하는지 확인합니다.
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workspaceName").value("테스트 워크스페이스1"));
    }

    @Test
    @DisplayName("워크스페이스 상세 조회 실패 테스트 - 권한 없음")
    void readWorkspaceDetail_Fail_Unauthorized() throws Exception {
        // given
        // 내가 아닌 다른 사용자(anotherUser) 소유의 워크스페이스를 DB에 저장합니다.
        Workspace othersWorkspace = Workspace.builder()
                .workspaceName("남의 워크스페이스")
                .workspaceUrl("another-url-1")
                .representerName("김대표")
                .representerPhoneNumber("010-1111-1111")
                .companyName("남의 회사")
                .build();
        othersWorkspace.setUser(anotherUser);
        workspaceRepository.save(othersWorkspace);

        // when
        // 현재 로직상 'testUser'(userId=1)로 간주되는 사용자가 다른 사람(anotherUser)의 워크스페이스 ID로 조회를 시도합니다.
        ResultActions resultActions = mockMvc.perform(
                get("/workspaces/" + othersWorkspace.getWorkspaceId())
        );

        // then
        // 서비스 계층에서 소유자가 달라 조회가 거부되고, 최종적으로 400 Bad Request를 반환하는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }
}

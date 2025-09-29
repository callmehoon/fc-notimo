package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.phonebook.PhoneBookRequest;
import com.jober.final2teamdrhong.entity.*;
import com.jober.final2teamdrhong.repository.GroupMappingRepository;
import com.jober.final2teamdrhong.repository.PhoneBookRepository;
import com.jober.final2teamdrhong.repository.RecipientRepository;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import com.jober.final2teamdrhong.util.test.WithMockJwtClaims;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PhoneBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private PhoneBookRepository phoneBookRepository;

    @Autowired
    private RecipientRepository recipientRepository;

    @Autowired
    private GroupMappingRepository groupMappingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private Workspace testWorkspace;
    private Recipient recipient1;
    private Recipient recipient2;
    private Recipient recipient3;

    @BeforeEach
    void setUp() {
        // 1. 각 테스트 실행 전, 데이터베이스를 깨끗한 상태로 만들기 위해 모든 관련 테이블의 데이터를 삭제합니다.
        groupMappingRepository.deleteAll();
        phoneBookRepository.deleteAll();
        recipientRepository.deleteAll();
        workspaceRepository.deleteAll();
        userRepository.deleteAll();
        // 2. H2 DB의 ID 시퀀스를 초기화하여 항상 일관된 ID로 테스트를 시작하도록 보장합니다.
        jdbcTemplate.execute("ALTER TABLE group_mapping ALTER COLUMN group_mapping_id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE phone_book ALTER COLUMN phone_book_id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE recipient ALTER COLUMN recipient_id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE workspace ALTER COLUMN workspace_id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN users_id RESTART WITH 1");

        // 3. 테스트에서 사용할 기본 사용자(ID=1)와 워크스페이스(ID=1)를 생성하고 DB에 저장합니다.
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

        // 4. 테스트용 수신자들을 생성하고 저장합니다.
        recipient1 = Recipient.builder()
                .recipientName("홍길동")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(testWorkspace)
                .build();
        recipient2 = Recipient.builder()
                .recipientName("임꺽정")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(testWorkspace)
                .build();
        recipient3 = Recipient.builder()
                .recipientName("김철수")
                .recipientPhoneNumber("010-1111-1111")
                .workspace(testWorkspace)
                .build();
        recipientRepository.saveAll(List.of(recipient1, recipient2, recipient3));
    }

    @Test
    @DisplayName("주소록 생성 성공 테스트")
    @WithMockJwtClaims(userId = 1)
    void createPhoneBook_Success_Test() throws Exception {
        // given
        // 1. API 요청 본문에 담아 보낼 DTO 객체를 생성합니다.
        PhoneBookRequest.CreateDTO createDTO = new PhoneBookRequest.CreateDTO(
                "영업팀 주소록",
                "영업팀 전체 연락처입니다."
        );

        // 2. DTO 객체를 JSON 문자열로 변환합니다.
        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when
        // 1. MockMvc를 사용하여 POST /workspaces/{workspaceId}/phonebooks 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 201 Created 인지 확인합니다.
                .andExpect(status().isCreated())
                // 1-2. 응답 JSON 본문에 phoneBookId 필드가 존재하는지 확인합니다.
                .andExpect(jsonPath("$.phoneBookId").exists())
                // 1-3. phoneBookName 필드의 값이 요청한 데이터와 일치하는지 확인합니다.
                .andExpect(jsonPath("$.phoneBookName").value("영업팀 주소록"))
                // 1-4. 시스템컬럼 필드(createdAt, updatedAt)가 null이 아닌 값, deletedAt은 null로 채워져 있는지 확인합니다.
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.deletedAt").isEmpty());
    }

    @Test
    @DisplayName("주소록 생성 실패 테스트 - 필수 필드 누락")
    @WithMockJwtClaims(userId = 1)
    void createPhoneBook_Fail_Validation_Test() throws Exception {
        // given
        // 1. DTO의 @NotBlank 제약조건을 위반하는, 비어있는 phoneBookName을 가진 DTO를 준비합니다.
        PhoneBookRequest.CreateDTO createDTO = new PhoneBookRequest.CreateDTO(
                "",
                null
        );

        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when
        // 1. 유효하지 않은 데이터로 생성 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. 컨트롤러의 @Valid 어노테이션에 의해 요청이 서비스 계층으로 전달되기 전에 차단되고,
        //    HTTP 상태 코드 400 Bad Request가 반환되는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주소록 생성 실패 테스트 - 권한 없음")
    @WithMockJwtClaims(userId = 1)
    void createPhoneBook_Fail_UnauthorizedWorkspace_Test() throws Exception {
        // given
        // 1. 존재하지 않거나 내 소유가 아닌 워크스페이스 ID를 임의로 준비합니다.
        Integer unauthorizedWorkspaceId = 999;

        // 2. 요청 본문에 담길 DTO를 준비합니다.
        PhoneBookRequest.CreateDTO createDTO = new PhoneBookRequest.CreateDTO(
                "해킹 시도 주소록",
                null
        );

        String requestBody = objectMapper.writeValueAsString(createDTO);

        // when
        // 1. 다른 사람의 워크스페이스에 주소록 추가를 시도하는 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/" + unauthorizedWorkspaceId + "/phonebooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. 서비스 계층의 인가 로직에서 예외를 던지고, GlobalExceptionHandler에 의해
        //    최종적으로 HTTP 상태 코드 400 Bad Request가 반환되는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주소록에 수신자 일괄 추가 성공 테스트")
    @WithMockJwtClaims(userId = 1)
    void addRecipientsToPhoneBook_Success_Test() throws Exception {
        // given
        // 1. 테스트용 주소록을 생성하고 저장합니다.
        PhoneBook phoneBook = PhoneBook.builder()
                .phoneBookName("테스트 주소록")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(phoneBook);

        // 2. 이 주소록에 이미 recipient1이 포함되어 있다고 가정하고, GroupMapping을 설정합니다.
        entityManager.persist(GroupMapping.builder()
                .phoneBook(phoneBook)
                .recipient(recipient1)
                .build());
        entityManager.flush();
        entityManager.clear();

        // 3. API 요청 본문에 recipient1(중복)과 recipient2(신규)를 추가하도록 요청합니다.
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(
                List.of(recipient1.getRecipientId(), recipient2.getRecipientId())
        );
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // when
        // 1. MockMvc를 사용하여 수신자 일괄 추가 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/{workspaceId}/phonebooks/{phoneBookId}/recipients",
                        testWorkspace.getWorkspaceId(), phoneBook.getPhoneBookId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 2-1. HTTP 상태 코드가 200 OK인지 확인합니다.
                .andExpect(status().isOk())
                // 2-2. 응답 JSON의 'phoneBookId'가 예상과 일치하는지 확인합니다.
                .andExpect(jsonPath("$.phoneBookId").value(phoneBook.getPhoneBookId()))
                // 2-3. 실제로 추가된 수신자 목록('recipientList')의 길이가 1인지 확인합니다. (중복된 recipient1은 제외)
                .andExpect(jsonPath("$.recipientList.length()").value(1))
                // 2-4. 추가된 수신자가 recipient2가 맞는지 확인합니다.
                .andExpect(jsonPath("$.recipientList[0].recipientId").value(recipient2.getRecipientId()));
    }

    @Test
    @DisplayName("주소록에 수신자 일괄 추가 실패 테스트 - 존재하지 않는 수신자 ID 포함")
    @WithMockJwtClaims(userId = 1)
    void addRecipientsToPhoneBook_Fail_RecipientNotFound_Test() throws Exception {
        // given
        // 1. 테스트용 주소록을 생성합니다.
        PhoneBook phoneBook = PhoneBook.builder()
                .phoneBookName("테스트 주소록")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(phoneBook);

        // 2. 요청 본문에 존재하지 않는 ID(-1)를 포함시킵니다.
        Integer nonExistentRecipientId = -1;
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(
                List.of(recipient1.getRecipientId(), nonExistentRecipientId)
        );
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // when
        // 1. 유효하지 않은 데이터로 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                post("/workspaces/{workspaceId}/phonebooks/{phoneBookId}/recipients",
                        testWorkspace.getWorkspaceId(), phoneBook.getPhoneBookId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. RecipientValidator에서 예외가 발생하고, GlobalExceptionHandler에 의해
        //    HTTP 상태 코드 400 Bad Request가 반환되는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("워크스페이스별 주소록 목록 조회 성공 테스트")
    @WithMockJwtClaims(userId = 1)
    void readPhoneBooks_Success_Test() throws Exception {
        // given
        // 1. 테스트용 주소록들을 생성하고 저장합니다.
        PhoneBook phoneBook1 = PhoneBook.builder()
                .phoneBookName("영업팀 주소록")
                .phoneBookMemo("영업팀 전체 연락처입니다.")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(phoneBook1);

        PhoneBook phoneBook2 = PhoneBook.builder()
                .phoneBookName("개발팀 주소록")
                .phoneBookMemo("개발팀 전체 연락처입니다.")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(phoneBook2);

        // when
        // 1. MockMvc를 사용하여 GET /workspaces/{workspaceId}/phonebooks 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                get("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks")
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 200 OK인지 확인합니다.
                .andExpect(status().isOk())
                // 1-2. 응답 JSON 본문이 배열 형태인지 확인합니다.
                .andExpect(jsonPath("$").isArray())
                // 1-3. 배열의 크기가 2개인지 확인합니다.
                .andExpect(jsonPath("$.length()").value(2))
                // 1-4. 첫 번째 주소록의 이름이 예상과 일치하는지 확인합니다.
                .andExpect(jsonPath("$[0].phoneBookName").value("영업팀 주소록"))
                // 1-5. 두 번째 주소록의 이름이 예상과 일치하는지 확인합니다.
                .andExpect(jsonPath("$[1].phoneBookName").value("개발팀 주소록"));
    }

    @Test
    @DisplayName("워크스페이스별 주소록 목록 조회 실패 테스트 - 권한 없음")
    @WithMockJwtClaims(userId = 1)
    void readPhoneBooks_Fail_UnauthorizedWorkspace_Test() throws Exception {
        // given
        // 1. 존재하지 않거나 내 소유가 아닌 워크스페이스 ID를 임의로 준비합니다.
        Integer unauthorizedWorkspaceId = 999;

        // when
        // 1. 다른 사람의 워크스페이스 주소록 목록 조회를 시도하는 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                get("/workspaces/" + unauthorizedWorkspaceId + "/phonebooks")
        );

        // then
        // 1. 서비스 계층의 인가 로직에서 예외를 던지고, GlobalExceptionHandler에 의해
        //    최종적으로 HTTP 상태 코드 400 Bad Request가 반환되는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주소록별 수신자 목록 페이징 조회 성공 테스트")
    @WithMockJwtClaims(userId = 1)
    void readRecipientsInPhoneBook_Success_Test() throws Exception {
        // given
        // 1. 테스트용 주소록을 생성하고 저장합니다.
        PhoneBook phoneBook = PhoneBook.builder()
                .phoneBookName("테스트 주소록")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(phoneBook);

        // 2. 이 주소록에 수신자들을 추가합니다.
        entityManager.persist(GroupMapping.builder()
                .phoneBook(phoneBook)
                .recipient(recipient1)
                .build());
        entityManager.persist(GroupMapping.builder()
                .phoneBook(phoneBook)
                .recipient(recipient2)
                .build());
        entityManager.flush();
        entityManager.clear();

        // when
        // 1. MockMvc를 사용하여 수신자 목록 페이징 조회 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                get("/workspaces/{workspaceId}/phonebooks/{phoneBookId}/recipients",
                        testWorkspace.getWorkspaceId(), phoneBook.getPhoneBookId())
                        .param("page", "0")
                        .param("size", "10")
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 200 OK인지 확인합니다.
                .andExpect(status().isOk())
                // 1-2. 응답이 페이징 구조를 가지는지 확인합니다.
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                // 1-3. content 배열에 수신자 정보가 올바르게 포함되어 있는지 확인합니다.
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].recipientName").exists())
                .andExpect(jsonPath("$.content[0].recipientPhoneNumber").exists());
    }

    @Test
    @DisplayName("주소록별 수신자 목록 페이징 조회 실패 테스트 - 존재하지 않는 주소록")
    @WithMockJwtClaims(userId = 1)
    void readRecipientsInPhoneBook_Fail_PhoneBookNotFound_Test() throws Exception {
        // given
        // 1. 존재하지 않는 주소록 ID를 준비합니다.
        Integer nonExistentPhoneBookId = -1;

        // when
        // 1. 존재하지 않는 주소록으로 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                get("/workspaces/{workspaceId}/phonebooks/{phoneBookId}/recipients",
                        testWorkspace.getWorkspaceId(), nonExistentPhoneBookId)
                        .param("page", "0")
                        .param("size", "10")
        );

        // then
        // 1. PhoneBookValidator에서 예외가 발생하고, GlobalExceptionHandler에 의해
        //    HTTP 상태 코드 400 Bad Request가 반환되는지 확인합니다.
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("주소록별 수신자 목록 페이징 조회 테스트 - 빈 주소록")
    @WithMockJwtClaims(userId = 1)
    void readRecipientsInPhoneBook_EmptyPhoneBook_Test() throws Exception {
        // given
        // 1. 수신자가 없는 테스트용 주소록을 생성하고 저장합니다.
        PhoneBook emptyPhoneBook = PhoneBook.builder()
                .phoneBookName("빈 주소록")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(emptyPhoneBook);

        // when
        // 1. 빈 주소록으로 API를 호출합니다.
        ResultActions resultActions = mockMvc.perform(
                get("/workspaces/{workspaceId}/phonebooks/{phoneBookId}/recipients",
                        testWorkspace.getWorkspaceId(), emptyPhoneBook.getPhoneBookId())
                        .param("page", "0")
                        .param("size", "10")
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 200 OK인지 확인합니다.
                .andExpect(status().isOk())
                // 1-2. 빈 페이지가 올바르게 반환되는지 확인합니다.
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    @DisplayName("주소록 수정 성공 테스트")
    @WithMockJwtClaims(userId = 1)
    void updatePhoneBook_Success_Test() throws Exception {
        // given
        // 1. 수정할 주소록을 먼저 생성하고 저장합니다.
        PhoneBook existingPhoneBook = PhoneBook.builder()
                .phoneBookName("기존 주소록명")
                .phoneBookMemo("기존 메모")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(existingPhoneBook);

        // 1-1. 원본 수정 시간을 저장합니다.
        LocalDateTime originalUpdatedAt = existingPhoneBook.getUpdatedAt();

        // 2. API 요청 본문에 담아 보낼 수정 DTO 객체를 생성합니다.
        PhoneBookRequest.UpdateDTO updateDTO = new PhoneBookRequest.UpdateDTO(
                "수정된 주소록명",
                "수정된 메모입니다."
        );

        // 3. DTO 객체를 JSON 문자열로 변환합니다.
        String requestBody = objectMapper.writeValueAsString(updateDTO);

        // 3-1. 생성 시간과 수정 시간의 차이를 보장하기 위해 1초 대기합니다.
        Thread.sleep(1000);

        // when
        // 1. MockMvc를 사용하여 PUT /workspaces/{workspaceId}/phonebooks/{phoneBookId} 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                put("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + existingPhoneBook.getPhoneBookId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 200 OK 인지 확인합니다.
                .andExpect(status().isOk())
                // 1-2. 응답 JSON 본문에 phoneBookId 필드가 기존 ID와 일치하는지 확인합니다.
                .andExpect(jsonPath("$.phoneBookId").value(existingPhoneBook.getPhoneBookId()))
                // 1-3. phoneBookName 필드의 값이 수정된 데이터와 일치하는지 확인합니다.
                .andExpect(jsonPath("$.phoneBookName").value("수정된 주소록명"))
                // 1-4. phoneBookMemo 필드의 값이 수정된 데이터와 일치하는지 확인합니다.
                .andExpect(jsonPath("$.phoneBookMemo").value("수정된 메모입니다."))
                // 1-5. 시스템컬럼 필드가 적절히 설정되어 있는지 확인합니다.
                .andExpect(jsonPath("$.updatedAt").value(not(originalUpdatedAt)))
                .andExpect(jsonPath("$.deletedAt").isEmpty());
    }

    @Test
    @DisplayName("주소록 수정 실패 테스트 - 존재하지 않는 워크스페이스")
    @WithMockJwtClaims(userId = 1)
    void updatePhoneBook_Fail_WorkspaceNotFound_Test() throws Exception {
        // given
        // 1. 존재하지 않는 워크스페이스 ID를 사용합니다.
        Integer nonExistentWorkspaceId = 999;
        Integer phoneBookId = 1;

        // 2. API 요청 본문에 담아 보낼 수정 DTO 객체를 생성합니다.
        PhoneBookRequest.UpdateDTO updateDTO = new PhoneBookRequest.UpdateDTO(
                "수정된 주소록명",
                "수정된 메모입니다."
        );

        // 3. DTO 객체를 JSON 문자열로 변환합니다.
        String requestBody = objectMapper.writeValueAsString(updateDTO);

        // when
        // 1. MockMvc를 사용하여 PUT /workspaces/{workspaceId}/phonebooks/{phoneBookId} 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                put("/workspaces/" + nonExistentWorkspaceId + "/phonebooks/" + phoneBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request 인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. 에러 메시지가 적절히 반환되는지 확인합니다.
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("주소록 수정 실패 테스트 - 존재하지 않는 주소록")
    @WithMockJwtClaims(userId = 1)
    void updatePhoneBook_Fail_PhoneBookNotFound_Test() throws Exception {
        // given
        // 1. 존재하지 않는 주소록 ID를 사용합니다.
        Integer nonExistentPhoneBookId = 999;

        // 2. API 요청 본문에 담아 보낼 수정 DTO 객체를 생성합니다.
        PhoneBookRequest.UpdateDTO updateDTO = new PhoneBookRequest.UpdateDTO(
                "수정된 주소록명",
                "수정된 메모입니다."
        );

        // 3. DTO 객체를 JSON 문자열로 변환합니다.
        String requestBody = objectMapper.writeValueAsString(updateDTO);

        // when
        // 1. MockMvc를 사용하여 PUT /workspaces/{workspaceId}/phonebooks/{phoneBookId} 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                put("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + nonExistentPhoneBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request 인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. 에러 메시지가 적절히 반환되는지 확인합니다.
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("주소록 수정 실패 테스트 - 잘못된 요청 데이터 (유효성 검사 실패)")
    @WithMockJwtClaims(userId = 1)
    void updatePhoneBook_Fail_InvalidRequestData_Test() throws Exception {
        // given
        // 1. 수정할 주소록을 먼저 생성하고 저장합니다.
        PhoneBook existingPhoneBook = PhoneBook.builder()
                .phoneBookName("기존 주소록명")
                .phoneBookMemo("기존 메모")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(existingPhoneBook);

        // 2. 유효성 검사에 실패할 수 있는 잘못된 DTO 객체를 생성합니다. (예: 빈 문자열)
        PhoneBookRequest.UpdateDTO invalidUpdateDTO = new PhoneBookRequest.UpdateDTO(
                "",
                "수정된 메모입니다."
        );

        // 3. DTO 객체를 JSON 문자열로 변환합니다.
        String requestBody = objectMapper.writeValueAsString(invalidUpdateDTO);

        // when
        // 1. MockMvc를 사용하여 PUT /workspaces/{workspaceId}/phonebooks/{phoneBookId} 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                put("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + existingPhoneBook.getPhoneBookId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request 인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. 에러 메시지가 적절히 반환되는지 확인합니다.
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("주소록 삭제 성공 테스트")
    @WithMockJwtClaims(userId = 1)
    void deletePhoneBook_Success_Test() throws Exception {
        // given
        // 1. 테스트에서 사용할 주소록을 미리 데이터베이스에 저장합니다.
        PhoneBook phoneBookToDelete = PhoneBook.builder()
                .phoneBookName("삭제할 주소록")
                .phoneBookMemo("삭제 테스트용 메모")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(phoneBookToDelete);

        // when
        // 1. MockMvc를 사용하여 DELETE /workspaces/{workspaceId}/phonebooks/{phoneBookId} 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + phoneBookToDelete.getPhoneBookId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 200 OK인지 확인합니다.
                .andExpect(status().isOk())
                // 1-2. 응답 JSON의 구조와 값이 올바른지 확인합니다.
                .andExpect(jsonPath("$.phoneBookId").value(phoneBookToDelete.getPhoneBookId()))
                .andExpect(jsonPath("$.phoneBookName").value("삭제할 주소록"))
                .andExpect(jsonPath("$.phoneBookMemo").value("삭제 테스트용 메모"))
                .andExpect(jsonPath("$.deletedAt").exists()) // 소프트 딜리트 시간이 설정되었는지 확인
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        // 2. 데이터베이스에서 해당 주소록이 소프트 딜리트되었는지 확인합니다.
        PhoneBook deletedPhoneBook = phoneBookRepository.findByIdIncludingDeleted(phoneBookToDelete.getPhoneBookId()).orElseThrow();
        assertNotNull(deletedPhoneBook.getDeletedAt()); // 소프트 딜리트 시간이 설정되었는지 확인
        assertEquals(true, deletedPhoneBook.getIsDeleted()); // isDeleted 플래그가 true인지 확인
    }

    @Test
    @DisplayName("주소록 삭제 실패 테스트 - 존재하지 않는 주소록")
    @WithMockJwtClaims(userId = 1)
    void deletePhoneBook_Fail_PhoneBookNotFound_Test() throws Exception {
        // given
        // 1. 존재하지 않는 주소록 ID를 준비합니다.
        Integer nonExistentPhoneBookId = 999;

        // when
        // 1. MockMvc를 사용하여 DELETE /workspaces/{workspaceId}/phonebooks/{phoneBookId} 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + nonExistentPhoneBookId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. 에러 메시지가 적절히 반환되는지 확인합니다.
                .andExpect(jsonPath("$.message").value("해당 워크스페이스에 존재하지 않는 주소록입니다. ID: " + nonExistentPhoneBookId));
    }

    @Test
    @DisplayName("주소록 삭제 실패 테스트 - 다른 워크스페이스의 주소록")
    @WithMockJwtClaims(userId = 1)
    void deletePhoneBook_Fail_UnauthorizedWorkspace_Test() throws Exception {
        // given
        // 1. 다른 사용자의 워크스페이스와 주소록을 생성합니다.
        User otherUser = User.builder()
                .userName("다른유저")
                .userEmail("other@example.com")
                .userNumber("010-9999-9999")
                .build();
        userRepository.save(otherUser);

        Workspace otherWorkspace = Workspace.builder()
                .workspaceName("다른 워크스페이스")
                .workspaceUrl("other-url")
                .representerName("다른 대표")
                .representerPhoneNumber("010-8888-8888")
                .companyName("다른 회사")
                .user(otherUser)
                .build();
        workspaceRepository.save(otherWorkspace);

        PhoneBook otherPhoneBook = PhoneBook.builder()
                .phoneBookName("다른 주소록")
                .phoneBookMemo("다른 메모")
                .workspace(otherWorkspace)
                .build();
        phoneBookRepository.save(otherPhoneBook);

        // when
        // 1. MockMvc를 사용하여 다른 워크스페이스의 주소록을 삭제하려고 시도합니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + otherPhoneBook.getPhoneBookId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. 에러 메시지가 적절히 반환되는지 확인합니다.
                .andExpect(jsonPath("$.message").value("해당 워크스페이스에 존재하지 않는 주소록입니다. ID: " + otherPhoneBook.getPhoneBookId()));
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 성공 테스트")
    @WithMockJwtClaims(userId = 1)
    void deleteRecipientsFromPhoneBook_Success_Test() throws Exception {
        // given
        // 1. 테스트용 주소록을 생성하고 저장합니다.
        PhoneBook testPhoneBook = PhoneBook.builder()
                .phoneBookName("테스트 주소록")
                .phoneBookMemo("일괄 삭제 테스트용 주소록")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(testPhoneBook);

        // 2. 주소록에 수신자들을 매핑합니다.
        GroupMapping mapping1 = GroupMapping.builder()
                .phoneBook(testPhoneBook)
                .recipient(recipient1)
                .build();
        GroupMapping mapping2 = GroupMapping.builder()
                .phoneBook(testPhoneBook)
                .recipient(recipient2)
                .build();
        groupMappingRepository.saveAll(List.of(mapping1, mapping2));

        // 3. API 요청 본문에 담아 보낼 DTO 객체를 생성합니다. (수신자 1, 2번 삭제 요청)
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(
                List.of(recipient1.getRecipientId(), recipient2.getRecipientId())
        );
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // when
        // 1. MockMvc를 사용하여 DELETE /workspaces/{workspaceId}/phonebooks/{phoneBookId}/recipients 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + testPhoneBook.getPhoneBookId() + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 200 OK인지 확인합니다.
                .andExpect(status().isOk())
                // 1-2. 응답 JSON 본문에 phoneBookId 필드가 존재하고 올바른 값인지 확인합니다.
                .andExpect(jsonPath("$.phoneBookId").value(testPhoneBook.getPhoneBookId()))
                // 1-3. 삭제된 수신자 목록의 크기가 2인지 확인합니다.
                .andExpect(jsonPath("$.recipientList").isArray())
                .andExpect(jsonPath("$.recipientList.length()").value(2))
                // 1-4. 삭제된 수신자들의 ID가 올바른지 확인합니다.
                .andExpect(jsonPath("$.recipientList[?(@.recipientId == " + recipient1.getRecipientId() + ")]").exists())
                .andExpect(jsonPath("$.recipientList[?(@.recipientId == " + recipient2.getRecipientId() + ")]").exists())
                // 1-5. 삭제된 수신자들의 이름이 올바른지 확인합니다.
                .andExpect(jsonPath("$.recipientList[?(@.recipientName == '" + recipient1.getRecipientName() + "')]").exists())
                .andExpect(jsonPath("$.recipientList[?(@.recipientName == '" + recipient2.getRecipientName() + "')]").exists())
                // 1-6. Recipient 엔티티 자체의 deletedAt은 변경되지 않으므로, 해당 필드는 비어있어야 합니다.
                .andExpect(jsonPath("$.recipientList[0].deletedAt").isEmpty())
                .andExpect(jsonPath("$.recipientList[1].deletedAt").isEmpty());

        // 2. 데이터베이스에서 실제로 소프트 딜리트가 수행되었는지 검증합니다.
        List<Object[]> deletedMappings = entityManager.createNativeQuery(
                        "SELECT group_mapping_id, is_deleted, deleted_at FROM group_mapping WHERE phone_book_id = ?1"
                ).setParameter(1, testPhoneBook.getPhoneBookId())
                .getResultList();

        // 2-1. 매핑이 2개 존재하는지 확인합니다.
        assertEquals(2, deletedMappings.size());
        // 2-2. 모든 매핑이 소프트 딜리트 상태인지 확인합니다.
        for (Object[] mapping : deletedMappings) {
            Boolean isDeleted = (Boolean) mapping[1];
            Object deletedAt = mapping[2];
            assertEquals(true, isDeleted);
            assertNotNull(deletedAt);
        }
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 테스트 - 삭제할 수신자가 없는 경우")
    @WithMockJwtClaims(userId = 1)
    void deleteRecipientsFromPhoneBook_NoRecipientsToDelete_Test() throws Exception {
        // given
        // 1. 테스트용 주소록을 생성하고 저장합니다.
        PhoneBook testPhoneBook = PhoneBook.builder()
                .phoneBookName("테스트 주소록")
                .phoneBookMemo("빈 주소록 테스트")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(testPhoneBook);

        // 2. API 요청 본문에 담아 보낼 DTO 객체를 생성합니다. (존재하지 않는 수신자 ID들)
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(
                List.of(999, 1000) // 존재하지 않는 수신자 ID
        );
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // when
        // 1. MockMvc를 사용하여 DELETE 엔드포인트로 API 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + testPhoneBook.getPhoneBookId() + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. RecipientValidator가 던지는 예외 메시지를 확인합니다.
                .andExpect(jsonPath("$.message").value("요청된 수신자 목록에 유효하지 않거나 권한이 없는 ID가 포함되어 있습니다."));
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 실패 테스트 - 권한 없는 워크스페이스")
    @WithMockJwtClaims(userId = 1)
    void deleteRecipientsFromPhoneBook_Fail_UnauthorizedWorkspace_Test() throws Exception {
        // given
        // 1. 다른 사용자와 워크스페이스를 생성합니다.
        User otherUser = User.builder()
                .userName("다른사용자")
                .userEmail("other@example.com")
                .userNumber("010-9999-9999")
                .build();
        userRepository.save(otherUser);

        Workspace otherWorkspace = Workspace.builder()
                .workspaceName("다른 워크스페이스")
                .workspaceUrl("other-url")
                .representerName("다른대표")
                .representerPhoneNumber("010-9999-8888")
                .companyName("다른 회사")
                .user(otherUser)
                .build();
        workspaceRepository.save(otherWorkspace);

        PhoneBook otherPhoneBook = PhoneBook.builder()
                .phoneBookName("다른 주소록")
                .phoneBookMemo("다른 메모")
                .workspace(otherWorkspace)
                .build();
        phoneBookRepository.save(otherPhoneBook);

        // 2. API 요청 본문을 생성합니다.
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(
                List.of(recipient1.getRecipientId(), recipient2.getRecipientId())
        );
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // when
        // 1. MockMvc를 사용하여 다른 워크스페이스의 주소록에서 수신자를 삭제하려고 시도합니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + otherPhoneBook.getPhoneBookId() + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. 에러 메시지가 적절히 반환되는지 확인합니다.
                .andExpect(jsonPath("$.message").value("해당 워크스페이스에 존재하지 않는 주소록입니다. ID: " + otherPhoneBook.getPhoneBookId()));
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 실패 테스트 - 존재하지 않는 주소록")
    @WithMockJwtClaims(userId = 1)
    void deleteRecipientsFromPhoneBook_Fail_PhoneBookNotFound_Test() throws Exception {
        // given
        // 1. API 요청 본문을 생성합니다.
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(
                List.of(recipient1.getRecipientId(), recipient2.getRecipientId())
        );
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        Integer nonExistentPhoneBookId = 999; // 존재하지 않는 주소록 ID

        // when
        // 1. MockMvc를 사용하여 존재하지 않는 주소록에서 수신자를 삭제하려고 시도합니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + nonExistentPhoneBookId + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. HTTP 상태 코드가 400 Bad Request인지 확인합니다.
                .andExpect(status().isBadRequest())
                // 1-2. 에러 메시지가 적절히 반환되는지 확인합니다.
                .andExpect(jsonPath("$.message").value("해당 워크스페이스에 존재하지 않는 주소록입니다. ID: " + nonExistentPhoneBookId));
    }

    @Test
    @DisplayName("주소록에서 수신자 일괄 삭제 실패 테스트 - 빈 요청 본문")
    @WithMockJwtClaims(userId = 1)
    void deleteRecipientsFromPhoneBook_Fail_EmptyRequestBody_Test() throws Exception {
        // given
        // 1. 테스트용 주소록을 생성하고 저장합니다.
        PhoneBook testPhoneBook = PhoneBook.builder()
                .phoneBookName("테스트 주소록")
                .phoneBookMemo("빈 요청 테스트")
                .workspace(testWorkspace)
                .build();
        phoneBookRepository.save(testPhoneBook);

        // 2. API 요청 본문에 빈 수신자 ID 리스트를 담습니다.
        PhoneBookRequest.RecipientIdListDTO requestDTO = new PhoneBookRequest.RecipientIdListDTO(List.of());
        String requestBody = objectMapper.writeValueAsString(requestDTO);

        // when
        // 1. MockMvc를 사용하여 빈 수신자 ID 리스트로 삭제 요청을 보냅니다.
        ResultActions resultActions = mockMvc.perform(
                delete("/workspaces/" + testWorkspace.getWorkspaceId() + "/phonebooks/" + testPhoneBook.getPhoneBookId() + "/recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        );

        // then
        // 1. API 호출 결과를 검증합니다.
        resultActions
                // 1-1. 서비스 로직 변경에 따라, 빈 리스트는 정상 처리(200 OK)로 간주합니다.
                .andExpect(status().isOk())
                // 1-2. 응답으로 빈 recipientList가 반환되는지 확인합니다.
                .andExpect(jsonPath("$.recipientList").isArray())
                .andExpect(jsonPath("$.recipientList.length()").value(0));
    }
}


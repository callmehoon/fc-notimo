package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplatePageableRequest;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateUpdateRequest;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.service.IndividualTemplateService;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;
import com.jober.final2teamdrhong.util.test.WithMockJwtClaims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IndividualTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IndividualTemplateService individualTemplateService;

    @MockitoBean
    private WorkspaceValidator workspaceValidator;


    private IndividualTemplateResponse makeResponse(
            Integer id,
            String title,
            String content,
            String buttonTitle,
            Integer workspaceId,
            boolean isDeleted,
            IndividualTemplate.Status status
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new IndividualTemplateResponse(
                id,
                title,
                content,
                buttonTitle,
                workspaceId,
                now.minusMinutes(1),
                now,
                isDeleted,
                status
        );
    }

    // ============================
    // CREATE
    // ============================

    // CREATE
    @Test
    @WithMockJwtClaims
    @DisplayName("빈 템플릿 생성 성공")
    void createEmptyTemplate_success() throws Exception {
        IndividualTemplateResponse expected = makeResponse(1, "t", "c", "b", 99, false, IndividualTemplate.Status.DRAFT);

        given(individualTemplateService.createTemplate(99, 1)).willReturn(expected);

        mockMvc.perform(post("/templates/{workspaceId}", 99)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(1))
                .andExpect(jsonPath("$.workspaceId").value(99));

        verify(individualTemplateService).createTemplate(99, 1);
    }

    @Test
    @WithMockJwtClaims
    @DisplayName("빈 템플릿 생성 비동기 성공")
    void createEmptyTemplateAsync_success() throws Exception {
        Integer workspaceId = 77;
        Integer userId = 1;

        // ✅ validator 동작 mock
        given(workspaceValidator.validateAndGetWorkspace(eq(workspaceId), eq(userId)))
                .willReturn(mock(Workspace.class));

        IndividualTemplateResponse expected =
                makeResponse(2, "async", null, null, workspaceId, false, IndividualTemplate.Status.DRAFT);

        given(individualTemplateService.createTemplateAsync(workspaceId, userId))
                .willReturn(CompletableFuture.completedFuture(expected));

        mockMvc.perform(post("/templates/{workspaceId}/async", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(2))
                .andExpect(jsonPath("$.workspaceId").value(workspaceId));

        // ✅ 검증
        verify(workspaceValidator).validateAndGetWorkspace(workspaceId, userId);
        verify(individualTemplateService).createTemplateAsync(workspaceId, userId);
    }


    @Test
    @WithMockJwtClaims
    @DisplayName("공용 템플릿 기반 생성 성공")
    void createFromPublicTemplate_success() throws Exception {
        IndividualTemplateResponse expected = makeResponse(3, "pub", "cont", "btn", 55, false, IndividualTemplate.Status.DRAFT);

        given(individualTemplateService.createIndividualTemplateFromPublic(10, 55, 1)).willReturn(expected);

        mockMvc.perform(post("/templates/{workspaceId}/from-public/{publicTemplateId}", 55, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(3))
                .andExpect(jsonPath("$.workspaceId").value(55));

        verify(individualTemplateService).createIndividualTemplateFromPublic(10, 55, 1);
    }

    @Test
    @WithMockJwtClaims
    @DisplayName("공용 템플릿 기반 생성 비동기 성공")
    void createFromPublicTemplateAsync_success() throws Exception {
        IndividualTemplateResponse expected = makeResponse(4, "pubAsync", null, null, 44, false, IndividualTemplate.Status.APPROVED);

        given(individualTemplateService.createIndividualTemplateFromPublicAsync(20, 44, 1))
                .willReturn(CompletableFuture.completedFuture(expected));

        mockMvc.perform(post("/templates/{workspaceId}/from-public/{publicTemplateId}/async", 44, 20))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(4))
                .andExpect(jsonPath("$.workspaceId").value(44));

        verify(individualTemplateService).createIndividualTemplateFromPublicAsync(20, 44, 1);
    }

    // ============================
    // READ
    // ============================

    @Test
    @WithMockJwtClaims
    @DisplayName("전체 템플릿 조회 성공")
    void getAllTemplates_success() throws Exception {
        Integer workspaceId = 1;

        // validator mock 설정
        given(workspaceValidator.validateAndGetWorkspace(eq(workspaceId), eq(1)))
                .willReturn(mock(Workspace.class));

        // service mock 설정
        IndividualTemplateResponse row =
                makeResponse(1, "Test", null, null, workspaceId, false, IndividualTemplate.Status.DRAFT);

        given(individualTemplateService.getAllTemplates(eq(workspaceId), eq(1), any(IndividualTemplatePageableRequest.class)))
                .willReturn(new PageImpl<>(List.of(row), PageRequest.of(0, 10), 1));

        // when & then
        mockMvc.perform(get("/{workspaceId}/templates", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].individualTemplateId").value(1))
                .andExpect(jsonPath("$.content[0].individualTemplateTitle").value("Test"));

        // verify
        verify(workspaceValidator).validateAndGetWorkspace(workspaceId, 1);
        verify(individualTemplateService).getAllTemplates(eq(workspaceId), eq(1), any(IndividualTemplatePageableRequest.class));
    }


    @Test
    @WithMockJwtClaims
    @DisplayName("전체 템플릿 비동기 조회 성공")
    void getAllTemplatesAsync_success() throws Exception {
        Integer workspaceId = 2;
        IndividualTemplateResponse row = makeResponse(
                2, "AsyncTest", null, null, workspaceId, false, IndividualTemplate.Status.DRAFT);

        given(individualTemplateService.getAllTemplatesAsync(
                eq(workspaceId), eq(1), any(IndividualTemplatePageableRequest.class)))
                .willReturn(CompletableFuture.completedFuture(
                        new PageImpl<>(List.of(row), PageRequest.of(0, 10), 1)));

        mockMvc.perform(get("/{workspaceId}/templates/async", workspaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].workspaceId").value(workspaceId));
    }

    @Test
    @WithMockJwtClaims
    @DisplayName("단일 템플릿 조회 성공")
    void getTemplate_success() throws Exception {
        Integer workspaceId = 7, templateId = 10;
        IndividualTemplateResponse response = makeResponse(templateId, "single", "c", null, workspaceId, false, IndividualTemplate.Status.DRAFT);

        given(individualTemplateService.getIndividualTemplate(workspaceId, 1, templateId)).willReturn(response);

        mockMvc.perform(get("/{workspaceId}/templates/{id}", workspaceId, templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(templateId))
                .andExpect(jsonPath("$.workspaceId").value(workspaceId));

        verify(individualTemplateService).getIndividualTemplate(workspaceId, 1, templateId);
    }

    @Test
    @WithMockJwtClaims
    @DisplayName("단일 템플릿 비동기 조회 성공")
    void getTemplateAsync_success() throws Exception {
        Integer workspaceId = 8, templateId = 11;
        IndividualTemplateResponse response = makeResponse(templateId, "asyncSingle", null, null, workspaceId, false, IndividualTemplate.Status.APPROVED);

        given(individualTemplateService.getIndividualTemplateAsync(workspaceId, 1, templateId))
                .willReturn(CompletableFuture.completedFuture(response));

        mockMvc.perform(get("/{workspaceId}/templates/{id}/async", workspaceId, templateId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(templateId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(individualTemplateService).getIndividualTemplateAsync(workspaceId, 1, templateId);
    }

    // ============================
    // DELETE
    // ============================

    @Test
    @WithMockJwtClaims
    @DisplayName("삭제 성공 시 204 반환")
    void deleteTemplate_success() throws Exception {
        Integer workspaceId = 1, templateId = 10;

        mockMvc.perform(delete("/{workspaceId}/templates/{id}", workspaceId, templateId))
                .andExpect(status().isNoContent());

        verify(individualTemplateService).deleteTemplate(templateId, workspaceId, 1);
    }

    @Test
    @WithMockJwtClaims
    @DisplayName("템플릿 수정 성공")
    void updateTemplate_success() throws Exception {
        Integer workspaceId = 10, templateId = 1;

        IndividualTemplateUpdateRequest request =
                new IndividualTemplateUpdateRequest("수정된 제목", "수정된 내용", "수정된 버튼", "AI채팅", "사용자채팅");

        IndividualTemplateResponse expected = makeResponse(
                templateId,
                "수정된 제목",
                "수정된 내용",
                "수정된 버튼",
                workspaceId,
                false,
                IndividualTemplate.Status.DRAFT
        );

        given(individualTemplateService.updateTemplate(
                eq(workspaceId), eq(templateId), any(IndividualTemplateUpdateRequest.class), eq(1)))
                .willReturn(expected);

        mockMvc.perform(put("/{workspaceId}/templates/{id}", workspaceId, templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // ← request 사용됨
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(templateId))
                .andExpect(jsonPath("$.individualTemplateTitle").value("수정된 제목"));

        verify(individualTemplateService).updateTemplate(
                eq(workspaceId), eq(templateId), any(IndividualTemplateUpdateRequest.class), eq(1));
    }

    @Test
    @WithMockJwtClaims   // 기본 userId=1
    @DisplayName("템플릿 상태 변경 성공")
    void updateTemplateStatus_success() throws Exception {
        // given
        Integer workspaceId = 10;
        Integer templateId = 5;

        IndividualTemplateResponse expected = makeResponse(
                templateId,
                "제목",
                "내용",
                "버튼",
                workspaceId,
                false,
                IndividualTemplate.Status.APPROVED // 변경된 상태
        );

        // service mock
        given(individualTemplateService.updateTemplateStatus(
                eq(workspaceId), eq(templateId), eq(1), eq(IndividualTemplate.Status.APPROVED))
        ).willReturn(expected);

        // when & then
        mockMvc.perform(put("/{workspaceId}/templates/{id}/status", workspaceId, templateId)
                        .param("status", "APPROVED") // ✅ @ParameterObject → 쿼리 파라미터 전달
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.individualTemplateId").value(templateId))
                .andExpect(jsonPath("$.workspaceId").value(workspaceId))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(individualTemplateService).updateTemplateStatus(workspaceId, templateId, 1, IndividualTemplate.Status.APPROVED);
    }

}

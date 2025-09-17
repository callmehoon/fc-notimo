package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplatePageableRequest;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.entity.User.UserRole;
import com.jober.final2teamdrhong.service.IndividualTemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class IndividualTemplateControllerTest {

    @Mock
    IndividualTemplateService individualTemplateService;

    @InjectMocks
    IndividualTemplateController controller;

    @Test
    void createEmptyTemplate_정상_호출() {
        // given
        JwtClaims mockClaims = createMockJwtClaims(1, "test@test.com");

        IndividualTemplateResponse expectedResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateId", 1);
        ReflectionTestUtils.setField(expectedResponse, "workspaceId", 99);

        given(individualTemplateService.createTemplate(99)).willReturn(expectedResponse);

        // when
        ResponseEntity<IndividualTemplateResponse> result =
                controller.createEmptyTemplate(99, mockClaims);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getIndividualTemplateId()).isEqualTo(1);
        assertThat(result.getBody().getWorkspaceId()).isEqualTo(99);

        verify(individualTemplateService).validateWorkspaceOwnership(99, 1);
        verify(individualTemplateService).createTemplate(99);
    }

    @Test
    void createEmptyTemplateAsync_정상_호출() throws ExecutionException, InterruptedException {
        // given
        JwtClaims mockClaims = createMockJwtClaims(2, "test@test.com");

        IndividualTemplateResponse expectedResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateId", 2);
        ReflectionTestUtils.setField(expectedResponse, "workspaceId", 77);

        given(individualTemplateService.createTemplateAsync(77))
                .willReturn(CompletableFuture.completedFuture(expectedResponse));

        // when
        CompletableFuture<ResponseEntity<IndividualTemplateResponse>> result =
                controller.createEmptyTemplateAsync(77, mockClaims);

        // then
        ResponseEntity<IndividualTemplateResponse> response = result.get();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIndividualTemplateId()).isEqualTo(2);
        assertThat(response.getBody().getWorkspaceId()).isEqualTo(77);

        verify(individualTemplateService).validateWorkspaceOwnership(77, 2);
        verify(individualTemplateService).createTemplateAsync(77);
    }

    private JwtClaims createMockJwtClaims(Integer userId, String email) {
        return JwtClaims.builder()
                .userId(userId)
                .email(email)
                .userName("testUser")
                .userRole(UserRole.USER) // UserRole enum import 필요
                .tokenType("access")
                .jti("test-jti-123")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }
    @Test
    void getTemplate_정상_호출() {
        // given
        Integer workspaceId = 7;
        Integer individualTemplateId = 10;

        IndividualTemplateResponse expectedResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateId", individualTemplateId);
        ReflectionTestUtils.setField(expectedResponse, "workspaceId", workspaceId);
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateTitle", "Single Template");
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateContent", "Template Content");
        ReflectionTestUtils.setField(expectedResponse, "status", IndividualTemplate.Status.DRAFT);

        given(individualTemplateService.getIndividualTemplate(workspaceId, individualTemplateId))
                .willReturn(expectedResponse);

        // when
        ResponseEntity<IndividualTemplateResponse> result =
                controller.getTemplate(workspaceId, individualTemplateId);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getIndividualTemplateId()).isEqualTo(individualTemplateId);
        assertThat(result.getBody().getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(result.getBody().getIndividualTemplateTitle()).isEqualTo("Single Template");
        assertThat(result.getBody().getIndividualTemplateContent()).isEqualTo("Template Content");
        assertThat(result.getBody().getStatus()).isEqualTo(IndividualTemplate.Status.DRAFT);

        verify(individualTemplateService).getIndividualTemplate(workspaceId, individualTemplateId);
    }

    @Test
    void getTemplateAsync_정상_호출() throws ExecutionException, InterruptedException {
        // given
        Integer workspaceId = 8;
        Integer individualTemplateId = 11;

        IndividualTemplateResponse expectedResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateId", individualTemplateId);
        ReflectionTestUtils.setField(expectedResponse, "workspaceId", workspaceId);
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateTitle", "Async Single Template");
        ReflectionTestUtils.setField(expectedResponse, "status", IndividualTemplate.Status.APPROVED);

        given(individualTemplateService.getIndividualTemplateAsync(workspaceId, individualTemplateId))
                .willReturn(CompletableFuture.completedFuture(expectedResponse));

        // when
        CompletableFuture<ResponseEntity<IndividualTemplateResponse>> result =
                controller.getTemplateAsync(workspaceId, individualTemplateId);

        // then
        ResponseEntity<IndividualTemplateResponse> response = result.get();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIndividualTemplateId()).isEqualTo(individualTemplateId);
        assertThat(response.getBody().getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(response.getBody().getIndividualTemplateTitle()).isEqualTo("Async Single Template");
        assertThat(response.getBody().getStatus()).isEqualTo(IndividualTemplate.Status.APPROVED);

        verify(individualTemplateService).getIndividualTemplateAsync(workspaceId, individualTemplateId);
    }

    @Test
    void getAllTemplates_정상_호출() {
        // given
        Integer workspaceId = 1;
        IndividualTemplatePageableRequest request = new IndividualTemplatePageableRequest();

        IndividualTemplateResponse templateResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(templateResponse, "individualTemplateId", 1);
        ReflectionTestUtils.setField(templateResponse, "workspaceId", workspaceId);
        ReflectionTestUtils.setField(templateResponse, "individualTemplateTitle", "Test Template");

        Page<IndividualTemplateResponse> expectedPage = new PageImpl<>(
                List.of(templateResponse), PageRequest.of(0, 10), 1);

        given(individualTemplateService.getAllTemplates(eq(workspaceId), any(Pageable.class)))
                .willReturn(expectedPage);

        // when
        ResponseEntity<Page<IndividualTemplateResponse>> result =
                controller.getAllTemplates(workspaceId, request);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getContent().get(0).getIndividualTemplateId()).isEqualTo(1);
        assertThat(result.getBody().getContent().get(0).getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(result.getBody().getContent().get(0).getIndividualTemplateTitle()).isEqualTo("Test Template");

        verify(individualTemplateService).getAllTemplates(eq(workspaceId), any(Pageable.class));
    }

    @Test
    void getTemplatesByStatus_DRAFT_정상_호출() {
        // given
        Integer workspaceId = 3;
        IndividualTemplate.Status status = IndividualTemplate.Status.DRAFT;
        IndividualTemplatePageableRequest request = new IndividualTemplatePageableRequest();

        IndividualTemplateResponse templateResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(templateResponse, "individualTemplateId", 3);
        ReflectionTestUtils.setField(templateResponse, "workspaceId", workspaceId);
        ReflectionTestUtils.setField(templateResponse, "status", status);

        Page<IndividualTemplateResponse> expectedPage = new PageImpl<>(
                List.of(templateResponse), PageRequest.of(0, 10), 1);

        given(individualTemplateService.getIndividualTemplateByStatus(eq(workspaceId), eq(status), any(Pageable.class)))
                .willReturn(expectedPage);

        // when
        ResponseEntity<Page<IndividualTemplateResponse>> result =
                controller.getTemplatesByStatus(workspaceId, status, request);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
        assertThat(result.getBody().getContent().get(0).getIndividualTemplateId()).isEqualTo(3);
        assertThat(result.getBody().getContent().get(0).getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(result.getBody().getContent().get(0).getStatus()).isEqualTo(status);

        verify(individualTemplateService).getIndividualTemplateByStatus(eq(workspaceId), eq(status), any(Pageable.class));
    }

    // ----------------------
    // DELETE : 단일 템플릿 삭제
    // ----------------------
    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<Void> handleNotFound(EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    @Test
    @DisplayName("DELETE /{workspaceId}/templates/{individualTemplateId} -> 204 No Content")
    void deleteTemplate_success_returns204() throws Exception {
        Integer workspaceId = 1;
        Integer individualTemplateId = 10;

        mvc.perform(delete("/{workspaceId}/templates/{individualTemplateId}",
                workspaceId, individualTemplateId))
                .andExpect(status().isNoContent());

        // 서비스가 올바른 ID로 호출되었는지 검증
        verify(individualTemplateService).deleteTemplate(individualTemplateId);
    }

    @Test
    @DisplayName("없는 템플릿 삭제 시 404 Not Found")
    void deleteTemplate_notFound_returns404() throws Exception {
        Integer workspaceId = 1;
        Integer missingId = 999;

        doThrow(new EntityNotFoundException("not found"))
                .when(individualTemplateService).deleteTemplate(missingId);

        // when & then
        mvc.perform(delete("/{workspaceId}/templates/{individualTemplateId}",
                        workspaceId, missingId))
                .andExpect(status().isNotFound());

        verify(individualTemplateService).deleteTemplate(missingId);
    }
}
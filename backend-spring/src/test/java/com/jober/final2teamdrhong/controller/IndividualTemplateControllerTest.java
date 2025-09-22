package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.entity.User.UserRole;
import com.jober.final2teamdrhong.service.IndividualTemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
    void createEmptyTemplateAsync_정상_호출() {
        // given
        JwtClaims mockClaims = createMockJwtClaims(2, "test@test.com");

        IndividualTemplateResponse expectedResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateId", 2);
        ReflectionTestUtils.setField(expectedResponse, "workspaceId", 77);

        given(individualTemplateService.createTemplateAsync(77))
                .willReturn(CompletableFuture.completedFuture(expectedResponse));

        // when
        ResponseEntity<IndividualTemplateResponse> response =
                controller.createEmptyTemplateAsync(77, mockClaims);

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIndividualTemplateId()).isEqualTo(2);
        assertThat(response.getBody().getWorkspaceId()).isEqualTo(77);

        verify(individualTemplateService).validateWorkspaceOwnership(77, 2);
        verify(individualTemplateService).createTemplateAsync(77);
    }

    @Test
    void createFromPublicTemplate_정상_호출() {
        // given
        JwtClaims mockClaims = createMockJwtClaims(3, "test@test.com");

        IndividualTemplateResponse expectedResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateId", 3);
        ReflectionTestUtils.setField(expectedResponse, "workspaceId", 55);

        given(individualTemplateService.createIndividualTemplateFromPublic(10, 55, 3))
                .willReturn(expectedResponse);

        // when
        ResponseEntity<IndividualTemplateResponse> result =
                controller.createFromPublicTemplate(10, 55, mockClaims);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getIndividualTemplateId()).isEqualTo(3);
        assertThat(result.getBody().getWorkspaceId()).isEqualTo(55);

        verify(individualTemplateService).validateWorkspaceOwnership(55, 3);
        verify(individualTemplateService).createIndividualTemplateFromPublic(10, 55, 3);
    }

    @Test
    void createFromPublicTemplateAsync_정상_호출() {
        // given
        JwtClaims mockClaims = createMockJwtClaims(4, "test@test.com");

        IndividualTemplateResponse expectedResponse = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(expectedResponse, "individualTemplateId", 4);
        ReflectionTestUtils.setField(expectedResponse, "workspaceId", 44);

        given(individualTemplateService.createIndividualTemplateFromPublicAsync(20, 44, 4))
                .willReturn(CompletableFuture.completedFuture(expectedResponse));

        // when
        ResponseEntity<IndividualTemplateResponse> result =
                controller.createFromPublicTemplateAsync(20, 44, mockClaims);

        // then
        assertThat(result.getStatusCodeValue()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getIndividualTemplateId()).isEqualTo(4);
        assertThat(result.getBody().getWorkspaceId()).isEqualTo(44);

        verify(individualTemplateService).validateWorkspaceOwnership(44, 4);
        verify(individualTemplateService).createIndividualTemplateFromPublicAsync(20, 44, 4);
    }

    private JwtClaims createMockJwtClaims(Integer userId, String email) {
        return JwtClaims.builder()
                .userId(userId)
                .email(email)
                .userName("testUser")
                .userRole(UserRole.USER)
                .tokenType("access")
                .jti("test-jti-123")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }
}

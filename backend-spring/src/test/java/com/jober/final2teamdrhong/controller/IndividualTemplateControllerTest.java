package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.config.ExTestSecurityConfig;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.service.IndividualTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = IndividualTemplateController.class)
@Import(ExTestSecurityConfig.class) // 테스트용 시큐리티: permitAll / CSRF off
class IndividualTemplateControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    IndividualTemplateService individualTemplateService;

    @Test
    void createEmptyTemplate_200과_JSON반환() throws Exception {
        // 응답 DTO 인스턴스 생성 (필드 직접 주입)
        IndividualTemplateResponse resp = new IndividualTemplateResponse(); // @NoArgsConstructor 가정
        ReflectionTestUtils.setField(resp, "individualTemplateId", 1);
        ReflectionTestUtils.setField(resp, "workspaceId", 99);

        given(individualTemplateService.createTemplate(99)).willReturn(resp);

        mvc.perform(post("/templates/{workspaceId}", 99))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.individualTemplateId").value(1))
                .andExpect(jsonPath("$.workspaceId").value(99));

        verify(individualTemplateService).createTemplate(99);
    }

    @Test
    void createEmptyTemplateAsync_200과_JSON반환() throws Exception {
        IndividualTemplateResponse resp = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(resp, "individualTemplateId", 2);
        ReflectionTestUtils.setField(resp, "workspaceId", 77);

        given(individualTemplateService.createTemplateAsync(77))
                .willReturn(CompletableFuture.completedFuture(resp));

        // 1차: 비동기 시작 확인
        MvcResult mvcResult = mvc.perform(post("/templates/{workspaceId}/async", 77))
                .andExpect(request().asyncStarted())
                .andReturn();

        // 2차: async 결과 디스패치 후 본검증
        mvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.individualTemplateId").value(2))
                .andExpect(jsonPath("$.workspaceId").value(77));

        verify(individualTemplateService).createTemplateAsync(77);
    }

    // ----------------------
    // READ: 전체 목록 조회 (동기)
    // ----------------------
    @Test
    void getAllTemplates_200과_Page_JSON반환() throws Exception {
        // given
        IndividualTemplateResponse r1 = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(r1, "individualTemplateId", 10);
        ReflectionTestUtils.setField(r1, "workspaceId", 1);

        IndividualTemplateResponse r2 = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(r2, "individualTemplateId", 11);
        ReflectionTestUtils.setField(r2, "workspaceId", 1);

        Page<IndividualTemplateResponse> page =
                new PageImpl<>(List.of(r1, r2)); // 기본 페이지 메타: size/content 등 생성

        given(individualTemplateService.getAllTemplates(anyInt(), anyString(), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mvc.perform(get("/{workspaceId}/templates", 1)
                        // 페이지 파라미터를 임의로 전달(미전달 시 @PageableDefault 동작)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt,desc")
                        .param("sortType", "latest"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                // Page 응답 구조 검증
                .andExpect(jsonPath("$.content[0].individualTemplateId").value(10))
                .andExpect(jsonPath("$.content[0].workspaceId").value(1))
                .andExpect(jsonPath("$.content[1].individualTemplateId").value(11))
                .andExpect(jsonPath("$.content[1].workspaceId").value(1))
                .andExpect(jsonPath("$.size").value(2)) // PageImpl의 size(=content size)
                .andExpect(jsonPath("$.number").value(0)); // 요청한 page

        verify(individualTemplateService).getAllTemplates(eq(1), eq("latest"), any(Pageable.class));
    }

    @Test
    void getAllTemplates_기본값_sortType_사용() throws Exception {
        // given
        IndividualTemplateResponse r1 = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(r1, "individualTemplateId", 10);
        ReflectionTestUtils.setField(r1, "workspaceId", 1);

        Page<IndividualTemplateResponse> page = new PageImpl<>(List.of(r1));

        given(individualTemplateService.getAllTemplates(anyInt(), anyString(), any(Pageable.class)))
                .willReturn(page);

        // when & then - sortType 파라미터를 전달하지 않음 (기본값 "latest" 사용)
        mvc.perform(get("/{workspaceId}/templates", 1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.content[0].individualTemplateId").value(10));

        verify(individualTemplateService).getAllTemplates(eq(1), eq("latest"), any(Pageable.class));
    }

    // ----------------------
    // READ: 전체 목록 조회 (비동기)
    // ----------------------
    @Test
    void getAllTemplatesAsync_200과_Page_JSON반환() throws Exception {
        IndividualTemplateResponse r1 = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(r1, "individualTemplateId", 20);
        ReflectionTestUtils.setField(r1, "workspaceId", 2);

        Page<IndividualTemplateResponse> page = new PageImpl<>(List.of(r1));

        given(individualTemplateService.getAllTemplatesAsync(anyInt(), anyString(), any(Pageable.class)))
                .willReturn(CompletableFuture.completedFuture(page));

        MvcResult mvcResult = mvc.perform(get("/{workspaceId}/templates/async", 2)
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortType", "title"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.content[0].individualTemplateId").value(20))
                .andExpect(jsonPath("$.content[0].workspaceId").value(2));

        verify(individualTemplateService).getAllTemplatesAsync(eq(2), eq("title"), any(Pageable.class));
    }

    // ----------------------
    // READ: 상태별 조회 (동기)
    // ----------------------
    @Test
    void getTemplatesByStatus_200과_Page_JSON반환() throws Exception {
        // given
        IndividualTemplateResponse r1 = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(r1, "individualTemplateId", 30);
        ReflectionTestUtils.setField(r1, "workspaceId", 3);

        Page<IndividualTemplateResponse> page = new PageImpl<>(List.of(r1));

        given(individualTemplateService.getIndividualTemplateByStatus(
                anyInt(), any(IndividualTemplate.Status.class), anyString(), any(Pageable.class)))
                .willReturn(page);

        // when & then
        mvc.perform(get("/{workspaceId}/templates/status/{status}", 3, "DRAFT")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortType", "latest"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.content[0].individualTemplateId").value(30))
                .andExpect(jsonPath("$.content[0].workspaceId").value(3));

        verify(individualTemplateService).getIndividualTemplateByStatus(
                eq(3), eq(IndividualTemplate.Status.DRAFT), eq("latest"), any(Pageable.class));
    }

    @Test
    void getTemplatesByStatus_기본값_sortType_사용() throws Exception {
        // given
        IndividualTemplateResponse r1 = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(r1, "individualTemplateId", 31);
        ReflectionTestUtils.setField(r1, "workspaceId", 3);

        Page<IndividualTemplateResponse> page = new PageImpl<>(List.of(r1));

        given(individualTemplateService.getIndividualTemplateByStatus(
                anyInt(), any(IndividualTemplate.Status.class), anyString(), any(Pageable.class)))
                .willReturn(page);

        // when & then - sortType 파라미터 미전달 (기본값 "latest" 사용)
        mvc.perform(get("/{workspaceId}/templates/status/{status}", 3, "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.content[0].individualTemplateId").value(31));

        verify(individualTemplateService).getIndividualTemplateByStatus(
                eq(3), eq(IndividualTemplate.Status.APPROVED), eq("latest"), any(Pageable.class));
    }

    // ----------------------
    // READ: 상태별 조회 (비동기)
    // ----------------------
    @Test
    void getTemplatesByStatusAsync_200과_Page_JSON반환() throws Exception {
        IndividualTemplateResponse r1 = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(r1, "individualTemplateId", 40);
        ReflectionTestUtils.setField(r1, "workspaceId", 4);

        Page<IndividualTemplateResponse> page = new PageImpl<>(List.of(r1));

        given(individualTemplateService.getIndividualTemplateByStatusAsync(
                anyInt(), any(IndividualTemplate.Status.class), anyString(), any(Pageable.class)))
                .willReturn(CompletableFuture.completedFuture(page));

        MvcResult mvcResult = mvc.perform(get("/{workspaceId}/templates/status/{status}/async", 4, "APPROVED")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortType", "title"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.content[0].individualTemplateId").value(40))
                .andExpect(jsonPath("$.content[0].workspaceId").value(4));

        verify(individualTemplateService).getIndividualTemplateByStatusAsync(
                eq(4), eq(IndividualTemplate.Status.APPROVED), eq("title"), any(Pageable.class));
    }

    // ----------------------
    // READ: 단일 조회 (동기)
    // ----------------------
    @Test
    void getTemplate_200과_JSON반환() throws Exception {
        // given
        IndividualTemplateResponse resp = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(resp, "individualTemplateId", 33);
        ReflectionTestUtils.setField(resp, "workspaceId", 3);

        given(individualTemplateService.getIndividualTemplate(3, 33))
                .willReturn(resp);

        // when & then
        mvc.perform(get("/{workspaceId}/templates/{individualTemplateId}", 3, 33))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.individualTemplateId").value(33))
                .andExpect(jsonPath("$.workspaceId").value(3));

        verify(individualTemplateService).getIndividualTemplate(3, 33);
    }

    // ----------------------
    // READ: 단일 조회 (비동기)
    // ----------------------
    @Test
    void getTemplateAsync_200과_JSON반환() throws Exception {
        IndividualTemplateResponse resp = new IndividualTemplateResponse();
        ReflectionTestUtils.setField(resp, "individualTemplateId", 44);
        ReflectionTestUtils.setField(resp, "workspaceId", 4);

        given(individualTemplateService.getIndividualTemplateAsync(4, 44))
                .willReturn(CompletableFuture.completedFuture(resp));

        MvcResult mvcResult = mvc.perform(get("/{workspaceId}/templates/{individualTemplateId}/async", 4, 44))
                .andExpect(request().asyncStarted())
                .andReturn();

        mvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.individualTemplateId").value(44))
                .andExpect(jsonPath("$.workspaceId").value(4));

        verify(individualTemplateService).getIndividualTemplateAsync(4, 44);
    }
}
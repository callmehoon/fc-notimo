package com.jober.final2teamdrhong.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateCreateRequest;
import com.jober.final2teamdrhong.service.PublicTemplateService;
import org.mockito.ArgumentCaptor;
import com.jober.final2teamdrhong.filter.JwtAuthenticationFilter;

@WebMvcTest(PublicTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicTemplateControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicTemplateService publicTemplateService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean(name = "jpaMappingContext")
    private MappingContext<?, ?> jpaMappingContext;

    @Test
    @DisplayName("기본 파라미터로 템플릿 조회 - 최신순 정렬")
    void getPublicTemplates_WithDefaultParams_ReturnsRecentSorted() throws Exception {
        // given
        List<PublicTemplateResponse> mockContent = List.of(
            createMockResponse(1, "최신템플릿", "Content1"),
            createMockResponse(2, "오래된템플릿", "Content2")
        );
        Page<PublicTemplateResponse> mockPage = new PageImpl<>(mockContent, PageRequest.of(0, 10), 2);
        
        when(publicTemplateService.getTemplates(any(Pageable.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/public-templates")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("최신템플릿"))
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("오래된템플릿"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(publicTemplateService, times(1)).getTemplates(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        Assertions.assertEquals(0, pageable.getPageNumber());
        Assertions.assertEquals(10, pageable.getPageSize());
        Assertions.assertNotNull(pageable.getSort().getOrderFor("createdAt"));
        Assertions.assertTrue(pageable.getSort().getOrderFor("createdAt").isDescending());
    }

    @Test
    @DisplayName("공유순 정렬로 템플릿 조회")
    void getPublicTemplates_WithShareSort_ReturnsShareSorted() throws Exception {
        // given
        List<PublicTemplateResponse> mockContent = List.of(
            createMockResponse(1, "높은공유템플릿", "Content1"),
            createMockResponse(2, "낮은공유템플릿", "Content2")
        );
        Page<PublicTemplateResponse> mockPage = new PageImpl<>(mockContent, PageRequest.of(0, 10), 2);
        
        when(publicTemplateService.getTemplates(any(Pageable.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("sort", "shareCount")
                        .param("direction", "DESC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("높은공유템플릿"))
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("낮은공유템플릿"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(publicTemplateService, times(1)).getTemplates(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        Assertions.assertEquals(0, pageable.getPageNumber());
        Assertions.assertEquals(10, pageable.getPageSize());
        Assertions.assertNotNull(pageable.getSort().getOrderFor("shareCount"));
        Assertions.assertTrue(pageable.getSort().getOrderFor("shareCount").isDescending());
    }

    @Test
    @DisplayName("조회순 정렬로 템플릿 조회")
    void getPublicTemplates_WithViewSort_ReturnsViewSorted() throws Exception {
        // given
        List<PublicTemplateResponse> mockContent = List.of(
            createMockResponse(1, "높은조회템플릿", "Content1"),
            createMockResponse(2, "낮은조회템플릿", "Content2")
        );
        Page<PublicTemplateResponse> mockPage = new PageImpl<>(mockContent, PageRequest.of(0, 10), 2);
        
        when(publicTemplateService.getTemplates(any(Pageable.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("sort", "viewCount")
                        .param("direction", "DESC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("높은조회템플릿"))
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("낮은조회템플릿"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(publicTemplateService, times(1)).getTemplates(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        Assertions.assertEquals(0, pageable.getPageNumber());
        Assertions.assertEquals(10, pageable.getPageSize());
        Assertions.assertNotNull(pageable.getSort().getOrderFor("viewCount"));
        Assertions.assertTrue(pageable.getSort().getOrderFor("viewCount").isDescending());
    }

    @Test
    @DisplayName("제목순 정렬로 템플릿 조회")
    void getPublicTemplates_WithTitleSort_ReturnsTitleSorted() throws Exception {
        // given
        List<PublicTemplateResponse> mockContent = List.of(
            createMockResponse(1, "가나다", "Content1"),
            createMockResponse(2, "나다라", "Content2")
        );
        Page<PublicTemplateResponse> mockPage = new PageImpl<>(mockContent, PageRequest.of(0, 10), 2);
        
        when(publicTemplateService.getTemplates(any(Pageable.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("sort", "publicTemplateTitle")
                        .param("direction", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("가나다"))
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("나다라"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(publicTemplateService, times(1)).getTemplates(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        Assertions.assertEquals(0, pageable.getPageNumber());
        Assertions.assertEquals(10, pageable.getPageSize());
        Assertions.assertNotNull(pageable.getSort().getOrderFor("publicTemplateTitle"));
        Assertions.assertTrue(pageable.getSort().getOrderFor("publicTemplateTitle").isAscending());
    }

    @Test
    @DisplayName("페이징 파라미터로 템플릿 조회")
    void getPublicTemplates_WithPagingParams_ReturnsPagedResult() throws Exception {
        // given
        List<PublicTemplateResponse> mockContent = List.of(
            createMockResponse(1, "템플릿1", "Content1")
        );
        Page<PublicTemplateResponse> mockPage = new PageImpl<>(mockContent, PageRequest.of(1, 2), 5);
        
        when(publicTemplateService.getTemplates(any(Pageable.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(publicTemplateService, times(1)).getTemplates(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        Assertions.assertEquals(1, pageable.getPageNumber());
        Assertions.assertEquals(2, pageable.getPageSize());
        Assertions.assertNotNull(pageable.getSort().getOrderFor("createdAt"));
        Assertions.assertTrue(pageable.getSort().getOrderFor("createdAt").isDescending());
    }

    @Test
    @DisplayName("잘못된 정렬 옵션으로 템플릿 조회 - 400 에러 반환")
    void getPublicTemplates_WithInvalidSort_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("sort", "invalid")
                        .param("direction", "DESC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(publicTemplateService, never()).getTemplates(any(Pageable.class));
    }

    @Test
    @DisplayName("잘못된 정렬 방향으로 템플릿 조회 - 400 에러 반환")
    void getPublicTemplates_WithInvalidDirection_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("sort", "createdAt")
                        .param("direction", "INVALID")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(publicTemplateService, never()).getTemplates(any(Pageable.class));
    }

    @Test
    @DisplayName("잘못된 페이지 번호로 템플릿 조회 - 400 에러 반환")
    void getPublicTemplates_WithInvalidPage_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("page", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(publicTemplateService, never()).getTemplates(any(Pageable.class));
    }

    @Test
    @DisplayName("잘못된 페이지 크기로 템플릿 조회 - 400 에러 반환")
    void getPublicTemplates_WithInvalidSize_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(get("/public-templates")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(publicTemplateService, never()).getTemplates(any(Pageable.class));
    }

    @Test
    @DisplayName("빈 결과 조회")
    void getPublicTemplates_WithEmptyResult_ReturnsEmptyPage() throws Exception {
        // given
        Page<PublicTemplateResponse> mockPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        
        when(publicTemplateService.getTemplates(any(Pageable.class))).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/public-templates")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(publicTemplateService, times(1)).getTemplates(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        Assertions.assertEquals(0, pageable.getPageNumber());
        Assertions.assertEquals(10, pageable.getPageSize());
        Assertions.assertNotNull(pageable.getSort().getOrderFor("createdAt"));
        Assertions.assertTrue(pageable.getSort().getOrderFor("createdAt").isDescending());
    }

    private PublicTemplateResponse createMockResponse(Integer id, String title, String content) {
        return new PublicTemplateResponse(
                id,
                title,
                content,
                null
        );
    }

    @Test
    @DisplayName("공용 템플릿 생성 성공 - 201 반환")
    void createPublicTemplate_Success_ReturnsCreated() throws Exception {
        // given
        Integer individualTemplateId = 10;
        PublicTemplateResponse response = new PublicTemplateResponse(1, "제목", "내용", "버튼");

        when(publicTemplateService.createPublicTemplate(any(PublicTemplateCreateRequest.class), any(Integer.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/public-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"individualTemplateId\": " + individualTemplateId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicTemplateId").value(1))
                .andExpect(jsonPath("$.publicTemplateTitle").value("제목"))
                .andExpect(jsonPath("$.publicTemplateContent").value("내용"))
                .andExpect(jsonPath("$.buttonTitle").value("버튼"));

        ArgumentCaptor<PublicTemplateCreateRequest> requestCaptor = ArgumentCaptor.forClass(PublicTemplateCreateRequest.class);
        verify(publicTemplateService, times(1)).createPublicTemplate(requestCaptor.capture(), any(Integer.class));
        PublicTemplateCreateRequest captured = requestCaptor.getValue();
        Assertions.assertEquals(individualTemplateId, captured.individualTemplateId());
    }

    @Test
    @DisplayName("공용 템플릿 생성 실패 - 유효성 오류 400")
    void createPublicTemplate_ValidationError_ReturnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/public-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(publicTemplateService, never()).createPublicTemplate(any(PublicTemplateCreateRequest.class), any(Integer.class));
    }

    @Test
    @DisplayName("공용 템플릿 생성 실패 - 개인 템플릿 없음 또는 워크스페이스 소유권 없음 400")
    void createPublicTemplate_NotFoundOrNotOwned_ReturnsBadRequest() throws Exception {
        // given
        when(publicTemplateService.createPublicTemplate(any(PublicTemplateCreateRequest.class), any(Integer.class)))
                .thenThrow(new IllegalArgumentException("해당 개인 템플릿을 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(post("/public-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"individualTemplateId\": 999}"))
                .andExpect(status().isBadRequest());

        verify(publicTemplateService, times(1)).createPublicTemplate(any(PublicTemplateCreateRequest.class), any(Integer.class));
    }
}

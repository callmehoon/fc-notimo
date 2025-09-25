package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.util.test.WithMockJwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockJwtClaims(userId = 1)
class PublicTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PublicTemplateRepository repository;

    @Autowired
    private IndividualTemplateRepository individualTemplateRepository;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 DB 초기화
        repository.deleteAll();
        individualTemplateRepository.deleteAll();

        // 정렬 테스트를 위한 다양한 데이터 생성
        PublicTemplate t1 = PublicTemplate.builder()
                .publicTemplateTitle("가나다")
                .publicTemplateContent("Content1")
                .buttonTitle("버튼1")
                .shareCount(5)
                .viewCount(20)
                .createdAt(LocalDateTime.now().minusDays(3))
                .updatedAt(LocalDateTime.now().minusDays(3))
                .build();

        PublicTemplate t2 = PublicTemplate.builder()
                .publicTemplateTitle("나다라")
                .publicTemplateContent("Content2")
                .buttonTitle("버튼2")
                .shareCount(15)
                .viewCount(10)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();

        PublicTemplate t3 = PublicTemplate.builder()
                .publicTemplateTitle("다라마")
                .publicTemplateContent("Content3")
                .buttonTitle("버튼3")
                .shareCount(8)
                .viewCount(30)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        PublicTemplate t4 = PublicTemplate.builder()
                .publicTemplateTitle("라마바")
                .publicTemplateContent("Content4")
                .buttonTitle("버튼4")
                .shareCount(12)
                .viewCount(25)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 삭제된 템플릿
        PublicTemplate deletedTemplate = PublicTemplate.builder()
                .publicTemplateTitle("삭제된템플릿")
                .publicTemplateContent("Deleted Content")
                .buttonTitle("삭제된버튼")
                .shareCount(100)
                .viewCount(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        deletedTemplate.softDelete();

        repository.save(t1);
        repository.save(t2);
        repository.save(t3);
        repository.save(t4);
        repository.save(deletedTemplate);
    }

    @Test
    @DisplayName("삭제되지 않은 템플릿만 조회되어야 함")
    void testGetAllPublicTemplates_returnsNonDeleted() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))  // 삭제되지 않은 4개만
                .andExpect(jsonPath("$.content[*].publicTemplateTitle")
                    .value(org.hamcrest.Matchers.containsInAnyOrder("가나다", "나다라", "다라마", "라마바")));
    }

    @Test
    @DisplayName("공유순 정렬 API 테스트")
    void testGetTemplatesByShareCount() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sort", "shareCount")
                        .param("direction", "DESC")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("나다라"))  // 가장 높은 공유수(15)
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("라마바"))  // 공유수(12)
                .andExpect(jsonPath("$.content[2].publicTemplateTitle").value("다라마"))  // 공유수(8)
                .andExpect(jsonPath("$.content[3].publicTemplateTitle").value("가나다")); // 가장 낮은 공유수(5)
    }

    @Test
    @DisplayName("조회순 정렬 API 테스트")
    @WithMockJwtClaims(userId = 1)
    void testGetTemplatesByViewCount() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sort", "viewCount")
                        .param("direction", "DESC")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("다라마"))  // 가장 높은 조회수(30)
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("라마바"))  // 조회수(25)
                .andExpect(jsonPath("$.content[2].publicTemplateTitle").value("가나다"))  // 조회수(20)
                .andExpect(jsonPath("$.content[3].publicTemplateTitle").value("나다라")); // 가장 낮은 조회수(10)
    }

    @Test
    @DisplayName("제목 가나다순 정렬 API 테스트")
    void testGetTemplatesByTitle() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sort", "publicTemplateTitle")
                        .param("direction", "ASC")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("가나다"))
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("나다라"))
                .andExpect(jsonPath("$.content[2].publicTemplateTitle").value("다라마"))
                .andExpect(jsonPath("$.content[3].publicTemplateTitle").value("라마바"));
    }

    @Test
    @DisplayName("잘못된 정렬 옵션 API 테스트 - 400 에러 반환")
    void testGetTemplatesByInvalidSort() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sort", "invalid")
                        .param("direction", "DESC")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 정렬 방향 API 테스트 - 400 에러 반환")
    void testGetTemplatesByInvalidDirection() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sort", "createdAt")
                        .param("direction", "INVALID")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 페이지 번호 API 테스트 - 400 에러 반환")
    void testGetTemplatesByInvalidPage() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("page", "-1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 페이지 크기 API 테스트 - 400 에러 반환")
    void testGetTemplatesByInvalidSize() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("page", "0")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("너무 큰 페이지 크기 API 테스트 - 400 에러 반환")
    void testGetTemplatesByTooLargeSize() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("page", "0")
                        .param("size", "200")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("페이징 API 테스트")
    void testGetTemplatesWithPaging() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sort", "shareCount")
                        .param("direction", "DESC")
                        .param("page", "0")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    @DisplayName("두 번째 페이지 API 테스트")
    void testGetTemplatesSecondPage() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sort", "shareCount")
                        .param("direction", "DESC")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    @DisplayName("공용 템플릿 생성 실패 - 유효성 검증 400")
    void testCreatePublicTemplate_ValidationError() throws Exception {
        mockMvc.perform(post("/public-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}

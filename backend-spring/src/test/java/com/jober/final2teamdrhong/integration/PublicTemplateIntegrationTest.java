package com.jober.final2teamdrhong.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")  // application-test.properties 사용
@AutoConfigureMockMvc(addFilters = false)
class PublicTemplateIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PublicTemplateRepository repository;

    @BeforeEach
    void setUp() {
        // 매 테스트마다 DB 초기화
        repository.deleteAll();

        // 정렬 테스트를 위한 다양한 데이터 생성
        PublicTemplate t1 = new PublicTemplate("가나다", "Content1", false);
        t1.setShareCount(5);
        t1.setViewCount(20);
        t1.setCreatedAt(LocalDateTime.now().minusDays(3));

        PublicTemplate t2 = new PublicTemplate("나다라", "Content2", false);
        t2.setShareCount(15);
        t2.setViewCount(10);
        t2.setCreatedAt(LocalDateTime.now().minusDays(2));

        PublicTemplate t3 = new PublicTemplate("다라마", "Content3", false);
        t3.setShareCount(8);
        t3.setViewCount(30);
        t3.setCreatedAt(LocalDateTime.now().minusDays(1));

        PublicTemplate t4 = new PublicTemplate("라마바", "Content4", false);
        t4.setShareCount(12);
        t4.setViewCount(25);
        t4.setCreatedAt(LocalDateTime.now());

        // 삭제된 템플릿
        PublicTemplate deletedTemplate = new PublicTemplate("삭제된템플릿", "Deleted Content", true);
        deletedTemplate.setShareCount(100);
        deletedTemplate.setViewCount(100);
        deletedTemplate.setCreatedAt(LocalDateTime.now());

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
                        .param("sortBy", "share")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].shareCount").value(15))  // 가장 높은 공유수
                .andExpect(jsonPath("$.content[1].shareCount").value(12))
                .andExpect(jsonPath("$.content[2].shareCount").value(8))
                .andExpect(jsonPath("$.content[3].shareCount").value(5));  // 가장 낮은 공유수
    }

    @Test
    @DisplayName("조회순 정렬 API 테스트")
    void testGetTemplatesByViewCount() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sortBy", "view")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].viewCount").value(30))  // 가장 높은 조회수
                .andExpect(jsonPath("$.content[1].viewCount").value(25))
                .andExpect(jsonPath("$.content[2].viewCount").value(20))
                .andExpect(jsonPath("$.content[3].viewCount").value(10));  // 가장 낮은 조회수
    }

    @Test
    @DisplayName("최신순 정렬 API 테스트")
    void testGetTemplatesByRecent() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sortBy", "recent")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("라마바"))  // 가장 최근 생성
                .andExpect(jsonPath("$.content[1].publicTemplateTitle").value("다라마"))
                .andExpect(jsonPath("$.content[2].publicTemplateTitle").value("나다라"))
                .andExpect(jsonPath("$.content[3].publicTemplateTitle").value("가나다")); // 가장 오래된 생성
    }

    @Test
    @DisplayName("제목 가나다순 정렬 API 테스트")
    void testGetTemplatesByTitle() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sortBy", "title")
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
    @DisplayName("기본값 정렬 API 테스트 - 최신순으로 정렬되어야 함")
    void testGetTemplatesByDefault() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("라마바")); // 최신순이므로 가장 최근 생성된 것이 첫 번째
    }

    @Test
    @DisplayName("잘못된 정렬 옵션 API 테스트 - 기본값(최신순)으로 정렬되어야 함")
    void testGetTemplatesByInvalidSort() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sortBy", "invalid")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.content[0].publicTemplateTitle").value("라마바")); // 최신순이므로 가장 최근 생성된 것이 첫 번째
    }

    @Test
    @DisplayName("페이징 API 테스트")
    void testGetTemplatesWithPaging() throws Exception {
        mockMvc.perform(get("/public-templates")
                        .param("sortBy", "share")
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
                        .param("sortBy", "share")
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
}

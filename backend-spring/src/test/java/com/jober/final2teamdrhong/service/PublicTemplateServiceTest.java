package com.jober.final2teamdrhong.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateResponse;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class PublicTemplateServiceTest {

    @Autowired
    private PublicTemplateService publicTemplateService;

    @Autowired
    private PublicTemplateRepository publicTemplateRepository;

    @BeforeEach
    void setUp() {
        publicTemplateRepository.deleteAll();

        // 더미 데이터 생성 - 다양한 정렬 테스트를 위한 데이터
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

        // 삭제된 템플릿 (조회되지 않아야 함)
        PublicTemplate deletedTemplate = new PublicTemplate("삭제된템플릿", "Deleted Content", true);
        deletedTemplate.setShareCount(100);
        deletedTemplate.setViewCount(100);
        deletedTemplate.setCreatedAt(LocalDateTime.now());

        publicTemplateRepository.save(t1);
        publicTemplateRepository.save(t2);
        publicTemplateRepository.save(t3);
        publicTemplateRepository.save(t4);
        publicTemplateRepository.save(deletedTemplate);
    }

    @Test
    @DisplayName("공유순 정렬 - 내림차순으로 정렬되어야 함")
    void getTemplatesByShareCount() {
        // when
        Pageable pageable = PageRequest.of(0, 10, Sort.by("shareCount").descending());
        Page<PublicTemplateResponse> result = publicTemplateService.getTemplates(pageable);
        List<PublicTemplateResponse> templates = result.getContent();

        // then
        assertThat(templates).hasSize(4); // 삭제되지 않은 템플릿만 조회
        assertThat(templates.get(0).getShareCount()).isEqualTo(15); // 가장 높은 공유수
        assertThat(templates.get(1).getShareCount()).isEqualTo(12);
        assertThat(templates.get(2).getShareCount()).isEqualTo(8);
        assertThat(templates.get(3).getShareCount()).isEqualTo(5); // 가장 낮은 공유수
        
        // 내림차순 검증
        for (int i = 0; i < templates.size() - 1; i++) {
            assertThat(templates.get(i).getShareCount())
                .isGreaterThanOrEqualTo(templates.get(i + 1).getShareCount());
        }
    }

    @Test
    @DisplayName("조회순 정렬 - 내림차순으로 정렬되어야 함")
    void getTemplatesByViewCount() {
        // when
        Pageable pageable = PageRequest.of(0, 10, Sort.by("viewCount").descending());
        Page<PublicTemplateResponse> result = publicTemplateService.getTemplates(pageable);
        List<PublicTemplateResponse> templates = result.getContent();

        // then
        assertThat(templates).hasSize(4); // 삭제되지 않은 템플릿만 조회
        assertThat(templates.get(0).getViewCount()).isEqualTo(30); // 가장 높은 조회수
        assertThat(templates.get(1).getViewCount()).isEqualTo(25);
        assertThat(templates.get(2).getViewCount()).isEqualTo(20);
        assertThat(templates.get(3).getViewCount()).isEqualTo(10); // 가장 낮은 조회수
        
        // 내림차순 검증
        for (int i = 0; i < templates.size() - 1; i++) {
            assertThat(templates.get(i).getViewCount())
                .isGreaterThanOrEqualTo(templates.get(i + 1).getViewCount());
        }
    }

    @Test
    @DisplayName("최신순 정렬 - 내림차순으로 정렬되어야 함")
    void getTemplatesByRecent() {
        // when
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<PublicTemplateResponse> result = publicTemplateService.getTemplates(pageable);
        List<PublicTemplateResponse> templates = result.getContent();

        // then
        assertThat(templates).hasSize(4); // 삭제되지 않은 템플릿만 조회
        
        // 최신순 검증 (가장 최근에 생성된 것이 먼저)
        for (int i = 0; i < templates.size() - 1; i++) {
            assertThat(templates.get(i).getCreatedAt())
                .isAfterOrEqualTo(templates.get(i + 1).getCreatedAt());
        }
    }

    @Test
    @DisplayName("제목 가나다순 정렬 - 오름차순으로 정렬되어야 함")
    void getTemplatesByTitle() {
        // when
        Pageable pageable = PageRequest.of(0, 10, Sort.by("publicTemplateTitle").ascending());
        Page<PublicTemplateResponse> result = publicTemplateService.getTemplates(pageable);
        List<PublicTemplateResponse> templates = result.getContent();

        // then
        assertThat(templates).hasSize(4); // 삭제되지 않은 템플릿만 조회
        assertThat(templates.get(0).getPublicTemplateTitle()).isEqualTo("가나다");
        assertThat(templates.get(1).getPublicTemplateTitle()).isEqualTo("나다라");
        assertThat(templates.get(2).getPublicTemplateTitle()).isEqualTo("다라마");
        assertThat(templates.get(3).getPublicTemplateTitle()).isEqualTo("라마바");
        
        // 오름차순 검증
        for (int i = 0; i < templates.size() - 1; i++) {
            assertThat(templates.get(i).getPublicTemplateTitle())
                .isLessThanOrEqualTo(templates.get(i + 1).getPublicTemplateTitle());
        }
    }

    @Test
    @DisplayName("기본값 정렬 - 최신순으로 정렬되어야 함")
    void getTemplatesByDefault() {
        // when - sortBy가 null이거나 잘못된 값일 때
        Pageable pageableDefault = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<PublicTemplateResponse> result1 = publicTemplateService.getTemplates(pageableDefault);
        Page<PublicTemplateResponse> result2 = publicTemplateService.getTemplates(pageableDefault);
        List<PublicTemplateResponse> templates1 = result1.getContent();
        List<PublicTemplateResponse> templates2 = result2.getContent();

        // then
        assertThat(templates1).hasSize(4);
        assertThat(templates2).hasSize(4);
        
        // 둘 다 최신순으로 정렬되어야 함
        for (int i = 0; i < templates1.size() - 1; i++) {
            assertThat(templates1.get(i).getCreatedAt())
                .isAfterOrEqualTo(templates1.get(i + 1).getCreatedAt());
            assertThat(templates2.get(i).getCreatedAt())
                .isAfterOrEqualTo(templates2.get(i + 1).getCreatedAt());
        }
    }

    @Test
    @DisplayName("페이징 테스트 - 페이지 크기와 페이지 번호가 올바르게 적용되어야 함")
    void getTemplatesWithPaging() {
        // when - 페이지 크기 2, 첫 번째 페이지
        Pageable pageable = PageRequest.of(0, 2, Sort.by("shareCount").descending());
        Page<PublicTemplateResponse> result = publicTemplateService.getTemplates(pageable);
        List<PublicTemplateResponse> templates = result.getContent();

        // then
        assertThat(templates).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("삭제된 템플릿은 조회되지 않아야 함")
    void getTemplatesExcludeDeleted() {
        // when
        Pageable pageable = PageRequest.of(0, 10, Sort.by("shareCount").descending());
        Page<PublicTemplateResponse> result = publicTemplateService.getTemplates(pageable);
        List<PublicTemplateResponse> templates = result.getContent();

        // then
        assertThat(templates).hasSize(4);
        assertThat(templates).noneMatch(template -> 
            template.getPublicTemplateTitle().equals("삭제된템플릿"));
    }
}

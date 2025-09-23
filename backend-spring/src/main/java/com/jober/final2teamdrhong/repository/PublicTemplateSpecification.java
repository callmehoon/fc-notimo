package com.jober.final2teamdrhong.repository;

import com.jober.final2teamdrhong.dto.publicTemplate.PublicTemplateSearchRequest;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import org.springframework.data.jpa.domain.Specification;

/**
 * PublicTemplate 엔티티에 대한 동적 검색 쿼리를 생성하는 Specification 클래스.
 * <p>
 * 이 클래스는 {@link org.springframework.data.jpa.repository.JpaSpecificationExecutor}와 함께 사용되며,
 * 검색 조건(키워드, 검색 대상)에 따라 JPA {@link Specification} 객체를 반환한다.
 * 모든 메소드는 정적(static)으로 제공되므로 인스턴스화할 필요가 없다.
 * </p>
 */
public class PublicTemplateSpecification {
    
    /**
     * 검색 요청에 맞춰 {@link PublicTemplate} 엔티티에 대한 JPA {@link Specification}을 만들어준다.
     * 
     * @param search 검색 요청 DTO (keyword, searchTarget 포함)
     * @return 생성된 {@link Specification} (where 절을 구성하는 Predicate 제공)
     */
    public static Specification<PublicTemplate> withSearch(PublicTemplateSearchRequest search) {

        return (root, query, cb) -> {
            // 검색 요청이 없거나 키워드가 없으면 조건 없음
            if (search == null || search.getKeyword() == null || search.getKeyword().isBlank()) {
                return cb.conjunction(); // 조건 없음
            }
            
            String keyword = "%" + search.getKeyword() + "%";
            
            // searchTarget 이 TITLE/CONTENT/ALL 인 경우 분기 처리
            return switch (search.getSearchTarget()) {
                case TITLE -> cb.like(root.get("publicTemplateTitle"), keyword);
                case CONTENT -> cb.like(root.get("publicTemplateContent"), keyword);
                case ALL -> cb.or(
                        cb.like(root.get("publicTemplateTitle"), keyword),
                        cb.like(root.get("publicTemplateContent"), keyword)
                );
            };
        };

    }
}

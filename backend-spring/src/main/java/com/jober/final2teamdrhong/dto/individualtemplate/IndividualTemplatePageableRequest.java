package com.jober.final2teamdrhong.dto.individualtemplate;

import com.jober.final2teamdrhong.entity.IndividualTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualTemplatePageableRequest {

    @Schema(description = "페이지(0부터 시작)", example = "0")
    @Min(0)
    private Integer page = 0;

    @Schema(description = "페이지 당 개수(1~100)", example = "1")
    @Min(1) @Max(100)
    private Integer size = 20;

    @Schema(description = "정렬 타입", example = "latest", allowableValues = {"latest", "title"})
    @Pattern(regexp = "latest|title", message = "sortType은 latest 또는 title 이어야 합니다.")
    private String sortType = "latest";

    @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"})
    @Pattern(regexp = "asc|desc", message = "direction은 asc 또는 desc 이어야 합니다.")
    private String direction = "desc";

    @Schema(description = "상태 필터(선택). 미지정 시 전체 조회", example = "DRAFT", nullable = true)
    private IndividualTemplate.Status status;

    /**
     * 컨트롤러/서비스 외부 노출 없이 내부에서만 Pageable 생성
     * 정렬 키 매핑 정책
     * - latest -> createdAt (최신순)
     * - title  -> individualTemplateTitle (제목)
     * 필요 시 여기만 수정하면 전체 정책이 일관되게 반영됨
     */
    public Pageable toPageable() {
        // 1) 안전한 방향 파싱 (기본 DESC)
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Sort sort;
        switch (sortType) {
            case "title" -> {
                // 2) 제목순: 대소문자 무시 + null 뒤로
                Sort.Order titleOrder = Sort.Order.by("individualTemplateTitle")
                        .with(dir)
                        .ignoreCase()
                        .with(Sort.NullHandling.NULLS_LAST);
                sort = Sort.by(titleOrder);
            }
            case "latest" -> {
                // 3) 최신순: updatedAt
                sort = Sort.by(Sort.Order.by("updatedAt").with(dir));
            }
            default -> {
                // 기본: 최신순 desc
                sort = Sort.by(Sort.Order.by("updatedAt").with(Sort.Direction.DESC));
            }
        }

        // 4) 안정 정렬용 타이브레이커
        sort = sort.and(Sort.by(Sort.Order.desc("individualTemplateId")));

        return PageRequest.of(page, size, sort);
    }

}

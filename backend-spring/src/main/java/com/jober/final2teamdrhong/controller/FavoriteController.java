package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.favorite.FavoritePageRequest;
import com.jober.final2teamdrhong.dto.favorite.FavoriteResponse;
import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.entity.Favorite.TemplateType;
import com.jober.final2teamdrhong.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "즐겨찾기 API", description = "즐겨찾기 생성/조회/수정/삭제 관련 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 개인 템플릿을 즐겨찾기에 추가(create)
     * @param request workspaceId와 templateId를 포함한 DTO
     * @return 성공 시 HTTP 200 OK
     */
    @PostMapping("/individual/favorite")
    public ResponseEntity<Void> createIndividualTemplateFavorite(@Valid @RequestBody IndividualTemplateFavoriteRequest request) {
            favoriteService.createIndividualTemplateFavorite(request);
            return ResponseEntity.ok().build();
    }

    /**
     * 공용 템플릿을 즐겨찾기에 추가(create)
     * @param request workspaceId와 templateId를 포함한 DTO
     * @return 성공 시 HTTP 200 OK
     */
    @PostMapping("/public/favorite")
    public ResponseEntity<Void> createPublicTemplateFavorite(@Valid @RequestBody PublicTemplateFavoriteRequest request) {
        favoriteService.createPublicTemplateFavorite(request);
        return ResponseEntity.ok().build();
    }


    /**
     * 특정 워크스페이스에 속한 즐겨찾기 목록을 조회(read)합니다.
     * templateType 파라미터가 없으면 페이징 없이 전체 목록을 반환하고,
     * 있으면 해당 타입의 템플릿만 페이징하여 반환합니다.
     * @param workspaceId 조회의 기준이 되는 워크스페이스 ID
     * @param templateType 템플릿 유형 (public 또는 individual, optional)
     * @param favoritePageRequest 페이징 정보
     * @return 즐겨찾기 목록 (List 또는 Page)과 HTTP 200 OK
     */
    @GetMapping("/favorites")
    public ResponseEntity<Page<FavoriteResponse>> getFavoritesByWorkspace(
            @RequestParam("workspaceId") Integer workspaceId,
            @RequestParam(value = "templateType", required = false) TemplateType templateType,
            @ModelAttribute FavoritePageRequest favoritePageRequest) {

        Page<FavoriteResponse> favorites = favoriteService.getFavoritesByWorkspace(workspaceId, templateType, favoritePageRequest);
        return ResponseEntity.ok(favorites);
    }

    /**
     * 즐겨찾기를 삭제(delete)
     * @param favoriteId 삭제할 즐겨찾기 ID
     * @return 성공 시 HTTP 204 No Content
     */
    @DeleteMapping("/favorites/{favoriteId}")
    public ResponseEntity<?> deleteFavorite(@PathVariable Integer favoriteId) {
        favoriteService.deleteFavorite(favoriteId);
        return ResponseEntity.noContent().build();
    }
}

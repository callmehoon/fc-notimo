package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 개인 템플릿을 즐겨찾기에 추가(create)
     * @param request workspaceId와 templateId를 포함한 DTO
     * @return 성공 시 HTTP 200 OK
     */
    @PostMapping("/individual/fav")
    public ResponseEntity<?> createIndividualTemplateFavorite(@Valid @RequestBody IndividualTemplateFavoriteRequest request) {
        favoriteService.createIndividualTemplateFavorite(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 공용 템플릿을 즐겨찾기에 추가(create)
     * @param request workspaceId와 templateId를 포함한 DTO
     * @return 성공 시 HTTP 200 OK
     */
    @PostMapping("/public/fav")
    public ResponseEntity<?> createPublicTemplateFavorite(@Valid @RequestBody PublicTemplateFavoriteRequest request) {
        favoriteService.createPublicTemplateFavorite(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 워크스페이스에 속한 즐겨찾기 목록을 조회(read)합니다.
     * templateType 파라미터가 없으면 페이징 없이 전체 목록을 반환하고,
     * 있으면 해당 타입의 템플릿만 페이징하여 반환합니다.
     * @param workspaceId 조회의 기준이 되는 워크스페이스 ID
     * @param templateType 템플릿 유형 (public 또는 individual, optional)
     * @param pageable 페이징 정보 (templateType이 있을 경우에만 유효)
     * @return 즐겨찾기 목록 (List 또는 Page)과 HTTP 200 OK
     */
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavoritesByWorkspace(@RequestParam("workspaceId") Integer workspaceId,
                                                     @RequestParam(value = "templateType", required = false) String templateType,
                                                     @PageableDefault(size = 10, sort = "favoriteId", direction = Sort.Direction.DESC) Pageable pageable) {
        // templateType 파라미터가 없는 경우 전체 목록 조회
        if (!StringUtils.hasText(templateType)) {
            return ResponseEntity.ok(favoriteService.getFavoritesByWorkspace(workspaceId));
        }

        // templateType 파라미터가 있는 경우 페이징 조회
        return ResponseEntity.ok(favoriteService.getFavoritesByWorkspace(workspaceId, templateType, pageable));
    }
}

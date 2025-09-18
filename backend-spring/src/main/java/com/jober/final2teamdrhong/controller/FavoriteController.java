package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.service.FavoriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}

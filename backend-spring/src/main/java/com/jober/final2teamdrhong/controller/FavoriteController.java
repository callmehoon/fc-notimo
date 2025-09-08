package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 개인 템플릿을 즐겨찾기에 추가(create)
     * @param request workspaceId와 templateId를 포함한 DTO
     * @return 성공 시 HTTP 200 OK
     */
    @PostMapping("/individual/fav")
    public ResponseEntity<Void> createTemplateFavorite(@RequestBody IndividualTemplateFavoriteRequest request) {
        favoriteService.createIndividualTemplateFavorite(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 공용 템플릿을 즐겨찾기에 추가(create)
     * @param request workspaceId와 templateId를 포함한 DTO
     * @return 성공 시 HTTP 200 OK
     */
    @PostMapping("/public/fav")
    public ResponseEntity<Void> createPublicTemplateFavorite(@RequestBody PublicTemplateFavoriteRequest request) {
        favoriteService.createPublicTemplateFavorite(request);
        return ResponseEntity.ok().build();
    }

}

package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.favorite.FavoriteResponse;
import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.service.FavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        try {
            favoriteService.createIndividualTemplateFavorite(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()),HttpStatus.CONFLICT);
        }
    }

    /**
     * 공용 템플릿을 즐겨찾기에 추가(create)
     * @param request workspaceId와 templateId를 포함한 DTO
     * @return 성공 시 HTTP 200 OK
     */
    @PostMapping("/public/fav")
    public ResponseEntity<?> createPublicTemplateFavorite(@Valid @RequestBody PublicTemplateFavoriteRequest request) {
        try {
            favoriteService.createPublicTemplateFavorite(request);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()),HttpStatus.CONFLICT);
        }
    }

    /**
     * 특정 워크스페이스에 속한 모든 즐겨찾기 목록을 조회(read)
     * @param workspaceId 조회의 기준이 되는 워크스페이스 ID
     * @return 성공 시 즐겨찾기 목록과 HTTP 200 OK, 실패 시 에러 메시지와 HTTP 404 Not Found
     */
    @GetMapping("/favorites")
    public ResponseEntity<?> getFavoritesByWorkspace(@RequestParam("workspaceId") Integer workspaceId) {
        try {
            List<FavoriteResponse> favorites = favoriteService.getFavoritesByWorkspace(workspaceId);
            return ResponseEntity.ok(favorites);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
        }
    }

}

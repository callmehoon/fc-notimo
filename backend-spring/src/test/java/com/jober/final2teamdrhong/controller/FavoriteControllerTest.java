package com.jober.final2teamdrhong.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.favorite.FavoritePageRequest;
import com.jober.final2teamdrhong.dto.favorite.FavoriteResponse;
import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.filter.JwtAuthenticationFilter;
import com.jober.final2teamdrhong.service.FavoriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteController.class)
@AutoConfigureMockMvc(addFilters = false)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FavoriteService favoriteService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    // ====================== Create ======================
    /**
     * 개인 템플릿 즐겨찾기 생성 테스트
     */
    @Test
    @DisplayName("성공(단위): 개인 템플릿 즐겨찾기 생성 API 호출")
    void createIndividualTemplateFavorite_ApiCall_Success() throws Exception {
        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(1, 10);
        String requestBody = objectMapper.writeValueAsString(request);

        doNothing().when(favoriteService).createIndividualTemplateFavorite(any(IndividualTemplateFavoriteRequest.class));

        mockMvc.perform(post("/individual/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(favoriteService).createIndividualTemplateFavorite(any(IndividualTemplateFavoriteRequest.class));
    }

    @Test
    @DisplayName("실패(단위): 개인 템플릿 즐겨찾기 생성 시 workspaceId가 null이면 400 에러 발생")
    void createIndividualFavorite_FailsWith_NullWorkspaceId() throws Exception {
        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(null, 10);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/individual/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(favoriteService, never()).createIndividualTemplateFavorite(any());
    }

    @Test
    @DisplayName("실패(단위): 개인 템플릿 즐겨찾기 생성 시 individualTemplateId가 null이면 400 에러 발생")
    void createIndividualFavorite_FailsWith_NullTemplateId() throws Exception {
        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(1, null);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/individual/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(favoriteService, never()).createIndividualTemplateFavorite(any());
    }



    /**
     * 공용 템플릿 즐겨찾기 생성 테스트
     */
    @Test
    @DisplayName("성공(단위): 공용 템플릿 즐겨찾기 생성 API 호출")
    void createPublicTemplateFavorite_ApiCall_Success() throws Exception {
        PublicTemplateFavoriteRequest request = new PublicTemplateFavoriteRequest(1, 100);
        String requestBody = objectMapper.writeValueAsString(request);

        doNothing().when(favoriteService).createPublicTemplateFavorite(any(PublicTemplateFavoriteRequest.class));

        mockMvc.perform(post("/public/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(favoriteService).createPublicTemplateFavorite(any(PublicTemplateFavoriteRequest.class));
    }

    @Test
    @DisplayName("실패(단위): 공용 템플릿 즐겨찾기 생성 시 workspaceId가 null이면 400 에러 발생")
    void createPublicFavorite_FailsWith_NullWorkspaceId() throws Exception {
        PublicTemplateFavoriteRequest request = new PublicTemplateFavoriteRequest(null, 100);
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/public/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(favoriteService, never()).createPublicTemplateFavorite(any());
    }

    @Test
    @DisplayName("실패(단위): 공용 템플릿 즐겨찾기 생성 시 publicTemplateId가 null이면 400 에러 발생")
    void createPublicFavorite_FailsWith_NullTemplateId() throws Exception {
        // given
        PublicTemplateFavoriteRequest request = new PublicTemplateFavoriteRequest(1, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/public/favorite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        // 서비스 메서드는 절대 호출되지 않아야 함
        verify(favoriteService, never()).createPublicTemplateFavorite(any());
    }





    // ====================== Read ======================
    /**
     * 즐겨찾기 목록 조회 테스트
     */
    @Test
    @DisplayName("성공(단위): 즐겨찾기 목록 페이징 조회")
    void getFavorites_withTemplateType_requestsPage() throws Exception {
        // given
        Page<FavoriteResponse> mockPage = new PageImpl<>(List.of());
        when(favoriteService.getFavoritesByWorkspace(any(Integer.class), any(), any(FavoritePageRequest.class)))
                .thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/favorites")
                        .param("workspaceId", "1")
                        .param("templateType", "PUBLIC")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());

        verify(favoriteService).getFavoritesByWorkspace(any(Integer.class), any(), any(FavoritePageRequest.class));
    }




    // ====================== Delete ======================
    /**
     * 즐겨찾기 삭제 테스트
     */
    @Test
    @DisplayName("성공(단위): 즐겨찾기 삭제 API 호출")
    void deleteFavorite_ApiCall_Success() throws Exception {
        // given
        Integer favoriteId = 1;
        doNothing().when(favoriteService).deleteFavorite(favoriteId);

        // when & then
        mockMvc.perform(delete("/favorites/{favoriteId}", favoriteId))
                .andExpect(status().isNoContent());

        verify(favoriteService, times(1)).deleteFavorite(favoriteId);
    }

    @Test
    @DisplayName("실패(단위): 존재하지 않는 즐겨찾기 삭제 시 400 에러 발생")
    void deleteFavorite_ApiCall_NotFound() throws Exception {
        // given
        Integer favoriteId = 999;
        doThrow(new IllegalArgumentException("해당 즐겨찾기를 찾을 수 없습니다."))
                .when(favoriteService).deleteFavorite(favoriteId);

        // when & then
        mockMvc.perform(delete("/favorites/{favoriteId}", favoriteId))
                .andExpect(status().isBadRequest());

        verify(favoriteService, times(1)).deleteFavorite(favoriteId);
    }




    // ====================== Delete ======================
    /**
     * 즐겨찾기 삭제 테스트
     */
    @Test
    @DisplayName("성공(단위): 즐겨찾기 삭제 API 호출")
    void deleteFavorite_ApiCall_Success() throws Exception {
        // given
        Integer favoriteId = 1;
        doNothing().when(favoriteService).deleteFavorite(favoriteId);

        // when & then
        mockMvc.perform(delete("/favorites/{favoriteId}", favoriteId))
                .andExpect(status().isNoContent());

        verify(favoriteService, times(1)).deleteFavorite(favoriteId);
    }

    @Test
    @DisplayName("실패(단위): 존재하지 않는 즐겨찾기 삭제 시 400 에러 발생")
    void deleteFavorite_ApiCall_NotFound() throws Exception {
        // given
        Integer favoriteId = 999;
        doThrow(new IllegalArgumentException("해당 즐겨찾기를 찾을 수 없습니다."))
                .when(favoriteService).deleteFavorite(favoriteId);

        // when & then
        mockMvc.perform(delete("/favorites/{favoriteId}", favoriteId))
                .andExpect(status().isBadRequest());

        verify(favoriteService, times(1)).deleteFavorite(favoriteId);
    }
}

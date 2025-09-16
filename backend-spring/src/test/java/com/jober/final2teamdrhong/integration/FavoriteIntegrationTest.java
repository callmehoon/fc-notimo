package com.jober.final2teamdrhong.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.dto.favorite.PublicTemplateFavoriteRequest;
import com.jober.final2teamdrhong.entity.*;
import com.jober.final2teamdrhong.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavoriteCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private IndividualTemplateRepository individualTemplateRepository;
    @Autowired
    private PublicTemplateRepository publicTemplateRepository;
    @Autowired
    private UserRepository userRepository;

    private Workspace savedWorkspace;
    private IndividualTemplate savedIndividualTemplate;
    private PublicTemplate savedPublicTemplate;

    @BeforeEach
    void setUp() {
        User user = User.create("테스트유저", "test@example.com", "010-1111-1111");
        User savedUser = userRepository.save(user);

        Workspace workspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url-unique")
                .representerName("홍길동")
                .representerPhoneNumber("010-0000-0000")
                .companyName("테스트 회사")
                .build();

        workspace.setUser(savedUser);

        savedWorkspace = workspaceRepository.save(workspace);

        IndividualTemplate individualTemplate = IndividualTemplate.builder()
                .workspace(savedWorkspace)
                .individualTemplateTitle("테스트 개인 템플릿")
                .individualTemplateContent("테스트 내용입니다.")
                .buttonTitle("확인")
                .build();
        savedIndividualTemplate = individualTemplateRepository.save(individualTemplate);

        PublicTemplate publicTemplate = PublicTemplate.builder()
                .publicTemplateTitle("테스트 공용 템플릿")
                .publicTemplateContent("테스트 내용입니다.")
                .buttonTitle("확인")
                .build();
        savedPublicTemplate = publicTemplateRepository.save(publicTemplate);
    }

    /**
     * 개인 템플릿 즐겨찾기 생성 기능 테스트
     */
    @Test
    @DisplayName("성공 : 개인 템플릿 즐겨찾기 생성")
    @WithMockUser
    void createIndividualTemplateFavorite_Success() throws Exception {
        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(
                savedWorkspace.getWorkspaceId(),
                savedIndividualTemplate.getIndividualTemplateId()
        );
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/individual/fav")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패 : 중복된 즐겨찾기 생성 시 409 Conflict 응답")
    @WithMockUser
    void createIndividualTemplateFavorite_Fail_AlreadyExists() throws Exception {
        Favorite existingFavorite = Favorite.builder()
                .workspace(savedWorkspace)
                .individualTemplate(savedIndividualTemplate)
                .build();
        favoriteRepository.save(existingFavorite);

        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(
                savedWorkspace.getWorkspaceId(),
                savedIndividualTemplate.getIndividualTemplateId()
        );
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/individual/fav")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict()); // <-- 4xx 에러 대신 더 명확한 409 Conflict를 기대
    }

    /**
     * 공용 템플릿 즐겨찾기 생성 기능 테스트
     */
    @Test
    @DisplayName("성공(공용) : 공용 템플릿 즐겨찾기 생성 기능 전체 흐름 테스트")
    @WithMockUser
    void createPublicTemplateFavorite_Integration_Success() throws Exception {
        PublicTemplateFavoriteRequest request = new PublicTemplateFavoriteRequest(
                savedWorkspace.getWorkspaceId(),
                savedPublicTemplate.getPublicTemplateId()
        );
        String requestBody = objectMapper.writeValueAsString(request);
        long initialCount = favoriteRepository.count();

        mockMvc.perform(post("/public/fav")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        assertThat(favoriteRepository.count()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("실패(공용) : 중복된 공용 즐겨찾기 생성 시 4xx 에러 응답")
    @WithMockUser
    void createPublicTemplateFavorite_Integration_AlreadyExists() throws Exception {
        Favorite existingFavorite = Favorite.builder()
                .workspace(savedWorkspace)
                .publicTemplate(savedPublicTemplate)
                .build();
        favoriteRepository.save(existingFavorite);

        PublicTemplateFavoriteRequest request = new PublicTemplateFavoriteRequest(
                savedWorkspace.getWorkspaceId(),
                savedPublicTemplate.getPublicTemplateId()
        );
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/public/fav")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict());
    }

}

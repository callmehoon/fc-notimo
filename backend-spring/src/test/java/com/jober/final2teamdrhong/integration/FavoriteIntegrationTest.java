package com.jober.final2teamdrhong.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jober.final2teamdrhong.dto.favorite.IndividualTemplateFavoriteRequest;
import com.jober.final2teamdrhong.entity.Favorite;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.User;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.FavoriteRepository;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.UserRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
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
    private UserRepository userRepository;

    private Workspace savedWorkspace;
    private IndividualTemplate savedTemplate;
    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(new User("테스트유저", "test@example.com", "010-1111-1111", User.UserRole.USER));

        Workspace workspace = Workspace.builder()
                .workspaceName("테스트 워크스페이스")
                .workspaceUrl("test-url-unique")
                .representerName("홍길동")
                .representerPhoneNumber("010-0000-0000")
                .companyName("테스트 회사")
                .build();

        workspace.setUser(savedUser);

        savedWorkspace = workspaceRepository.save(workspace);

        IndividualTemplate template = IndividualTemplate.builder()
                .workspaceId(savedWorkspace)
                .individualTemplateTitle("테스트 개인 템플릿")
                .individualTemplateContent("테스트 내용입니다.")
                .buttonTitle("확인")
                .build();
        savedTemplate = individualTemplateRepository.save(template);
    }

    @Test
    @DisplayName("성공 : 개인 템플릿 즐겨찾기 생성 기능 전체 흐름 테스트")
    @WithMockUser
    void createIndividualTemplateFavorite_Integration_Success() throws Exception {
        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(
                savedWorkspace.getWorkspaceId(),
                savedTemplate.getIndividualTemplateId()
        );
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/individual/fav")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        assertThat(favoriteRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("실패 : 중복된 즐겨찾기 생성 시 4xx 에러 응답")
    @WithMockUser
    void createIndividualTemplateFavorite_Integration_AlreadyExists() throws Exception {
        Favorite existingFavorite = Favorite.builder()
                .workspace(savedWorkspace)
                .individualTemplate(savedTemplate)
                .build();
        favoriteRepository.save(existingFavorite);

        IndividualTemplateFavoriteRequest request = new IndividualTemplateFavoriteRequest(
                savedWorkspace.getWorkspaceId(),
                savedTemplate.getIndividualTemplateId()
        );
        String requestBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/individual/fav")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is4xxClientError());
    }
}

package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndividualTemplateService {

    private final IndividualTemplateRepository individualTemplateRepo;
    private final WorkspaceRepository workspaceRepo;

    /**
     * 비어있는 템플릿 생성 (title/content/button 전부 "")
     * 요청의 문자열 필드는 무시하고 workspaceId만 사용함.
     */
    @Transactional
    public IndividualTemplateResponse createTemplate(Integer workspaceId) {
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 workspaceId 입니다. id=" + workspaceId));

        IndividualTemplate entity = IndividualTemplate.builder()
                .workspaceId(workspace)
                .individualTemplateTitle(null)           // null 저장
                .individualTemplateContent(null)         // null 저장
                .buttonTitle(null)                       // null 저장
                .build();

        IndividualTemplate saved = individualTemplateRepo.save(entity);

        // save() 직후 createdAt, updatedAt은 Hibernate가 채워주기 때문에 사용 가능
        return new IndividualTemplateResponse(
                saved.getIndividualTemplateId(),
                saved.getIndividualTemplateTitle(),
                saved.getIndividualTemplateContent(),
                saved.getButtonTitle(),
                saved.getWorkspace().getWorkspaceId(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.isDeleted()
        );
    }

    @Async
    @Transactional
    public CompletableFuture<IndividualTemplateResponse> createTemplateAsync(Integer workspaceId) {
        boolean isVirtual = Thread.currentThread().isVirtual();
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), isVirtual);

        IndividualTemplateResponse individualTemplateResponse = createTemplate(workspaceId);
        return CompletableFuture.completedFuture(individualTemplateResponse);
    }

    /**
     * 개인 템플릿 전체 조회
     */
    @Transactional
    public Page<IndividualTemplateResponse> getAllTemplates(Integer workspaceId, Pageable pageable){
        return individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalse(workspaceId, pageable)
                .map(saved -> new IndividualTemplateResponse(
                        saved.getIndividualTemplateId(),
                        saved.getIndividualTemplateTitle(),
                        saved.getIndividualTemplateContent(),
                        saved.getButtonTitle(),
                        saved.getWorkspace().getWorkspaceId(),
                        saved.getCreatedAt(),
                        saved.getUpdatedAt(),
                        saved.isDeleted()
                ));
    }

    @Async
    @Transactional
    public CompletableFuture<Page<IndividualTemplateResponse>> getAllTemplatesAsync(Integer workspaceId, Pageable pageable){
        boolean isVirtual = Thread.currentThread().isVirtual();
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), isVirtual);

        Page<IndividualTemplateResponse> individualTemplateResponses = getAllTemplates(workspaceId,pageable);
        return CompletableFuture.completedFuture(individualTemplateResponses);
    }

    /**
     * 개인 템플릿 단일 조회
     */
    @Transactional
    public IndividualTemplateResponse getIndividualTemplate(Integer workspaceId, Integer individualTemplateId) {
        IndividualTemplate saved = individualTemplateRepo
                .findByIndividualTemplateIdAndWorkspace_WorkspaceIdAndIsDeletedFalse(individualTemplateId, workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("해당 템플릿이 존재하지 않습니다. id = " + individualTemplateId));

        return new IndividualTemplateResponse(
                saved.getIndividualTemplateId(),
                saved.getIndividualTemplateTitle(),
                saved.getIndividualTemplateContent(),
                saved.getButtonTitle(),
                saved.getWorkspace().getWorkspaceId(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                saved.isDeleted()
        );
    }

    @Async
    @Transactional
    public CompletableFuture<IndividualTemplateResponse> getIndividualTemplateAsync(Integer workspaceId, Integer individualTemplateId) {
        boolean isVirtual = Thread.currentThread().isVirtual();
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), isVirtual);

        IndividualTemplateResponse individualTemplateResponse = getIndividualTemplate(workspaceId, individualTemplateId);
        return CompletableFuture.completedFuture(individualTemplateResponse);
    }
}

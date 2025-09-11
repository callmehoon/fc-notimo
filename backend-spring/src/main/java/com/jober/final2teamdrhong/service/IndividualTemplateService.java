package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.WorkspaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;   // ✅ springframework transactional 사용

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndividualTemplateService {

    private final IndividualTemplateRepository individualTemplateRepo;
    private final WorkspaceRepository workspaceRepo;

    /**
     * 비어있는 템플릿 생성 (title/content/button 전부 "")
     */
    @Transactional
    public IndividualTemplateResponse createTemplate(Integer workspaceId) {
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 workspaceId 입니다. id=" + workspaceId));

        IndividualTemplate entity = IndividualTemplate.builder()
                .workspaceId(workspace)
                .individualTemplateTitle(null)
                .individualTemplateContent(null)
                .buttonTitle(null)
                .build();

        IndividualTemplate saved = individualTemplateRepo.save(entity);

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
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return CompletableFuture.completedFuture(createTemplate(workspaceId));
    }

    /**
     * 개인 템플릿 전체 조회
     */
    @Transactional(readOnly = true)
    public Page<IndividualTemplateResponse> getAllTemplates(
            Integer workspaceId,
            String sortType,
            Pageable pageable) {

        Sort sort = "title".equalsIgnoreCase(sortType)
                ? Sort.by("IndividualTemplateTitle").ascending()
                : Sort.by("createdAt").descending();

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        return individualTemplateRepo.findByWorkspace_WorkspaceIdAndIsDeletedFalse(workspaceId, sortedPageable)
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

    /**
     * 정렬 기준 기본값(최신순)을 사용 하는 전체 조회
     */
    @Transactional(readOnly = true)
    public Page<IndividualTemplateResponse> getAllTemplates(
            Integer workspaceId,
            Pageable pageable) {
        return getAllTemplates(workspaceId, "latest", pageable);
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Page<IndividualTemplateResponse>> getAllTemplatesAsync(
            Integer workspaceId,
            String sortType,
            Pageable pageable) {
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return CompletableFuture.completedFuture(getAllTemplates(workspaceId, sortType, pageable));
    }

    /**
     * 개인 템플릿 상태별 조회
     */
    @Transactional(readOnly = true)
    public Page<IndividualTemplateResponse> getIndividualTemplateByStatus(
            Integer workspaceId,
            IndividualTemplate.Status status,
            String sortType,
            Pageable pageable) {

        Sort sort = "title".equalsIgnoreCase(sortType)
                ? Sort.by("individualTemplateTitle").ascending()
                : Sort.by("createdAt").descending();

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        return individualTemplateRepo
                .findByWorkspace_WorkspaceIdAndIsDeletedFalseAndStatus(workspaceId, status, sortedPageable)
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
    @Transactional(readOnly = true)
    public CompletableFuture<Page<IndividualTemplateResponse>> getIndividualTemplateByStatusAsync(
            Integer workspaceId,
            IndividualTemplate.Status status,
            String sortType,
            Pageable pageable) {

        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return CompletableFuture.completedFuture(getIndividualTemplateByStatus(workspaceId, status, sortType, pageable));
    }

    /**
     * 개인 템플릿 단일 조회
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public CompletableFuture<IndividualTemplateResponse> getIndividualTemplateAsync(Integer workspaceId, Integer individualTemplateId) {
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return CompletableFuture.completedFuture(getIndividualTemplate(workspaceId, individualTemplateId));
    }

    /**
     * 개인 템플릿 소프트 딜리트
     * isDeleted가 false가 아닌 경우도 포함.
     * 워크스페이스 내에 다른 사용자가 템플릿을 먼저 지워버릴 경우를 대비
     */
    @Transactional
    public void deleteTemplate(Integer individualTemplateId){
        int updated = individualTemplateRepo.softDeleteByIndividualTemplateId(individualTemplateId);
        if(updated == 1){
            log.info("Soft.deleted template id = {}", individualTemplateId);
            return;
        }

        // updated == 0 인 경우 (isDeleted != false 인 경우)만 해당 부분으로 옴. 존재 x 또는 이미 삭제됨.
        boolean exists = individualTemplateRepo.existsById(individualTemplateId);
        if(!exists) {
            throw new EntityNotFoundException("템플릿이 존재하지 않습니다. id = " + individualTemplateId);
        }

        // 이미 삭제된 상태라면 그냥 성공으로 봄.
        log.info("Template already soft-deleted. id = {}", individualTemplateId);
    }
}

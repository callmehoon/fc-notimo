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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
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
    public void validateWorkspaceOwnership(Integer workspaceId, Integer userId) {
        if (userId == null)
            throw new AccessDeniedException("인증이 필요합니다.");

        boolean exists = workspaceRepo.existsByWorkspaceIdAndUser_UserId(workspaceId, userId);
        if(!exists)
            throw new AccessDeniedException("해당 워크스페이스에 접근 권한이 없습니다.");
    }

    @Transactional
    public IndividualTemplateResponse createTemplate(Integer workspaceId) {
        Workspace workspace = workspaceRepo.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 workspaceId 입니다. id=" + workspaceId));

        IndividualTemplate entity = IndividualTemplate.builder()
                .workspace(workspace)
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
                saved.getIsDeleted()
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
                        saved.getIsDeleted()
                ));
    }

    /**
     * 정렬 기준 기본값(최신순)을 사용 하는 전체 조회
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<IndividualTemplateResponse> getAllTemplates(
            Integer workspaceId,
            Pageable pageable) {
        return getAllTemplates(workspaceId, "latest", pageable);
    }

    @Async
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
                        saved.getIsDeleted()
                ));
    }

    @Async
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
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
                saved.getIsDeleted()
        );
    }

    @Async
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public CompletableFuture<IndividualTemplateResponse> getIndividualTemplateAsync(Integer workspaceId, Integer individualTemplateId) {
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return CompletableFuture.completedFuture(getIndividualTemplate(workspaceId, individualTemplateId));
    }
}

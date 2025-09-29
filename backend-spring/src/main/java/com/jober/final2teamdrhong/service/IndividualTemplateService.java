package com.jober.final2teamdrhong.service;

import com.jober.final2teamdrhong.dto.individualtemplate.HistoryResponse;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplatePageableRequest;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse;
import com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateUpdateRequest;
import com.jober.final2teamdrhong.entity.IndividualTemplate;
import com.jober.final2teamdrhong.entity.PublicTemplate;
import com.jober.final2teamdrhong.entity.TemplateModifiedHistory;
import com.jober.final2teamdrhong.entity.Workspace;
import com.jober.final2teamdrhong.repository.IndividualTemplateRepository;
import com.jober.final2teamdrhong.repository.PublicTemplateRepository;
import com.jober.final2teamdrhong.repository.TemplateModifiedHistoryRepository;
import com.jober.final2teamdrhong.service.validator.WorkspaceValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.jober.final2teamdrhong.dto.individualtemplate.IndividualTemplateResponse.toResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndividualTemplateService {

    private final IndividualTemplateRepository individualTemplateRepository;
    private final PublicTemplateRepository publicTemplateRepository;
    private final WorkspaceValidator workspaceValidator;
    private final TemplateModifiedHistoryRepository templateModifiedHistoryRepository;

    @Transactional
    public IndividualTemplateResponse createTemplate(Integer workspaceId, Integer userId) {
        // 워크스페이스 검증
        Workspace workspace = workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        IndividualTemplate entity = IndividualTemplate.builder()
                .workspace(workspace)
                .individualTemplateTitle(null)           // null 저장
                .individualTemplateContent(null)         // null 저장
                .buttonTitle(null)                       // null 저장
                .build();

        IndividualTemplate individualTemplate = individualTemplateRepository.save(entity);

        // save() 직후 createdAt, updatedAt은 Hibernate가 채워주기 때문에 사용 가능
        return toResponse(individualTemplate);
    }

    @Async
    @Transactional
    public CompletableFuture<IndividualTemplateResponse> createTemplateAsync(Integer workspaceId, Integer userId) {
        boolean isVirtual = Thread.currentThread().isVirtual();
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), isVirtual);

        IndividualTemplateResponse individualTemplateResponse = createTemplate(workspaceId, userId);
        return CompletableFuture.completedFuture(individualTemplateResponse);
    }

    /**
     * 공용 템플릿을 기반으로 개인 템플릿 생성
     */
    @Transactional
    public IndividualTemplateResponse createIndividualTemplateFromPublic(
            Integer publicTemplateId,
            Integer workspaceId,
            Integer userId
    ) {
        // 공용 템플릿 조회
        PublicTemplate publicTemplate = publicTemplateRepository.findByIdOrThrow(publicTemplateId);

        // 워크스페이스 조회
        Workspace workspace = workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        // 복사 후 개인 템플릿 생성
        IndividualTemplate newIndividualTemplate = IndividualTemplate.builder()
                .individualTemplateTitle(publicTemplate.getPublicTemplateTitle())
                .individualTemplateContent(publicTemplate.getPublicTemplateContent())
                .buttonTitle(publicTemplate.getButtonTitle())
                .workspace(workspace)
                .build();

        IndividualTemplate individualTemplate = individualTemplateRepository.save(newIndividualTemplate);

        return toResponse(individualTemplate);
    }

    @Async
    @Transactional
    public CompletableFuture<IndividualTemplateResponse> createIndividualTemplateFromPublicAsync(
            Integer publicTemplateId,
            Integer workspaceId,
            Integer userId
    ) {
        boolean isVirtual = Thread.currentThread().isVirtual();
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), isVirtual);

        IndividualTemplateResponse individualTemplateResponse = createIndividualTemplateFromPublic(
                publicTemplateId, workspaceId, userId);
        return CompletableFuture.completedFuture(individualTemplateResponse);
    }

    /**
     * 개인 템플릿 전체 조회
     */
    @Transactional(readOnly = true)
    public Page<IndividualTemplateResponse> getAllTemplates(
            Integer workspaceId,
            Integer userId,
            IndividualTemplatePageableRequest pageableRequest) {

        // 워크스페이스 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        // 최신순은 기존 DTO 정렬(Pageable) 그대로 사용 가능
        if ("latest".equalsIgnoreCase(pageableRequest.getSortType())) {
            Pageable pageable = pageableRequest.toPageable();

            if (pageableRequest.getStatus() == null) {
                return individualTemplateRepository.findByWorkspace_WorkspaceId(workspaceId, pageable)
                        .map(IndividualTemplateResponse::toResponse);
            } else {
                return individualTemplateRepository.findByWorkspace_WorkspaceIdAndStatus(
                                workspaceId,
                                pageableRequest.getStatus(),
                                pageable)
                        .map(IndividualTemplateResponse::toResponse);
            }
        }

        // 제목순은 NULL 뒤로 가야 하므로 Repository 전용 쿼리 사용
        Pageable pageable = PageRequest.of(pageableRequest.getPage(), pageableRequest.getSize());

        if (pageableRequest.getStatus() == null) {
            return individualTemplateRepository.findAllByWorkspaceOrderByTitleAsc(workspaceId, pageable)
                    .map(IndividualTemplateResponse::toResponse);
        } else {
            return individualTemplateRepository.findAllByWorkspaceAndStatusOrderByTitleAsc(
                            workspaceId,
                            pageableRequest.getStatus(),
                            pageable)
                    .map(IndividualTemplateResponse::toResponse);
        }
    }


    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Page<IndividualTemplateResponse>> getAllTemplatesAsync(
            Integer workspaceId,
            Integer userId,
            IndividualTemplatePageableRequest pageableRequest) {
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return CompletableFuture.completedFuture(getAllTemplates(workspaceId, userId, pageableRequest));
    }

    /**
     * 개인 템플릿 단일 조회
     */
    @Transactional(readOnly = true)
    public IndividualTemplateResponse getIndividualTemplate(Integer workspaceId,
                                                            Integer userId,
                                                            Integer individualTemplateId) {

        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        IndividualTemplate individualTemplate = workspaceValidator.validateTemplateOwnership(workspaceId, individualTemplateId);

        return IndividualTemplateResponse.toResponse(individualTemplate);
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<IndividualTemplateResponse> getIndividualTemplateAsync(Integer workspaceId,
                                                                                    Integer userId,
                                                                                    Integer individualTemplateId) {
        log.info("[@Async] thread={}, isVirtual={}", Thread.currentThread().getName(), Thread.currentThread().isVirtual());
        return CompletableFuture.completedFuture(getIndividualTemplate(workspaceId, userId, individualTemplateId));
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> getTemplateModifiedHistories(Integer workspaceId, Integer individualTemplateId, Integer userId) {
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);
        IndividualTemplate individualTemplate = workspaceValidator.validateTemplateOwnership(workspaceId, individualTemplateId);

        List<TemplateModifiedHistory> histories = templateModifiedHistoryRepository
                .findAllByIndividualTemplateOrderByCreatedAtDesc(individualTemplate);

        return histories.stream()
                .map(HistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 개인 템플릿 소프트 딜리트
     * isDeleted가 false가 아닌 경우도 포함.
     * 워크스페이스 내에 다른 사용자가 템플릿을 먼저 지워버릴 경우를 대비
     */
    @Transactional
    public void deleteTemplate(Integer individualTemplateId,
                               Integer workspaceId,
                               Integer userId){

        // 워크스페이스 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        IndividualTemplate individualTemplate = workspaceValidator.validateTemplateOwnership(workspaceId, individualTemplateId);

        templateModifiedHistoryRepository.bulkSoftDeleteByTemplate(individualTemplate);

        individualTemplate.softDelete();
        individualTemplateRepository.save(individualTemplate);
        log.info("Soft deleted template id = {}", individualTemplateId);
    }

    @Transactional
    public IndividualTemplateResponse updateTemplate(
            Integer workspaceId,
            Integer individualTemplateId,
            IndividualTemplateUpdateRequest request,
            Integer userId) {

        // 워크스페이스 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        IndividualTemplate individualTemplate = workspaceValidator.validateTemplateOwnership(workspaceId, individualTemplateId);

        individualTemplate.update(
                request.getIndividualTemplateTitle(),
                request.getIndividualTemplateContent(),
                request.getButtonTitle(),
                IndividualTemplate.Status.DRAFT
        );

        TemplateModifiedHistory history = TemplateModifiedHistory.builder()
                .historyTitle(request.getIndividualTemplateTitle())
                .historyContent(request.getIndividualTemplateContent())
                .buttonTitle(request.getButtonTitle())
                .chatAi(request.getChatAi())
                .chatUser(request.getChatUser())
                .status(IndividualTemplate.Status.DRAFT)
                .individualTemplate(individualTemplate)
                .build();

        templateModifiedHistoryRepository.save(history);

        return IndividualTemplateResponse.toResponse(individualTemplate);
    }

    @Transactional
    public IndividualTemplateResponse updateTemplateStatus(
            Integer workspaceId,
            Integer individualTemplateId,
            Integer userId,
            IndividualTemplate.Status status
    ) {
        // 워크스페이스 검증
        workspaceValidator.validateAndGetWorkspace(workspaceId, userId);

        IndividualTemplate individualTemplate = workspaceValidator.validateTemplateOwnership(workspaceId, individualTemplateId);
        individualTemplate.updateStatus(status);

        return IndividualTemplateResponse.toResponse(individualTemplate);
    }

}

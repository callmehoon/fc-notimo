package com.jober.final2teamdrhong.controller;

import com.jober.final2teamdrhong.dto.jwtClaims.JwtClaims;
import com.jober.final2teamdrhong.dto.recipient.RecipientRequest;
import com.jober.final2teamdrhong.dto.recipient.RecipientResponse;
import com.jober.final2teamdrhong.exception.ErrorResponse;
import com.jober.final2teamdrhong.service.RecipientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 수신자(Recipient) 관련 HTTP 요청을 처리하는 컨트롤러입니다.
 */
@Tag(name = "수신자 관리 API", description = "수신자 생성, 조회, 수정, 삭제 등 수신자와 관련된 API를 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces/{workspaceId}/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    /**
     * 특정 워크스페이스에 새로운 수신자를 생성하는 API
     * <p>
     * 요청한 사용자가 해당 워크스페이스에 대한 접근 권한이 있는지 확인 후, 수신자를 생성합니다.
     *
     * @param createDTO   클라이언트로부터 받은 수신자 생성을 위한 데이터 (JSON, @Valid로 검증됨)
     * @param workspaceId 수신자를 추가할 워크스페이스의 ID
     * @param jwtClaims {@link AuthenticationPrincipal}을 통해 SecurityContext에서 직접 주입받는 현재 로그인된 사용자의 JWT 정보 객체
     * @return 상태 코드 201 (Created)와 함께 생성된 수신자의 정보를 담은 ResponseEntity
     */
    @Operation(summary = "수신자 생성", description = "특정 워크스페이스에 새로운 수신자를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "수신자 생성 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RecipientResponse.SimpleDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 요청 데이터 유효성 검사 실패 또는 비즈니스 규칙 위배 (예: 권한 없는 워크스페이스)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<RecipientResponse.SimpleDTO> createRecipient(@Valid @RequestBody RecipientRequest.CreateDTO createDTO,
                                                                       @PathVariable Integer workspaceId,
                                                                       @AuthenticationPrincipal JwtClaims jwtClaims) {
        Integer currentUserId = jwtClaims.getUserId();
        RecipientResponse.SimpleDTO createdRecipient = recipientService.createRecipient(createDTO, workspaceId, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecipient);
    }

    /**
     * 특정 워크스페이스에 속한 모든 수신자 목록을 페이징하여 조회하는 API
     * <p>
     * 요청한 사용자가 해당 워크스페이스에 대한 접근 권한이 있는지 확인 후, 수신자 목록을 반환합니다.
     *
     * @param workspaceId 수신자 목록을 조회할 워크스페이스의 ID
     * @param pageable    페이지 번호, 페이지 크기, 정렬 방법을 담은 객체
     * @param jwtClaims {@link AuthenticationPrincipal}을 통해 SecurityContext에서 직접 주입받는 현재 로그인된 사용자의 JWT 정보 객체
     * @return 상태 코드 200 (OK)와 함께 페이징된 수신자 목록 정보를 담은 ResponseEntity
     */
    @Operation(summary = "수신자 목록 페이징 조회", description = "특정 워크스페이스에 속한 모든 수신자 목록을 페이징하여 조회합니다. " +
            "page, size, sort 파라미터를 사용하여 페이징을 제어할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수신자 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: 존재하지 않거나 권한 없는 워크스페이스 접근",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<Page<RecipientResponse.SimpleDTO>> readRecipients(@PathVariable Integer workspaceId,
                                                                            @PageableDefault(size = 50,
                                                                                    sort = "createdAt",
                                                                                    direction = Sort.Direction.DESC)
                                                                            Pageable pageable,
                                                                            @AuthenticationPrincipal JwtClaims jwtClaims) {
        Integer currentUserId = jwtClaims.getUserId();
        Page<RecipientResponse.SimpleDTO> recipientPage = recipientService.readRecipients(workspaceId, currentUserId, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(recipientPage);
    }
}

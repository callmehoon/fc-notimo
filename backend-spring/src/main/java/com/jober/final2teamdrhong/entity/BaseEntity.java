package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * 엔티티의 수정 시각(updatedAt)을 현재 시간으로 갱신합니다.
     * JPA의 Dirty Checking에 의해 DB에 반영됩니다.
     * 수정 시간은 Asia/Seoul 시간대를 기준으로 기록됩니다.
     */
    public void update() {
        // Java의 ZoneId를 사용하여 서울 시간대의 현재 시간을 가져옵니다.
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }

    /**
     * 엔티티를 소프트 삭제 상태로 변경합니다.
     * 삭제 시간은 Asia/Seoul 시간대를 기준으로 기록됩니다.
     */
    public void softDelete() {
        this.isDeleted = true;
        // Java의 ZoneId를 사용하여 서울 시간대의 현재 시간을 가져옵니다.
        this.updatedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        this.deletedAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }

    /**
     * JPA 엔티티가 저장(@PrePersist)되거나 업데이트(@PreUpdate)되기 직전에 호출되어,
     * 해당 엔티티가 가진 모든 String 타입 필드의 앞뒤 공백을 자동으로 제거(trim)합니다.
     * 이 메서드는 리플렉션을 사용하여 필드에 동적으로 접근하고 값을 수정합니다.
     */
    @PrePersist
    @PreUpdate
    public void trimStringFields() {
        // this.getClass().getDeclaredFields()를 통해 현재 객체의 클래스에 선언된 모든 필드(private 포함)를 가져와 순회합니다.
        for (Field field : this.getClass().getDeclaredFields()) {
            // 각 필드의 타입이 String 클래스인지 확인합니다.
            if (field.getType().equals(String.class)) {
                try {
                    // private 필드에 접근할 수 있도록 접근 제한을 해제합니다.
                    field.setAccessible(true);
                    // 현재 객체(this)에서 해당 필드의 실제 값(Object)을 가져옵니다.
                    Object value = field.get(this);
                    // 필드의 값이 null이 아닐 경우에만 trim()을 수행합니다.
                    if (value != null) {
                        // 가져온 값을 String으로 캐스팅하고 trim() 메서드를 호출한 뒤, 그 결과를 다시 필드에 설정합니다.
                        field.set(this, ((String) value).trim());
                    }
                } catch (IllegalAccessException e) {
                    // 리플렉션 사용 중 접근 권한 예외가 발생하면, 트랜잭션을 안전하게 롤백시키기 위해 RuntimeException으로 전환하여 던집니다.
                    throw new RuntimeException("Failed to trim string fields", e);
                }
            }
        }
    }
}

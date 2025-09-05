package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipient")
@Getter
@Setter
@ToString(exclude = {"workspace", "groupMappings"})
@NoArgsConstructor
@SQLDelete(sql = "UPDATE recipient SET is_deleted = true, deleted_at = NOW() WHERE recipient_id = ?")
@SQLRestriction("is_deleted = false")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id", nullable = false)
    private Integer recipientId;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone_number", nullable = false)
    private String recipientPhoneNumber;

    @Column(name = "recipient_memo", length = 1000)
    private String recipientMemo;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false) // FK
    private Workspace workspace;

    @OneToMany(mappedBy = "recipient", fetch = FetchType.LAZY)
    private List<GroupMapping> groupMappings = new ArrayList<>();

    /**
     * 수신인이 특정 그룹에 소속되도록 멤버십(GroupMapping) 정보를 추가합니다.
     * @param groupMapping 수신인과 그룹을 연결하는 매핑 객체
     */
    public void addGroupMapping(GroupMapping groupMapping) {
        this.groupMappings.add(groupMapping);
        groupMapping.setRecipient(this);
    }
}

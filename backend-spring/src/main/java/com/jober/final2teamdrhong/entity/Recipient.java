package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipient")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_id")
    private Integer recipientId;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone_number")
    private String recipientPhoneNumber;

    @Column(name = "recipient_memo", length = 1000)
    private String recipientMemo;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id") // FK
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

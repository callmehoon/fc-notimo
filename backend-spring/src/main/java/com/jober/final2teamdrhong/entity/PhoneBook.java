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
@Table(name = "phone_book")
@Getter
@Setter
@ToString(exclude = {"workspace", "groupMappings"})
@NoArgsConstructor
@SQLDelete(sql = "UPDATE phone_book SET is_deleted = true, deleted_at = NOW() WHERE phone_book_id = ?")
@SQLRestriction("is_deleted = false")
public class PhoneBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phone_book_id", nullable = false) // PK
    private Integer phoneBookId;

    @Column(name = "phone_book_name", nullable = false)
    private String phoneBookName;

    @Column(name = "phone_book_memo", length = 1000)
    private String phoneBookMemo;

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

    @OneToMany(mappedBy = "phoneBook", fetch = FetchType.LAZY)
    private List<GroupMapping> groupMappings = new ArrayList<>();

    /**
     * 주소록 그룹(PhoneBook)에 새로운 멤버(GroupMapping)를 추가합니다.
     * @param groupMapping 그룹과 수신인을 연결하는 매핑 객체
     */
    public void addGroupMapping(GroupMapping groupMapping) {
        this.groupMappings.add(groupMapping);
        groupMapping.setPhoneBook(this);
    }
}

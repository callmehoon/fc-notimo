package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_mapping")
@Getter
@Setter
@ToString(exclude = {"phoneBook", "recipient"})
@NoArgsConstructor
@SQLDelete(sql = "UPDATE group_mapping SET is_deleted = true, deleted_at = NOW() WHERE group_mapping_id = ?")
@SQLRestriction("is_deleted = false")
public class GroupMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_mapping_id", nullable = false) // PK
    private Integer groupMappingId;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_book_id", nullable = false) // FK
    private PhoneBook phoneBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false) // FK
    private Recipient recipient;
}

package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_mapping")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class GroupMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_mapping_id") // PK
    private Integer groupMappingId;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", columnDefinition = "DATETIME")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_book_id") // FK
    private PhoneBook phoneBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id") // FK
    private Recipient recipient;
}

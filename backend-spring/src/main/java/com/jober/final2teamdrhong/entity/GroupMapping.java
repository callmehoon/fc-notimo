package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "group_mapping")
@Getter
@Setter
@ToString(exclude = {"phoneBook", "recipient"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("is_deleted = false")
public class GroupMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_mapping_id", nullable = false) // PK
    private Integer groupMappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_book_id", nullable = false) // FK
    private PhoneBook phoneBook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false) // FK
    private Recipient recipient;
}

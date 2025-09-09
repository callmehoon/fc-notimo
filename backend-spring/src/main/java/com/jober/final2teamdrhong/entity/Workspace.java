package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspace")
@Getter
@Setter
@ToString(exclude = {"phoneBooks", "recipients", "user"})
@NoArgsConstructor
@SQLRestriction("is_deleted = false")
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workspace_id", nullable = false)
    private Integer workspaceId;

    @Column(name = "workspace_name", nullable = false)
    private String workspaceName;

    @Column(name = "workspace_subname")
    private String workspaceSubname;

    @Column(name = "workspace_address")
    private String workspaceAddress;

    @Column(name = "workspace_detail_address")
    private String workspaceDetailAddress;

    @Column(name = "workspace_url", nullable = false, unique = true)
    private String workspaceUrl;

    @Column(name = "representer_name", nullable = false)
    private String representerName;

    @Column(name = "representer_phone_number", nullable = false)
    private String representerPhoneNumber;

    @Column(name = "representer_email")
    private String representerEmail;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_register_number")
    private String companyRegisterNumber;

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

    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    private List<PhoneBook> phoneBooks = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    private List<Recipient> recipients = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false) // FK
    private User user;

    /**
     * 워크스페이스에 주소록 그룹(PhoneBook)을 추가합니다.
     *
     * @param phoneBook 워크스페이스에 추가할 주소록 그룹 객체
     */
    public void addPhoneBook(PhoneBook phoneBook) {
        this.phoneBooks.add(phoneBook);
        phoneBook.setWorkspace(this);
    }

    /**
     * 워크스페이스에 수신인(Recipient)을 추가합니다.
     *
     * @param recipient 워크스페이스에 추가할 수신인 객체
     */
    public void addRecipient(Recipient recipient) {
        this.recipients.add(recipient);
        recipient.setWorkspace(this);
    }

    @Builder
    public Workspace(@NonNull String workspaceName,
                     String workspaceSubname,
                     String workspaceAddress,
                     String workspaceDetailAddress,
                     @NonNull String workspaceUrl,
                     @NonNull String representerName,
                     @NonNull String representerPhoneNumber,
                     String representerEmail,
                     @NonNull String companyName,
                     String companyRegisterNumber,
                     @NonNull User user) {
        this.workspaceName = workspaceName;
        this.workspaceSubname = workspaceSubname;
        this.representerName = representerName;
        this.representerPhoneNumber = representerPhoneNumber;
        this.representerEmail = representerEmail;
        this.workspaceAddress = workspaceAddress;
        this.workspaceDetailAddress = workspaceDetailAddress;
        this.workspaceUrl = workspaceUrl;
        this.companyName = companyName;
        this.companyRegisterNumber = companyRegisterNumber;
        this.user = user;
    }
}

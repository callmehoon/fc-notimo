package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspace")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workspace_id")
    private Integer workspaceId;

    @Column(name = "workspace_name")
    private String workspaceName;

    @Column(name = "workspace_subname")
    private String workspaceSubname;

    @Column(name = "representer_name")
    private String representerName;

    @Column(name = "representer_phone_number")
    private String representerPhoneNumber;

    @Column(name = "representer_email")
    private String representerEmail;

    @Column(name = "workspace_address")
    private String workspaceAddress;

    @Column(name = "workspace_detail_address")
    private String workspaceDetailAddress;

    @Column(name = "workspace_url")
    private String workspaceUrl;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_register_number")
    private String companyRegisterNumber;

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

    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    private List<PhoneBook> phoneBooks = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    private List<Recipient> recipients = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // FK
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
    public Workspace(String workspaceName,
                     String workspaceSubname,
                     String workspaceAddress,
                     String workspaceDetailAddress,
                     String workspaceUrl,
                     String representerName,
                     String representerPhoneNumber,
                     String representerEmail,
                     String companyName,
                     String companyRegisterNumber) {
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
    }
}

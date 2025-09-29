package com.jober.final2teamdrhong.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspace")
@Getter
@Setter
@ToString(exclude = {"phoneBooks", "recipients", "user"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("is_deleted = false")
public class Workspace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workspace_id", nullable = false)
    private Integer workspaceId;

    @NonNull
    @Column(name = "workspace_name", nullable = false)
    private String workspaceName;

    @Column(name = "workspace_subname")
    private String workspaceSubname;

    @Column(name = "workspace_address")
    private String workspaceAddress;

    @Column(name = "workspace_detail_address")
    private String workspaceDetailAddress;

    @NonNull
    @Column(name = "workspace_url", nullable = false)
    private String workspaceUrl;

    @NonNull
    @Column(name = "representer_name", nullable = false)
    private String representerName;

    @NonNull
    @Column(name = "representer_phone_number", nullable = false)
    private String representerPhoneNumber;

    @Column(name = "representer_email")
    private String representerEmail;

    @NonNull
    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_register_number")
    private String companyRegisterNumber;

    @Builder.Default
    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    private List<PhoneBook> phoneBooks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
    private List<Recipient> recipients = new ArrayList<>();

    @NonNull
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
}

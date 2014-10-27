package com.p.p.server.model.bean;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.security.Principal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Users", uniqueConstraints = @UniqueConstraint(columnNames = {"mail"}))
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@id")
public class User implements Principal {

    @Id
    @Column(name = "id")
    String id = UUID.randomUUID().toString();

    @Column(name = "Created", nullable = false)
    Date created = new Date();

    @Column(name = "Deleted", nullable = true)
    Date deleted;

    @Column(name = "Name", nullable = false)
    @JsonProperty("name")
    String name;

    @Column(name = "Mail", nullable = false)
    @Index(name = "mailIndex")
    @JsonProperty("mail")
    String mail;

    @Column(name = "Phone", nullable = true)
    @JsonProperty("phone")
    String phone;

    @Column(name = "Enabled", nullable = false)
    @JsonProperty("enabled")
    boolean enabled = true;

    @ManyToMany(
            targetEntity = Role.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @JsonProperty("roles")
    Set<Role> roles = new HashSet<>();

    @Column(name = "password", nullable = false)
    @JsonProperty("password")
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @Cascade(value = {org.hibernate.annotations.CascadeType.ALL})
    Set<Posting> postings;

    /**
     * Needed for de-serialization
     */
    public User() {
    }

    public User(String name, String mail, String password) {
        this.name = name;
        this.mail = mail;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        // Check if the id is correct - will throw IllegalArgumentException otherwise
        UUID.fromString(id);
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getDeleted() {
        return this.deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Posting> getPostings() {
        return postings;
    }

    public void setPostings(Set<Posting> postings) {
        this.postings = postings;
    }
}

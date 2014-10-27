package com.p.p.server.model.bean;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.annotations.Index;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Roles", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@id")
public class Role implements GrantedAuthority {

    /**
     * Needed for de-serialization
     */
    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    @Id
    String id = UUID.randomUUID().toString();

    @Column(name = "name")
    @Index(name = "nameIndex")
    String name;

    @ManyToMany(
            targetEntity = User.class,
            cascade = CascadeType.PERSIST,
            fetch = FetchType.EAGER
    )
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonProperty("users")
    Set<User> users = new HashSet<>();

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    @Transient
    public String getAuthority() {
        return getName();
    }
}

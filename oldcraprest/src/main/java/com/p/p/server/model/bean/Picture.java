package com.p.p.server.model.bean;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.p.p.server.model.repository.PDataRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "Pictures")
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class,
        property = "@id")
public class Picture {

    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "POSTING_ID")
    private Posting posting;

    private String message;

    private Long blobId;

    /**
     * Needed for de-serialization
     */
    public Picture() {
    }

    public Picture(Posting posting, String message) {
        this.posting = posting;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Posting getPosting() {
        return posting;
    }

    public void setPosting(Posting posting) {
        this.posting = posting;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(name = "BLOB_ID")
    public Long getBlobId() {
        return blobId;
    }

    public void setBlobId(Long blobId) {
        this.blobId = blobId;
    }
}

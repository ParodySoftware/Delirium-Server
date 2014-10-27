package com.p.p.server.model.bean;

import javax.persistence.*;

@Entity
@Table(name = "pdata")
public class PData {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "binary_data")
    private byte[] data;

    /**
     * Needed for de-serialization
     */
    public PData() {
    }

    public PData(byte[] data) {
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}

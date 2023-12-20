package com.project.Auth.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "keys")
public class SymmetricKeyTable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private int uid;

    @Column
    private String aesKey;

    @Column
    private int sessionID;

    public SymmetricKeyTable() {
    }

    public SymmetricKeyTable(int id, String aesKey, int sessionID) {
        this.uid = id;
        this.aesKey = aesKey;
        this.sessionID = sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public int getSessionID() {
        return sessionID;
    }

    public int getId() {
        return uid;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}
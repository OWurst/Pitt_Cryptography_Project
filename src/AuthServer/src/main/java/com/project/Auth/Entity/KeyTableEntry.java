package com.project.Auth.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// these key table entries will be for managing rs's with outdated keys
@Entity
@Table(name = "GroupKeyManagement")
public class KeyTableEntry {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int kid;

    @Column
    private int gid;

    @Column(length = 48, nullable = false)
    private String rsid;

    @Column(length = 48, nullable = false)
    private String aesKey;

    @Column
    private boolean isOutdated;

    public KeyTableEntry() {
    }

    public KeyTableEntry(int groupID, String rsid, String aesKey) {
        this.gid = groupID;
        this.rsid = rsid;
        this.aesKey = aesKey;
        this.isOutdated = false;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int groupID) {
        this.gid = groupID;
    }

    public String getRsid() {
        return rsid;
    }

    public void setRsid(String rsid) {
        this.rsid = rsid;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public int getId() {
        return kid;
    }

    public boolean isOutdated() {
        return isOutdated;
    }

    public void setOutdated(boolean isOutdated) {
        this.isOutdated = isOutdated;
    }
}

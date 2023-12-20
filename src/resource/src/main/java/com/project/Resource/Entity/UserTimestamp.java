package com.project.Resource.Entity;

import java.sql.Timestamp;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class UserTimestamp {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private int modifiedByWho;

    @Column(columnDefinition = "TIMESTAMP")
    private long timestamp;

    @ManyToOne
    @JoinColumn(name = "fileMetaData_id", nullable = false)
    @JsonBackReference
    private FileMetaData fileMetaData;

    public UserTimestamp() {

    }

    public UserTimestamp(int modifiedByWho) {
        this.modifiedByWho = modifiedByWho;
        this.timestamp = System.currentTimeMillis() / 1000;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public int getUserWhoModified() {
        return modifiedByWho;
    }

    // getter and setter fields for FileMetaData
    public FileMetaData getFileMetaData() {
        return fileMetaData;
    }

    public void setFileMetaData(FileMetaData fileMetaData) {
        this.fileMetaData = fileMetaData;
    }
}

package com.project.Auth.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Timestamp {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int tid;

    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime timestamp;

    public Timestamp() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimeStamp() {
        return timestamp;
    }
}
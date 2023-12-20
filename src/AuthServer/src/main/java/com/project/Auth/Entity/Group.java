package com.project.Auth.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resource_group")
public class Group {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gid;

    @Column(length = 30, unique = true, nullable = false)
    private String name;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name = "creator_id", referencedColumnName = "uid")
    private User creator;

    @Column(length = 48, nullable = true)
    private String aesKey;

    @Column
    private int joinCode;

    public Group() {
    }

    public Group(String name, User owner) {
        this.name = name;
        this.creator = owner;

        this.joinCode = (int) (Math.random() * (99999 - 10000)) + 10000;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return gid;
    }

    public String getName() {
        return name;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int getJoinCode() {
        return joinCode;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }
}

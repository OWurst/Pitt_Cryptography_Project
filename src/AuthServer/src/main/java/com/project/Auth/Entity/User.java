package com.project.Auth.Entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int uid;

    @Column(length = 30, unique = true, nullable = false)
    private String username;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 25, nullable = false)
    private String firstname;

    @Column(length = 25, nullable = false)
    private String lastname;

    @Column
    private boolean isLoggedIn;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Timestamp.class)
    @JoinColumn(name = "entity_logins", referencedColumnName = "uid")
    private List<Timestamp> loginLog;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Timestamp.class)
    @JoinColumn(name = "entity_logouts", referencedColumnName = "uid")
    private List<Timestamp> logoutLog;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Timestamp.class)
    @JoinColumn(name = "entity_loginFails", referencedColumnName = "uid")
    private List<Timestamp> failedLoginLog;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;

        loginLog = new ArrayList<Timestamp>();
        logoutLog = new ArrayList<Timestamp>();
    }

    public User(String username, String password, String firstname, String lastname) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;

        loginLog = new ArrayList<Timestamp>();
        logoutLog = new ArrayList<Timestamp>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {
        return uid;
    }

    public List<Timestamp> getLoginLog() {
        return loginLog;
    }

    public List<Timestamp> getLogoutLog() {
        return logoutLog;
    }

    public List<Timestamp> getFailedLoginLog() {
        return failedLoginLog;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void logLoggedIn() {
        loginLog.add(new Timestamp());
    }

    public void logLoggedOut() {
        logoutLog.add(new Timestamp());
    }

    public void logFailedLogin() {
        failedLoginLog.add(new Timestamp());
    }
}
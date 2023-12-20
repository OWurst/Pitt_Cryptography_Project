package com.project.Resource.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UserSessionData {
   
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private int userUID;

    @Column
    private long challengeNonce;

    @Column
    private String sharedKey;

    @Column(length = 100, nullable = false)
    private String ipAddr;

    @Column
    int sessionID;

    public UserSessionData() {

    }

    public UserSessionData(int newUserId, long newChallengeNonce, String newSharedKey, String newIPAddress, int sessionID) {
        this.userUID = newUserId;
        this.challengeNonce = newChallengeNonce;
        this.sharedKey = newSharedKey;
        this.ipAddr = newIPAddress;
        this.sessionID = sessionID;
    }

    public int getUserUID() {
        return userUID;
    }

    public void setUserUID(int userUID) {
        this.userUID = userUID;
    }

    public long getChallengeNonce() {
        return challengeNonce;
    }

    public void setChallengeNonce(long challengeNonce) {
        this.challengeNonce = challengeNonce;
    }

    public String getSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }
}

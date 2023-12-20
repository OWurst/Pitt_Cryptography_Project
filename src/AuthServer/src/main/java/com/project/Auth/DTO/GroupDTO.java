package com.project.Auth.DTO;

public class GroupDTO {
    private String groupName;
    private int joinCode;
    private int uid;
    private int groupID;
    private int newLeaderID;
    private String authToken;
    private long timestamp;
    private String rsid;

    public String getAuthToken() {
        return authToken;
    }

    public String getRsid() {
        return rsid;
    }

    public void setRsid(String rsid) {
        this.rsid = rsid;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getJoinCode() {
        return joinCode;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setJoinCode(int joinCode) {
        this.joinCode = joinCode;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getNewLeaderID() {
        return newLeaderID;
    }

    public void setNewLeaderID(int newLeaderID) {
        this.newLeaderID = newLeaderID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

package com.project.Auth.DTO;

public class RemoveFromGroupDTO {
    private int uid;
    private int userToRemoveID;
    private int groupID;
    private String authToken;
    private long timestamp;
    private String rsid;

    public int getUid() {
        return uid;
    }

    public String getRsid() {
        return rsid;
    }

    public void setRsid(String rsid) {
        this.rsid = rsid;
    }

    public int getUserToRemoveID() {
        return userToRemoveID;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUserToRemoveID(int userToRemoveID) {
        this.userToRemoveID = userToRemoveID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

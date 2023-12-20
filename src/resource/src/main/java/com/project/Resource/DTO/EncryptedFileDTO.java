package com.project.Resource.DTO;

public class EncryptedFileDTO {

    private int teamID;
    private String authToken;
    private long timestamp;
    private String encryptedFile;
    private String encryptedFileMetaData;
    private String encryptedFileName;
    private String encryptedFileIV;
    private int correspondingUserID;

    public EncryptedFileDTO() {

    }

    public EncryptedFileDTO(int teamID, String authToken, long timestamp, String encryptedFile,
            String encryptedFileMetaData, String encryptedFileName, int correspondingUserID, String encryptedFileIV) {
        this.teamID = teamID;
        this.authToken = authToken;
        this.timestamp = timestamp;
        this.encryptedFileMetaData = encryptedFileMetaData;
        this.encryptedFileName = encryptedFileName;
        this.correspondingUserID = correspondingUserID;
        this.encryptedFile = encryptedFile;
        this.encryptedFileIV = encryptedFileIV;

    }

    public int getCorrespondingUserID() {
        return correspondingUserID;
    }

    public void setCorrespondingUserID(int correspondingUserID) {
        this.correspondingUserID = correspondingUserID;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
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

    public String getEncryptedFile() {
        return encryptedFile;
    }

    public void setEncryptedFile(String encryptedFile) {
        this.encryptedFile = encryptedFile;
    }

    public String getEncryptedFileMetaData() {
        return encryptedFileMetaData;
    }

    public void setEncryptedFileMetaData(String encryptedFileMetaData) {
        this.encryptedFileMetaData = encryptedFileMetaData;
    }

    public String getEncryptedFileName() {
        return encryptedFileName;
    }

    public void setFileID(String encryptedFileName) {
        this.encryptedFileName = encryptedFileName;
    }

    public String getEncryptedFileIV() {
        return encryptedFileIV;
    }

    public void setEncryptedFileIV(String encryptedFileIV) {
        this.encryptedFileIV = encryptedFileIV;
    }
}

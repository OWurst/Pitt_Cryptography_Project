package com.project.Resource.Entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class FileMetaData {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int fileID;

    @Column
    private int userUploadedID;

    @Column
    private int teamID;

    @Column
    private String encryptedFileMetadata;

    @Column
    private String encryptedFileHashname;

    @Column
    private String encryptedFileIV;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "fileMetaData", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<UserTimestamp> fileModifiedList;

    // ! Blank constructor
    public FileMetaData() {

    }

    public FileMetaData(int userUploadedID, int teamID, String encryptedFileMetadata, String encryptedFileHashname,
            String encryptedFileIV) {

        this.userUploadedID = userUploadedID;
        this.teamID = teamID;
        this.encryptedFileMetadata = encryptedFileMetadata;
        this.encryptedFileHashname = encryptedFileHashname;
        this.encryptedFileIV = encryptedFileIV;
        this.fileModifiedList = new ArrayList<UserTimestamp>();
        UserTimestamp userTimestamp = new UserTimestamp(userUploadedID);
        userTimestamp.setFileMetaData(this);
        fileModifiedList.add(0, userTimestamp);
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public int getUserUploadedID() {
        return userUploadedID;
    }

    public void setUserUploadedID(int userUploadedID) {
        this.userUploadedID = userUploadedID;
    }

    public void addToModifiedList(UserTimestamp newUserModify) {
        this.fileModifiedList.add(newUserModify);
    }

    public String getEncryptedFileMetadata() {
        return encryptedFileMetadata;
    }

    public void setEncryptedFileMetadata(String encryptedFileMetadata) {
        this.encryptedFileMetadata = encryptedFileMetadata;
    }

    public String getEncryptedFileHashName() {
        return encryptedFileHashname;
    }

    public void setEncryptedFileHashName(String encryptedFileHashName) {
        this.encryptedFileHashname = encryptedFileHashName;
    }

    public String getEncryptedFileIV() {
        return encryptedFileIV;
    }

    public void setEncryptedFileIV(String encryptedFileIV) {
        this.encryptedFileIV = encryptedFileIV;
    }

    public void newModification(int userWhoModified, String newEncryptedMetadata, String newEncryptedFileHashName,
            String newEncryptedFileIV) {
        this.encryptedFileMetadata = newEncryptedMetadata;
        this.encryptedFileHashname = newEncryptedFileHashName;
        this.encryptedFileIV = newEncryptedFileIV;

        UserTimestamp userTimestamp = new UserTimestamp(userWhoModified);
        userTimestamp.setFileMetaData(this);
        fileModifiedList.add(userTimestamp);
    }

    public List<UserTimestamp> getFileModifiedList() {
        return fileModifiedList;
    }

    public void setFileModifiedList(List<UserTimestamp> fileModifiedList) {
        this.fileModifiedList = fileModifiedList;
    }

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int id) {
        this.fileID = id;
    }
}

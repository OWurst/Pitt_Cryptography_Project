package com.project.Resource.DTO;

public class SecureHandshakeDTO {
    
    private String sharedKeyPayload; 
    private String iv; 
    private String aesPayload;
    private int userID;
   
  
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

     public String getAesPayload() {
        return aesPayload;
    }

    public void setAesPayload(String aesPayload) {
        this.aesPayload = aesPayload;
    }

    public String getSharedKeyPayload() {
        return sharedKeyPayload;
    }

    public void setSharedKeyPayload(String sharedKeyPayload) {
        this.sharedKeyPayload = sharedKeyPayload;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

   

}

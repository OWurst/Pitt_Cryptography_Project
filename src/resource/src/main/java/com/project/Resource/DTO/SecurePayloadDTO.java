package com.project.Resource.DTO;

public class SecurePayloadDTO {

    private String payload;
    private String iv;
    private String hmac; 
    private int userID; 


    public SecurePayloadDTO() {
        
    }
    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
    public String getHMAC() {
        return hmac;
    }

    public String setHMAC(String hmac) {
        return this.hmac = hmac;
    }
    
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getIV() {
        return iv;
    }

    public void setIV(String iv) {
        this.iv = iv;
    }

}

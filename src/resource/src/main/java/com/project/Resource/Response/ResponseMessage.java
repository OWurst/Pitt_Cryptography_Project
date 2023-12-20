package com.project.Resource.Response;

// File Upload, Download, Delete, Update Response
public class ResponseMessage {
    private String encryptedData;
    private String iv;
    private String HMAC;
    private String plainTextMessage;

    public ResponseMessage() {
    }
    
    public ResponseMessage(String plainTextMessage) {
        this.plainTextMessage = plainTextMessage;
    }

    public ResponseMessage(String encryptedData, String iv, String HMAC, String plainTextMessage) {
        this.iv = iv;
        this.encryptedData = encryptedData;
        this.HMAC = HMAC;
        this.plainTextMessage = plainTextMessage;
    }
    public ResponseMessage(String encryptedData, String iv) {
        this.iv = iv;
        this.encryptedData = encryptedData;
    }

    public ResponseMessage(String encryptedData, String iv, String HMAC) {
        this.iv = iv;
        this.encryptedData = encryptedData;
        this.HMAC = HMAC;
    }
    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getHMAC() {
        return HMAC;
    }
    
    public void setHMAC(String HMAC) {
        this.HMAC = HMAC;
    }
    
   public String getPlainTextMessage() {
        return plainTextMessage;
   }
   public void setPlainTextMessage(String plainTextMessage) {
    this.plainTextMessage = plainTextMessage;
   }

}

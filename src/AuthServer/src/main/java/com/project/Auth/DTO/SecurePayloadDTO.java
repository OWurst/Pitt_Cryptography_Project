package com.project.Auth.DTO;

public class SecurePayloadDTO {
    int uid;

    String iv;

    String encryptedPayloadString;
    String encryptedAESKeyString;
    String aesEncryptedTimestampString;
    String hmac;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getEncryptedPayloadString() {
        return encryptedPayloadString;
    }

    public void setEncryptedPayloadString(String encryptedPayloadString) {
        this.encryptedPayloadString = encryptedPayloadString;
    }

    public String getEncryptedAESKeyString() {
        return encryptedAESKeyString;
    }

    public void setEncryptedAESKeyString(String encryptedAESKeyString) {
        this.encryptedAESKeyString = encryptedAESKeyString;
    }

    public String getAesEncryptedTimestampString() {
        return aesEncryptedTimestampString;
    }

    public void setAesEncryptedTimestampString(String aesEncryptedTimestampString) {
        this.aesEncryptedTimestampString = aesEncryptedTimestampString;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public String getHmac() {
        return hmac;
    }
}

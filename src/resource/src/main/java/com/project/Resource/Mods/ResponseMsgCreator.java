package com.project.Resource.Mods;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ResponseMsgCreator {

    @Autowired
    private Helper helper;

    public JSONObject buildEncryptedResponseMessage(String message, String sharedKey) {

        try {
            IvParameterSpec iv = helper.generateIV();
            String encryptedBase64ErrorMessage = helper.encryptAES(message.getBytes(), sharedKey, iv);
            JSONObject messageAndIV = new JSONObject();
            messageAndIV.put("Base64Message", encryptedBase64ErrorMessage);
            messageAndIV.put("Base64EncodedIV", Base64.getEncoder().encodeToString(iv.getIV()));
            return messageAndIV;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public JSONObject downloadFileFormater(Resource encryptedFile, String sharedKey) {
        try {
            // convert file to byte array, base 64 it, and generate iv
            InputStream inputStream = encryptedFile.getInputStream();
            byte[] fileBytes = inputStream.readAllBytes();
            IvParameterSpec iv = helper.generateIV();

            // Generate a timestamp
            long timestamp = System.currentTimeMillis() / 1000;

            // Make json to be encrypted
            JSONObject fileBytesAndTimestamp = new JSONObject();
            fileBytesAndTimestamp.put("file", Base64.getEncoder().encodeToString(fileBytes));
            fileBytesAndTimestamp.put("timestamp", timestamp);

            String encryptedBase64Message = helper.encryptAES(fileBytesAndTimestamp.toString().getBytes(), sharedKey,
                    iv);
            String HMAC = helper.generateHMAC(sharedKey, encryptedBase64Message);

            JSONObject finalResponse = new JSONObject();

            finalResponse.put("payload", encryptedBase64Message);
            finalResponse.put("Base64EncodedIV", Base64.getEncoder().encodeToString(iv.getIV()));
            finalResponse.put("HMAC", HMAC);
            return finalResponse;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public JSONObject listFileFormater(String encryptedFileList, String sharedKey) {
        try {
            // convert metadata to byte array, base 64 it, and generate iv
            IvParameterSpec iv = helper.generateIV();

            // Generate a timestamp
            long timestamp = System.currentTimeMillis() / 1000;

            // Make json to be encrypted
            JSONObject listBytesAndTimestamp = new JSONObject();
            JSONArray listBytesArray = new JSONArray(encryptedFileList);
            listBytesAndTimestamp.put("files", listBytesArray);
            listBytesAndTimestamp.put("timestamp", timestamp);

            String encryptedBase64Message = helper.encryptAES(listBytesAndTimestamp.toString().getBytes(), sharedKey,
                    iv);
            System.out.println("base64 string message before hmac is " + encryptedBase64Message);
            String HMAC = helper.generateHMAC(sharedKey, encryptedBase64Message);

            JSONObject finalResponse = new JSONObject();

            finalResponse.put("payload", encryptedBase64Message);
            finalResponse.put("Base64EncodedIV", Base64.getEncoder().encodeToString(iv.getIV()));
            finalResponse.put("HMAC", HMAC);
            return finalResponse;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

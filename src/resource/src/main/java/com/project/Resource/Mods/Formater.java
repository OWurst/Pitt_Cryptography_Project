package com.project.Resource.Mods;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.spec.IvParameterSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.project.Resource.DTO.SecurePayloadDTO;
import com.project.Resource.DTO.EncryptedFileDTO;
import com.project.Resource.Entity.UserSessionData;
import com.project.Resource.Repository.UserSessionRepository;
@Component
public class Formater {
    

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private Helper helper;


    public EncryptedFileDTO convertToEncryptedFileDTO(SecurePayloadDTO securePayloadDTO, String whichType) throws Exception {

        // First we need to parse for the user's UID and get their shared key 
        UserSessionData thisUserSessionData = userSessionRepository.findByUserUID(securePayloadDTO.getUserID());

        if (thisUserSessionData == null) {
            throw new Exception("User not found");
        }
        else { 
            //! First we verify HMAC 
            if (!helper.verifyHMAC(securePayloadDTO.getHMAC(), thisUserSessionData.getSharedKey(), securePayloadDTO.getPayload())) {
                throw new Exception("HMAC verification failed");
            }

            //! HMAC verified. Can now decrypt payload
            byte[] decryptedBytes = helper.decryptAES(securePayloadDTO.getPayload(),thisUserSessionData.getSharedKey(), securePayloadDTO.getIV());
            String decryptedString = new String(decryptedBytes);

            ObjectMapper objectMapper = new ObjectMapper();
        
            return objectMapper.readValue(decryptedString, EncryptedFileDTO.class);
           
        }
    }
}

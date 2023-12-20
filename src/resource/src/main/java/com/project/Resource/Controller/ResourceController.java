package com.project.Resource.Controller;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.spec.IvParameterSpec;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import com.project.Resource.DTO.EncryptedFileDTO;
import com.project.Resource.DTO.SecureHandshakeDTO;
import com.project.Resource.DTO.SecurePayloadDTO;
import com.project.Resource.Entity.FileMetaData;
import com.project.Resource.Entity.UserSessionData;
import com.project.Resource.Mods.Formater;
import com.project.Resource.Mods.Helper;
import com.project.Resource.Mods.ResponseMsgCreator;
import com.project.Resource.Repository.UserSessionRepository;
import com.project.Resource.Response.ResponseMessage;
import com.project.Resource.Service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = { "authorization" })
public class ResourceController {

    HashMap<Integer, String> authStage = new HashMap<>();

    @Autowired
    FileService fileService;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private Helper helper;

    @Autowired
    private Formater formater;

    @Autowired
    private ResponseMsgCreator responseMsgBuilder;

    @PostMapping("/listFiles")
    public ResponseEntity<ResponseMessage> listFiles(@RequestBody SecurePayloadDTO payload, HttpServletRequest request)
            throws Exception {

        try {
            EncryptedFileDTO shortFileDTO = formater.convertToEncryptedFileDTO(payload, "list");
            // Perform Replay check.
            if (!helper.verifyTimeStamp(shortFileDTO.getTimestamp()))
                throw new Exception("Replay Attack Detected");
            // Verify token
            else if (!helper.verifyToken(shortFileDTO.getAuthToken(), shortFileDTO.getTeamID(), payload.getUserID()))
                throw new Exception("Access Denied");

            else {
                List<FileMetaData> files = fileService.listFiles(shortFileDTO.getTeamID());
                ObjectMapper objectMapper = new ObjectMapper();
                String filesAsJson = objectMapper.writeValueAsString(files);
                JSONObject responseMessage = responseMsgBuilder.listFileFormater(filesAsJson,
                        userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
                return ResponseEntity.status(HttpStatus.OK)
                        .body((new ResponseMessage(responseMessage.getString("payload"),
                                responseMessage.getString("Base64EncodedIV"), responseMessage.getString("HMAC"))));
            }

        } catch (Exception e) {
            // Types of errors returned: HMAC verification failed | Replay Attack Detected |
            // User not found | Access Denied | Duplicate file name | Could not upload the
            // file
            e.printStackTrace();
            JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage(e.getMessage(),
                    userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body((new ResponseMessage(errorMessage.getString("Base64Message"),
                            errorMessage.getString("Base64EncodedIV"))));
        }
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestBody SecurePayloadDTO payload, HttpServletRequest request)
            throws Exception {

        try {

            // ! Decrypt and build our DTO object we can work with. (Including HMAC
            // verficiation)
            EncryptedFileDTO newEncryptedFileDTO = formater.convertToEncryptedFileDTO(payload, "upload");

            // Perform Replay check.
            if (!helper.verifyTimeStamp(newEncryptedFileDTO.getTimestamp()))
                throw new Exception("Replay Attack Detected");

            // Verify token
            else if (!helper.verifyToken(newEncryptedFileDTO.getAuthToken(), newEncryptedFileDTO.getTeamID(),
                    payload.getUserID()))
                throw new Exception("Access Denied");

            // Perform upload
            else {
                fileService.uploadFile(newEncryptedFileDTO, request);
                JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage("File uploaded successfully",
                        userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
                ResponseEntity.status(HttpStatus.OK).body((new ResponseMessage(errorMessage.getString("Base64Message"),
                        errorMessage.getString("Base64EncodedIV"))));
                JSONObject responseMessage = responseMsgBuilder.buildEncryptedResponseMessage(
                        "File uploaded successfully",
                        userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
                return ResponseEntity.status(HttpStatus.OK)
                        .body((new ResponseMessage(responseMessage.getString("Base64Message"),
                                responseMessage.getString("Base64EncodedIV"))));
            }

        } catch (Exception e) {
            // Types of errors returned: HMAC verification failed | Replay Attack Detected |
            // User not found | Access Denied | Duplicate file name | Could not upload the
            // file
            JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage(e.getMessage(),
                    userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body((new ResponseMessage(errorMessage.getString("Base64Message"),
                            errorMessage.getString("Base64EncodedIV"))));

        }

    }

    @PostMapping("/downloadFile")
    public ResponseEntity<ResponseMessage> downloadFile(@RequestBody SecurePayloadDTO payload,
            HttpServletRequest request)
            throws Exception {

        try {
            // ! Decrypt and build our DTO object we can work with. (Including HMAC
            // verficiation)

            EncryptedFileDTO newEncryptedFileDTO = formater.convertToEncryptedFileDTO(payload, "download");
            // replay check
            if (!helper.verifyTimeStamp(newEncryptedFileDTO.getTimestamp()))
                throw new Exception("Replay Attack Detected");
            // verify token
            else if (!helper.verifyToken(newEncryptedFileDTO.getAuthToken(), newEncryptedFileDTO.getTeamID(),
                    payload.getUserID()))
                throw new Exception("Access Denied");
            // perform download
            else {
                Resource encryptedFile = fileService.downloadFile(newEncryptedFileDTO.getTeamID(),
                        newEncryptedFileDTO.getEncryptedFileName());
                JSONObject responseMessage = responseMsgBuilder.downloadFileFormater(encryptedFile,
                        userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());

                return ResponseEntity.status(HttpStatus.OK)
                        .body((new ResponseMessage(responseMessage.getString("payload"),
                                responseMessage.getString("Base64EncodedIV"), responseMessage.getString("HMAC"))));
            }

        } catch (Exception e) {
            // file not found | hmac verification failed | replay attack detected | token
            // verification failed | File does not exist
            JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage(e.getMessage(),
                    userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body((new ResponseMessage(errorMessage.getString("Base64Message"),
                            errorMessage.getString("Base64EncodedIV"))));
        }
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<ResponseMessage> deleteFile(@RequestBody SecurePayloadDTO payload, HttpServletRequest request)
            throws Exception {

        try {
            EncryptedFileDTO newEncryptedFileDTO = formater.convertToEncryptedFileDTO(payload, "delete");
            // replay check
            if (!helper.verifyTimeStamp(newEncryptedFileDTO.getTimestamp()))
                throw new Exception("Replay Attack Detected");
            // verify token
            else if (!helper.verifyToken(newEncryptedFileDTO.getAuthToken(), newEncryptedFileDTO.getTeamID(),
                    payload.getUserID()))
                throw new Exception("Access Denied");
            // perform delete
            else {
                fileService.deleteFile(newEncryptedFileDTO.getTeamID(), newEncryptedFileDTO.getEncryptedFileName());
                JSONObject responseMessage = responseMsgBuilder.buildEncryptedResponseMessage(
                        "File deleted successfully",
                        userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
                return ResponseEntity.status(HttpStatus.OK)
                        .body((new ResponseMessage(responseMessage.getString("Base64Message"),
                                responseMessage.getString("Base64EncodedIV"))));
            }
        } catch (Exception e) {
            // file not found | hmac verification failed | replay attack detected | token
            // verification failed | File to delete does not exist | Could not delete file
            JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage(e.getMessage(),
                    userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body((new ResponseMessage(errorMessage.getString("Base64Message"),
                            errorMessage.getString("Base64EncodedIV"))));
        }

    }

    @PostMapping("/updateFile")
    public ResponseEntity<ResponseMessage> updateFile(@RequestBody SecurePayloadDTO payload,
            HttpServletRequest request) throws Exception {

        try {

            EncryptedFileDTO newEncryptedFileDTO = formater.convertToEncryptedFileDTO(payload, "update");
            // Perform Replay check.
            if (!helper.verifyTimeStamp(newEncryptedFileDTO.getTimestamp()))
                throw new Exception("Replay Attack Detected");
            // Verify token
            else if (!helper.verifyToken(newEncryptedFileDTO.getAuthToken(), newEncryptedFileDTO.getTeamID(),
                    payload.getUserID()))
                throw new Exception("Access Denied");
            // Perform update

            else {
                fileService.updateFile(newEncryptedFileDTO, request);
                JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage("File updated successfully",
                        userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
                return ResponseEntity.status(HttpStatus.OK)
                        .body((new ResponseMessage(errorMessage.getString("Base64Message"),
                                errorMessage.getString("Base64EncodedIV"))));
            }

        } catch (Exception e) {
            // Catching errors: | HMAC verification failed | Replay Attack Detected | File
            // to update does not exist in DB | Access Denied | Could not delete file
            JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage(e.getMessage(),
                    userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body((new ResponseMessage(errorMessage.getString("Base64Message"),
                            errorMessage.getString("Base64EncodedIV"))));

        }
    }

    @DeleteMapping("/deleteAllFiles")
    public ResponseEntity<ResponseMessage> deleteAllFiles(@RequestBody SecurePayloadDTO payload,
            HttpServletRequest request) throws Exception {

        try {
            EncryptedFileDTO newEncryptedFileDTO = formater.convertToEncryptedFileDTO(payload, "deleteAllFiles");
            // replay check
            if (!helper.verifyTimeStamp(newEncryptedFileDTO.getTimestamp()))
                throw new Exception("Replay Attack Detected");
            // verify token
            else if (!helper.verifyToken(newEncryptedFileDTO.getAuthToken(), newEncryptedFileDTO.getTeamID(),
                    payload.getUserID()))
                throw new Exception("Access Denied");
            // perform delete
            else {
                fileService.deleteAllFiles(newEncryptedFileDTO.getTeamID());
                JSONObject responseMessage = responseMsgBuilder.buildEncryptedResponseMessage(
                        "Files deleted successfully",
                        userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
                return ResponseEntity.status(HttpStatus.OK)
                        .body((new ResponseMessage(responseMessage.getString("Base64Message"),
                                responseMessage.getString("Base64EncodedIV"))));
            }
        } catch (Exception e) {
            // Team has no files | hmac verification failed | replay attack detected | token
            // verification failed | Could not delete files
            JSONObject errorMessage = responseMsgBuilder.buildEncryptedResponseMessage(e.getMessage(),
                    userSessionRepository.findByUserUID(payload.getUserID()).getSharedKey());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body((new ResponseMessage(errorMessage.getString("Base64Message"),
                            errorMessage.getString("Base64EncodedIV"))));
        }
    }

    @PostMapping("/endSession")
    public ResponseEntity<?> endSession(@RequestBody SecurePayloadDTO securePayloadDTO,
            HttpServletRequest request) throws Exception {

        // ! First check to make sure the user requesting this is actually authenticated
        // if (authStage.get(securePayloadDTO.getUserID()) != "Authenticated") throw new
        // Exception();

        try {

            EncryptedFileDTO newEndSessionDTO = formater.convertToEncryptedFileDTO(securePayloadDTO, "endSession");
            if (!helper.verifyTimeStamp(newEndSessionDTO.getTimestamp()))
                throw new Exception("Replay Attack Detected");

            // Perform update
            fileService.endSession(securePayloadDTO, request);
            authStage.remove(securePayloadDTO.getUserID());
            return ResponseEntity.status(HttpStatus.OK).body("");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("");
        }

    }

    @PostMapping("/requestPublicKeyAndID")
    public ResponseEntity<ResponseMessage> requestPublicKey(@RequestBody SecurePayloadDTO requestkeyDTO,
            HttpServletRequest request) {

        try {
            String correctResponseJSON = fileService.requestMyKey(requestkeyDTO, request);
            authStage.put(requestkeyDTO.getUserID(), "passedOne");
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(correctResponseJSON));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(""));
        }

    }

    @PostMapping("/shareSessionKey")
    public ResponseEntity<ResponseMessage> shareSessionKey(@RequestBody SecureHandshakeDTO secureHandshakeDTO,
            HttpServletRequest request) throws Exception {

        if (authStage.get(secureHandshakeDTO.getUserID()) == null)
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(""));
        else {
            try {
                String challengeAndTS = fileService.shareSessionKey(secureHandshakeDTO, request);
                IvParameterSpec iv = helper.generateIV();
                String encryptedChallenge = helper.encryptAES(challengeAndTS.getBytes(StandardCharsets.UTF_8),
                        userSessionRepository.findByUserUID(secureHandshakeDTO.getUserID()).getSharedKey(), iv);
                authStage.put(secureHandshakeDTO.getUserID(), "passedTwo");
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseMessage(encryptedChallenge, Base64.getEncoder().encodeToString(iv.getIV())));

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(e.getMessage()));
            }
        }

    }

    @PostMapping("/challengeCheck")
    public ResponseEntity<ResponseMessage> challengeCheck(@RequestBody SecureHandshakeDTO secureHandshakeDTO,
            HttpServletRequest request) {

        try {
            if (authStage.get(secureHandshakeDTO.getUserID()) != "passedTwo")
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(""));
            else {
                boolean passed = fileService.challengeCheck(secureHandshakeDTO, request);
                if (!passed) {
                    authStage.remove(secureHandshakeDTO.getUserID());
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(""));
                } else {
                    authStage.put(secureHandshakeDTO.getUserID(), "Authenticated");
                    JSONObject successMessage = responseMsgBuilder.buildEncryptedResponseMessage(
                            "Congrats! you are now authenticated",
                            userSessionRepository.findByUserUID(secureHandshakeDTO.getUserID()).getSharedKey());
                    return ResponseEntity.status(HttpStatus.OK)
                            .body((new ResponseMessage(successMessage.getString("Base64Message"),
                                    successMessage.getString("Base64EncodedIV"))));
                }
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(""));
        }
    }
}

package com.project.Resource.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import com.project.Resource.Repository.FileRepository;
import com.project.Resource.Repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.project.Resource.DTO.EncryptedFileDTO;
import com.project.Resource.DTO.SecureHandshakeDTO;
import com.project.Resource.DTO.SecurePayloadDTO;
import com.project.Resource.Entity.FileMetaData;
import com.project.Resource.Entity.UserSessionData;
import com.project.Resource.Mods.Helper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class FileServiceImpl implements FileService {

    Map<String, List<String>> ACCEPTED_TYPES_AND_EXT = new HashMap<>();
    private final Path rootPath = Paths.get("storage");

    @Value("${resource.pubKeyPath}")
    private String pubKeyPath;

    @Value("${resource.privKeyPath}")
    private String privKeyPath;

    @Value("${auth.authPubKeyPath}")
    private String authPubKeyPath;

    @Autowired
    private Helper helper;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    public FileServiceImpl() {

    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for storage");
        }
    }

    @Override
    public List<FileMetaData> listFiles(int teamID) {
        return fileRepository.findByTeamID(teamID);
    }

    @Override
    public void uploadFile(EncryptedFileDTO newEncryptedFileDTO, HttpServletRequest request) throws Exception {

        // * Grab team folder based on id (if it doesn't exist yet, create it)
        File teamFolder = new File(this.rootPath.resolve(String.valueOf(newEncryptedFileDTO.getTeamID())).toString());
        teamFolder.mkdir();
        Path dest = this.rootPath.resolve(String.valueOf(newEncryptedFileDTO.getTeamID()));

        try {
            // Write input stream to destination
            // store this by the hash of the file Meta data
            MessageDigest digest = MessageDigest.getInstance("MD5");

            // Hash the encrypted file metadata
            byte[] hash = digest
                    .digest(newEncryptedFileDTO.getEncryptedFileMetaData().getBytes(StandardCharsets.UTF_8));

            // Convert the hash to a hexadecimal string
            StringBuilder hashHex = new StringBuilder();
            for (byte b : hash) {
                hashHex.append(String.format("%02x", b));
            }

            FileMetaData newFileMetaData = new FileMetaData(newEncryptedFileDTO.getCorrespondingUserID(),
                    newEncryptedFileDTO.getTeamID(),
                    newEncryptedFileDTO.getEncryptedFileMetaData(), hashHex.toString(),
                    newEncryptedFileDTO.getEncryptedFileIV());
            fileRepository.save(newFileMetaData);
            String path = dest.resolve(hashHex.toString() + ".txt").toString();
            try (FileOutputStream fos = new FileOutputStream(path)) {
                fos.write(Base64.getDecoder().decode(newEncryptedFileDTO.getEncryptedFile()));
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        }

        catch (Exception e) {
            if (e instanceof FileAlreadyExistsException)
                throw new Exception("Duplicate Encrypted File ID Already Exists");
            else
                throw new Exception(e.getMessage());
        }
    }

    @Override
    public Resource downloadFile(int teamID, String encryptedFileName) throws Exception {
        try {
            // Grab file metadata
            FileMetaData fileMetaData = fileRepository.findOneByTeamIDAndEncryptedFileHashname(teamID,
                    encryptedFileName);
            Path dest = this.rootPath.resolve(String.valueOf(teamID));
            // Grab file name with extension
            if (fileMetaData == null)
                throw new Exception("File does not exist!");

            // Grab file path
            Path filePath = dest.resolve(fileMetaData.getEncryptedFileHashName() + ".txt");
            Resource fileResource = new UrlResource(filePath.toUri());

            // Check if file exists and is readable and return it
            if (fileResource.exists() && fileResource.isReadable())
                return fileResource;
            else
                throw new Exception("Could not read the file!");

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(int teamID, String encryptedFilename) throws Exception {
        try {
            // Grab file metadata
            FileMetaData fileMetaData = fileRepository.findOneByTeamIDAndEncryptedFileHashname(teamID,
                    encryptedFilename);

            if (fileMetaData == null)
                throw new Exception("File does not exist!");

            Path file = rootPath.resolve(String.valueOf(teamID))
                    .resolve(fileMetaData.getEncryptedFileHashName() + ".txt");

            boolean deletedFromServer = Files.deleteIfExists(file);

            if (deletedFromServer) {
                fileRepository.delete(fileMetaData);
                return true;
            } else
                return false;
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }

    }

    @Override
    public void updateFile(EncryptedFileDTO encryptedUpdateFileDTO, HttpServletRequest request) throws Exception {

        try {

            // First check does this metadata exist yet
            FileMetaData fileMetaDataToUpdate = fileRepository.findOneByTeamIDAndEncryptedFileHashname(
                    encryptedUpdateFileDTO.getTeamID(), encryptedUpdateFileDTO.getEncryptedFileName());

            if (fileMetaDataToUpdate == null)
                throw new Exception("File to update does not exist!");

            else {

                // Find and delete original File on server
                Path oldFile = rootPath.resolve(String.valueOf(encryptedUpdateFileDTO.getTeamID()))
                        .resolve(fileMetaDataToUpdate.getEncryptedFileHashName() + "." + "txt");
                boolean couldDelete = Files.deleteIfExists(oldFile);
                if (!couldDelete)
                    throw new Exception("Could not delete file in file to update!");

                MessageDigest digest = MessageDigest.getInstance("MD5");

                // Hash the encrypted file metadata
                byte[] hash = digest
                        .digest(encryptedUpdateFileDTO.getEncryptedFileMetaData().getBytes(StandardCharsets.UTF_8));

                // Convert the hash to a hexadecimal string
                StringBuilder hashHex = new StringBuilder();
                for (byte b : hash) {
                    hashHex.append(String.format("%02x", b));
                }

                // Write new file to destination
                String filePath = this.rootPath.resolve(String.valueOf(encryptedUpdateFileDTO.getTeamID()))
                        .resolve(hashHex.toString() + ".txt").toString();
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(Base64.getDecoder().decode(encryptedUpdateFileDTO.getEncryptedFile()));
                } catch (Exception e) {
                    throw new Exception(e.getMessage());
                }

                // Update corresponding metadata, and save it back to the database
                fileMetaDataToUpdate.newModification(encryptedUpdateFileDTO.getCorrespondingUserID(),
                        encryptedUpdateFileDTO.getEncryptedFileMetaData(), hashHex.toString(),
                        encryptedUpdateFileDTO.getEncryptedFileIV());
                fileRepository.save(fileMetaDataToUpdate);
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    @Override
    public boolean deleteAllFiles(int teamID) throws Exception {

        try {
            // ! Delete all files in the directory
            Path dest = this.rootPath.resolve(String.valueOf(teamID));
            String path = dest.toString();
            File file = dest.toFile();
            deleteAllFilesHelper(file);

            // if directory is deleted
            if (new File(path).delete()) {
                // ! Delete all the associated metadata
                List<FileMetaData> fileList = fileRepository.findByTeamID(teamID);
                for (FileMetaData fileMeta : fileList) {
                    fileRepository.delete(fileMeta);
                }
                return true;
            } else
                return false;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public void deleteAllFilesHelper(File file) {
        for (File teamFile : file.listFiles()) {
            if (teamFile.isDirectory()) {
                deleteAllFilesHelper(teamFile);
            }
            teamFile.delete();
        }
    }

    @Override
    public boolean endSession(SecurePayloadDTO securePayloadDTO, HttpServletRequest request) throws Exception {
        try {
            userSessionRepository.delete(userSessionRepository.findByUserUID(securePayloadDTO.getUserID()));
            return true;
        } catch (Exception e) {
            throw new Exception();
        }
    }

    public String requestMyKey(SecurePayloadDTO requestKeyDTO, HttpServletRequest request) throws Exception {
        try {

            Path publicKeyPath = Paths.get(new URI("file:///" + pubKeyPath));

            byte[] pubKeyBytes = Files.readAllBytes(publicKeyPath);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(pubKeyBytes);
            byte[] digest = md.digest();
            String hash = Base64.getEncoder().encodeToString(digest);
            JSONObject finalResponse = new JSONObject();
            finalResponse.put("RSID", hash);
            finalResponse.put("pubKey", hash);
            return finalResponse.toString();

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public String shareSessionKey(SecureHandshakeDTO shareSessionKey, HttpServletRequest request) throws Exception {

        try {

            // First lets decrypt and get the shared key, then use it to decrypt the token
            // and timestamp
            byte[] decryptedSharedKey = helper
                    .decryptRSA(Base64.getDecoder().decode(shareSessionKey.getSharedKeyPayload()));

            byte[] decryptedAesPayload = helper.decryptAES(shareSessionKey.getAesPayload(),
                    Base64.getEncoder().encodeToString(decryptedSharedKey), shareSessionKey.getIv());

            String decryptedAesPayloadstr = new String(decryptedAesPayload);
            // this is really a json of Token and Timestamp
            JSONObject decryptedAesPayloadJson = new JSONObject(decryptedAesPayloadstr);
            // get token and timestamp
            String tokenStr = decryptedAesPayloadJson.getString("token");
            Long timestamp = decryptedAesPayloadJson.getLong("timestamp");

            // Protect against replay attack:
            if (!helper.verifyTimeStamp(timestamp))
                throw new Exception("Replay Attack Detected");

            // Check signature of token
            Path actualPubKeyPath = Paths.get(new URI("file:///" + authPubKeyPath));
            JwtParser parser = Jwts.parserBuilder().setSigningKey(helper.readPubKey(actualPubKeyPath.toFile())).build(); // *
                                                                                                                         // will
                                                                                                                         // fire
                                                                                                                         // exception
                                                                                                                         // if
                                                                                                                         // not
                                                                                                                         // signed
                                                                                                                         // by
                                                                                                                         // auth

            // Create challenge and save database instance
            Claims claims = parser.parseClaimsJws(tokenStr).getBody();
            int sessionID = claims.get("sessionID", Integer.class);
            Random random = new Random();
            long nonce = Math.abs(random.nextLong()) % (long) Math.pow(2, 53);
            String keyToStore = Base64.getEncoder().encodeToString(decryptedSharedKey);

            UserSessionData newUser = new UserSessionData(shareSessionKey.getUserID(), nonce, keyToStore,
                    request.getRemoteAddr(), sessionID);
            userSessionRepository.save(newUser);
            // Now generate encrypted challenge string
            JSONObject challenge = new JSONObject();
            challenge.put("nonce", nonce);
            challenge.put("timestamp", System.currentTimeMillis() / 1000);
            return challenge.toString();

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    @Override
    public boolean challengeCheck(SecureHandshakeDTO challengeCheckDTO, HttpServletRequest request) throws Exception {

        try {

            // First lets use the shared key to decrypt both the nonce and timestamp
            byte[] decryptedAesPayload = helper.decryptAES(challengeCheckDTO.getAesPayload(),
                    userSessionRepository.findByUserUID(challengeCheckDTO.getUserID()).getSharedKey(),
                    challengeCheckDTO.getIv());

            // this is really a json of a timestamp and nonce
            JSONObject decryptedAesPayloadJson = new JSONObject(new String(decryptedAesPayload));
            long timestamp = decryptedAesPayloadJson.getLong("timestamp");
            long nonce = decryptedAesPayloadJson.getLong("nonce");

            // Protect against replay attack:
            if (!helper.verifyTimeStamp(timestamp))
                throw new Exception("Replay Attack Detected");

            // Check if challenge was passed, clear user if not. Otherwise return true,
            // indicating authentication passed.
            if (nonce != userSessionRepository.findByUserUID(challengeCheckDTO.getUserID()).getChallengeNonce() + 1) {
                userSessionRepository.delete(userSessionRepository.findByUserUID(challengeCheckDTO.getUserID()));
                return false;
            } else
                return true;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}

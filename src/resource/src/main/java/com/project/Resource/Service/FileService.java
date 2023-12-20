package com.project.Resource.Service;

import java.util.HashMap;
import java.util.List;
import com.project.Resource.Entity.FileMetaData;
import com.project.Resource.DTO.EncryptedFileDTO;
import com.project.Resource.DTO.SecureHandshakeDTO;
import com.project.Resource.DTO.SecurePayloadDTO;


import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.springframework.core.io.Resource;
import jakarta.servlet.http.HttpServletRequest;

public interface FileService {

    public void init(); // * initialize folder

    public List<FileMetaData> listFiles(int teamID);

    public void uploadFile(EncryptedFileDTO newEncryptedFileDTO, HttpServletRequest request) throws Exception;

    public Resource downloadFile(int teamID, String encryptedFileName) throws Exception;

    public boolean deleteFile(int teamID, String encryptedFileName) throws Exception;

    public void updateFile(EncryptedFileDTO updateFileDTO, HttpServletRequest request) throws Exception;

    public boolean deleteAllFiles(int teamID) throws Exception;

    public boolean endSession(SecurePayloadDTO securePayloadDTO, HttpServletRequest request) throws Exception;

    public String requestMyKey(SecurePayloadDTO requestKeyDTO, HttpServletRequest request) throws Exception;

    public String shareSessionKey(SecureHandshakeDTO secureHandshakeDTO, HttpServletRequest request) throws Exception;

    public boolean challengeCheck(SecureHandshakeDTO challengeCheckDTO, HttpServletRequest request) throws Exception;
 
}

package com.project.Resource.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.project.Resource.Entity.FileMetaData;

@EnableJpaRepositories
public interface FileRepository extends JpaRepository<FileMetaData, Integer> {



    public List<FileMetaData> findByTeamID(int teamID);

    public FileMetaData findOneByTeamIDAndEncryptedFileHashname(int teamID, String encryptedFileName);
}
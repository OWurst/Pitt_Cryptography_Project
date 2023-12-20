package com.project.Auth.Service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.project.Auth.DTO.GroupDTO;
import com.project.Auth.DTO.RemoveFromGroupDTO;

public interface GroupService {
    public ResponseEntity<String> createGroup(GroupDTO groupDTO);

    public ResponseEntity<String> joinGroup(GroupDTO groupDTO);

    public ResponseEntity<String> getGroupInfo(GroupDTO groupDTO);

    public ResponseEntity<String> getJoinCode(GroupDTO groupDTO);

    public ResponseEntity<String> leaveGroup(GroupDTO groupDTO);

    public ResponseEntity<String> removeUserFromGroup(RemoveFromGroupDTO removeFromGroupDTO);

    public List<Integer> getTeamList(int id);

    public ResponseEntity<String> updatedGroupKey(GroupDTO groupDTO);
}

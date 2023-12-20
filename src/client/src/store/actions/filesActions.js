import { createAsyncThunk } from "@reduxjs/toolkit";
import { decryptWithAES, downloadFileFromResourceServer, encryptWithAES, makeRequestAndDecrpytResponseFromAuthServer, makeRequestAndDecrpytResponseFromResourceServer } from "../../utils";
import { setActiveGroup } from "./groupsActions";

const makeEncryptedFileData = ({ data, getState }) => {
    const key = getState().groups.activeGroup.groupKey;
    const { name, newName, extension, type } = data;

    const { iv, encrypted: encryptedFile } = encryptWithAES(data.file, Buffer.from(key, "base64"));

    const { encrypted: encryptedFileMetaData } = encryptWithAES(JSON.stringify({
        name: newName || name,
        extension,
        type,
        timestamp: Date.now()
    }), key, iv);

    const rtn = {
        correspondingUserID: getState().auth.user.uid,
        encryptedFileMetaData: encryptedFileMetaData.toString("base64"),
        encryptedFile: encryptedFile.toString("base64"),
        encryptedFileIV: iv.toString("base64")
    };
    if (data.encryptedFileName) rtn.encryptedFileName = data.encryptedFileName;

    return rtn;
};

export const uploadFile = createAsyncThunk(
    "/uploadFile",
    async (data, { rejectWithValue, getState, dispatch }) => {
        try {
            const newData = makeEncryptedFileData({ data, getState });

            const response = await makeRequestAndDecrpytResponseFromResourceServer(newData, "/resource/uploadFile", undefined, getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(err.response.data);
        }
    }
);

export const updateFile = createAsyncThunk(
    "/updateFile",
    async (data, { rejectWithValue, getState, dispatch }) => {
        try {
            const newData = makeEncryptedFileData({ data, getState });

            const response = await makeRequestAndDecrpytResponseFromResourceServer(newData, "/resource/updateFile", undefined, getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(err.response.data);
        }
    }
);

export const listFiles = createAsyncThunk(
    "/listFiles",
    async (data, { rejectWithValue, getState, dispatch }) => {
        try {
            const response = await makeRequestAndDecrpytResponseFromResourceServer(undefined, "/resource/listFiles", undefined, getState, dispatch);
            const decryptedFiles = [];
            if (response.files.length) {
                const { files } = response;
                for (const file of files) {
                    const { encryptedFileIV, encryptedFileMetadata, fileModifiedList, fileID, encryptedFileHashName } = file;
                    const activeGroup = getState().groups.activeGroup;
                    const groupKey = activeGroup.isGroupKeyOutOfDate ? activeGroup.oldKey : activeGroup.groupKey;
                    const decryptedFileMetaData = decryptWithAES(Buffer.from(encryptedFileMetadata, "base64"), Buffer.from(groupKey, "base64"), Buffer.from(encryptedFileIV, "base64"));
                    const { name, extension, type } = JSON.parse(decryptedFileMetaData);
                    const decryptedFile = {
                        name,
                        extension,
                        type,
                        fileModifiedList,
                        id: fileID,
                        encryptedFileHashName,
                        encryptedFileIV
                    };
                    decryptedFiles.push(decryptedFile);
                }
            }
            return decryptedFiles;
        }
        catch (err) {
            return rejectWithValue(err.response.data);
        }
    }
);

export const deleteFiles = createAsyncThunk(
    "/deleteFile",
    async (data, { rejectWithValue, getState, dispatch }) => {
        try {
            const successes = [];
            const failures = [];
            const { fileIDs } = data;
            for (const fileID of fileIDs) {
                try {
                    const file = getState().files.files.find(file => file.id === fileID);
                    const { encryptedFileHashName: encryptedFileName } = file;
                    const newData = { encryptedFileName };
                    const response = await makeRequestAndDecrpytResponseFromResourceServer(newData, "/resource/deleteFile", "DELETE", getState, dispatch);
                    successes.push(response);
                }
                catch (err) {
                    failures.push(err.response.data);
                }
            }
            return { successes, failures };
        }
        catch (err) {
            return rejectWithValue(err.response.data);
        }
    });

export const deleteAllFiles = createAsyncThunk(
    "/deleteAllFiles",
    async (data, { rejectWithValue, getState, dispatch }) => {
        try {
            const response = await makeRequestAndDecrpytResponseFromResourceServer(undefined, "/resource/deleteAllFiles", "DELETE", getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(err.response.data);
        }
    }
);

export const reencryptFiles = createAsyncThunk(
    "reencryptFiles",
    async (data, { rejectWithValue, getState, dispatch }) => {
        try {
            const files = getState().files.files;
            for (const file of files) {
                const decryptedFile = await downloadFileFromResourceServer(file, getState, dispatch);
                const { extension, id, name, type, encryptedFileHashName: encryptedFileName } = file;
                const metadata = {
                    file: decryptedFile,
                    newName: name,
                    extension,
                    type,
                    id,
                    encryptedFileName
                };
                const newData = makeEncryptedFileData({ data: metadata, getState });
                await makeRequestAndDecrpytResponseFromResourceServer(newData, "/resource/updateFile", undefined, getState, dispatch);
            }
            const activeGroup = getState().groups.activeGroup;
            const authData = { groupID: activeGroup.groupID };
            await makeRequestAndDecrpytResponseFromAuthServer(authData, "/auth/groups/updatedGroupKey", undefined, getState, dispatch);
            dispatch(setActiveGroup(activeGroup));
        }
        catch (err) {
            return rejectWithValue(err.response.data);
        }
    }
);

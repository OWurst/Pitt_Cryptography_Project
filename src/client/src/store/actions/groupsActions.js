import { createAsyncThunk } from "@reduxjs/toolkit";
import {
    makeRequestAndDecrpytResponseFromAuthServer as makeRequestAndDecrpytResponse
} from "../../utils";
import { logout } from "./authActions";

const rejectHandler = (err, dispatch) => {
    let data;
    try {
        data = JSON.parse(err?.response?.data);
    }
    catch (e) {
        data = err?.response?.data;
    }
    const error = data?.msg || data;
    if (error === "Invalid token") {
        dispatch(logout());
    }
    return error;
};

export const getGroups = createAsyncThunk(
    "auth/userGroups",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            const response = await makeRequestAndDecrpytResponse(undefined, "/auth/userGroups", undefined, getState, dispatch);
            return response.groups;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const getGroupInfo = createAsyncThunk(
    "/groups/info",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            const response = await makeRequestAndDecrpytResponse(data, "/auth/groups/info", undefined, getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const createGroup = createAsyncThunk(
    "/groups/create",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            const response = await makeRequestAndDecrpytResponse(data, "/auth/groups/create", undefined, getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const joinGroup = createAsyncThunk(
    "/groups/join",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            const response = await makeRequestAndDecrpytResponse(data, "/auth/groups/join", "PUT", getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const setActiveGroup = createAsyncThunk(
    "setActiveGroup",
    async (data, { rejectWithValue, dispatch, getState }) => {
        const { groupName, joinCode, groupID } = data;
        if (!groupID) return null;
        try {
            data = {
                groupName,
                joinCode,
                groupID
            };
            const response = await makeRequestAndDecrpytResponse(data, "/auth/groups/info", undefined, getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const leaveGroup = createAsyncThunk(
    "/groups/leave",
    async (data, { rejectWithValue, getState, dispatch }) => {
        try {
            const response = await makeRequestAndDecrpytResponse(data, "/auth/groups/leave", "DELETE", getState, dispatch);
            dispatch(getGroups());
            const groups = getState().groups.groups.filter(group => group.groupID !== data.groupID);
            if (!groups || !groups.length) {
                dispatch(setActiveGroup({ groupID: null }));
            }
            else {
                dispatch(setActiveGroup({ groupID: groups[0].groupID }));
            }
            return response;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const removeUserFromGroup = createAsyncThunk(
    "/groups/removeFromGroup",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            const response = await makeRequestAndDecrpytResponse(data, "/auth/groups/removeFromGroup", "DELETE", getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

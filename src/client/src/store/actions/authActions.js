import { createAsyncThunk } from "@reduxjs/toolkit";
import {
    makeRequestAndDecrpytResponseFromAuthServer as makeRequestAndDecrpytResponse,
    makeRequestAndDecrpytResponseFromResourceServer,
    performAuthenticationWithAuthServer,
    performAuthenticationWithResourceServer
} from "../../utils";

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

export const createAccount = createAsyncThunk(
    "auth/createAccount",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            return await performAuthenticationWithAuthServer({ url: "/auth/createAccount", data, getState });
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const login = createAsyncThunk(
    "auth/login",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            return await performAuthenticationWithAuthServer({ url: "/auth/login", data, getState });
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const logout = createAsyncThunk(
    "auth/logout",
    async (data, { dispatch, getState }) => {
        try {
            await makeRequestAndDecrpytResponseFromResourceServer(undefined, "/resource/endSession", "POST", getState, dispatch);
            const response = await makeRequestAndDecrpytResponse(undefined, "/auth/logout", undefined, getState, dispatch);
            return response;
        }
        catch (err) {
            return; // errors are irrelevant here since we're logging out anyway
        }
    }
);

export const deleteUser = createAsyncThunk(
    "auth/deleteAccount",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            const response = await makeRequestAndDecrpytResponse(undefined, "/auth/deleteAccount", "DELETE", getState, dispatch);
            return response;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

export const performResourceServerAuth = createAsyncThunk(
    "performResourceServerAuth",
    async (data, { rejectWithValue, getState }) => {
        try {
            const { authenticated, sessionKey } = await performAuthenticationWithResourceServer({
                uid: getState().auth.user.uid,
                authToken: getState().auth.user.authToken,
                resourceServerPubKeyPath: getState().config.resourceServerPubKeyPath
            });
            if (authenticated) {
                return sessionKey;
            }
            else {
                throw Error("Authentication with resource server failed.");
            }
        }
        catch (err) {
            if (typeof err?.response?.data === "string") return rejectWithValue(err?.response?.data);
            else return rejectWithValue(err?.response?.data?.error);
        }
    }
);

export const refreshAuthToken = createAsyncThunk(
    "/auth/refreshAuthToken",
    async (data, { rejectWithValue, dispatch, getState }) => {
        try {
            await makeRequestAndDecrpytResponse(data, "/auth/refreshAuthToken", undefined, getState, dispatch);
            return true;
        }
        catch (err) {
            return rejectWithValue(rejectHandler(err, dispatch));
        }
    }
);

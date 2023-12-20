import { createSlice } from "@reduxjs/toolkit";
import { deleteAllFiles, deleteFiles, listFiles, reencryptFiles, updateFile, uploadFile } from "../actions/filesActions";
import { logout, performResourceServerAuth } from "../actions/authActions";

const initialState = {
    files: [],
    fetching: false,
    reencryptingFiles: false,
    response: null,
    error: null,
    sessionKey: window.localStorage.getItem("resourceSessionKey") ? window.localStorage.getItem("resourceSessionKey") : null
};

const filesSlice = createSlice({
    name: "files",
    initialState,
    reducers: {
        clearResponse: state => {
            state.response = null;
        },
        clearError: state => {
            state.error = null;
        },
        setError: (state, { payload }) => {
            state.error = payload;
        }
    },
    extraReducers: builder => {
        builder
            .addCase(performResourceServerAuth.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(performResourceServerAuth.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(performResourceServerAuth.fulfilled, (state, { payload }) => {
                state.fetching = false;
                window.localStorage.setItem("resourceSessionKey", payload);
                state.sessionKey = payload;
            })
            .addCase(uploadFile.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(uploadFile.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(uploadFile.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(updateFile.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(updateFile.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(updateFile.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(listFiles.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(listFiles.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.files = payload;
            })
            .addCase(listFiles.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(deleteFiles.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(deleteFiles.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(deleteFiles.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(deleteAllFiles.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(deleteAllFiles.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(deleteAllFiles.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(logout.fulfilled, state => {
                state.files = [];
                state.fetching = false;
                state.response = null;
                state.error = null;
                state.sessionKey = null;
                window.localStorage.removeItem("resourceSessionKey");
            })
            .addCase(reencryptFiles.pending, state => {
                state.reencryptingFiles = true;
                state.error = null;
            })
            .addCase(reencryptFiles.fulfilled, state => {
                state.reencryptingFiles = false;
            });
    }
});

export const { clearResponse, clearError, setError } = filesSlice.actions;

export default filesSlice.reducer;

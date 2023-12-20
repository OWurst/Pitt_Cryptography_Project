import { createSlice } from "@reduxjs/toolkit";
import { createAccount, deleteUser, login, logout, refreshAuthToken } from "../actions/authActions";

const initialState = {
    fetching: false,
    user: window.localStorage.getItem("user") ? JSON.parse(window.localStorage.getItem("user")) : {
        username: null,
        firstname: null,
        lastname: null,
        uid: null,
        groups: [],
        authToken: null,
        AESKey: null
    },
    error: null
};

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        setAuthToken: (state, { payload }) => {
            state.user.authToken = payload;
            window.localStorage.setItem("user", JSON.stringify(state.user));
        }
    },
    extraReducers: builder => {
        builder
            .addCase(createAccount.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(createAccount.fulfilled, (state, { payload }) => {
                state.fetching = false;
                window.localStorage.setItem("user", JSON.stringify(payload));
                state.user = payload;
            })
            .addCase(createAccount.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(login.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(login.fulfilled, (state, { payload }) => {
                state.fetching = false;
                window.localStorage.setItem("user", JSON.stringify(payload));
                state.user = payload;
            })
            .addCase(login.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(logout.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(logout.fulfilled, state => {
                state.fetching = false;
                state.user = { ...initialState };
                window.localStorage.removeItem("user");
                state.error = null;
            })
            .addCase(logout.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(deleteUser.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(deleteUser.fulfilled, state => {
                state.fetching = false;
                window.localStorage.removeItem("user");
                state.user = { ...initialState.user };
                state.error = null;
            })
            .addCase(deleteUser.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(refreshAuthToken.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(refreshAuthToken.fulfilled, (state, { payload }) => {
                // taken care of in another function
            })
            .addCase(refreshAuthToken.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            });
    }
});

export const { setAuthToken } = authSlice.actions;

export default authSlice.reducer;

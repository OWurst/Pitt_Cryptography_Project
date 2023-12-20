import { createSlice } from "@reduxjs/toolkit";

const initialState = {
    authServer: window.localStorage.getItem("authServer"),
    resourceServer: window.localStorage.getItem("resourceServer"),
    resourceServerPubKeyPath: window.localStorage.getItem("resourceServerPubKeyPath"),
    resourceServerID: window.localStorage.getItem("resourceServerID")
};

const configSlice = createSlice({
    name: "config",
    initialState,
    reducers: {
        setConfig: (state, { payload }) => {
            Object.keys(payload).forEach(key => {
                window.localStorage.setItem(key, payload[key]);
                state[key] = payload[key];
            });
        },
        clearConfig: state => {
            Object.keys(initialState).forEach(key => {
                window.localStorage.removeItem(key);
                state[key] = null;
            });
        }
    }
});

export const { setConfig, clearConfig } = configSlice.actions;

export default configSlice.reducer;

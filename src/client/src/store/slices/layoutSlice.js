import { createSlice } from "@reduxjs/toolkit";
import { logout } from "../actions/authActions";

const initialState = {
    snackBar: {
        open: false,
        message: null,
        severity: null
    }
};

const layoutSlice = createSlice({
    name: "layout",
    initialState,
    reducers: {
        setSnackBar: (state, { payload }) => {
            state.snackBar = { open: true, ...payload };
        },
        closeSnackBar: state => {
            state.snackBar = { ...initialState.snackBar };
        }
    },
    extraReducers: builder => {
        builder
            .addCase(logout.fulfilled, state => {
                state.snackBar = { ...initialState.snackBar };
            });
    }
});

export const { setSnackBar, closeSnackBar } = layoutSlice.actions;
export default layoutSlice.reducer;

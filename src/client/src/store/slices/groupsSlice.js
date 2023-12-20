import { createSlice } from "@reduxjs/toolkit";
import { createGroup, getGroupInfo, getGroups, joinGroup, leaveGroup, removeUserFromGroup, setActiveGroup } from "../actions/groupsActions";
import { logout } from "../actions/authActions";

const initialState = {
    fetching: false,
    groups: window.localStorage.getItem("groups") ? JSON.parse(window.localStorage.getItem("groups")) : [],
    info: null,
    response: null,
    error: null,
    activeGroup: window.localStorage.getItem("activeGroup") ? JSON.parse(window.localStorage.getItem("activeGroup")) : null
};

const groupsSlice = createSlice({
    name: "groups",
    initialState,
    reducers: {
        clearResponse: state => {
            state.response = null;
        },
        clearInfo: state => {
            state.info = null;
        },
        setError: (state, { payload }) => {
            state.error = payload;
        },
        clearError: state => {
            state.error = null;
        }
    },
    extraReducers: builder => {
        builder
            .addCase(logout.fulfilled, state => {
                state.fetching = false;
                state.groups = [];
                state.response = null;
                state.error = null;
                state.activeGroup = null;
                state.info = null;
                window.localStorage.removeItem("groups");
                window.localStorage.removeItem("activeGroup");
            })
            .addCase(setActiveGroup.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(setActiveGroup.fulfilled, (state, { payload }) => {
                if (payload) {
                    state.fetching = false;
                    window.localStorage.setItem("activeGroup", JSON.stringify(payload));
                    state.activeGroup = payload;
                }
                else {
                    state.fetching = false;
                    state.activeGroup = null;
                    window.localStorage.removeItem("activeGroup");
                }
            })
            .addCase(setActiveGroup.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(createGroup.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(createGroup.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(createGroup.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(getGroups.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(getGroups.fulfilled, (state, { payload }) => {
                state.fetching = false;
                window.localStorage.setItem("groups", JSON.stringify(payload));
                state.groups = payload;
            })
            .addCase(getGroups.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(getGroupInfo.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(getGroupInfo.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.info = payload;
            })
            .addCase(getGroupInfo.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(joinGroup.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(joinGroup.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(joinGroup.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(leaveGroup.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(leaveGroup.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(leaveGroup.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            })
            .addCase(removeUserFromGroup.pending, state => {
                state.fetching = true;
                state.error = null;
            })
            .addCase(removeUserFromGroup.fulfilled, (state, { payload }) => {
                state.fetching = false;
                state.response = payload;
            })
            .addCase(removeUserFromGroup.rejected, (state, { payload }) => {
                state.fetching = false;
                state.error = payload;
            });
    }
});

export const { clearResponse, clearError, clearInfo, setError } = groupsSlice.actions;

export default groupsSlice.reducer;

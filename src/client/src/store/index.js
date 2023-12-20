import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/authSlice";
import groupsReducer from "./slices/groupsSlice";
import layoutReducer from "./slices/layoutSlice";
import filesReducer from "./slices/filesSlice";
import configReducer from "./slices/configSlice";
import { reencryptFiles } from "./actions/filesActions";

export const store = configureStore({
    reducer: {
        auth: authReducer,
        groups: groupsReducer,
        files: filesReducer,
        layout: layoutReducer,
        config: configReducer
    }
});

const handleGroupKeyChange = () => {
    store.subscribe(() => {
        const { activeGroup } = store.getState().groups;
        const { reencryptingFiles } = store.getState().files;
        if (activeGroup && activeGroup.isGroupKeyOutOfDate && !reencryptingFiles) {
            store.dispatch(reencryptFiles());
        }
    });
};

handleGroupKeyChange();

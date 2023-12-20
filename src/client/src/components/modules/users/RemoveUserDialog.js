import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { clearError, clearResponse } from "../../../store/slices/groupsSlice";
import { setSnackBar } from "../../../store/slices/layoutSlice";
import { Alert, Button, Dialog, DialogActions, DialogTitle } from "@mui/material";
import { removeUserFromGroup, setActiveGroup } from "../../../store/actions/groupsActions";

const RemoveUserDialog = ({ open, setOpen, userToRemove }) => {
    const dispatch = useDispatch();
    const { error, response } = useSelector(state => state.groups);
    const activeGroupID = useSelector(state => state.groups?.activeGroup?.groupID);

    const { username, firstname, lastname } = userToRemove;

    const handleClose = React.useCallback(() => {
        dispatch(clearError());
        dispatch(clearResponse());
        setOpen(false);
    }, [dispatch, setOpen]);

    const handleDelete = React.useCallback(() => {
        const { id: userToRemoveID } = userToRemove;
        dispatch(removeUserFromGroup({ userToRemoveID, groupID: activeGroupID }));
    }, [activeGroupID, dispatch, userToRemove]);

    React.useEffect(() => {
        if (response && response.msg && response.msg === "Delete Successful") {
            dispatch(setSnackBar({ severity: "success", message: "User Successfully Removed" }));
            dispatch(setActiveGroup({ groupID: activeGroupID }));
            handleClose();
        }
    }, [activeGroupID, dispatch, handleClose, response]);

    return (
        <Dialog open={open} onClose={handleClose}>
            <DialogTitle>Are you sure you want to remove {firstname} {lastname} ({username}) from the group?</DialogTitle>
            {error && <Alert severity="error">{error}</Alert>}
            <DialogActions>
                <Button onClick={handleClose}>Cancel</Button>
                <Button onClick={handleDelete}>Remove User</Button>
            </DialogActions>
        </Dialog>
    );
};

export default React.memo(RemoveUserDialog);

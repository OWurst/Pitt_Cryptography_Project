import { Alert, Button, Dialog, DialogActions, DialogTitle } from "@mui/material";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { clearError, clearResponse } from "../../../store/slices/filesSlice";
import { setSnackBar } from "../../../store/slices/layoutSlice";
import { deleteAllFiles, listFiles } from "../../../store/actions/filesActions";

const DeleteAllFilesDialog = ({ open, setOpen }) => {
    const dispatch = useDispatch();
    const { error, response } = useSelector(state => state.files);

    const handleClose = React.useCallback(() => {
        dispatch(clearError());
        dispatch(clearResponse());
        dispatch(listFiles());
        setOpen(false);
    }, [dispatch, setOpen]);

    const handleDelete = () => {
        dispatch(deleteAllFiles());
    };

    React.useEffect(() => {
        if (response && response === "Files deleted successfully") {
            dispatch(setSnackBar({ severity: "success", message: response }));
            handleClose();
        }
    }, [dispatch, handleClose, response]);

    return (
        <Dialog open={open} onClose={handleClose}>
            <DialogTitle>Are you sure you want to delete all files for your team?</DialogTitle>
            {error && <Alert severity="error">{error}</Alert>}
            <DialogActions>
                <Button onClick={handleClose}>Cancel</Button>
                <Button onClick={handleDelete}>Delete All Files</Button>
            </DialogActions>
        </Dialog>
    );
};

export default React.memo(DeleteAllFilesDialog);

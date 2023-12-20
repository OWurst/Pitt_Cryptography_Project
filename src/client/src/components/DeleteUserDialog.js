import { Button, Dialog, DialogActions, DialogTitle } from "@mui/material";
import React from "react";
import { useDispatch } from "react-redux";
import { deleteUser } from "../store/actions/authActions";

const DeleteUserDialog = ({ open, setOpen }) => {
    const dispatch = useDispatch();
    const handleDelete = () => {
        dispatch(deleteUser());
        setOpen(false);
    };
    return (
        <Dialog open={open}>
            <DialogTitle>Are you sure you want to delete your user?</DialogTitle>
            <DialogActions>
                <Button onClick={() => setOpen(false)}>Cancel</Button>
                <Button onClick={() => handleDelete()}>Delete</Button>
            </DialogActions>
        </Dialog>
    );
};

export default React.memo(DeleteUserDialog);

import { Dialog, DialogContent, DialogTitle, TextField } from "@mui/material";
import React from "react";
import Form from "../../Form";
import { useDispatch, useSelector } from "react-redux";
import { createGroup, getGroups } from "../../../store/actions/groupsActions";
import { clearResponse } from "../../../store/slices/groupsSlice";
import { setSnackBar } from "../../../store/slices/layoutSlice";

const CreateGroupDialog = ({ open, setOpen }) => {
    const dispatch = useDispatch();
    const { error, response } = useSelector(state => state.groups);

    const fields = [{ label: "Group Name", autoFocus: true, required: true, md: 12, Field: TextField }];

    const handleCreateGroup = data => {
        dispatch(createGroup(data));
    };

    React.useEffect(() => {
        if (response && response.msg && response.msg.startsWith("Success Creating Group:")) {
            dispatch(clearResponse());
            dispatch(getGroups());
            dispatch(setSnackBar({ message: response.msg, severity: "success" }));
            setOpen(false);
        }
    }, [dispatch, response, setOpen]);

    return (
        <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
            <DialogTitle variant="h3" style={{ textAlign: "center" }}>Create a Group</DialogTitle>
            <DialogContent>
                <Form
                    fields={fields}
                    submitHandler={handleCreateGroup}
                    submitText="Create Group"
                    error={error}
                />
            </DialogContent>
        </Dialog>);
};

export default React.memo(CreateGroupDialog);

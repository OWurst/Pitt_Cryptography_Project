import { Dialog, DialogContent, DialogTitle, TextField } from "@mui/material";
import React from "react";
import Form from "../../Form";
import { useDispatch, useSelector } from "react-redux";
import { getGroups, joinGroup } from "../../../store/actions/groupsActions";
import { clearResponse } from "../../../store/slices/groupsSlice";
import { setSnackBar } from "../../../store/slices/layoutSlice";

const JoinGroupDialog = ({ open, setOpen }) => {
    const dispatch = useDispatch();
    const { error, response } = useSelector(state => state.groups);

    const fields = [{ label: "Join Code", autoFocus: true, required: true, md: 12, Field: TextField }];

    const handleJoinGroup = data => {
        dispatch(joinGroup(data));
    };

    React.useEffect(() => {
        if (response && response.msg && response.msg === "Success") {
            dispatch(clearResponse());
            dispatch(getGroups());
            dispatch(setSnackBar({ message: response.msg, severity: "success" }));
            setOpen(false);
        }
    }, [dispatch, response, setOpen]);

    return (
        <Dialog open={open} onClose={() => setOpen(false)} maxWidth="sm" fullWidth>
            <DialogTitle variant="h3" style={{ textAlign: "center" }}>Join a Group</DialogTitle>
            <DialogContent>
                <Form
                    fields={fields}
                    submitHandler={handleJoinGroup}
                    submitText="Join Group"
                    error={error}
                />
            </DialogContent>
        </Dialog>);
};

export default React.memo(JoinGroupDialog);

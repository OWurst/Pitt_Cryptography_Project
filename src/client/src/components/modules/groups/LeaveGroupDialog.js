import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControl, FormControlLabel, FormLabel, Radio, RadioGroup, Typography } from "@mui/material";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import { getGroupInfo, getGroups, leaveGroup } from "../../../store/actions/groupsActions";
import { clearError, clearInfo, clearResponse, setError } from "../../../store/slices/groupsSlice";
import { setSnackBar } from "../../../store/slices/layoutSlice";

const NewLeaderChooser = ({ members, newLeader, setNewLeader }) => {
    const creatorID = useSelector(state => state.groups.info.creatorID);
    return (
        <FormControl sx={{ width: "100%", marginTop: "25px" }}>
            <FormLabel>You must choose a new leader:</FormLabel>
            <RadioGroup value={newLeader} onChange={e => setNewLeader(e.target.value)}>
                {members.map(member => {
                    if (member.id === creatorID) {
                        return null;
                    }
                    return (
                        <FormControlLabel key={`formControl-newLeader-${member.id}`} value={member.id} control={<Radio />} label={`${member.firstname} ${member.lastname} (${member.username})`} />
                    );
                })}
            </RadioGroup>
        </FormControl>
    );
};

const LeaveGroupDialog = ({ open, setOpen, groupName, groupID, creatorID }) => {
    const dispatch = useDispatch();
    const { error, response, info } = useSelector(state => state.groups);
    const [newLeader, setNewLeader] = React.useState(null);
    const userID = useSelector(state => state.auth.user.uid);

    const handleClose = () => {
        dispatch(clearInfo());
        dispatch(clearError());
        setOpen(false);
    };

    const handleLeaveGroup = () => {
        dispatch(clearError());
        const data = { groupID };
        if (creatorID === userID && info?.members?.length > 1) {
            if (!newLeader) {
                dispatch(setError("You must choose a new leader"));
                return;
            }
            data.newLeaderID = newLeader;
        }
        dispatch(leaveGroup(data));
    };

    React.useEffect(() => {
        if (creatorID === userID && !info) {
            dispatch(getGroupInfo({ groupID }));
        }
    }, []); // eslint-disable-line react-hooks/exhaustive-deps

    React.useEffect(() => {
        if (response && response.msg === "Delete Successful") {
            dispatch(clearResponse());
            dispatch(getGroups());
            dispatch(setSnackBar({ message: "Successfully left group", severity: "success" }));
            setOpen(false);
        }
    }, [dispatch, response, setOpen]);

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
            <DialogTitle variant="h3" style={{ textAlign: "center" }}>Leave {groupName}</DialogTitle>
            <DialogContent>
                <Typography variant="body1" textAlign="center">Are you sure you want to leave this group?</Typography>
                {info && info?.members ?
                    info?.members?.length > 1 ? <NewLeaderChooser members={info?.members} newLeader={newLeader} setNewLeader={setNewLeader} /> :
                        <Alert severity="warning" style={{ marginTop: "25px" }}>You are the only member of this group. Leaving this group will delete it.</Alert>
                    : null
                }
                {error && <Alert severity="error" style={{ marginTop: "25px" }}>{error}</Alert>}
            </DialogContent>
            <DialogActions>
                <Button onClick={handleLeaveGroup} color="primary">Leave</Button>
                <Button onClick={handleClose} color="secondary">Cancel</Button>
            </DialogActions>
        </Dialog>);
};

export default React.memo(LeaveGroupDialog);

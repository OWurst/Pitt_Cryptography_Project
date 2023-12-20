import React from "react";
import { Box, Button, Card, CardContent, CardHeader, Grid, Typography } from "@mui/material";
import styled from "@emotion/styled";
import CreateGroupDialog from "./CreateGroupDialog";
import { useSelector } from "react-redux";
import JoinGroupDialog from "./JoinGroupDialog";
import LeaveGroupDialog from "./LeaveGroupDialog";

const StyledCard = styled(Card)(() => ({
    height: "100%",
    display: "flex",
    flexDirection: "column"
}));

const StyledCardHeader = styled(CardHeader)(({ theme }) => ({
    padding: 10,
    background: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    textAlign: "center"
}));

const StyledCardContent = styled(CardContent)(() => ({
    flex: 1,
    "& > div": {
        height: "100%"
    }
}));

const GroupCard = ({ group }) => {
    const [leaveGroupDialogOpen, setLeaveGroupDialogOpen] = React.useState(false);
    return (
        <>
            <StyledCard>
                <StyledCardHeader
                    title={group.groupName}
                    titleTypographyProps={{ sx: { fontWeight: 700 } }}
                />
                <StyledCardContent>
                    <Box>
                        <Typography variant="body1" textAlign="center">Group Manager: {group.creator}</Typography>
                        {group.joinCode && Number(group.joinCode) !== 0 ? <Typography variant="body1" textAlign="center">Join Code: {group.joinCode}</Typography> : null}
                        <Button variant="contained" color="secondary" fullWidth sx={{ mt: 2 }} onClick={() => setLeaveGroupDialogOpen(true)}>Leave Group</Button>
                    </Box>
                </StyledCardContent>
            </StyledCard>
            {leaveGroupDialogOpen && <LeaveGroupDialog open={leaveGroupDialogOpen} setOpen={setLeaveGroupDialogOpen} {...group} />}
        </>
    );
};

const GroupsList = () => {
    const groups = useSelector(state => state.groups.groups);
    const [createGroupDialogOpen, setCreateGroupDialogOpen] = React.useState(false);
    const [joinGroupDialogOpen, setJoinGroupDialogOpen] = React.useState(false);

    return (
        <>
            <Box display="flex" justifyContent="space-between">
                <Typography variant="h1">My Groups</Typography>
                <Box>
                    <Button variant="contained" color="primary" onClick={() => setCreateGroupDialogOpen(true)}>Create Group</Button>
                    <Button variant="contained" color="secondary" style={{ marginLeft: "10px" }} onClick={() => setJoinGroupDialogOpen(true)}>Join Group</Button>
                </Box>
            </Box>
            <Grid container spacing={2} mt={1}>
                {(groups && !!groups.length) && groups.map(group => {
                    return (
                        <Grid item xs={12} md={4} key={`group-${group.groupID}`}>
                            <GroupCard group={group} />
                        </Grid>
                    );
                })}
            </Grid>
            {createGroupDialogOpen && <CreateGroupDialog open={createGroupDialogOpen} setOpen={setCreateGroupDialogOpen} />}
            {joinGroupDialogOpen && <JoinGroupDialog open={joinGroupDialogOpen} setOpen={setJoinGroupDialogOpen} />}
        </>
    );
};

export default React.memo(GroupsList);

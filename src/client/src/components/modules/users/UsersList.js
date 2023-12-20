import React from "react";
import { DataGrid } from "@mui/x-data-grid";
import { useSelector } from "react-redux";
import { Button } from "@mui/material";
import RemoveUserDialog from "./RemoveUserDialog";

const UsersList = () => {
    const activeGroupMembers = useSelector(state => state.groups?.activeGroup?.members);
    const activeGroupLeaderID = useSelector(state => state.groups?.activeGroup?.creatorID);
    const currentUserID = useSelector(state => state.auth.user.uid);
    const [removeUserDialogOpen, setRemoveUserDialogOpen] = React.useState(false);
    const [userToRemove, setUserToRemove] = React.useState(null);

    const handleRemoveUser = userToRemove => {
        setUserToRemove(userToRemove);
        setRemoveUserDialogOpen(true);
    };

    const columns = [
        {
            field: "actions",
            headerName:
                "Actions",
            flex: 1,
            renderCell: params => {
                if (params.row.id !== currentUserID && currentUserID === activeGroupLeaderID) {
                    return (
                        <Button variant="contained" color="primary" onClick={() => handleRemoveUser(params.row)}>Remove</Button>
                    );
                }
                return null;
            }
        },
        { field: "username", headerName: "Username", flex: 1 },
        { field: "firstname", headerName: "First Name", flex: 1 },
        { field: "lastname", headerName: "Last Name", flex: 1 }
    ];

    return (
        <>
            {activeGroupMembers ?
                <>
                    <DataGrid
                        rows={activeGroupMembers}
                        columns={columns}
                        pageSize={10}
                        rowsPerPageOptions={[10]}
                        disableRowSelectionOnClick
                        initialState={{
                            pagination: {
                                paginationModel: {
                                    pageSize: 10
                                }
                            }
                        }}
                    />
                    {removeUserDialogOpen && <RemoveUserDialog open={removeUserDialogOpen} setOpen={setRemoveUserDialogOpen} userToRemove={userToRemove} />}
                </>
                : "Please join a group to see its members"

            }
        </>
    );
};

export default React.memo(UsersList);

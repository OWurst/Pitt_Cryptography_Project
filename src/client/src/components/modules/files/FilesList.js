import React from "react";
import { Box, Button, Typography } from "@mui/material";
import { DataGrid, GridToolbarColumnsButton, GridToolbarContainer, GridToolbarDensitySelector, GridToolbarFilterButton, useGridApiRef } from "@mui/x-data-grid";
import { Delete, DeleteForever, FileDownload, FileUpload } from "@mui/icons-material";
import FileUploadDialog from "./FileUploadDialog";
import { useDispatch, useSelector } from "react-redux";
import { deleteFiles, listFiles } from "../../../store/actions/filesActions";
import { setSnackBar } from "../../../store/slices/layoutSlice";
import { clearError, clearResponse } from "../../../store/slices/filesSlice";
import DeleteAllFilesDialog from "./DeleteAllFilesDialog";
import { downloadFileFromResourceServer } from "../../../utils";

const FilesList = () => {
    const dispatch = useDispatch();
    const activeGroup = useSelector(state => state.groups.activeGroup);
    const activeGroupMembers = useSelector(state => state.groups?.activeGroup?.members);
    const { files, response, sessionKey } = useSelector(state => state.files);
    const groupKey = useSelector(state => state.groups.activeGroup?.groupKey);
    const auth = useSelector(state => state.auth);
    const [selectedRows, setSelectedRows] = React.useState([]);
    const [fileToUpdate, setFileToUpdate] = React.useState(null);
    const [fileUploadDialogOpen, setFileUploadDialogOpen] = React.useState(false);
    const [deleteAllDialogOpen, setDeleteAllDialogOpen] = React.useState(false);
    const gridApiRef = useGridApiRef();

    const updateHandler = React.useCallback(params => {
        const { extension, id, name, type, encryptedFileHashName: encryptedFileName } = params.row;
        setFileToUpdate({ oldExtension: extension, idToUpdate: id, oldName: name, oldType: type, encryptedFileName });
        setFileUploadDialogOpen(true);
    }, []);

    const columns = React.useMemo(() => {
        const dateOptions = { year: "numeric", month: "numeric", day: "numeric", hour: "numeric", minute: "numeric", second: "numeric" };
        return [
            {
                field: "id",
                headerName: "ID",
                width: 20
            },
            {
                field: "actions",
                headerName: "Actions",
                flex: 1,
                renderCell: params => {
                    return (
                        <Button variant="contained" size="small" onClick={() => updateHandler(params)}>
                            Overwrite
                        </Button>
                    );
                }
            },
            {
                field: "name",
                headerName: "Name",
                flex: 1
            },
            {
                field: "extension",
                headerName: "Extension",
                flex: 1
            },
            {
                field: "uploadedDate",
                headerName: "Uploaded Date",
                flex: 1,
                valueGetter: params => params.row.fileModifiedList[0].timeStamp,
                valueFormatter: params => new Date(params.value * 1000).toLocaleString(undefined, dateOptions)
            },
            {
                field: "uploadedBy",
                headerName: "Uploaded By",
                flex: 1,
                valueGetter: params => {
                    const lastModifiedIndex = params.row.fileModifiedList.length - 1;
                    const member = activeGroupMembers?.find(({ id }) => id === params.row.fileModifiedList[lastModifiedIndex].userWhoModified);
                    if (member) {
                        return `${member.firstname} ${member.lastname} (${member.username})`;
                    }
                    return "User no longer in group";
                }
            },
            {
                field: "lastUpdated",
                headerName: "Last Updated",
                flex: 1,
                valueGetter: params => params.row.fileModifiedList[params.row.fileModifiedList.length - 1].timeStamp,
                valueFormatter: params => new Date(params.value * 1000).toLocaleString(undefined, dateOptions)
            },
            {
                field: "lastUpdatedBy",
                headerName: "Last Updated By",
                flex: 1,
                valueGetter: params => {
                    const lastModifiedIndex = params.row.fileModifiedList.length - 1;
                    const member = activeGroupMembers?.find(({ id }) => id === params.row.fileModifiedList[lastModifiedIndex].userWhoModified); // check if this is the right index, it might need to be the last item in the list
                    if (member) {
                        return `${member.firstname} ${member.lastname} (${member.username})`;
                    }
                    return "User no longer in group";
                }
            }
        ];
    }, [activeGroupMembers, updateHandler]);

    React.useEffect(() => {
        if (activeGroup && groupKey) {
            dispatch(listFiles());
        }
    }, [activeGroup, dispatch, groupKey]);

    React.useEffect(() => {
        if (response && response.successes && response.failures) {
            let message = "";
            let severity = "success";
            if (response.successes.length) {
                message = message.concat(`Successfully deleted ${response.successes.length} files`);
            }
            if (response.failures.length) {
                message = message.concat(`\nFailed to delete ${response.failures.length} files`);
                severity = "warning";
                if (!response.successes.length) {
                    severity = "error";
                }
            }
            dispatch(setSnackBar({ message, severity }));
            dispatch(listFiles());
            dispatch(clearResponse());
            dispatch(clearError());
        }
    }, [dispatch, response]);

    const downloadHandler = React.useCallback(async () => {
        const getState = () => ({
            auth,
            files: {
                sessionKey
            },
            groups: {
                activeGroup
            }
        });
        const errors = [];
        for (const row of selectedRows) {
            try {
                const file = files.find(({ id }) => id === row);
                const decryptedFile = await downloadFileFromResourceServer(file, getState, dispatch);
                const blob = new Blob([decryptedFile], { type: file.type });
                const a = document.createElement("a");
                a.href = window.URL.createObjectURL(blob);
                a.download = `${file.name}.${file.extension}`;
                a.click();
            }
            catch (err) {
                const fileThatThrewError = files.find(({ id }) => id === row);
                const { name, extension } = fileThatThrewError;
                errors.push(`${name}.${extension} was unable to be downloaded due to: ${err}`);
            }
        }
        if (errors.length) {
            dispatch(setSnackBar({ message: errors.join("\n"), severity: "error" }));
        }
        setSelectedRows([]);
        gridApiRef.current.setRowSelectionModel([]);
    }, [activeGroup, auth, dispatch, files, gridApiRef, selectedRows, sessionKey]);

    const deleteHandler = React.useCallback(() => {
        dispatch(deleteFiles({ fileIDs: selectedRows }));
    }, [dispatch, selectedRows]);

    const deleteAllHandler = React.useCallback(() => {
        setDeleteAllDialogOpen(true);
    }, []);

    const CustomGridExport = () => (
        <Button
            color="primary"
            size="small"
            startIcon={<FileDownload />}
            onClick={downloadHandler}
        >
            Download Selected Files
        </Button>
    );

    const CustomGridUpload = () => (
        <Button
            color="primary"
            size="small"
            startIcon={<FileUpload />}
            onClick={() => setFileUploadDialogOpen(true)}
        >
            Upload Files
        </Button>
    );

    const CustomGridDelete = () => (
        <Button
            color="primary"
            size="small"
            startIcon={<Delete />}
            onClick={deleteHandler}
        >
            Delete Selected Files
        </Button>
    );

    const CustomGridDeleteAll = () => (
        <Button
            color="primary"
            size="small"
            startIcon={<DeleteForever />}
            onClick={deleteAllHandler}
        >
            Delete All Files
        </Button>
    );

    const CustomGridToolbar = () => {
        return (
            <GridToolbarContainer>
                <GridToolbarColumnsButton />
                <GridToolbarFilterButton />
                <GridToolbarDensitySelector />
                <CustomGridUpload />
                <CustomGridDeleteAll />
                {selectedRows.length > 0 &&
                    <>
                        <CustomGridExport />
                        <CustomGridDelete />
                    </>
                }
            </GridToolbarContainer>
        );
    };

    return (
        activeGroup ? <>
            <Typography variant="h1">{activeGroup?.groupName}&apos;s Files</Typography>
            <Box mt={2}>
                <DataGrid
                    apiRef={gridApiRef}
                    rows={files}
                    columns={columns}
                    checkboxSelection
                    initialState={{
                        pagination: {
                            paginationModel: {
                                pageSize: 10
                            }
                        }
                    }}
                    disableRowSelectionOnClick
                    pageSizeOptions={[5, 10, 20, 50]}
                    onRowSelectionModelChange={ids => setSelectedRows(ids)}
                    slots={{
                        toolbar: CustomGridToolbar
                    }}
                />
            </Box>
            {fileUploadDialogOpen && <FileUploadDialog open={fileUploadDialogOpen} setOpen={setFileUploadDialogOpen} isUpdate={fileToUpdate !== null} {...fileToUpdate} setFileToUpdate={setFileToUpdate} />}
            {deleteAllDialogOpen && <DeleteAllFilesDialog open={deleteAllDialogOpen} setOpen={setDeleteAllDialogOpen} />}
        </> :
            <>
                <Typography variant="h1">Not a member of any groups</Typography>
                <Typography variant="body1">Please join or create a group to begin using this feature.</Typography>
            </>

    );
};

export default React.memo(FilesList);

import React from "react";
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, List, ListItem, Typography, useTheme } from "@mui/material";
import { useDropzone } from "react-dropzone";
import { useDispatch, useSelector } from "react-redux";
import { listFiles, updateFile, uploadFile } from "../../../store/actions/filesActions";
import { setSnackBar } from "../../../store/slices/layoutSlice";
import { clearError, clearResponse, setError } from "../../../store/slices/filesSlice";

const dropzoneStyles = theme => ({
    flex: 1,
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
    padding: theme.spacing(5),
    borderWidth: 2,
    borderRadius: 2,
    borderStyle: "dashed",
    backgroundColor: "#fff",
    outline: "none",
    transition: "border .24s ease-in-out",
    marginBottom: theme.spacing(2)
});

const FileUploadDialog = ({ open, setOpen, isUpdate = false, idToUpdate, oldName, oldType, oldExtension, setFileToUpdate, encryptedFileName }) => {
    const dispatch = useDispatch();
    const accept = {
        "application/json": [".json"],
        "application/msword": [".doc", ".docx"],
        "application/pdf": [".pdf"],
        "application/rtf": [".rtf"],
        "application/vnd.ms-excel": [".xls", ".xlsx"],
        "application/vnd.ms-powerpoint": [".ppt", ".pptx"],
        "application/vnd.openxmlformats-officedocument.presentationml.presentation": [".ppt", ".pptx"],
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": [".xlsx"],
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document": [".doc", ".docx"],
        "application/xml": [".xml"],
        "audio/mpeg": [".mp3"],
        "image/bmp": [".bmp"],
        "image/gif": [".gif"],
        "image/jpeg": [".jpg", ".jpeg"],
        "image/png": [".png"],
        "image/tiff": [".tiff"],
        "image/vnd.microsoft.icon": [".ico"],
        "image/webp": [".webp"],
        "text/csv": [".csv"],
        "text/html": [".html"],
        "text/plain": [".txt"],
        "text/rtf": [".rtf"],
        "video/mp4": [".mp4"],
        "video/mpeg": [".mpeg"]
    };
    const { acceptedFiles, getRootProps, getInputProps } = useDropzone({
        maxFiles: 1,
        maxSize: 104857600, // 100 MB
        accept
    }); // set allowed file types
    const { response, error } = useSelector(state => state.files);
    const theme = useTheme();

    const files = React.useMemo(() => {
        return (
            <List>
                {acceptedFiles.map((file, index) => {
                    return (
                        <ListItem key={`${index}-${file.name}`}>
                            {file.name}
                        </ListItem>

                    );
                })}
            </List>
        );

    }, [acceptedFiles]);

    const handleClose = React.useCallback(() => {
        setOpen(false);
        dispatch(clearResponse());
        dispatch(clearError());
        setFileToUpdate(null);
    }, [dispatch, setFileToUpdate, setOpen]);

    const handleUpload = () => {
        for (const file of acceptedFiles) {
            const { name, type } = file;
            const reader = new FileReader();

            reader.onload = () => {
                const fileBytes = new Uint8Array(reader.result);

                const data = {
                    file: fileBytes,
                    name: name.split(".")[0],
                    type,
                    extension: name.split(".").pop()
                };
                dispatch(uploadFile(data));
            };
            reader.readAsArrayBuffer(file);
        }
    };

    const handleUpdate = React.useCallback(() => {
        dispatch(clearError());
        const [file] = acceptedFiles;
        const { name, type } = file;
        const extension = name.split(".").pop();
        if (extension !== oldExtension) {
            dispatch(setError("You cannot change the file extension"));
            return;
        }
        if (type !== oldType) {
            dispatch(setError("You cannot change the file type"));
            return;
        }

        const reader = new FileReader();
        reader.onload = () => {
            const fileBytes = new Uint8Array(reader.result);
            const data = {
                file: fileBytes,
                newName: name.split(".")[0],
                type,
                extension,
                oldName,
                fileID: idToUpdate,
                encryptedFileName
            };
            dispatch(updateFile(data));
        };
        reader.readAsArrayBuffer(file);

    }, [acceptedFiles, dispatch, encryptedFileName, idToUpdate, oldExtension, oldName, oldType]);

    React.useEffect(() => {
        if (response && response.includes("successfully")) {
            dispatch(setSnackBar({ message: response, severity: "success" }));
            dispatch(listFiles());
            handleClose();
        }
    }, [dispatch, handleClose, response]);

    return (
        <Dialog open={open} onClose={handleClose}>
            <DialogTitle>Upload Files</DialogTitle>
            <DialogContent>
                <section className="container">
                    <Box {...getRootProps({ style: dropzoneStyles(theme) })}>
                        <input {...getInputProps()} />
                        <Typography variant="body1">Drag &apos;n&apos; drop a file here, or click to select a file</Typography>
                    </Box>
                    <Typography variant="body1">A maximum of one file is allowed to be uploaded at a time</Typography>
                    {acceptedFiles.length > 0 &&
                        <Box mt={2}>
                            <Typography variant="h4">File To Upload:</Typography>
                            {files}
                        </Box>
                    }
                </section>
                {error && <Alert severity="error">{error}</Alert>}
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose}>Cancel</Button>
                <Button onClick={!isUpdate ? handleUpload : handleUpdate} disabled={!acceptedFiles.length}>{isUpdate ? "Update" : "Upload"}</Button>
            </DialogActions>
        </Dialog>
    );
};

export default React.memo(FileUploadDialog);

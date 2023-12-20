import React from "react";
import { ChevronLeft, ChevronRight, Delete, Diversity3, Edit, FolderCopy, Groups, Logout } from "@mui/icons-material";
// import { Dashboard, Person } from "@mui/icons-material";
import { alpha, styled, useTheme } from "@mui/material/styles";
import { Alert, Box, Button, Divider, IconButton, List, ListItemButton, ListItemIcon, ListItemText, Drawer as MuiDrawer, Snackbar, Toolbar } from "@mui/material";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import AppBar from "./AppBar";
import { logout } from "../store/actions/authActions";
import { useDispatch, useSelector } from "react-redux";
import { closeSnackBar } from "../store/slices/layoutSlice";
import DeleteUserDialog from "./DeleteUserDialog";
import { clearConfig } from "../store/slices/configSlice";

const DRAWER_WIDTH = 250;

const Drawer = styled(MuiDrawer, { shouldForwardProp: prop => prop !== "open" })(
    ({ theme, open }) => ({
        "& .MuiDrawer-paper": {
            position: "relative",
            whiteSpace: "nowrap",
            width: DRAWER_WIDTH,
            transition: theme.transitions.create("width", {
                easing: theme.transitions.easing.sharp,
                duration: theme.transitions.duration.enteringScreen
            }),
            boxSizing: "border-box",
            ...(!open && {
                overflowX: "hidden",
                transition: theme.transitions.create("width", {
                    easing: theme.transitions.easing.sharp,
                    duration: theme.transitions.duration.leavingScreen
                }),
                width: theme.spacing(7)
            })
        }
    })
);

const ListItems = ({ items }) => {
    const location = useLocation();
    const theme = useTheme();

    const activeColor = alpha(theme.palette.primary.main, 0.7);

    return (
        <Box sx={{ width: "100%" }}>
            {
                items.map(({ label, icon, to, onClick }) => (
                    <ListItemButton
                        key={`lefnav-btn-${label}`}
                        component={to ? NavLink : Button}
                        to={to}
                        onClick={typeof onClick === "function" ? onClick : () => { }}
                        sx={{
                            background: location.pathname === to ? activeColor : "",
                            "&:hover": {
                                background: location.pathname === to ? activeColor : "rgba(0, 0, 0, 0.08)"
                            }
                        }}
                    >
                        <ListItemIcon>{icon}</ListItemIcon>
                        <ListItemText primary={label} />
                    </ListItemButton>
                ))
            }
        </Box>
    );
};

const NavItems = () => {
    const dispatch = useDispatch();
    const [deleteUserOpen, setDeleteUserOpen] = React.useState(false);

    const topOfNavItems = [
        { label: "Files", icon: <FolderCopy />, to: "/files/list" },
        { label: "Current Group Members", icon: <Diversity3 />, to: "/members/list" }
    ];
    const bottomOfNavItems = [
        { label: "Groups", icon: <Groups />, to: "/groups/list" },
        { label: "Logout", icon: <Logout />, onClick: () => dispatch(logout()) },
        { label: "Edit Config", icon: <Edit />, onClick: () => dispatch(clearConfig()) },
        { label: "Delete Account", icon: <Delete />, onClick: () => setDeleteUserOpen(true) }
    ];

    return (
        <List component="nav" sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
            <Box sx={{ flexGrow: 1, display: "flex" }}>
                <ListItems items={topOfNavItems} />
            </Box>
            <Box sx={{ justifyContent: "flex-end", display: "flex", flexDirection: "column" }}>
                <ListItems items={bottomOfNavItems} />
            </Box>
            {deleteUserOpen && <DeleteUserDialog open={deleteUserOpen} setOpen={setDeleteUserOpen} />}
        </List>
    );
};

const LeftNav = () => {
    const dispatch = useDispatch();
    const location = useLocation();
    const navigate = useNavigate();

    const user = useSelector(state => state.auth.user);
    const [open, setOpen] = React.useState(false);
    const { open: snackBarOpen, message: snackBarMessage, severity: snackBarSeverity } = useSelector(state => state.layout.snackBar);
    const { authServer, resourceServer, resourceServerPubKeyPath } = useSelector(state => state.config);

    const handleCloseSnackbar = () => {
        dispatch(closeSnackBar());
    };

    const toggleDrawer = () => {
        setOpen(!open);
    };

    const isLoggedOut = !user.username && !user.firstname && !user.lastname && !user.uid;
    const configMissing = !authServer || !resourceServer || !resourceServerPubKeyPath;

    React.useEffect(() => {
        const canRedirect = location.pathname !== "/" && location.pathname !== "/signUp";
        if (isLoggedOut && canRedirect) navigate("/");
    }, [isLoggedOut, location, navigate]);

    React.useEffect(() => {
        if (configMissing) {
            navigate("/");
        }
    }, [configMissing, navigate]);

    return (
        <>
            <AppBar open={open} setOpen={setOpen} drawerWidth={DRAWER_WIDTH} />
            <Drawer variant="permanent" open={open}>
                <Toolbar
                    sx={{
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "flex-end",
                        px: [1]
                    }}
                >
                    <IconButton onClick={toggleDrawer}>
                        {open ? <ChevronLeft /> : <ChevronRight />}
                    </IconButton>
                </Toolbar>
                <Divider />
                <NavItems />
            </Drawer>
            {snackBarOpen &&
                <Snackbar open={snackBarOpen} autoHideDuration={6000} onClose={handleCloseSnackbar}>
                    <Alert onClose={handleCloseSnackbar} severity={snackBarSeverity} sx={{ width: "100%" }}>
                        {snackBarMessage}
                    </Alert>
                </Snackbar>
            }
        </>
    );
};

export default React.memo(LeftNav);

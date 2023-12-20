import React from "react";
import { styled } from "@mui/material/styles";
import { Box, IconButton, AppBar as MuiAppBar, Toolbar } from "@mui/material";
import { Menu } from "@mui/icons-material";
import GroupSelector from "./GroupSelector";

const StyledAppBar = styled(MuiAppBar, {
    shouldForwardProp: prop => !["open", "drawerWidth"].includes(prop)
})(({ theme, open, drawerWidth }) => ({
    zIndex: theme.zIndex.drawer + 1,
    transition: theme.transitions.create(["width", "margin"], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
    }),
    ...(open && {
        marginLeft: drawerWidth,
        width: `calc(100% - ${drawerWidth + 24}px)`,
        transition: theme.transitions.create(["width", "margin"], {
            easing: theme.transitions.easing.sharp,
            duration: theme.transitions.duration.enteringScreen
        })
    })
}));

const AppBar = ({ open, setOpen, drawerWidth }) => {
    const toggleDrawer = () => {
        setOpen(!open);
    };
    return (
        <StyledAppBar position="absolute" open={open} drawerWidth={drawerWidth}>
            <Toolbar sx={{ pr: "24px" }}>
                <IconButton
                    edge="start"
                    color="inherit"
                    aria-label="open drawer"
                    onClick={toggleDrawer}
                    sx={{
                        marginLeft: "12px",
                        marginRight: "36px",
                        ...(open && { display: "none" })
                    }}
                >
                    <Menu />
                </IconButton>
                <Box sx={{ flexGrow: 1 }} />
                <GroupSelector />
            </Toolbar>
        </StyledAppBar>
    );
};

export default React.memo(AppBar);

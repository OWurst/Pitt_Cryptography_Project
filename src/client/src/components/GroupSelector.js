import React from "react";
import { Edit, KeyboardArrowDown } from "@mui/icons-material";
import { alpha, Avatar, Box, Button, Divider, Menu, MenuItem, styled, Typography, useTheme } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { setActiveGroup } from "../store/actions/groupsActions";

const StyledMenu = styled(props => (
    <Menu
        elevation={0}
        anchorOrigin={{
            vertical: "bottom",
            horizontal: "right"
        }}
        transformOrigin={{
            vertical: "top",
            horizontal: "right"
        }}
        {...props}
    />
))(({ theme }) => ({
    "& .MuiPaper-root": {
        borderRadius: 6,
        marginTop: theme.spacing(1),
        minWidth: 180,
        color:
            theme.palette.mode === "light" ? "rgb(55, 65, 81)" : theme.palette.grey[300],
        boxShadow:
            "rgb(255, 255, 255) 0px 0px 0px 0px, rgba(0, 0, 0, 0.05) 0px 0px 0px 1px, rgba(0, 0, 0, 0.1) 0px 10px 15px -3px, rgba(0, 0, 0, 0.05) 0px 4px 6px -2px",
        "& .MuiMenu-list": {
            padding: "4px 0"
        },
        "& .MuiMenuItem-root": {
            "& .MuiSvgIcon-root": {
                fontSize: 18,
                color: theme.palette.text.secondary,
                marginRight: theme.spacing(1.5)
            },
            "&:active": {
                backgroundColor: alpha(
                    theme.palette.primary.main,
                    theme.palette.action.selectedOpacity
                )
            }
        }
    }
}));

const StyledAvatar = styled(Avatar)(({ theme }) => ({
    backgroundColor: theme.palette.secondary.main,
    height: theme.spacing(3),
    width: theme.spacing(3),
    marginBottom: theme.spacing(1),
    fontSize: theme.typography.fontSize,
    fontWeight: "bold"
}));

const GroupSelector = () => {
    const dispatch = useDispatch();
    const { groups, activeGroup } = useSelector(state => state.groups);
    const theme = useTheme();
    const navigate = useNavigate();
    const [anchorEl, setAnchorEl] = React.useState(null);
    const open = !!anchorEl;

    const handleClick = event => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };
    const updateActiveGroup = group => {
        dispatch(setActiveGroup(group));
        handleClose();
    };

    React.useEffect(() => {
        let isMounted = true;
        if (isMounted && groups?.length > 0) {
            dispatch(setActiveGroup(groups[0]));
        }
        return () => {
            isMounted = false;
        };
    }, [dispatch, groups]);

    return (
        <Box>
            <Button
                id="group-selector-btn"
                aria-controls={open ? "demo-customized-menu" : undefined}
                aria-haspopup="true"
                aria-expanded={open ? "true" : undefined}
                variant="contained"
                disableElevation
                onClick={handleClick}
                endIcon={<KeyboardArrowDown />}
            >
                Current Group: {activeGroup && activeGroup.groupName}
            </Button>
            <StyledMenu
                id="demo-customized-menu"
                MenuListProps={{
                    "aria-labelledby": "group-selector-btn"
                }}
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
            >
                {(groups && !!groups.length) && groups.map(group => (
                    <MenuItem
                        key={`group-selector-group-${group.groupID}`}
                        onClick={() => updateActiveGroup(group)}
                        disableRipple
                        sx={{ background: activeGroup?.groupID === group.groupID ? alpha(theme.palette.primary.main, .7) : "" }}
                    >
                        <Box sx={{ display: "flex", alignItems: "center", justifyContent: "center", mr: 1 }}>
                            <Box sx={{ mr: 1 }}>
                                <StyledAvatar>
                                    {group.groupName.slice(0, 1)}
                                </StyledAvatar>
                            </Box>
                            <Box>
                                <Typography>{group.groupName}</Typography>
                            </Box>
                        </Box>
                    </MenuItem>
                ))}
                <Divider />
                <MenuItem disableRipple onClick={() => {
                    handleClose();
                    navigate("/groups/list");
                }}
                >
                    <Edit />
                    Manage Groups
                </MenuItem>
            </StyledMenu>
        </Box>
    );
};

export default React.memo(GroupSelector);

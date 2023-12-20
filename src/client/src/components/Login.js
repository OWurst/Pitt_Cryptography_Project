import React from "react";
import { Button, Grid, Link, TextField } from "@mui/material";
import { LockOutlined } from "@mui/icons-material";
import Form from "./Form";
import { NavLink, useNavigate } from "react-router-dom";
import { login, performResourceServerAuth } from "../store/actions/authActions";
import { useDispatch, useSelector } from "react-redux";
import { getGroups } from "../store/actions/groupsActions";
import Config from "./Config";
import { clearConfig } from "../store/slices/configSlice";

const Login = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { error, fetching, user } = useSelector(state => state.auth);
    const { authServer, resourceServer, resourceServerPubKeyPath } = useSelector(state => state.config);
    const resourceServerSessionKey = useSelector(state => state.files.sessionKey);

    const configMissing = !authServer || !resourceServer || !resourceServerPubKeyPath;
    const loggedIn = user.username && user.firstname && user.lastname && user.uid && !fetching;

    const commonProps = { required: true, Field: TextField, md: 12 };
    const fields = [
        { label: "Username", autoFocus: true, ...commonProps },
        { label: "Password", type: "password", ...commonProps }
    ];

    const footer = (
        <Grid container justifyContent="flex-end">
            <Grid item xs={8}>
                <Link variant="body2" component={NavLink} to="/signUp">
                    {"Don't have an account? Sign Up"}
                </Link>
            </Grid>
            <Grid item xs={4}>
                <Link variant="body2" component={Button} onClick={() => dispatch(clearConfig())}>
                    Edit Config
                </Link>
            </Grid>
        </Grid>
    );

    const handleLogin = data => {
        dispatch(login(data));
    };

    React.useEffect(() => {
        if (loggedIn && !configMissing) {
            if (!resourceServerSessionKey) {
                dispatch(performResourceServerAuth());
            }
            else {
                navigate("/files/list");
                dispatch(getGroups());
            }
        }
    }, [user, navigate, fetching, dispatch, configMissing, loggedIn, resourceServerSessionKey]);

    return (
        configMissing ? <Config /> :
            <Form
                fields={fields}
                submitHandler={handleLogin}
                title="Sign in"
                icon={<LockOutlined />}
                submitText="Sign In"
                footer={footer}
                error={error}
            />
    );
};
export default React.memo(Login);

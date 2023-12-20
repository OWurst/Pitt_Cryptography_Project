import * as React from "react";
import { Button, Grid, TextField } from "@mui/material";
import { LockOutlined } from "@mui/icons-material";
import { Link, NavLink, useNavigate } from "react-router-dom";
import Form from "./Form";
import { useDispatch, useSelector } from "react-redux";
import { createAccount, performResourceServerAuth } from "../store/actions/authActions";
import Config from "./Config";
import { clearConfig } from "../store/slices/configSlice";

const SignUp = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { error, fetching, user } = useSelector(state => state.auth);
    const resourceServerSessionKey = useSelector(state => state.files.sessionKey);
    const { authServer, resourceServer, resourceServerPubKeyPath } = useSelector(state => state.config);

    const configMissing = !authServer || !resourceServer || !resourceServerPubKeyPath;
    const loggedIn = user.username && user.firstname && user.lastname && user.uid && !fetching;

    const commonProps = { required: true, Field: TextField };
    const fields = [
        { label: "First Name", id: "firstname", autoFocus: true, ...commonProps },
        { label: "Last Name", id: "lastname", ...commonProps },
        { label: "Username", md: 12, ...commonProps },
        { label: "Password", type: "password", md: 12, ...commonProps }
    ];
    const footer = (
        <Grid container justifyContent="flex-end">
            <Grid item xs={8}>
                <Link variant="body2" component={NavLink} to="/">
                    Already have an account? Sign in
                </Link>
            </Grid>
            <Grid item xs={4}>
                <Link variant="body2" component={Button} onClick={() => dispatch(clearConfig())}>
                    Edit Config
                </Link>
            </Grid>
        </Grid>
    );

    const handleSignup = data => {
        dispatch(createAccount(data));
    };

    React.useEffect(() => {
        if (loggedIn && !configMissing) {
            if (!resourceServerSessionKey) {
                dispatch(performResourceServerAuth());
            }
            else {
                navigate("/files/list");
            }
        }
    }, [user, navigate, fetching, configMissing, loggedIn, resourceServerSessionKey, dispatch]);

    return (
        configMissing ? <Config /> : <Form
            fields={fields}
            submitHandler={handleSignup}
            title="Sign Up"
            icon={<LockOutlined />}
            submitText="Sign Up"
            footer={footer}
            error={error}
        />
    );
};

export default React.memo(SignUp);

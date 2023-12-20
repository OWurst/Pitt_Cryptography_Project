import React from "react";
import Form from "./Form";
import { TextField } from "@mui/material";
import { setConfig } from "../store/slices/configSlice";
import { useDispatch } from "react-redux";
import { isPortReachable } from "../utils";
const fs = window.require("fs");
const crypto = window.require("crypto");

const Config = () => {
    const dispatch = useDispatch();
    const [error, setError] = React.useState(null);
    const commonProps = { required: true, Field: TextField, md: 12 };
    const serverFieldsProps = {
        ...commonProps,
        helperText: "Enter http://server:port i.e., http://localhost:8000"
    };
    const fields = [
        { label: "Auth Server", ...serverFieldsProps },
        { label: "Resource Server", ...serverFieldsProps },
        { label: "Absolute Path to the Resource Server Public Key", id: "resourceServerPubKeyPath", helperText: "Enter the absolute path of the resource server's public key that you obtained out-of-band", ...commonProps }
    ];

    const validateUrl = url => {
        const validUrlRegex = /^http:\/\/[A-Za-z0-9\-.]+:\d+\/?$/;
        return validUrlRegex.test(url);
    };

    const submitHandler = async data => {
        const urls = [data.authServer, data.resourceServer];

        if (urls[0] === urls[1]) {
            setError("Auth server and resource server cannot be the same");
            return;
        }

        for (const url of urls) {
            if (!validateUrl(url)) {
                setError("All URLs must be in the following format: http://SOME_IP_OR_DOMAIN:PORT");
                return;
            }
            const [host, port] = url.replace("http://", "").split(":");
            if (!(await isPortReachable(port, { host }))) {
                setError(`${url} is not reachable`);
                return;
            }
        }

        if (!fs.existsSync(data.resourceServerPubKeyPath)) {
            setError("The path to the resource server's public key is invalid");
            return;
        }

        const resourceServerPublicKey = fs.readFileSync(data.resourceServerPubKeyPath, "utf8");
        data.resourceServerID = crypto.createHash("sha256").update(resourceServerPublicKey).digest("hex");

        dispatch(setConfig(data));
    };

    return (
        <Form
            fields={fields}
            submitHandler={submitHandler}
            title="Config"
            submitText="Save"
            error={error}
        />
    );

};

export default React.memo(Config);

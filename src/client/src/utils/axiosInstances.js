import axios from "axios";

export const getInstance = instance => {
    const AUTH_SERVER_URL = window.localStorage.getItem("authServer");
    const FILE_SERVER_URL = window.localStorage.getItem("resourceServer");

    if (instance === "auth") {
        return axios.create({
            baseURL: AUTH_SERVER_URL
        });
    }
    else if (instance === "resource") {
        return axios.create({
            baseURL: FILE_SERVER_URL
        });
    }
    else {
        return null;
    }

};

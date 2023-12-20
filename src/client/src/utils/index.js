import { getInstance } from "./axiosInstances";
const net = window.require("net");

export const convertToCamelCase = str => {
    // Split the string into an array of words
    const words = str.split(" ");

    // Convert the first word to lowercase and concatenate the rest of the words
    const camelCase = words
        .map((word, index) => {
            if (index === 0) {
                return word.toLowerCase();
            }
            else {
                return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
            }
        })
        .join("");

    return camelCase;
};

export const makeRequest = async ({ url, method, body, headers }) => {
    const axiosInstance = getInstance(url.startsWith("/auth") ? "auth" : url.startsWith("/resource") ? "resource" : null);
    url = url.replace("/auth", "").replace("/resource", "");
    if (!axiosInstance) throw new Error("Invalid URL");

    const response = await axiosInstance({
        url,
        method,
        data: body,
        headers
    });
    return response;
};

export const isPortReachable = async (port, { host = "localhost", timeout = 1000 }) => {
    const promise = new Promise((resolve, reject) => {
        const socket = new net.Socket();
        const onError = () => {
            socket.destroy();
            reject(new Error("Port is not reachable"));
        };
        socket.setTimeout(timeout);
        socket.once("error", onError);
        socket.once("timeout", onError);
        socket.connect(port, host, () => {
            socket.end();
            resolve();
        });
    });

    try {
        await promise;
        return true;
    }
    catch (err) {
        return false;
    }
};

export const createTimestamp = () => {
    return Math.floor(Date.now() / 1000);
};

export const verifyResponseTimestamp = timestamp => {
    return Math.abs(createTimestamp() - timestamp) <= 10;
};

export * from "./cryptoUtils";
export * from "./requestUtils";

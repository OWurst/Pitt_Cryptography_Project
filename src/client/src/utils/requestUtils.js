const fs = window.require("fs");
const crypto = window.require("crypto");
import { createHmac, createTimestamp, decryptWithAES, encryptWithAES, encryptWithRSAPubKey, generateAESKey, makeRequest, verifyHmac, verifyResponseTimestamp } from ".";
import { refreshAuthToken } from "../store/actions/authActions";
import { setAuthToken } from "../store/slices/authSlice";

const requestAuthServerPubKey = async () => {
    const response = await makeRequest({ url: "/auth/requestPubKey", method: "GET" });
    let { publicKey } = response.data;
    publicKey = `-----BEGIN PUBLIC KEY-----\n${publicKey}\n-----END PUBLIC KEY-----`;
    return publicKey;
};

const requestResourceServerPubKeyAndID = async uid => {
    const response = await makeRequest({ url: "/resource/requestPublicKeyAndID", method: "POST", body: { userID: uid } });
    return response.data;
};

const verifyResourceServerPublicKeyAndID = async (pathToStoredPublicKey, receievedPublicKey, receievedID) => {
    const storedPublicKey = fs.readFileSync(pathToStoredPublicKey, "utf8");
    const hashedStoredKey = crypto.createHash("sha256").update(storedPublicKey).digest("hex");
    if (hashedStoredKey === receievedPublicKey && hashedStoredKey === receievedID) return true;
    else return false;
};

const shareSessionKeyWithResourceServer = async ({ sessionKey, uid, authToken, resourceServerPubKeyPath }) => {
    const publicKey = fs.readFileSync(resourceServerPubKeyPath, "utf8");
    const encryptedWithSessionKeyPayload = { token: authToken, timestamp: createTimestamp() };
    const { iv, encrypted: encryptedPayload } = encryptWithAES(JSON.stringify(encryptedWithSessionKeyPayload), sessionKey);
    const body = {
        aesPayload: encryptedPayload.toString("base64"),
        sharedKeyPayload: encryptWithRSAPubKey(Buffer.from(sessionKey, "base64"), publicKey),
        iv: iv.toString("base64"),
        userID: uid
    };
    let response;
    try {
        response = await makeRequest({ url: "/resource/shareSessionKey", method: "POST", body });
        return response.data;
    }
    catch (err) {
        const { encryptedData, iv: newIV } = err.response.data;
        err.response.data = decryptWithAES(Buffer.from(encryptedData, "base64"), sessionKey, Buffer.from(newIV, "base64"));
        throw err;
    }

};

const decryptResourceServerTimestampAndNonceChallenge = ({ encryptedData, sessionKey, iv }) => {
    const decodedPayload = Buffer.from(encryptedData, "base64");
    const decodedIV = Buffer.from(iv, "base64");
    const decrypted = decryptWithAES(decodedPayload, sessionKey, decodedIV);
    return decrypted;
};

const sendResourceServerChallengeResponse = async ({ uid, sessionKey, newNonce }) => {
    const encryptedWithSessionKeyPayload = { nonce: newNonce, timestamp: createTimestamp() };
    const { iv, encrypted: aesPayload } = encryptWithAES(JSON.stringify(encryptedWithSessionKeyPayload), sessionKey);
    const body = {
        aesPayload: aesPayload.toString("base64"),
        iv: iv.toString("base64"),
        userID: uid
    };
    let response;
    try {
        response = await makeRequest({ url: "/resource/challengeCheck", method: "POST", body });
    }
    catch (err) {
        const { encryptedData, iv: newIV } = err.response.data;
        err.response.data = decryptWithAES(Buffer.from(encryptedData, "base64"), sessionKey, Buffer.from(newIV, "base64"));
        throw err;
    }
    const { encryptedData, iv: newIV } = response.data;
    const decrypted = decryptWithAES(Buffer.from(encryptedData, "base64"), sessionKey, Buffer.from(newIV, "base64"));
    if (decrypted === "Congrats! you are now authenticated") {
        return true;
    }
    return false;
};

export const performAuthenticationWithAuthServer = async ({ url, data, getState }) => {
    data.rsid = getState().config.resourceServerID;
    const authServerPubKey = await requestAuthServerPubKey();
    data.timestamp = createTimestamp();

    const AESKey = generateAESKey();
    const encryptedAESKeyString = encryptWithRSAPubKey(AESKey, authServerPubKey);

    const { iv, encrypted: encryptedPayloadString } = encryptWithAES(JSON.stringify(data), AESKey);

    const authBody = {
        uid: data.uid,
        iv: iv.toString("base64"),
        encryptedPayloadString: encryptedPayloadString.toString("base64"),
        encryptedAESKeyString: encryptedAESKeyString,
        hmac: createHmac(encryptedPayloadString, Buffer.from(AESKey, "base64"))
    };

    try {
        const response = await makeRequest({ url, method: "POST", body: authBody });
        const { body, iv: newIV } = response.data;
        const decrypted = decryptWithAES(Buffer.from(body, "base64"), AESKey, Buffer.from(newIV, "base64"));
        const decryptedObj = JSON.parse(decrypted);
        return { ...decryptedObj, AESKey };
    }
    catch (err) {
        const { body, iv: newIV } = err.response.data;
        err.response.data = decryptWithAES(Buffer.from(body, "base64"), AESKey, Buffer.from(newIV, "base64"));
        throw err;
    }
};

export const performAuthenticationWithResourceServer = async ({ uid, authToken, resourceServerPubKeyPath }) => {
    const { publicKey: hashedPublicKey, id } = await requestResourceServerPubKeyAndID(uid);
    if (verifyResourceServerPublicKeyAndID(resourceServerPubKeyPath, hashedPublicKey, id)) {
        const sessionKey = generateAESKey();
        const { iv: newIV, encryptedData } = await shareSessionKeyWithResourceServer({ sessionKey, uid, authToken, resourceServerPubKeyPath });

        const decryptedTimeStampAndNonce = decryptResourceServerTimestampAndNonceChallenge({ encryptedData, sessionKey, iv: newIV });
        const { timestamp: decryptedTimeStamp, nonce } = JSON.parse(decryptedTimeStampAndNonce);

        if (!verifyResponseTimestamp(decryptedTimeStamp)) {
            throw new Error("Response timestamp verification failed."); // TODO: handle gracefully
        }

        const newNonce = nonce + 1;

        const authenticated = await sendResourceServerChallengeResponse({ uid, sessionKey, newNonce });
        return { authenticated, sessionKey }; // do something with this if false;
    }
};

const createRequestBodyForAuthServer = (data = {}, getState) => {
    const AESKey = getState().auth.user.AESKey;
    const payload = {
        ...data,
        uid: getState().auth.user.uid,
        authToken: getState().auth.user.authToken,
        timestamp: createTimestamp()
    };
    const { iv, encrypted: encryptedPayloadString } = encryptWithAES(JSON.stringify(payload), AESKey);
    const reqBody = {
        uid: getState().auth.user.uid,
        encryptedPayloadString: encryptedPayloadString.toString("base64"),
        iv: iv.toString("base64"),
        hmac: createHmac(encryptedPayloadString, Buffer.from(AESKey, "base64"))
    };
    return reqBody;
};

const createRequestBodyForResourceServer = (data = {}, getState, dispatch) => {
    const { groupID: teamID, groupName: teamName } = getState().groups.activeGroup;
    const authToken = getState().auth.user.authToken;
    const sessionKey = getState().files.sessionKey;
    dispatch(refreshAuthToken());

    if (!(teamID && teamName && authToken)) {
        throw new Error("Team ID, Team Name, and Auth Token are required to make a request to the resource server.");
    }

    const payload = {
        ...data,
        teamID,
        authToken,
        timestamp: createTimestamp()
    };

    const bodyToEncrypt = JSON.stringify(payload);
    const { iv, encrypted: encryptedPayloadString } = encryptWithAES(bodyToEncrypt, sessionKey);
    const reqBody = {
        userID: getState().auth.user.uid,
        payload: encryptedPayloadString.toString("base64"),
        iv: iv.toString("base64"),
        hmac: createHmac(encryptedPayloadString, Buffer.from(sessionKey, "base64"))
    };
    return reqBody;
};

export const makeRequestAndDecrpytResponseFromAuthServer = async (data, url, method = "POST", getState, dispatch) => {
    let response;
    const AESKey = getState().auth.user.AESKey;
    const reqBody = createRequestBodyForAuthServer(data, getState);
    try {
        response = await makeRequest({ url, method, body: reqBody });
    }
    catch (err) {
        const { body, iv } = err.response.data;
        err.response.data = decryptWithAES(Buffer.from(body, "base64"), AESKey, Buffer.from(iv, "base64"));
        throw err;
    }
    const { body, iv, hmac } = response.data;
    if (!verifyHmac(Buffer.from(body, "base64"), Buffer.from(AESKey, "base64"), hmac)) {
        console.log("hmac verification failed for ", url);
        throw new Error("HMAC verification failed."); // TODO: handle gracefully
    }
    const decrypted = decryptWithAES(Buffer.from(body, "base64"), AESKey, Buffer.from(iv, "base64"));
    const decryptedObj = JSON.parse(decrypted);
    const { authToken, timestamp, ...rest } = decryptedObj;

    if (!verifyResponseTimestamp(timestamp)) {
        throw new Error("Response timestamp verification failed."); // TODO: handle gracefully
    }

    if (authToken) {
        dispatch(setAuthToken(authToken));
    }
    return rest;
};

export const makeRequestAndDecrpytResponseFromResourceServer = async (data, url, method = "POST", getState, dispatch) => {
    const sessionKey = getState().files.sessionKey;
    const reqBody = createRequestBodyForResourceServer(data, getState, dispatch);
    let response;
    try {
        response = await makeRequest({ url, method, body: reqBody });
    }
    catch (err) {
        const { encryptedData, iv } = err.response.data;
        err.response.data = decryptWithAES(Buffer.from(encryptedData, "base64"), sessionKey, Buffer.from(iv, "base64"));
        throw err;
    }
    const { encryptedData, iv, hmac } = response.data;
    if (hmac && !verifyHmac(Buffer.from(encryptedData, "base64"), Buffer.from(sessionKey, "base64"), hmac)) {
        throw new Error("HMAC verification failed.");
    }
    const decryptToString = url !== "/resource/downloadFile";
    const decrypted = decryptWithAES(Buffer.from(encryptedData, "base64"), sessionKey, Buffer.from(iv, "base64"), decryptToString);
    try {
        const decryptedObj = JSON.parse(decrypted);
        if (decryptedObj.timestamp) {
            if (!verifyResponseTimestamp(decryptedObj.timestamp)) {
                throw new Error("Response timestamp verification failed.");
            }
        }
        return decryptedObj;
    }
    catch (err) {
        if (err instanceof SyntaxError) {
            return decrypted;
        }
        throw err;
    }
};

export const downloadFileFromResourceServer = async (file, getState, dispatch) => {
    const activeGroup = getState().groups.activeGroup;
    const groupKey = activeGroup.isGroupKeyOutOfDate ? activeGroup.oldKey : activeGroup.groupKey;
    const { encryptedFileHashName: encryptedFileName } = file;
    const data = { encryptedFileName };
    const response = await makeRequestAndDecrpytResponseFromResourceServer(data, "/resource/downloadFile", undefined, getState, dispatch);
    const { file: downloadedFile } = response;
    const fileBytes = Buffer.from(downloadedFile, "base64");
    const decryptedFile = decryptWithAES(fileBytes, Buffer.from(groupKey, "base64"), Buffer.from(file.encryptedFileIV, "base64"), false);
    return decryptedFile;

};

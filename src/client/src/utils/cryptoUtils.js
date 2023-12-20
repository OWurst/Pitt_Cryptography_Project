const crypto = window.require("crypto");

export const generateAESKey = (bytes = 24) => {
    return crypto.randomBytes(bytes).toString("base64");
};

export const encryptWithAES = (data, key, iv) => {
    if (!iv) {
        iv = crypto.randomBytes(16);
    }
    const cipher = crypto.createCipheriv("aes-192-ctr", Buffer.from(key, "base64"), iv);
    let encrypted = cipher.update(data);
    encrypted = Buffer.concat([encrypted, cipher.final()]);
    return { iv, encrypted };
};

export const decryptWithAES = (data, key, iv, decryptToString = true) => {
    try {
        const decipher = crypto.createDecipheriv("aes-192-ctr", Buffer.from(key, "base64"), iv);
        let decrypted = decipher.update(data);
        decrypted = Buffer.concat([decrypted, decipher.final()]);
        return decryptToString ? decrypted.toString() : decrypted;
    }
    catch (err) {
        return err;

    }
};

export const encryptWithRSAPubKey = (data, publicKey) => {
    try {
        const encrypted = crypto.publicEncrypt({
            key: publicKey,
            padding: crypto.constants.RSA_PKCS1_OAEP_PADDING,
            oaepHash: "sha256"
        }, Buffer.from(data));
        return encrypted.toString("base64");
    }
    catch (err) {
        return err;
    }
};

export const createHmac = (data, key) => {
    return crypto.createHmac("sha256", key).update(data).digest("base64");
};

export const verifyHmac = (data, key, hmac) => {
    return createHmac(data, key) === hmac;
};

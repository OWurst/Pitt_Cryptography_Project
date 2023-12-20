package com.project.Auth.Utils;

public class UtilTester {
    public static void main(String[] args) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

        String token = jwtTokenUtil.generateToken(1,
                "C:/Users/owurs/OneDrive/Desktop/Fall2023/Cyrptogtaphy/cs1653-project-securecoders/src/AuthServer/Auth.pem",
                1, null, null);
        boolean verified = jwtTokenUtil.verifyToken(token, 1,
                "C:/Users/owurs/OneDrive/Desktop/Fall2023/Cyrptogtaphy/cs1653-project-securecoders/src/AuthServer/Auth.pub");
        System.out.println("Token: " + verified);
    }
}

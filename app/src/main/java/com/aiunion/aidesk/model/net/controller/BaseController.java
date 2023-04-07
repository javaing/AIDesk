package com.aiunion.aidesk.model.net.controller;


import com.aiunion.aidesk.model.net.NanoHTTPD;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class BaseController {
    protected final static String AUTHORIZATION_KEY = "authorization";
    protected final static String SECRET = "faceai-android-pad";

    protected Map<String, String> files;

    protected int checkInternal(NanoHTTPD.IHTTPSession session) {
        NanoHTTPD.ContentType ct = new NanoHTTPD.ContentType(session.getHeaders().get("content-type")).tryUTF8();
        session.getHeaders().put("content-type", ct.getContentTypeHeader());
        files = new HashMap<>();
        try {
            session.parseBody(files);
        } catch (IOException | NanoHTTPD.ResponseException e) {
            return 500;
        }
        return 200;
    }

    protected boolean checkAuthorization(String authorization) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest.reset();
        digest.update(SECRET.getBytes(StandardCharsets.UTF_8));
        String hashString = String.format("%064x", new BigInteger(1, digest.digest()));
        return hashString.equals(authorization);
    }
}

package me.lyon.pul.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import java.security.SecureRandom;

public class TokenUtils {
    private TokenUtils() {
    }

    private static final SecureRandom secureRandom = new SecureRandom();
    public static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final char[] symbols = CHARACTERS.toCharArray();
    public static final int SECURE_TOKEN_LENGTH = 16;
    private static final byte[] buf = new byte[SECURE_TOKEN_LENGTH];


    public static String generateNewToken() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = (byte) symbols[secureRandom.nextInt(symbols.length)];
        return StringUtils.stripEnd(Base64.encodeBase64String(buf), "=");
    }
}

package utils;

import org.apache.commons.codec.binary.Hex;

import java.security.SecureRandom;

public class Generator {
    public static String generatePasswordSaltHex() {
        try {
            byte[] salt = new byte[8];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
            return Hex.encodeHexString(salt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

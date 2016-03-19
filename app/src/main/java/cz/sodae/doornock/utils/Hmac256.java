package cz.sodae.doornock.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Hmac256 {


    /**
     * Calculate HMAC-265 by secret and message
     * @param secret key
     * @param message content
     * @return calculated HMAC256 in hex with left zero padding
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    public static String calculate(String secret, String message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return String.format("%064x", new java.math.BigInteger(1,
                sha256_HMAC.doFinal(message.getBytes())
        ));
    }
}

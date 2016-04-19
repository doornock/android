package cz.sodae.doornock.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class SignerAndVerifier {
    static final String ALGORITHM = "SHA256withRSA";

    public static byte[] sign(byte[] data, PrivateKey key)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(key);
        signer.update(data);
        return (signer.sign());
    }

    public static boolean verify(byte[] data, PublicKey key, byte[] sig)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initVerify(key);
        signer.update(data);
        return (signer.verify(sig));

    }

}

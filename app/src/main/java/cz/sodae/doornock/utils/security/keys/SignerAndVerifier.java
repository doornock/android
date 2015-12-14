package cz.sodae.doornock.utils.security.keys;

import java.security.*;

public class SignerAndVerifier
{
    static final String ALGORITHM = "SHA256withRSA"; // SHA1WithRSA

    public static byte[] sign(byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initSign(key);
        signer.update(data);
        return (signer.sign());
    }

    public static boolean verify(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance(ALGORITHM);
        signer.initVerify(key);
        signer.update(data);
        return (signer.verify(sig));

    }

}

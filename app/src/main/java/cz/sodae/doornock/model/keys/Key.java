package cz.sodae.doornock.model.keys;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import cz.sodae.doornock.utils.security.keys.SignerAndVerifier;

public class Key
{
    static final String ALGORITHM = "RSA";

    private Long id;
    private String title;
    private PrivateKey privateKey;
    private PublicKey publicKey;


    public Key(byte[] privateKey, byte[] publicKey) throws InvalidKeySpecException {

        try {
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            this.privateKey = keyFactory.generatePrivate(privateKeySpec);
            this.publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Key(byte[] privateKey, byte[] publicKey, String title) throws InvalidKeySpecException {
        this(privateKey, publicKey);
        this.title = title;
    }


    public Key(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public Key(PrivateKey privateKey, PublicKey publicKey, String title) {
        this(privateKey, publicKey);
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public Key setId(Long id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Key setTitle(String title) {
        this.title = title;
        return this;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public byte[] sign(byte[] data) throws Exception {
        return SignerAndVerifier.sign(data, this.privateKey);
    }

    public boolean verify(byte[] data, byte[] signature) throws Exception {
        return SignerAndVerifier.verify(data, this.publicKey, signature);
    }

    public static Key generateKey(String title) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(2048);
            KeyPair pair = generator.genKeyPair();

            return new Key(pair.getPrivate(), pair.getPublic(), title);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}

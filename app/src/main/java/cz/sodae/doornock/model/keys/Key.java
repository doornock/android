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

import cz.sodae.doornock.utils.SignerAndVerifier;

/**
 * Key entity
 */
public class Key {
    static final String ALGORITHM = "RSA";

    /**
     * Local id
     */
    private Long id;

    /**
     * Name
     */
    private String title;

    /**
     * Private key
     */
    private PrivateKey privateKey;

    /**
     * Public key
     */
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

    /**
     * Sign data SHA-256 with private key
     */
    public byte[] sign(byte[] data) throws Exception {
        return SignerAndVerifier.sign(data, this.privateKey);
    }

    /**
     * Verify data if is signed by this private key and SHA-256
     */
    public boolean verify(byte[] data, byte[] signature) throws Exception {
        return SignerAndVerifier.verify(data, this.publicKey, signature);
    }

    /**
     * Generates object with 2048 bits RSA key
     *
     * @param title human readable name
     * @return entity with generated key
     */
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

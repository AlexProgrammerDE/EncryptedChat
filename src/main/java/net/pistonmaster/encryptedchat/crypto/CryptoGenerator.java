package net.pistonmaster.encryptedchat.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class CryptoGenerator {
    public static KeyPair generateRSAKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

            kpg.initialize(2048);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecretKey generateAESKey() {
        try {
            KeyGenerator kpg = KeyGenerator.getInstance("AES");

            kpg.init(256);
            return kpg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey getPublicKey(PrivateKey privateKey) {
        try {
            RSAPrivateCrtKey rsaPrivateKey = (RSAPrivateCrtKey) privateKey;

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(), rsaPrivateKey.getPublicExponent());

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }
}

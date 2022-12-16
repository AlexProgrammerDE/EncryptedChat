package net.pistonmaster.encryptedchat.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

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
}

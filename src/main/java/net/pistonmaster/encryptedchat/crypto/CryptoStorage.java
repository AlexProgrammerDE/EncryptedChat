package net.pistonmaster.encryptedchat.crypto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoStorage {
    public static PrivateKey loadPrivateKey(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);

            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(ks);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey loadPublicKey(Path path) {
        try {
            return loadPublicKey(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey loadPublicKey(String base64) {
        return loadPublicKey(Base64.getDecoder().decode(base64));
    }

    private static PublicKey loadPublicKey(byte[] bytes) {
        try {
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
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

    public static void saveKey(Key key, Path path) {
        try {
            Files.write(path, key.getEncoded());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String saveKeyToString(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}

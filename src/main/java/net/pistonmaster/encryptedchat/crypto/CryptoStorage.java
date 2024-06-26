package net.pistonmaster.encryptedchat.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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

    public static SecretKey loadAESKey(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);

            return new SecretKeySpec(bytes, "AES");
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

    public static SecretKey loadAESKey(String base64) {
        return loadAESKey(Base64.getDecoder().decode(base64));
    }

    private static SecretKey loadAESKey(byte[] bytes) {
        return new SecretKeySpec(bytes, "AES");
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

    public static X509Certificate loadCertificate(Path path) {
        try {
            byte[] bytes = Files.readAllBytes(path);

            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}

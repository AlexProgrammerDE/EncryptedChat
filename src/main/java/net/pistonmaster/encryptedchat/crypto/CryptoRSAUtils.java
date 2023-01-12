package net.pistonmaster.encryptedchat.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class CryptoRSAUtils {
    public static String encrypt(String message, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String message, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(message)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sign(String message, PrivateKey privateKey) {
        try {
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(message.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(privateSignature.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException |
                 SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(String message, String signature, PublicKey publicKey) {
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(message.getBytes(StandardCharsets.UTF_8));

            return publicSignature.verify(Base64.getDecoder().decode(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException |
                 SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}

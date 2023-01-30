package net.pistonmaster.encryptedchat;

import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.nio.file.Files;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;

import static net.pistonmaster.encryptedchat.EncryptedChat.*;

public class X509Generator {
    public static void main(String[] args) throws Exception {
        var keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair();
        var subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
        var now = Instant.now();
        var validFrom = Date.from(now);
        var validTo = Date.from(now.plusSeconds(60L * 60 * 24 * 365));
        var certBuilder = new X509v3CertificateBuilder(
                new X500Name("CN=My Application,O=My Organisation,L=My City,C=DE"),
                BigInteger.ONE,
                validFrom,
                validTo,
                new X500Name("CN=My Application,O=My Organisation,L=My City,C=DE"),
                subPubKeyInfo
        );
        var signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider(new BouncyCastleProvider())
                .build(keyPair.getPrivate());
        var certificate = certBuilder.build(signer);

        Files.createDirectories(SERVER_PATH);

        try (JcaPEMWriter writer = new JcaPEMWriter(Files.newBufferedWriter(CERT_PATH))) {
            writer.writeObject(certificate);
        }

        CryptoStorage.saveKey(keyPair.getPrivate(), SERVER_PATH.resolve("server.key"));
    }
}

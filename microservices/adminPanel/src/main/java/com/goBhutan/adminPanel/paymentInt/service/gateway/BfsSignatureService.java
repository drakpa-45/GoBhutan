package com.goBhutan.adminPanel.paymentInt.service.gateway;

import com.goBhutan.adminPanel.paymentInt.config.BfsSecureProperties;
import com.goBhutan.adminPanel.paymentInt.entity.PaymentWalletConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class BfsSignatureService {

    private final BfsSecureProperties bfsProperties;
    private final BfsSourceStringBuilder sourceStringBuilder;

    public BfsSignatureService(BfsSecureProperties bfsProperties,
                               BfsSourceStringBuilder sourceStringBuilder) {
        this.bfsProperties = bfsProperties;
        this.sourceStringBuilder = sourceStringBuilder;
    }

    public String signRequest(PaymentWalletConfig config, Map<String, String> fields) {
        try {
            String msgType = fields.get("bfs_msgType");
            String source = sourceStringBuilder.buildRequestSourceString(msgType, fields);

            log.info("BFS SOURCE STRING: {}", source);

            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(loadPrivateKey(config.getPrivateKeyPath()));
            signature.update(source.getBytes(StandardCharsets.UTF_8));

            String hex = toUpperHex(signature.sign());
            log.info("BFS CHECKSUM: {}", hex);

            return hex;

        } catch (Exception ex) {
            throw new RuntimeException("Unable to sign BFS request", ex);
        }
    }

    public String buildRequestSourceString(Map<String, String> fields) {
        String msgType = fields.get("bfs_msgType");
        return sourceStringBuilder.buildRequestSourceString(msgType, fields);
    }

    public boolean verifyResponse(PaymentWalletConfig config, String msgType, Map<String, String> fields) {

        if (!bfsProperties.isVerifyResponseSignature()) {
            return true;
        }

        String checksum = fields.get("bfs_checkSum");
        if (checksum == null || checksum.isBlank()) {
            return false;
        }

        final String source;
        try {
            source = sourceStringBuilder.buildResponseSourceString(msgType, fields);
        } catch (IllegalArgumentException ex) {
            log.warn("Unsupported BFS response msgType for signature verification: {}", msgType);
            return false;
        }

        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(loadPublicKey(config.getBfsPublicCertPath()));
            signature.update(source.getBytes(StandardCharsets.UTF_8));

            return signature.verify(fromHex(checksum));

        } catch (Exception ex) {
            log.warn("BFS response signature verification failed: {}", ex.getMessage());
            return false;
        }
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = Files.readString(Path.of(path), StandardCharsets.UTF_8);

        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(normalized);

        return KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of(path));

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        Certificate cert = factory.generateCertificate(new ByteArrayInputStream(bytes));

        return cert.getPublicKey();
    }

    private String toUpperHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    private byte[] fromHex(String hex) {
        hex = hex.replaceAll("\\s", "");
        byte[] result = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length(); i += 2) {
            result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return result;
    }
}

package com.pranit.todo.security.load;

import com.pranit.todo.exception.InvalidRequestException;
import com.pranit.todo.exception.ResourceNotFoundException;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public class LoadKey {

    public static PrivateKey loadPrivateKey(final String pemPath) {
        validatePemFile(pemPath);
        try {
            final String key = readKey(pemPath)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            final byte[] decoded = Base64.getDecoder().decode(key);
            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ResourceNotFoundException("Failed to load private key from path: " + pemPath);
        }
    }

    private static void validatePemFile(final String path) {
        if (path == null || path.isBlank()) {
            throw new InvalidRequestException("PEM key file path must not be null or blank.");
        }

        if (!path.toLowerCase().endsWith(".pem")) {
            throw new InvalidRequestException("Only .pem key files are supported. Provided path: " + path);
        }
    }

    private static String readKey(final String keyPath) throws IOException {
        if (keyPath.startsWith("classpath:")) {
            return readKeyFromResource(keyPath.substring("classpath:".length()));
        }
        final Path path = Path.of(keyPath);
        if (Files.exists(path)) {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        return readKeyFromResource(keyPath);
    }

    private static String readKeyFromResource(final String resourcePath) throws IOException {
        try (final InputStream is = LoadKey.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new ResourceNotFoundException("Key file not found in classpath or filesystem: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static PublicKey loadPublicKey(final String pemPath) {
        validatePemFile(pemPath);
        try {
            final String key = readKey(pemPath)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            final byte[] decoded = Base64.getDecoder().decode(key);
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ResourceNotFoundException("Failed to load public key from path: " + pemPath);
        }
    }
}

package com.excycle.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class WeChatPaySignature {
    
    public static String generateSignature(String privateKeyPath) throws Exception {
        // Build the message string (equivalent to echo -n -e)
        String message = "GET\n" +
                "/v3/marketing/partnerships?limit=5&offset=10&authorized_data=%7B%22business_type%22%3A%22FAVOR_STOCK%22%2C%20%22stock_id%22%3A%222433405%22%7D&partner=%7B%22type%22%3A%22APPID%22%2C%22appid%22%3A%22wx4e1916a585d1f4e9%22%2C%22merchant_id%22%3A%222480029552%22%7D\n" +
                "1554208460\n" +
                "593BEC0C930BF1AFEB40B4A08C8FB242\n" +
                "\n";
        
        // Load private key from PEM file
        PrivateKey privateKey = loadPrivateKey(privateKeyPath);
        
        // Sign with SHA256withRSA
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        
        // Encode to Base64
        String base64Signature = Base64.getEncoder().encodeToString(signatureBytes);
        
        return base64Signature;
    }
    
    private static PrivateKey loadPrivateKey(String filePath) throws Exception {
        // Read PEM file
        String pemContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        
        // Remove PEM headers and footers
        pemContent = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        
        // Decode Base64
        byte[] keyBytes = Base64.getDecoder().decode(pemContent);
        
        // Generate private key
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
    
    public static void main(String[] args) {
        System.out.println(new File(".").getAbsolutePath());
        try {
            String signature = generateSignature("apiclient_test_key.pem");
            System.out.println(signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
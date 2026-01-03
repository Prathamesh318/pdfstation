package com.app.pdfstation.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfProtectionService {

    public void protectPdf(String inputPath, String outputPath, String userPassword, String ownerPassword, PermissionConfig permissions) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
            AccessPermission ap = new AccessPermission();
            ap.setCanPrint(permissions.allowPrinting);
            ap.setCanExtractContent(permissions.allowCopying);
            ap.setCanModify(permissions.allowModification);
            ap.setCanAssembleDocument(permissions.allowAssembly);

            // If owner password is not provided, use user password + generic suffix
            String effectiveOwnerPwd = (ownerPassword != null && !ownerPassword.isEmpty()) 
                    ? ownerPassword 
                    : (userPassword + "_owner");

            StandardProtectionPolicy policy = new StandardProtectionPolicy(effectiveOwnerPwd, userPassword, ap);
            policy.setEncryptionKeyLength(256); // AES 256
            policy.setPreferAES(true);

            document.protect(policy);
            document.save(outputPath);
        }
    }

    public void removeProtection(String inputPath, String outputPath, String password) throws IOException {
        // Load with password
        try (PDDocument document = Loader.loadPDF(new File(inputPath), password)) {
            if (document.isEncrypted()) {
                document.setAllSecurityToBeRemoved(true);
            }
            document.save(outputPath);
        }
    }

    public static class PermissionConfig {
        boolean allowPrinting;
        boolean allowCopying;
        boolean allowModification;
        boolean allowAssembly;

        public PermissionConfig(boolean allowPrinting, boolean allowCopying, boolean allowModification, boolean allowAssembly) {
            this.allowPrinting = allowPrinting;
            this.allowCopying = allowCopying;
            this.allowModification = allowModification;
            this.allowAssembly = allowAssembly;
        }
    }
}

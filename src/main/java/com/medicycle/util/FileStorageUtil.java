package com.medicycle.util;

import com.medicycle.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class FileStorageUtil {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "application/pdf", "image/jpeg", "image/jpg", "image/png"
    );
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".jpg", ".jpeg", ".png");
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @Value("${medicycle.upload-dir:./uploads}")
    private String uploadDir;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is empty");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("File size exceeds 10MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("Invalid file type. Allowed: PDF, JPG, PNG");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null) throw new BusinessException("Invalid filename");
        String ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("Invalid file extension");
        }
        String storedName = UUID.randomUUID() + ext;
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
            Path target = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {}", storedName);
            return storedName;
        } catch (IOException e) {
            throw new BusinessException("Failed to store file: " + e.getMessage());
        }
    }

    public void delete(String filename) {
        try {
            Path file = Paths.get(uploadDir).toAbsolutePath().resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Could not delete file {}: {}", filename, e.getMessage());
        }
    }
}

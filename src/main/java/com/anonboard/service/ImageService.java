package com.anonboard.service;

import com.anonboard.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {

    private final Cloudinary cloudinary;

    public ImageService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {

        if (cloudName.isEmpty() || apiKey.isEmpty() || apiSecret.isEmpty()) {
            this.cloudinary = null;
        } else {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret));
        }
    }

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        validateImage(file);

        // If Cloudinary is not configured, return a placeholder
        if (cloudinary == null) {
            return "https://via.placeholder.com/800x600?text=Image+Upload+(Configure+Cloudinary)";
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "anonboard",
                    "resource_type", "image",
                    "transformation", ObjectUtils.asMap(
                            "quality", "auto",
                            "fetch_format", "auto")));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    private void validateImage(MultipartFile file) {
        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Image size must not exceed 5MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        // Check allowed formats
        String[] allowedTypes = { "image/jpeg", "image/png", "image/gif", "image/webp" };
        boolean isAllowed = false;
        for (String type : allowedTypes) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new BadRequestException("Allowed image formats: JPEG, PNG, GIF, WebP");
        }
    }
}

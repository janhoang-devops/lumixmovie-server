package com.project.lumix.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;

import com.project.lumix.dto.response.CloudinaryResponse;
import com.project.lumix.exception.CloudinaryException;
import com.project.lumix.util.FileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public class CloudinaryService {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CloudinaryResponse uploadFile(MultipartFile file, String allowPattern) {
        try {
            FileUploadUtil.assertAllowed(file, allowPattern);
            log.info("Uploading file: {}", file.getOriginalFilename());

            String uniqueFileName = FileUploadUtil.getUniqueFileName(file);
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("public_id", uniqueFileName, "resource_type", "auto")
            );

            String publicId = (String) uploadResult.get("public_id");
            String url = (String) uploadResult.get("secure_url");

            log.info("Uploaded to Cloudinary - publicId: {}, url: {}", publicId, url);
            return CloudinaryResponse.builder()
                    .publicId(publicId)
                    .url(url)
                    .build();
        } catch (IOException e) {
            log.error("Upload failed for file: {}", file.getOriginalFilename(), e);
            throw new CloudinaryException("Could not upload file to Cloudinary", e);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Successfully deleted file with publicId: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
            throw new CloudinaryException("Could not delete file from Cloudinary", e);
        }
    }
}


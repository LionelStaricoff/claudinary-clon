package com.openlabmx.claudinary.util;

import com.openlabmx.claudinary.entity.ImageEntity;
import com.openlabmx.claudinary.entity.Project;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.exception.ImageProcessingException;
import com.openlabmx.claudinary.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageProperties storageProperties;
    private final ImageUtils imageUtils;

    public ImageEntity storeImage(MultipartFile file, User user, Project project) throws StorageException, ImageProcessingException {
        // Validate file
        imageUtils.validateFile(file);
        
        // Check storage limit
        checkStorageLimit(user, file.getSize());
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = imageUtils.generateUniqueFilename(originalFilename);
        String extension = imageUtils.getFileExtension(originalFilename);
        String contentType = imageUtils.getContentType(extension);
        String originalFormat = imageUtils.getFormatName(extension);
        
        // Create paths
        Path userProjectPath = imageUtils.getUserProjectPath(user.getId(), project.getId());
        Path webpPath = imageUtils.getUserWebpPath(user.getId(), project.getId());
        Path thumbnailPath = imageUtils.getUserThumbnailPath(user.getId(), project.getId());
        
        // Ensure directories exist
        imageUtils.ensureDirectoryExists(userProjectPath.toString());
        imageUtils.ensureDirectoryExists(webpPath.toString());
        imageUtils.ensureDirectoryExists(thumbnailPath.toString());
        
        // Save original file
        File originalFile = userProjectPath.resolve(uniqueFilename).toFile();
        try {
            file.transferTo(originalFile);
            log.info("Saved original file: {}", originalFile.getAbsolutePath());
        } catch (IOException e) {
            throw new StorageException("Failed to save original file: " + e.getMessage(), e);
        }
        
        // Read image dimensions
        int width = 0;
        int height = 0;
        Long fileSize = originalFile.length();
        
        try {
            BufferedImage image = imageUtils.readImage(originalFile);
            width = image.getWidth();
            height = image.getHeight();
        } catch (ImageProcessingException e) {
            log.warn("Could not read image dimensions for {}: {}", originalFilename, e.getMessage());
        }
        
        // Create WebP version if convertible
        String webpFilename = null;
        String webpPathStr = null;
        Long webpSize = null;
        
        if (imageUtils.isConvertibleToWebP(extension)) {
            try {
                webpFilename = imageUtils.generateWebpFilename(uniqueFilename);
                File webpFile = webpPath.resolve(webpFilename).toFile();
                
                BufferedImage originalImage = imageUtils.readImage(originalFile);
                BufferedImage webpImage = imageUtils.convertToWebP(originalImage);
                
                // Save as WebP
                saveAsWebp(webpImage, webpFile);
                
                webpPathStr = imageUtils.buildWebpRelativePath(user.getId(), project.getId(), webpFilename);
                webpSize = webpFile.length();
                
                log.info("Created WebP version: {}", webpFile.getAbsolutePath());
            } catch (Exception e) {
                log.warn("Failed to create WebP version for {}: {}", originalFilename, e.getMessage());
            }
        }
        
        // Create thumbnail
        String thumbnailFilename = imageUtils.generateThumbnailFilename(uniqueFilename, "200");
        File thumbnailFile = thumbnailPath.resolve(thumbnailFilename).toFile();
        
        try {
            BufferedImage originalImage = imageUtils.readImage(originalFile);
            BufferedImage thumbnail = imageUtils.resizeImageMaintainingAspectRatio(originalImage, 200, 200);
            imageUtils.saveImage(thumbnail, "PNG", thumbnailFile);
            log.info("Created thumbnail: {}", thumbnailFile.getAbsolutePath());
        } catch (Exception e) {
            log.warn("Failed to create thumbnail for {}: {}", originalFilename, e.getMessage());
        }
        
        // Build relative paths
        String relativePath = imageUtils.buildRelativePath(user.getId(), project.getId(), uniqueFilename);
        String webpRelativePath = webpPathStr;
        String thumbnailRelativePath = imageUtils.buildThumbnailRelativePath(user.getId(), project.getId(), thumbnailFilename);
        
        // Build URLs
        String url = imageUtils.getPublicUrl(relativePath);
        String webpUrl = webpRelativePath != null ? imageUtils.getWebpPublicUrl(webpRelativePath) : null;
        String thumbnailUrl = imageUtils.getThumbnailPublicUrl(thumbnailRelativePath);
        
        // Create ImageEntity
        return ImageEntity.builder()
            .filename(uniqueFilename)
            .originalFilename(originalFilename)
            .filePath(originalFile.getAbsolutePath())
            .webpPath(webpPathStr)
            .url(url)
            .webpUrl(webpUrl)
            .fileSize(fileSize)
            .webpSize(webpSize)
            .width(width)
            .height(height)
            .contentType(contentType)
            .originalFormat(originalFormat)
            .isPublic(project.getIsPublic())
            .user(user)
            .project(project)
            .build();
    }

    private void saveAsWebp(BufferedImage image, File outputFile) throws ImageProcessingException {
        try {
            // Use webp-imageio library to save as WebP
            // First, check if we can use the native WebP writer
            boolean saved = ImageIO.write(image, "webp", outputFile);
            
            if (!saved) {
                // Fallback: save as PNG and rename (for basic functionality)
                File tempPngFile = new File(outputFile.getAbsolutePath() + ".png");
                ImageIO.write(image, "PNG", tempPngFile);
                Files.move(tempPngFile.toPath(), outputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log.warn("WebP format not available, saved as PNG but with .webp extension");
            }
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to save as WebP: " + e.getMessage(), e);
        }
    }

    public void deleteImageFiles(ImageEntity image) throws StorageException {
        try {
            // Delete original file
            if (image.getFilePath() != null) {
                File originalFile = new File(image.getFilePath());
                if (originalFile.exists()) {
                    Files.delete(originalFile.toPath());
                    log.info("Deleted original file: {}", image.getFilePath());
                }
            }
            
            // Delete WebP file
            if (image.getWebpPath() != null) {
                File webpFile = new File(image.getWebpPath());
                if (webpFile.exists()) {
                    Files.delete(webpFile.toPath());
                    log.info("Deleted WebP file: {}", image.getWebpPath());
                }
            }
            
            // Delete thumbnail (construct path from filename)
            if (image.getFilePath() != null) {
                Path filePath = Paths.get(image.getFilePath());
                Path parentPath = filePath.getParent();
                Path thumbnailPath = parentPath.getParent().resolve("thumbnails");
                String thumbnailName = imageUtils.generateThumbnailFilename(filePath.getFileName().toString(), "200");
                File thumbnailFile = thumbnailPath.resolve(thumbnailName).toFile();
                
                if (thumbnailFile.exists()) {
                    Files.delete(thumbnailFile.toPath());
                    log.info("Deleted thumbnail file: {}", thumbnailFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            throw new StorageException("Failed to delete image files: " + e.getMessage(), e);
        }
    }

    public void checkStorageLimit(User user, long fileSize) throws StorageException {
        long maxStorage = storageProperties.getMaxStoragePerUser();
        long currentStorage = user.getStorageUsed() != null ? user.getStorageUsed() : 0L;
        
        if (currentStorage + fileSize > maxStorage) {
            throw new StorageException(
                "Storage limit exceeded. Current: " + formatBytes(currentStorage) + 
                ", File: " + formatBytes(fileSize) + 
                ", Limit: " + formatBytes(maxStorage)
            );
        }
    }

    public void updateUserStorage(User user, long fileSize) {
        long currentStorage = user.getStorageUsed() != null ? user.getStorageUsed() : 0L;
        user.setStorageUsed(currentStorage + fileSize);
    }

    public void freeUserStorage(User user, long fileSize) {
        long currentStorage = user.getStorageUsed() != null ? user.getStorageUsed() : 0L;
        user.setStorageUsed(Math.max(0L, currentStorage - fileSize));
    }

    public String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTP".charAt(exp - 1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), pre);
    }

    public void cleanupEmptyDirectories(UUID userId) {
        try {
            Path userPath = imageUtils.getUserUploadPath(userId);
            if (Files.exists(userPath)) {
                cleanupDirectory(userPath);
            }
        } catch (IOException e) {
            log.error("Failed to cleanup empty directories for user {}: {}", userId, e.getMessage());
        }
    }

    private void cleanupDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) return;
        
        boolean hasFiles = false;
        for (Path subPath : Files.list(directory).toList()) {
            if (Files.isDirectory(subPath)) {
                cleanupDirectory(subPath);
            } else {
                hasFiles = true;
            }
        }
        
        // If directory is empty, delete it
        if (!hasFiles && Files.list(directory).count() == 0) {
            Files.delete(directory);
            log.info("Cleaned up empty directory: {}", directory);
        }
    }

    public void initStorageDirectories() {
        try {
            imageUtils.ensureDirectoryExists(storageProperties.getUploadDir());
            imageUtils.ensureDirectoryExists(storageProperties.getWebpDirectory());
            imageUtils.ensureDirectoryExists(storageProperties.getThumbnailDir());
            log.info("Storage directories initialized");
        } catch (StorageException e) {
            log.error("Failed to initialize storage directories: {}", e.getMessage());
        }
    }
}

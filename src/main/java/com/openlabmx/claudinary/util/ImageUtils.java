package com.openlabmx.claudinary.util;

import com.openlabmx.claudinary.exception.ImageProcessingException;
import com.openlabmx.claudinary.exception.StorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUtils {

    private static final Set<String> SUPPORTED_FORMATS = new HashSet<>(Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", 
        "heic", "heif", "svg", "ico", "psd", "pdf", "webp"
    ));
    
    private static final Set<String> WEBP_CONVERTIBLE_FORMATS = new HashSet<>(Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "heic", "heif", "ico", "psd", "pdf"
    ));
    
    private final StorageProperties storageProperties;

    public boolean isSupportedFormat(String extension) {
        return SUPPORTED_FORMATS.contains(extension.toLowerCase());
    }

    public boolean isConvertibleToWebP(String extension) {
        return WEBP_CONVERTIBLE_FORMATS.contains(extension.toLowerCase());
    }

    public String getFileExtension(String filename) {
        return FilenameUtils.getExtension(filename).toLowerCase();
    }

    public String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String baseName = FilenameUtils.getBaseName(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return baseName + "_" + uuid + "." + extension;
    }

    public String generateWebpFilename(String originalFilename) {
        String baseName = FilenameUtils.getBaseName(originalFilename);
        return baseName + ".webp";
    }

    public String generateThumbnailFilename(String originalFilename) {
        String baseName = FilenameUtils.getBaseName(originalFilename);
        return baseName + "_thumb.webp";
    }

    public String generateThumbnailFilename(String filename, String size) {
        String baseName = FilenameUtils.getBaseName(filename);
        return baseName + "_thumb_" + size + ".webp";
    }

    public String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "tiff", "tif" -> "image/tiff";
            case "heic" -> "image/heic";
            case "heif" -> "image/heif";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            case "psd" -> "image/vnd.adobe.photoshop";
            case "pdf" -> "application/pdf";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    public String getFormatName(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg" -> "JPEG";
            case "jpeg" -> "JPEG";
            case "png" -> "PNG";
            case "gif" -> "GIF";
            case "bmp" -> "BMP";
            case "tiff", "tif" -> "TIFF";
            case "heic" -> "HEIC";
            case "heif" -> "HEIF";
            case "svg" -> "SVG";
            case "ico" -> "ICO";
            case "psd" -> "PSD";
            case "pdf" -> "PDF";
            case "webp" -> "WEBP";
            default -> extension.toUpperCase();
        };
    }

    public void validateFile(MultipartFile file) throws StorageException {
        if (file == null || file.isEmpty()) {
            throw new StorageException("File is empty or null");
        }
        
        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new StorageException("File size exceeds maximum limit of " + 
                (storageProperties.getMaxFileSize() / (1024 * 1024)) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new StorageException("File name is missing");
        }
        
        String extension = getFileExtension(originalFilename);
        if (!isSupportedFormat(extension)) {
            throw new StorageException("Unsupported file format: " + extension + 
                ". Supported formats: " + SUPPORTED_FORMATS);
        }
    }

    public BufferedImage readImage(MultipartFile file) throws ImageProcessingException {
        try {
            return ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image: " + e.getMessage(), e);
        }
    }

    public BufferedImage readImage(File file) throws ImageProcessingException {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image: " + e.getMessage(), e);
        }
    }

    public BufferedImage readImage(InputStream inputStream) throws ImageProcessingException {
        try {
            return ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to read image: " + e.getMessage(), e);
        }
    }

    public void saveImage(BufferedImage image, String formatName, File outputFile) 
            throws ImageProcessingException {
        try {
            boolean success = ImageIO.write(image, formatName, outputFile);
            if (!success) {
                throw new ImageProcessingException("Failed to save image as " + formatName);
            }
        } catch (IOException e) {
            throw new ImageProcessingException("Failed to save image: " + e.getMessage(), e);
        }
    }

    public BufferedImage convertToWebP(BufferedImage image) throws ImageProcessingException {
        // Create a new BufferedImage with the same dimensions
        BufferedImage webpImage = new BufferedImage(
            image.getWidth(), 
            image.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g = webpImage.createGraphics();
        try {
            g.drawImage(image, 0, 0, null);
            return webpImage;
        } finally {
            g.dispose();
        }
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) 
            throws ImageProcessingException {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = outputImage.createGraphics();
        try {
            g.drawImage(resultingImage, 0, 0, null);
            return outputImage;
        } finally {
            g.dispose();
        }
    }

    public BufferedImage resizeImageMaintainingAspectRatio(BufferedImage originalImage, int maxWidth, int maxHeight) 
            throws ImageProcessingException {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        double aspectRatio = (double) originalWidth / originalHeight;
        
        int newWidth, newHeight;
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            if (originalWidth > originalHeight) {
                newWidth = Math.min(maxWidth, originalWidth);
                newHeight = (int) (newWidth / aspectRatio);
            } else {
                newHeight = Math.min(maxHeight, originalHeight);
                newWidth = (int) (newHeight * aspectRatio);
            }
        } else {
            newWidth = originalWidth;
            newHeight = originalHeight;
        }
        
        return resizeImage(originalImage, newWidth, newHeight);
    }

    public boolean convertHeicToPng(File heicFile, File pngFile) throws ImageProcessingException {
        try {
            // Try using ImageIO with HEIC support
            BufferedImage image = ImageIO.read(heicFile);
            if (image != null) {
                return ImageIO.write(image, "PNG", pngFile);
            }
            return false;
        } catch (Exception e) {
            log.warn("Failed to convert HEIC to PNG using ImageIO: {}", e.getMessage());
            return false;
        }
    }

    public boolean convertPdfToPng(File pdfFile, File pngFile, int pageIndex, int dpi) throws ImageProcessingException {
        try {
            // This is a simplified approach - in production, use PDFBox or similar
            // For now, we'll just throw an exception as this needs proper PDF rendering
            throw new ImageProcessingException("PDF to PNG conversion not yet implemented");
        } catch (Exception e) {
            throw new ImageProcessingException("Failed to convert PDF to PNG: " + e.getMessage(), e);
        }
    }

    public void ensureDirectoryExists(String directoryPath) throws StorageException {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Created directory: {}", directoryPath);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to create directory: " + directoryPath + ": " + e.getMessage(), e);
        }
    }

    public Path getUserUploadPath(UUID userId) {
        Path basePath = Paths.get(storageProperties.getUploadDir());
        return basePath.resolve(userId.toString());
    }

    public Path getUserProjectPath(UUID userId, UUID projectId) {
        return getUserUploadPath(userId).resolve(projectId.toString());
    }

    public Path getUserWebpPath(UUID userId, UUID projectId) {
        Path userProjectPath = getUserProjectPath(userId, projectId);
        return userProjectPath.resolve("webp");
    }

    public Path getUserThumbnailPath(UUID userId, UUID projectId) {
        Path userProjectPath = getUserProjectPath(userId, projectId);
        return userProjectPath.resolve("thumbnails");
    }

    public String getPublicUrl(String relativePath) {
        return storageProperties.getBaseUrl() + "/images/" + relativePath;
    }

    public String getWebpPublicUrl(String relativePath) {
        return storageProperties.getBaseUrl() + "/images/webp/" + relativePath;
    }

    public String getThumbnailPublicUrl(String relativePath) {
        return storageProperties.getBaseUrl() + "/images/thumbnails/" + relativePath;
    }

    public long getFileSize(File file) {
        return file.length();
    }

    public long getFileSize(MultipartFile file) {
        return file.getSize();
    }

    public String buildRelativePath(UUID userId, UUID projectId, String filename) {
        return userId.toString() + "/" + projectId.toString() + "/" + filename;
    }

    public String buildWebpRelativePath(UUID userId, UUID projectId, String filename) {
        return userId.toString() + "/" + projectId.toString() + "/webp/" + filename;
    }

    public String buildThumbnailRelativePath(UUID userId, UUID projectId, String filename) {
        return userId.toString() + "/" + projectId.toString() + "/thumbnails/" + filename;
    }
}

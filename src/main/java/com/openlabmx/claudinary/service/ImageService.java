package com.openlabmx.claudinary.service;

import com.openlabmx.claudinary.dto.request.ImageUploadRequest;
import com.openlabmx.claudinary.dto.response.ImageResponse;
import com.openlabmx.claudinary.entity.ImageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ImageService {
    
    ImageResponse uploadImage(ImageUploadRequest request) throws IOException;
    ImageResponse uploadImage(MultipartFile file, UUID projectId) throws IOException;
    ImageResponse getImageById(UUID imageId);
    ImageResponse getImageByFilename(String filename);
    Page<ImageResponse> getImagesByUserId(UUID userId, Pageable pageable);
    Page<ImageResponse> getImagesByProjectId(UUID projectId, Pageable pageable);
    List<ImageResponse> getImagesByUserIdAndProjectId(UUID userId, UUID projectId);
    
    ImageResponse updateImage(UUID imageId, ImageUploadRequest request) throws IOException;
    void deleteImage(UUID imageId);
    void deleteImageByUser(UUID userId, UUID imageId);
    
    ImageEntity getImageEntityById(UUID imageId);
    
    List<ImageResponse> searchImagesByUserId(UUID userId, String query);
    List<ImageResponse> searchImagesByProjectId(UUID projectId, String query);
    
    Page<ImageResponse> getImagesByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    List<ImageResponse> getImagesByFormat(UUID userId, String format);
    
    Integer countImagesByUserId(UUID userId);
    Long getTotalStorageUsedByUserId(UUID userId);
    
    void incrementViewCount(UUID imageId);
    void incrementDownloadCount(UUID imageId);
    
    List<ImageResponse> getRecentImagesByUserId(UUID userId, int limit);
    List<ImageResponse> getPopularImagesByUserId(UUID userId, int limit);
    
    byte[] getImageBytes(UUID imageId) throws IOException;
    byte[] getWebpImageBytes(UUID imageId) throws IOException;
    byte[] getThumbnailBytes(UUID imageId) throws IOException;
    
    List<ImageResponse> getPublicImages();
    List<ImageResponse> getPublicImagesByUserId(UUID userId);
}

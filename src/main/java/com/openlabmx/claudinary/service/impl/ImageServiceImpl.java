package com.openlabmx.claudinary.service.impl;

import com.openlabmx.claudinary.dto.request.ImageUploadRequest;
import com.openlabmx.claudinary.dto.response.ImageResponse;
import com.openlabmx.claudinary.entity.ImageEntity;
import com.openlabmx.claudinary.entity.Project;
import com.openlabmx.claudinary.entity.User;
import com.openlabmx.claudinary.exception.ResourceNotFoundException;
import com.openlabmx.claudinary.repository.ImageRepository;
import com.openlabmx.claudinary.service.ImageService;
import com.openlabmx.claudinary.service.ProjectService;
import com.openlabmx.claudinary.service.UserService;
import com.openlabmx.claudinary.util.FileStorageService;
import com.openlabmx.claudinary.util.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserService userService;
    private final ProjectService projectService;
    private final FileStorageService fileStorageService;
    private final ImageUtils imageUtils;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public ImageResponse uploadImage(ImageUploadRequest request) throws IOException {
        User user = userService.getCurrentUserEntity();
        Project project;
        
        if (request.getProjectId() != null) {
            project = projectService.getProjectEntityById(request.getProjectId());
            // Verify project belongs to user
            if (!project.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Project not found or does not belong to you");
            }
        } else {
            // Use default project
            project = projectService.getDefaultProjectForUser(user.getId());
        }
        
        request.validateFile();
        
        ImageEntity imageEntity = fileStorageService.storeImage(
            request.getFile(), user, project
        );
        
        imageEntity = imageRepository.save(imageEntity);
        
        // Update user storage
        fileStorageService.updateUserStorage(user, imageEntity.getFileSize());
        
        // Update project image count
        project.setImageCount(project.getImageCount() + 1);
        
        ImageResponse response = modelMapper.map(imageEntity, ImageResponse.class);
        
        // Enrich response with additional data
        if (project != null) {
            response.setProjectId(project.getId());
            response.setProjectName(project.getName());
            response.setProjectIsDefault(project.getIsDefault());
        }
        
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setHasWebpVersion(imageEntity.getWebpUrl() != null);
        response.setFullUrl(imageEntity.getUrl());
        response.setFullWebpUrl(imageEntity.getWebpUrl());
        response.setThumbnailUrl(imageUtils.getThumbnailPublicUrl(
            imageUtils.buildThumbnailRelativePath(user.getId(), project.getId(), 
                imageUtils.generateThumbnailFilename(imageEntity.getFilename(), "200"))
        ));
        
        log.info("Uploaded image: {} by user: {}", imageEntity.getOriginalFilename(), user.getUsername());
        
        return response;
    }

    @Override
    @Transactional
    public ImageResponse uploadImage(MultipartFile file, UUID projectId) throws IOException {
        ImageUploadRequest request = ImageUploadRequest.builder()
            .file(file)
            .originalFilename(file.getOriginalFilename())
            .projectId(projectId)
            .build();
        
        return uploadImage(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageResponse getImageById(UUID imageId) {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        ImageResponse response = modelMapper.map(image, ImageResponse.class);
        
        if (image.getProject() != null) {
            response.setProjectId(image.getProject().getId());
            response.setProjectName(image.getProject().getName());
            response.setProjectIsDefault(image.getProject().getIsDefault());
        }
        
        response.setUserId(image.getUser().getId());
        response.setUsername(image.getUser().getUsername());
        response.setHasWebpVersion(image.getWebpUrl() != null);
        response.setFullUrl(image.getUrl());
        response.setFullWebpUrl(image.getWebpUrl());
        
        if (image.getFilePath() != null) {
            String thumbnailFilename = imageUtils.generateThumbnailFilename(image.getFilename(), "200");
            response.setThumbnailUrl(imageUtils.getThumbnailPublicUrl(
                imageUtils.buildThumbnailRelativePath(
                    image.getUser().getId(), 
                    image.getProject().getId(), 
                    thumbnailFilename
                )
            ));
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ImageResponse getImageByFilename(String filename) {
        ImageEntity image = imageRepository.findByFilename(filename)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with filename: " + filename));
        
        return getImageById(image.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImageResponse> getImagesByUserId(UUID userId, Pageable pageable) {
        Page<ImageEntity> images = imageRepository.findByUserId(userId, pageable);
        return images.map(this::mapToImageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImageResponse> getImagesByProjectId(UUID projectId, Pageable pageable) {
        Page<ImageEntity> images = imageRepository.findByProjectId(projectId, pageable);
        return images.map(this::mapToImageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getImagesByUserIdAndProjectId(UUID userId, UUID projectId) {
        return imageRepository.findByUserIdAndProjectId(userId, projectId).stream()
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ImageResponse updateImage(UUID imageId, ImageUploadRequest request) throws IOException {
        ImageEntity existingImage = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        User user = userService.getCurrentUserEntity();
        
        // Verify image belongs to user
        if (!existingImage.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Image not found or does not belong to you");
        }
        
        Project project;
        if (request.getProjectId() != null) {
            project = projectService.getProjectEntityById(request.getProjectId());
            // Verify project belongs to user
            if (!project.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Project not found or does not belong to you");
            }
        } else {
            project = existingImage.getProject();
        }
        
        // Delete old files
        fileStorageService.deleteImageFiles(existingImage);
        
        // Store new image
        ImageEntity newImage = fileStorageService.storeImage(
            request.getFile(), user, project
        );
        
        // Update existing entity with new data
        existingImage.setFilename(newImage.getFilename());
        existingImage.setOriginalFilename(newImage.getOriginalFilename());
        existingImage.setFilePath(newImage.getFilePath());
        existingImage.setWebpPath(newImage.getWebpPath());
        existingImage.setUrl(newImage.getUrl());
        existingImage.setWebpUrl(newImage.getWebpUrl());
        existingImage.setFileSize(newImage.getFileSize());
        existingImage.setWebpSize(newImage.getWebpSize());
        existingImage.setWidth(newImage.getWidth());
        existingImage.setHeight(newImage.getHeight());
        existingImage.setContentType(newImage.getContentType());
        existingImage.setOriginalFormat(newImage.getOriginalFormat());
        existingImage.setProject(project);
        
        existingImage = imageRepository.save(existingImage);
        
        return modelMapper.map(existingImage, ImageResponse.class);
    }

    @Override
    @Transactional
    public void deleteImage(UUID imageId) {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        User user = image.getUser();
        
        // Delete files from storage
        fileStorageService.deleteImageFiles(image);
        
        // Update user storage
        fileStorageService.freeUserStorage(user, image.getFileSize());
        
        // Update project image count
        Project project = image.getProject();
        if (project != null) {
            project.setImageCount(Math.max(0, project.getImageCount() - 1));
        }
        
        imageRepository.delete(image);
        
        log.info("Deleted image: {} by user: {}", image.getOriginalFilename(), user.getUsername());
    }

    @Override
    @Transactional
    public void deleteImageByUser(UUID userId, UUID imageId) {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        if (!image.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Image not found or does not belong to this user");
        }
        
        deleteImage(imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageEntity getImageEntityById(UUID imageId) {
        return imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> searchImagesByUserId(UUID userId, String query) {
        if (query == null || query.isBlank()) {
            return imageRepository.findByUserId(userId).stream()
                .map(this::mapToImageResponse)
                .collect(Collectors.toList());
        }
        
        return imageRepository.searchByUserIdAndQuery(userId, query).stream()
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> searchImagesByProjectId(UUID projectId, String query) {
        if (query == null || query.isBlank()) {
            return imageRepository.findByProjectId(projectId).stream()
                .map(this::mapToImageResponse)
                .collect(Collectors.toList());
        }
        
        return imageRepository.searchByProjectIdAndQuery(projectId, query).stream()
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImageResponse> getImagesByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        List<ImageEntity> images = imageRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        // Note: This is simplified - for proper pagination, we'd need to use a custom query
        return mapImageListToPage(images, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getImagesByFormat(UUID userId, String format) {
        return imageRepository.findByUserIdAndOriginalFormat(userId, format).stream()
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer countImagesByUserId(UUID userId) {
        return imageRepository.countByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalStorageUsedByUserId(UUID userId) {
        return imageRepository.sumFileSizeByUserId(userId);
    }

    @Override
    @Transactional
    public void incrementViewCount(UUID imageId) {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        image.incrementViewCount();
        imageRepository.save(image);
    }

    @Override
    @Transactional
    public void incrementDownloadCount(UUID imageId) {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        image.incrementDownloadCount();
        imageRepository.save(image);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getRecentImagesByUserId(UUID userId, int limit) {
        return imageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream()
            .limit(limit)
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getPopularImagesByUserId(UUID userId, int limit) {
        return imageRepository.findTop10ByUserIdOrderByViewCountDesc(userId).stream()
            .limit(limit)
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageBytes(UUID imageId) throws IOException {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        Path path = Paths.get(image.getFilePath());
        return Files.readAllBytes(path);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getWebpImageBytes(UUID imageId) throws IOException {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        if (image.getWebpPath() == null) {
            throw new ResourceNotFoundException("WebP version not available for image: " + imageId);
        }
        
        Path path = Paths.get(image.getWebpPath());
        return Files.readAllBytes(path);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getThumbnailBytes(UUID imageId) throws IOException {
        ImageEntity image = imageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));
        
        String thumbnailFilename = imageUtils.generateThumbnailFilename(image.getFilename(), "200");
        Path thumbnailPath = imageUtils.getUserThumbnailPath(image.getUser().getId(), image.getProject().getId())
            .resolve(thumbnailFilename);
        
        return Files.readAllBytes(thumbnailPath);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getPublicImages() {
        return imageRepository.findByIsPublicTrue().stream()
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImageResponse> getPublicImagesByUserId(UUID userId) {
        return imageRepository.findByUserIdAndIsPublicTrue(userId).stream()
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
    }

    // Helper method to map ImageEntity to ImageResponse with enrichment
    private ImageResponse mapToImageResponse(ImageEntity image) {
        ImageResponse response = modelMapper.map(image, ImageResponse.class);
        
        if (image.getProject() != null) {
            response.setProjectId(image.getProject().getId());
            response.setProjectName(image.getProject().getName());
            response.setProjectIsDefault(image.getProject().getIsDefault());
        }
        
        response.setUserId(image.getUser().getId());
        response.setUsername(image.getUser().getUsername());
        response.setHasWebpVersion(image.getWebpUrl() != null);
        response.setFullUrl(image.getUrl());
        response.setFullWebpUrl(image.getWebpUrl());
        
        if (image.getFilePath() != null) {
            String thumbnailFilename = imageUtils.generateThumbnailFilename(image.getFilename(), "200");
            response.setThumbnailUrl(imageUtils.getThumbnailPublicUrl(
                imageUtils.buildThumbnailRelativePath(
                    image.getUser().getId(), 
                    image.getProject() != null ? image.getProject().getId() : UUID.fromString("00000000-0000-0000-0000-000000000000"), 
                    thumbnailFilename
                )
            ));
        }
        
        return response;
    }

    private Page<ImageResponse> mapImageListToPage(List<ImageEntity> images, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), images.size());
        
        List<ImageResponse> responses = images.subList(start, end).stream()
            .map(this::mapToImageResponse)
            .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(
            responses, 
            pageable, 
            images.size()
        );
    }
}

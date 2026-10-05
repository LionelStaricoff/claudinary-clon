package com.openlabmx.claudinary.controller;

import com.openlabmx.claudinary.dto.request.ImageUploadRequest;
import com.openlabmx.claudinary.dto.response.ImageResponse;
import com.openlabmx.claudinary.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image management API endpoints")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Upload image", description = "Upload a new image")
    public ResponseEntity<ImageResponse> uploadImage(
            @ModelAttribute ImageUploadRequest request) throws IOException {
        ImageResponse response = imageService.uploadImage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/upload/project/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Upload image to project", description = "Upload a new image to a specific project")
    public ResponseEntity<ImageResponse> uploadImageToProject(
            @PathVariable UUID projectId,
            @RequestParam("file") MultipartFile file) throws IOException {
        ImageResponse response = imageService.uploadImage(file, projectId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{imageId}")
    @Operation(summary = "Get image by ID", description = "Get image information by ID")
    public ResponseEntity<ImageResponse> getImageById(@PathVariable UUID imageId) {
        ImageResponse response = imageService.getImageById(imageId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filename/{filename}")
    @Operation(summary = "Get image by filename", description = "Get image information by filename")
    public ResponseEntity<ImageResponse> getImageByFilename(@PathVariable String filename) {
        ImageResponse response = imageService.getImageByFilename(filename);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get images by user ID", description = "Get paginated list of images by user ID")
    public ResponseEntity<Page<ImageResponse>> getImagesByUserId(
            @PathVariable UUID userId, 
            @ParameterObject Pageable pageable) {
        Page<ImageResponse> responses = imageService.getImagesByUserId(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get images by project ID", description = "Get paginated list of images by project ID")
    public ResponseEntity<Page<ImageResponse>> getImagesByProjectId(
            @PathVariable UUID projectId, 
            @ParameterObject Pageable pageable) {
        Page<ImageResponse> responses = imageService.getImagesByProjectId(projectId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/project/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get images by user and project", description = "Get list of images by user ID and project ID")
    public ResponseEntity<List<ImageResponse>> getImagesByUserIdAndProjectId(
            @PathVariable UUID userId, 
            @PathVariable UUID projectId) {
        List<ImageResponse> responses = imageService.getImagesByUserIdAndProjectId(userId, projectId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Update image", description = "Update image information")
    public ResponseEntity<ImageResponse> updateImage(
            @PathVariable UUID imageId, 
            @ModelAttribute ImageUploadRequest request) throws IOException {
        ImageResponse response = imageService.updateImage(imageId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Delete image", description = "Delete image by ID")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}/image/{imageId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @Operation(summary = "Delete image by user", description = "Delete image by user ID and image ID (Admin only)")
    public ResponseEntity<Void> deleteImageByUser(
            @PathVariable UUID userId, 
            @PathVariable UUID imageId) {
        imageService.deleteImageByUser(userId, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{imageId}/view")
    @Operation(summary = "Get image bytes", description = "Get the raw bytes of an image")
    public ResponseEntity<byte[]> getImageBytes(@PathVariable UUID imageId) throws IOException {
        byte[] bytes = imageService.getImageBytes(imageId);
        ImageResponse image = imageService.getImageById(imageId);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getOriginalFilename() + "\"")
            .body(bytes);
    }

    @GetMapping("/{imageId}/webp")
    @Operation(summary = "Get WebP image bytes", description = "Get the WebP version of an image")
    public ResponseEntity<byte[]> getWebpImageBytes(@PathVariable UUID imageId) throws IOException {
        byte[] bytes = imageService.getWebpImageBytes(imageId);
        ImageResponse image = imageService.getImageById(imageId);
        
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("image/webp"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFilename().replaceAll("\\.[^.]*$", ".webp") + "\"")
            .body(bytes);
    }

    @GetMapping("/{imageId}/thumbnail")
    @Operation(summary = "Get thumbnail bytes", description = "Get the thumbnail of an image")
    public ResponseEntity<byte[]> getThumbnailBytes(@PathVariable UUID imageId) throws IOException {
        byte[] bytes = imageService.getThumbnailBytes(imageId);
        
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("image/webp"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(bytes);
    }

    @GetMapping("/{imageId}/download")
    @Operation(summary = "Download image", description = "Download the original image file")
    public ResponseEntity<byte[]> downloadImage(@PathVariable UUID imageId) throws IOException {
        byte[] bytes = imageService.getImageBytes(imageId);
        ImageResponse image = imageService.getImageById(imageId);
        
        // Increment download count
        imageService.incrementDownloadCount(imageId);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getOriginalFilename() + "\"")
            .body(bytes);
    }

    @GetMapping("/{imageId}/view-count")
    @Operation(summary = "Increment view count", description = "Increment the view count for an image")
    public ResponseEntity<Void> incrementViewCount(@PathVariable UUID imageId) {
        imageService.incrementViewCount(imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/search")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Search images by user", description = "Search images for a specific user")
    public ResponseEntity<List<ImageResponse>> searchImagesByUserId(
            @PathVariable UUID userId, 
            @RequestParam String query) {
        List<ImageResponse> responses = imageService.searchImagesByUserId(userId, query);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/project/{projectId}/search")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Search images by project", description = "Search images for a specific project")
    public ResponseEntity<List<ImageResponse>> searchImagesByProjectId(
            @PathVariable UUID projectId, 
            @RequestParam String query) {
        List<ImageResponse> responses = imageService.searchImagesByProjectId(projectId, query);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/recent")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get recent images by user", description = "Get the most recent images for a user")
    public ResponseEntity<List<ImageResponse>> getRecentImagesByUserId(
            @PathVariable UUID userId, 
            @RequestParam(defaultValue = "10") int limit) {
        List<ImageResponse> responses = imageService.getRecentImagesByUserId(userId, limit);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/popular")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get popular images by user", description = "Get the most popular images for a user")
    public ResponseEntity<List<ImageResponse>> getPopularImagesByUserId(
            @PathVariable UUID userId, 
            @RequestParam(defaultValue = "10") int limit) {
        List<ImageResponse> responses = imageService.getPopularImagesByUserId(userId, limit);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Count images by user", description = "Get the count of images for a user")
    public ResponseEntity<Integer> countImagesByUserId(@PathVariable UUID userId) {
        Integer count = imageService.countImagesByUserId(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/user/{userId}/storage")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get total storage used by user", description = "Get the total storage used by a user")
    public ResponseEntity<Long> getTotalStorageUsedByUserId(@PathVariable UUID userId) {
        Long storageUsed = imageService.getTotalStorageUsedByUserId(userId);
        return ResponseEntity.ok(storageUsed);
    }

    @GetMapping("/user/{userId}/date-range")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get images by date range", description = "Get images uploaded within a specific date range")
    public ResponseEntity<Page<ImageResponse>> getImagesByDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @ParameterObject Pageable pageable) {
        Page<ImageResponse> responses = imageService.getImagesByDateRange(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/format/{format}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @Operation(summary = "Get images by format", description = "Get images of a specific format for a user")
    public ResponseEntity<List<ImageResponse>> getImagesByFormat(
            @PathVariable UUID userId, 
            @PathVariable String format) {
        List<ImageResponse> responses = imageService.getImagesByFormat(userId, format);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/public")
    @Operation(summary = "Get all public images", description = "Get list of all public images")
    public ResponseEntity<List<ImageResponse>> getPublicImages() {
        List<ImageResponse> responses = imageService.getPublicImages();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/public/user/{userId}")
    @Operation(summary = "Get public images by user", description = "Get list of public images for a specific user")
    public ResponseEntity<List<ImageResponse>> getPublicImagesByUserId(@PathVariable UUID userId) {
        List<ImageResponse> responses = imageService.getPublicImagesByUserId(userId);
        return ResponseEntity.ok(responses);
    }
}

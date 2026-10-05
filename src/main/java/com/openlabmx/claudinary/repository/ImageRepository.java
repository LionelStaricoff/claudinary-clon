package com.openlabmx.claudinary.repository;
import com.openlabmx.claudinary.entity.ImageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {
    List<ImageEntity> findByUserId(UUID userId);
    Page<ImageEntity> findByUserId(UUID userId, Pageable pageable);
    List<ImageEntity> findByProjectId(UUID projectId);
    Page<ImageEntity> findByProjectId(UUID projectId, Pageable pageable);
    List<ImageEntity> findByUserIdAndProjectId(UUID userId, UUID projectId);
    Page<ImageEntity> findByUserIdAndProjectId(UUID userId, UUID projectId, Pageable pageable);
    Optional<ImageEntity> findByFilename(String filename);
    Optional<ImageEntity> findByUserIdAndFilename(UUID userId, String filename);
    @Query("SELECT i FROM ImageEntity i WHERE i.user.id = :userId AND (i.originalFilename LIKE %:query% OR i.filename LIKE %:query%)")
    List<ImageEntity> searchByUserIdAndQuery(@Param("userId") UUID userId, @Param("query") String query);
    @Query("SELECT i FROM ImageEntity i WHERE i.project.id = :projectId AND (i.originalFilename LIKE %:query% OR i.filename LIKE %:query%)")
    List<ImageEntity> searchByProjectIdAndQuery(@Param("projectId") UUID projectId, @Param("query") String query);
    @Query("SELECT i FROM ImageEntity i WHERE i.user.id = :userId AND i.createdAt >= :startDate AND i.createdAt <= :endDate")
    List<ImageEntity> findByUserIdAndDateRange(@Param("userId") UUID userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    @Query("SELECT i FROM ImageEntity i WHERE i.user.id = :userId AND i.originalFormat = :format")
    List<ImageEntity> findByUserIdAndOriginalFormat(@Param("userId") UUID userId, @Param("format") String format);
    @Query("SELECT COUNT(i) FROM ImageEntity i WHERE i.user.id = :userId")
    Integer countByUserId(@Param("userId") UUID userId);
    @Query("SELECT COALESCE(SUM(i.fileSize), 0) FROM ImageEntity i WHERE i.user.id = :userId")
    Long sumFileSizeByUserId(@Param("userId") UUID userId);
    void deleteByUserIdAndId(UUID userId, UUID imageId);
    void deleteByProjectId(UUID projectId);
    List<ImageEntity> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<ImageEntity> findTop10ByUserIdOrderByViewCountDesc(UUID userId);
    
    List<ImageEntity> findByIsPublicTrue();
    List<ImageEntity> findByUserIdAndIsPublicTrue(UUID userId);
}

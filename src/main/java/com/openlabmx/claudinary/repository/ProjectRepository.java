package com.openlabmx.claudinary.repository;
import com.openlabmx.claudinary.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByUserId(UUID userId);
    Optional<Project> findByUserIdAndIsDefaultTrue(UUID userId);
    Optional<Project> findByUserIdAndName(UUID userId, String name);
    Boolean existsByUserIdAndName(UUID userId, String name);
    Boolean existsByUserIdAndIsDefaultTrue(UUID userId);
    @Query("SELECT p FROM Project p WHERE p.user.id = :userId AND (p.name LIKE %:query% OR p.description LIKE %:query%)")
    List<Project> searchByUserIdAndQuery(@Param("userId") UUID userId, @Param("query") String query);
    void deleteByUserIdAndId(UUID userId, UUID projectId);
    List<Project> findByUserIdAndIsPublicTrue(UUID userId);
    @Query("SELECT COUNT(p) FROM Project p WHERE p.user.id = :userId")
    Integer countByUserId(@Param("userId") UUID userId);
}

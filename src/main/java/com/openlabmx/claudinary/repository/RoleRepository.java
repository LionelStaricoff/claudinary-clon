package com.openlabmx.claudinary.repository;
import com.openlabmx.claudinary.entity.Role;
import com.openlabmx.claudinary.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleType name);
    Boolean existsByName(RoleType name);
}

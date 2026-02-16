package app.sandori.stocker.api.repositories;

import app.sandori.stocker.api.entities.AllowedUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllowedUserRepository extends JpaRepository<AllowedUserEntity, String> {
}

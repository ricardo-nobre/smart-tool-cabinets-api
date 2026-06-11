package smarttoolcabinets.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.user.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para User.
 *
 * Serve de base para gestao administrativa de utilizadores.
 * Evolucao futura: queries de autorizacao e filtros por role/estado.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPinHash(String pinHash);

    Optional<User> findByNfcUid(String nfcUid);
}


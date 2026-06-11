package smarttoolcabinets.cabinet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.cabinet.domain.Cabinet;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para Cabinet.
 *
 * Existe para encapsular acesso a persistencia de armarios.
 * Evolucao futura: queries especificas de negocio conforme necessidades do fluxo.
 */
public interface CabinetRepository extends JpaRepository<Cabinet, UUID> {

    Optional<Cabinet> findByCode(String code);

    boolean existsByCode(String code);


}


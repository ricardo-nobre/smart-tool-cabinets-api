package smarttoolcabinets.cabinet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.cabinet.domain.Cabinet;

import java.util.Optional;
import java.util.UUID;

public interface CabinetRepository extends JpaRepository<Cabinet, UUID> {

    Optional<Cabinet> findByCode(String code);

    boolean existsByCode(String code);


}


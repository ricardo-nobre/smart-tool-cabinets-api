package smarttoolcabinets.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.event.domain.CabinetEvent;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para CabinetEvent.
 *
 * Existe para guardar e consultar eventos recebidos do dispositivo.
 * Evolucao futura: consultas por tipo/intervalo temporal para analise e auditoria.
 */
public interface CabinetEventRepository extends JpaRepository<CabinetEvent, UUID> {

    List<CabinetEvent> findByCabinetAccessId(UUID cabinetAccessId);
}


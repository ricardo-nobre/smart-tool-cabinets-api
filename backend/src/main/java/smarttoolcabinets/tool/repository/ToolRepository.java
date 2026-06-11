package smarttoolcabinets.tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.tool.domain.Tool;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio JPA para Tool.
 *
 * Existe para operacoes de leitura/escrita da tabela de ferramentas.
 * Evolucao futura: filtros por estado e consultas de suporte ao delta de inventario/custodia.
 */
public interface ToolRepository extends JpaRepository<Tool, UUID> {

    List<Tool> findByCabinetId(UUID cabinetId);

    List<Tool> findByTagCodeIn(List<String> tagCodes);

    boolean existsByTagCode(String tagCode);
}


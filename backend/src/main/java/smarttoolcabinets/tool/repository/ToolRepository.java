package smarttoolcabinets.tool.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smarttoolcabinets.tool.domain.Tool;

import java.util.List;
import java.util.UUID;

public interface ToolRepository extends JpaRepository<Tool, UUID> {

    List<Tool> findByCabinetId(UUID cabinetId);

    List<Tool> findByTagCodeIn(List<String> tagCodes);

    boolean existsByTagCode(String tagCode);
}


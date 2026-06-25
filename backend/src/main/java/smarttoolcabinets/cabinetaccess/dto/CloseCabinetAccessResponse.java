package smarttoolcabinets.cabinetaccess.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta de fecho final de CabinetAccess.
 */
public record CloseCabinetAccessResponse (
        UUID cabinetAccessId,
        String status,
        OffsetDateTime closedAt,
        String operationalResult,
        int assignmentsCreatedCount,
        int assignmentsReturnedCount,
        int unknownTagsCount,
        boolean discrepancyFlag
){
}

package smarttoolcabinets.cabinet.service;

import org.springframework.stereotype.Service;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.dto.AdminCabinetCreateRequest;
import smarttoolcabinets.cabinet.repository.CabinetRepository;

/**
 * Service administrativo para gestao de armarios.
 *
 * Evolucao futura: regras de validacao e persistencia de armarios.
 */
@Service
public class AdminCabinetService {

    private final CabinetRepository cabinetRepository;

    public AdminCabinetService(CabinetRepository cabinetRepository) {
        this.cabinetRepository = cabinetRepository;
    }

    /**
     * Objetivo: criar um novo armario por endpoint admin.
     * Inputs esperados: dados base de criacao do armario.
     * Output esperado: id do armario criado.
     * Passos logicos a implementar:
     * 1) Validar unicidade de codigo.
     * 2) Construir entidade Cabinet.
     * 3) Persistir em repositorio.
     * 4) Registar auditoria da operacao.
     * Notas: aplicar regras de ativacao inicial.
     */
    public String createCabinet(AdminCabinetCreateRequest request) {
        String code = request.code().trim();
        if(cabinetRepository.existsByCode(code)){
            throw new IllegalArgumentException("Cabinet code already exists: " + code);
        }
        String name = request.name().trim();
        String location = null;
        if(request.location() != null) {
            location = request.location().trim();
        }

        Cabinet cabinet = Cabinet.newCabinet(code,name,location);
        Cabinet saved = cabinetRepository.save(cabinet);
        return saved.getId().toString();
    }
}


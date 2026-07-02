package smarttoolcabinets.cabinet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttoolcabinets.cabinet.domain.Cabinet;
import smarttoolcabinets.cabinet.dto.AdminCabinetCreateRequest;
import smarttoolcabinets.cabinet.repository.CabinetRepository;

@Service
public class AdminCabinetService {

    private final CabinetRepository cabinetRepository;

    public AdminCabinetService(CabinetRepository cabinetRepository) {
        this.cabinetRepository = cabinetRepository;
    }

    @Transactional
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


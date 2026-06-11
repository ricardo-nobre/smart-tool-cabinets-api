package smarttoolcabinets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de arranque do contexto Spring.
 *
 * Existe para garantir que o skeleton tecnico sobe sem erros basicos.
 */
@SpringBootTest
@ActiveProfiles("test")
class SmartToolCabinetsApplicationTests {

    @Test
    void contextLoads() {
        // Context load smoke test.
    }
}


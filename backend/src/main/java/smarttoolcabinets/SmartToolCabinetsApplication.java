package smarttoolcabinets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da API Smart Tool Cabinets.
 *
 * Esta classe existe para inicializar o contexto Spring e permitir o arranque
 * do backend com a configuração base da semana.
 */
@SpringBootApplication
public class SmartToolCabinetsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartToolCabinetsApplication.class, args);
    }
}


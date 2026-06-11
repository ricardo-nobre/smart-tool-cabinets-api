package smarttoolcabinets.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração base de documentação da API.
 *
 * Esta classe existe para permitir que o Swagger UI arranque já nesta semana,
 * mesmo com endpoints ainda em modo skeleton.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartToolCabinetsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Smart Tool Cabinets API")
                .version("v0-skeleton")
                .description("Base tecnica inicial para desenvolvimento incremental")
                .contact(new Contact().name("Projeto LEIRT")));
    }
}


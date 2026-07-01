package smarttoolcabinets.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartToolCabinetsOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Smart Tool Cabinets API")
                .version("v1-mvp")
                .description("API de comunicacao para smart tool cabinets - MVP academico")
                .contact(new Contact().name("Projeto LEIRT")));
    }
}


package za.co.fintrack.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Hibernate6 module to handle lazy loading proxies
        Hibernate6Module hibernate6Module = new Hibernate6Module();

        // Configure the module to force lazy loading during serialization
        // This ensures that lazy-loaded objects are fully loaded and serialized
        hibernate6Module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
        hibernate6Module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        hibernate6Module.disable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);

        mapper.registerModule(hibernate6Module);

        return mapper;
    }
}

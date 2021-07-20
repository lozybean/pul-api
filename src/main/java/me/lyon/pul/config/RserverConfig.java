package me.lyon.pul.config;

import lombok.Data;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rserver")
public class RserverConfig {
    private String host;
    private Integer port;

    @Bean
    public RConnection rConnection() throws RserveException {
        return new RConnection("127.0.0.1", 6311);
    }
}

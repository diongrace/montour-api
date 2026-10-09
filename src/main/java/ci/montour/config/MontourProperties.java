package ci.montour.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "montour")
public record MontourProperties(String frontendUrl, Jwt jwt, Demo demo) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Demo(String motDePasse) {
    }
}
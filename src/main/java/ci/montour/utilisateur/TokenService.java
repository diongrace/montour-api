package ci.montour.utilisateur;

import ci.montour.config.MontourProperties;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final MontourProperties properties;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, MontourProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    public String generer(Utilisateur utilisateur) {
        Instant maintenant = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("montour")
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(properties.jwt().expirationMinutes(), ChronoUnit.MINUTES))
                .subject(utilisateur.getEmail())
                .claim("role", utilisateur.getRole().name())
                .claim("nom", utilisateur.getNom())
                .claim("etablissement", utilisateur.getEtablissement().getSlug())
                .build();
        JwsHeader entete = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(entete, claims)).getTokenValue();
    }
}

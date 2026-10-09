package ci.montour.admin;

import ci.montour.config.MontourProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QrCodeController {

    private final MontourProperties properties;

    public QrCodeController(MontourProperties properties) {
        this.properties = properties;
    }

    /** QR code à imprimer : il ouvre la page de choix du service de l'établissement. */
    @GetMapping(value = "/api/admin/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] qrCode(@AuthenticationPrincipal Jwt jwt) throws WriterException, IOException {
        String lien = properties.frontendUrl() + "/e/" + jwt.getClaimAsString("etablissement");
        BitMatrix matrice = new QRCodeWriter().encode(lien, BarcodeFormat.QR_CODE, 512, 512);
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrice, "PNG", sortie);
        return sortie.toByteArray();
    }
}

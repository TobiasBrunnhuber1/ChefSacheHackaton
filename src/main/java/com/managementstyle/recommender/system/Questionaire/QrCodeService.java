package com.managementstyle.recommender.system.Questionaire;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.util.Map;

// Erzeugt lokale SVG-QR-Codes ohne externe Internetabhängigkeit für Team-Einladungslinks
@Service
public class QrCodeService {

    // Generiert ein skalierbares SVG für beliebige Einladungs-URLs
    public String generateSvg(String content) {
        String payload = (content == null || content.isBlank()) ? "http://localhost:8080/" : content.trim();
        try {
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.CHARACTER_SET, "UTF-8",
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 1
            );
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 0, 0, hints);
            int size = matrix.getWidth();

            StringBuilder path = new StringBuilder();
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (matrix.get(x, y)) {
                        path.append('M').append(x).append(',').append(y).append("h1v1h-1z");
                    }
                }
            }
            return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 " + size + " " + size
                    + "\" shape-rendering=\"crispEdges\" role=\"img\" aria-label=\"QR-Code für Einladungslink\">"
                    + "<rect width=\"100%\" height=\"100%\" fill=\"#ffffff\"/>"
                    + "<path d=\"" + path + "\" fill=\"#172f3b\"/>"
                    + "</svg>";
        } catch (WriterException ex) {
            throw new IllegalStateException("QR-Code konnte nicht erstellt werden.", ex);
        }
    }
}

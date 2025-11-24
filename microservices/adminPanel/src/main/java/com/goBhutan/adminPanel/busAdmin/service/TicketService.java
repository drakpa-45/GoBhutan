package com.goBhutan.adminPanel.busAdmin.service;

import com.goBhutan.adminPanel.busAdmin.entity.SeatBooking;
import com.goBhutan.adminPanel.busAdmin.repository.SeatBookingRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.DocumentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final SeatBookingRepository bookingRepo;

    public byte[] generateTicket(Long bookingId) throws Exception {

        SeatBooking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        String qrData = "BOOKING:" + booking.getId() +
                "|CID:" + booking.getApplicantCid() +
                "|SEAT:" + booking.getSeatNumber();

        String qrBase64 = createQrCodeBase64(qrData);

        String html = buildHtmlTicket(booking, qrBase64);

        return convertHtmlToPdf(html);
    }

    private String buildHtmlTicket(SeatBooking booking, String qrBase64) {

        return """
        <html>
        <body style='font-family: Arial;'>
            <h2>Bus Ticket</h2>
            <p><strong>Passenger CID:</strong> %s</p>
            <p><strong>Email:</strong> %s</p>
            <p><strong>Mobile:</strong> %s</p>
            <p><strong>Seat Number:</strong> %d</p>
            <p><strong>Schedule:</strong> %s</p>
            <img src="data:image/png;base64,%s" width="180"/>
        </body>
        </html>
        """.formatted(
                booking.getApplicantCid(),
                booking.getApplicantEmail(),
                booking.getApplicantMobile(),
                booking.getSeatNumber(),
                booking.getSchedule().getDepartureTime(),
                qrBase64
        );
    }

    private String createQrCodeBase64(String data) throws Exception {
        int size = 300;
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "png", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    private byte[] convertHtmlToPdf(String html) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        try {
            renderer.createPDF(output);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        return output.toByteArray();
    }


}

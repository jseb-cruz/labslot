package dev.team.labslot.adapter.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import dev.team.labslot.core.constant.ReservationState;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.port.ReportGateway;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Genera los reportes en PDF con OpenPDF 2.0.5 (paquete com.lowagie.text; es la última versión para Java 17).
 * Las fuentes usan la codificación Cp1252 (Windows-1252), que incluye tildes, ñ y el guion "–".
 */
public class OpenPdfReportGateway implements ReportGateway {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    // Colores (los mismos tonos de Bootstrap que usa la web)
    private static final Color PRIMARY = new Color(13, 110, 253);
    private static final Color MUTED = new Color(108, 117, 125);
    private static final Color STRIPE = new Color(242, 244, 247);

    private static final Font TITLE_FONT = font(FontFactory.HELVETICA_BOLD, 18, PRIMARY);
    private static final Font SUBTITLE_FONT = font(FontFactory.HELVETICA, 10, MUTED);
    private static final Font SECTION_FONT = font(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
    private static final Font HEADER_FONT = font(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font CELL_FONT = font(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font CELL_BOLD_FONT = font(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
    private static final Font EMPTY_FONT = font(FontFactory.HELVETICA_OBLIQUE, 11, MUTED);
    private static final Font FOOTER_FONT = font(FontFactory.HELVETICA, 8, MUTED);

    @Override
    public byte[] generateReservationReport(LocalDate startDate,
                                            LocalDate endDate,
                                            List<Reservation> reservations,
                                            List<Laboratory> laboratories,
                                            List<User> users,
                                            LocalDateTime generatedAt) {
        // La reserva solo guarda ids: con estos mapas se muestran el código del laboratorio y el nombre del solicitante
        Map<UUID, Laboratory> laboratoriesById = laboratories.stream()
                .collect(Collectors.toMap(Laboratory::laboratoryId, Function.identity()));
        Map<UUID, User> usersById = users.stream()
                .collect(Collectors.toMap(User::userId, Function.identity()));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 48);
        try {
            PdfWriter.getInstance(document, output);

            // Pie de página con el número de página (se configura antes de abrir el documento)
            HeaderFooter footer = new HeaderFooter(new Phrase("LabSlot · USTA Tunja · Página ", FOOTER_FONT), true);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setBorder(Rectangle.NO_BORDER);
            document.setFooter(footer);

            document.open();
            addTitle(document, startDate, endDate, generatedAt);
            if (reservations.isEmpty()) {
                Paragraph empty = new Paragraph("No hay reservas en el rango seleccionado", EMPTY_FONT);
                empty.setSpacingBefore(16);
                document.add(empty);
            } else {
                document.add(reservationsTable(reservations, laboratoriesById, usersById));
            }
            addSummary(document, reservations);
            document.close();
        } catch (DocumentException exception) {
            throw new IllegalStateException("No se pudo generar el reporte PDF", exception);
        }
        return output.toByteArray();
    }

    private void addTitle(Document document, LocalDate startDate, LocalDate endDate, LocalDateTime generatedAt) {
        document.add(new Paragraph("Reporte de reservas – LabSlot", TITLE_FONT));
        Paragraph range = new Paragraph("Rango: " + startDate.format(DATE_FORMAT) + " – " + endDate.format(DATE_FORMAT),
                SUBTITLE_FONT);
        range.setSpacingBefore(4);
        document.add(range);
        document.add(new Paragraph("Generado el " + generatedAt.format(DATE_FORMAT) + " a las "
                + generatedAt.format(TIME_FORMAT) + " (hora de Colombia)", SUBTITLE_FONT));
    }

    private PdfPTable reservationsTable(List<Reservation> reservations,
                                        Map<UUID, Laboratory> laboratoriesById,
                                        Map<UUID, User> usersById) {
        PdfPTable table = new PdfPTable(new float[]{1.4f, 3.2f, 1.5f, 1.7f, 1.3f, 1.5f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(14);
        // El encabezado se repite en cada página
        table.setHeaderRows(1);

        for (String header : List.of("Laboratorio", "Solicitante", "Fecha", "Horario", "Asistentes", "Estado")) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(PRIMARY);
            cell.setBorderColor(PRIMARY);
            cell.setPadding(6);
            table.addCell(cell);
        }

        int row = 0;
        for (Reservation reservation : reservations) {
            // Filas alternas en gris claro para que la tabla sea fácil de leer
            Color background = row % 2 == 0 ? Color.WHITE : STRIPE;
            Laboratory laboratory = laboratoriesById.get(reservation.laboratoryId());
            User user = usersById.get(reservation.userId());
            table.addCell(bodyCell(laboratory != null ? laboratory.code() : "—", background, Element.ALIGN_LEFT));
            table.addCell(bodyCell(user != null ? user.fullName() : "—", background, Element.ALIGN_LEFT));
            table.addCell(bodyCell(reservation.date().format(DATE_FORMAT), background, Element.ALIGN_LEFT));
            table.addCell(bodyCell(reservation.startTime().format(TIME_FORMAT) + " – "
                    + reservation.endTime().format(TIME_FORMAT), background, Element.ALIGN_LEFT));
            table.addCell(bodyCell(String.valueOf(reservation.attendees()), background, Element.ALIGN_CENTER));
            table.addCell(bodyCell(stateLabel(reservation.state()), background, Element.ALIGN_LEFT));
            row++;
        }
        return table;
    }

    // Total de reservas por estado (todos los estados, aunque tengan 0) y total general
    private void addSummary(Document document, List<Reservation> reservations) {
        Map<ReservationState, Integer> totals = new EnumMap<>(ReservationState.class);
        for (ReservationState state : ReservationState.values()) {
            totals.put(state, 0);
        }
        for (Reservation reservation : reservations) {
            totals.merge(reservation.state(), 1, Integer::sum);
        }

        Paragraph title = new Paragraph("Resumen por estado", SECTION_FONT);
        title.setSpacingBefore(18);
        document.add(title);

        PdfPTable summary = new PdfPTable(new float[]{3f, 1f});
        summary.setWidthPercentage(40);
        summary.setHorizontalAlignment(Element.ALIGN_LEFT);
        summary.setSpacingBefore(6);
        for (Map.Entry<ReservationState, Integer> total : totals.entrySet()) {
            summary.addCell(bodyCell(stateLabel(total.getKey()), Color.WHITE, Element.ALIGN_LEFT));
            summary.addCell(bodyCell(String.valueOf(total.getValue()), Color.WHITE, Element.ALIGN_RIGHT));
        }
        PdfPCell totalLabel = bodyCell("Total", STRIPE, Element.ALIGN_LEFT);
        totalLabel.setPhrase(new Phrase("Total", CELL_BOLD_FONT));
        summary.addCell(totalLabel);
        PdfPCell totalValue = bodyCell(String.valueOf(reservations.size()), STRIPE, Element.ALIGN_RIGHT);
        totalValue.setPhrase(new Phrase(String.valueOf(reservations.size()), CELL_BOLD_FONT));
        summary.addCell(totalValue);
        document.add(summary);
    }

    private PdfPCell bodyCell(String text, Color background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, CELL_FONT));
        cell.setBackgroundColor(background);
        cell.setBorderColor(new Color(222, 226, 230));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    // Estado en español, igual que en la web
    private String stateLabel(ReservationState state) {
        return switch (state) {
            case RSTATE_PENDING -> "Pendiente";
            case RSTATE_APPROVED -> "Aprobada";
            case RSTATE_REJECTED -> "Rechazada";
            case RSTATE_CANCELLED -> "Cancelada";
            case RSTATE_COMPLETED -> "Completada";
        };
    }

    // Fuente estándar Helvetica con codificación Cp1252, que incluye tildes y ñ (no hace falta embeber la fuente)
    private static Font font(String name, float size, Color color) {
        return FontFactory.getFont(name, BaseFont.CP1252, BaseFont.NOT_EMBEDDED, size, Font.NORMAL, color);
    }
}

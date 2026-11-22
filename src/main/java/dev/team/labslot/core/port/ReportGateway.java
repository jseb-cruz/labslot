package dev.team.labslot.core.port;

import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Puerto para generar reportes. El core no sabe con qué tecnología se generan (el adaptador usa OpenPDF).
 * Devuelve el archivo como bytes, igual que el ReportGateway de astranova.
 */
public interface ReportGateway {

    // Reporte de reservas de un rango de fechas. Las reservas llegan ya filtradas y ordenadas;
    // los laboratorios y usuarios sirven para mostrar el código y el nombre del solicitante.
    byte[] generateReservationReport(LocalDate startDate,
                                     LocalDate endDate,
                                     List<Reservation> reservations,
                                     List<Laboratory> laboratories,
                                     List<User> users,
                                     LocalDateTime generatedAt);
}

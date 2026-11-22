package dev.team.labslot.adapter.repository.mariadb.repositories;

import dev.team.labslot.adapter.repository.mariadb.entity.ReservationJdbi;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * Consultas SQL de la tabla reservations. JDBI genera la implementación.
 * Las columnas en snake_case se asignan a los campos del record en camelCase.
 */
@RegisterConstructorMapper(ReservationJdbi.class)
public interface ReservationDao {

    @SqlQuery("SELECT reservation_id, laboratory_id, user_id, reservation_date, start_time, end_time, "
            + "attendees, purpose, state, created_at FROM reservations ORDER BY reservation_date, start_time")
    List<ReservationJdbi> findAll();

    @SqlQuery("SELECT reservation_id, laboratory_id, user_id, reservation_date, start_time, end_time, "
            + "attendees, purpose, state, created_at FROM reservations WHERE reservation_id = :reservationId")
    Optional<ReservationJdbi> findById(@Bind("reservationId") String reservationId);

    @SqlUpdate("INSERT INTO reservations (reservation_id, laboratory_id, user_id, reservation_date, start_time, "
            + "end_time, attendees, purpose, state, created_at) VALUES (:reservationId, :laboratoryId, :userId, "
            + ":reservationDate, :startTime, :endTime, :attendees, :purpose, :state, :createdAt)")
    void insert(@BindMethods ReservationJdbi reservation);

    // No se actualizan el laboratorio, el usuario ni la fecha de creación
    @SqlUpdate("UPDATE reservations SET reservation_date = :reservationDate, start_time = :startTime, "
            + "end_time = :endTime, attendees = :attendees, purpose = :purpose, state = :state "
            + "WHERE reservation_id = :reservationId")
    void update(@BindMethods ReservationJdbi reservation);

    @SqlUpdate("DELETE FROM reservations WHERE reservation_id = :reservationId")
    void delete(@Bind("reservationId") String reservationId);
}

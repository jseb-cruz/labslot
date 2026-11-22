package dev.team.labslot.adapter.repository.mariadb.repositories;

import dev.team.labslot.adapter.repository.mariadb.entity.LaboratoryJdbi;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * Consultas SQL de la tabla laboratories. JDBI genera la implementación.
 * Las columnas en snake_case se asignan a los campos del record en camelCase.
 */
@RegisterConstructorMapper(LaboratoryJdbi.class)
public interface LaboratoryDao {

    @SqlQuery("SELECT laboratory_id, code, name, building, capacity, state FROM laboratories ORDER BY code")
    List<LaboratoryJdbi> findAll();

    @SqlQuery("SELECT laboratory_id, code, name, building, capacity, state FROM laboratories WHERE laboratory_id = :laboratoryId")
    Optional<LaboratoryJdbi> findById(@Bind("laboratoryId") String laboratoryId);

    @SqlUpdate("INSERT INTO laboratories (laboratory_id, code, name, building, capacity, state) "
            + "VALUES (:laboratoryId, :code, :name, :building, :capacity, :state)")
    void insert(@BindMethods LaboratoryJdbi laboratory);

    @SqlUpdate("UPDATE laboratories SET code = :code, name = :name, building = :building, capacity = :capacity, state = :state "
            + "WHERE laboratory_id = :laboratoryId")
    void update(@BindMethods LaboratoryJdbi laboratory);

    @SqlUpdate("DELETE FROM laboratories WHERE laboratory_id = :laboratoryId")
    void delete(@Bind("laboratoryId") String laboratoryId);
}

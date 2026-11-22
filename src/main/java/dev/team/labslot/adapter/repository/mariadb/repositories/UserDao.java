package dev.team.labslot.adapter.repository.mariadb.repositories;

import dev.team.labslot.adapter.repository.mariadb.entity.UserJdbi;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * Consultas SQL de la tabla users. JDBI genera la implementación.
 * Las columnas en snake_case se asignan a los campos del record en camelCase.
 */
@RegisterConstructorMapper(UserJdbi.class)
public interface UserDao {

    @SqlQuery("SELECT user_id, full_name, email, password, role FROM users ORDER BY full_name")
    List<UserJdbi> findAll();

    @SqlQuery("SELECT user_id, full_name, email, password, role FROM users WHERE user_id = :userId")
    Optional<UserJdbi> findById(@Bind("userId") String userId);

    @SqlUpdate("INSERT INTO users (user_id, full_name, email, password, role) "
            + "VALUES (:userId, :fullName, :email, :password, :role)")
    void insert(@BindMethods UserJdbi user);

    @SqlUpdate("UPDATE users SET full_name = :fullName, email = :email, password = :password, role = :role "
            + "WHERE user_id = :userId")
    void update(@BindMethods UserJdbi user);

    @SqlUpdate("DELETE FROM users WHERE user_id = :userId")
    void delete(@Bind("userId") String userId);
}

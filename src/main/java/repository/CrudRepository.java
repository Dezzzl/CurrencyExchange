package repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CrudRepository<K, T> {
    List<T> findAll() throws SQLException;

    Optional<T> findById(K id) throws SQLException;

    boolean delete(K id) throws SQLException;

    void update(T entity) throws SQLException;

    Optional<T> save(T entity) throws SQLException;
}

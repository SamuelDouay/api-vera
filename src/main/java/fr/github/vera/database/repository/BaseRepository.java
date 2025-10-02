package fr.github.vera.database.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T, ID> extends BaseRequest implements IRepository<T, ID> {
    protected final String tableName;
    protected final Class<T> entityClass;

    public BaseRepository(String tableName, Class<T> entityClass) {
        this.tableName = tableName;
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(), Optional.empty(), "GET BY ID " + tableName, id);
    }

    @Override
    public List<T> findAll(int limit, int offset) {
        String sql = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, rs -> mapResultSetList(rs), new ArrayList<>(),
                "GET ALL PAGINATED " + tableName, limit, offset);
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        return executeQuery(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0, "COUNT " + tableName);
    }

    // Méthodes abstraites à implémenter dans les classes filles
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;

    protected abstract List<T> mapResultSetList(ResultSet rs) throws SQLException;
}
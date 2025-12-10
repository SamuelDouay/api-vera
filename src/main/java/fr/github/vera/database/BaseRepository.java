package fr.github.vera.database;

import fr.github.vera.model.Identifiable;
import fr.github.vera.repository.IRepository;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class BaseRepository<T extends Identifiable<I>, I> extends BaseRequest implements IRepository<T, I> {
    private static final Map<Class<?>, TypeHandler> TYPE_HANDLERS = new HashMap<>();

    static {
        // Integer types
        TYPE_HANDLERS.put(Integer.class, ResultSet::getInt);
        TYPE_HANDLERS.put(int.class, ResultSet::getInt);

        // String type
        TYPE_HANDLERS.put(String.class, ResultSet::getString);

        // Boolean types
        TYPE_HANDLERS.put(Boolean.class, ResultSet::getBoolean);
        TYPE_HANDLERS.put(boolean.class, ResultSet::getBoolean);

        // Long types
        TYPE_HANDLERS.put(Long.class, ResultSet::getLong);
        TYPE_HANDLERS.put(long.class, ResultSet::getLong);

        // Double types
        TYPE_HANDLERS.put(Double.class, ResultSet::getDouble);
        TYPE_HANDLERS.put(double.class, ResultSet::getDouble);

        // Float types
        TYPE_HANDLERS.put(Float.class, ResultSet::getFloat);
        TYPE_HANDLERS.put(float.class, ResultSet::getFloat);

        // Date/time types
        TYPE_HANDLERS.put(java.util.Date.class, ResultSet::getTimestamp);

        TYPE_HANDLERS.put(java.time.LocalDateTime.class, (rs, col) -> {
            java.sql.Timestamp timestamp = rs.getTimestamp(col);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        });

        TYPE_HANDLERS.put(java.time.LocalDate.class, (rs, col) -> {
            java.sql.Date date = rs.getDate(col);
            return date != null ? date.toLocalDate() : null;
        });

        TYPE_HANDLERS.put(java.time.Instant.class, (rs, col) -> {
            java.sql.Timestamp timestamp = rs.getTimestamp(col);
            return timestamp != null ? timestamp.toInstant() : null;
        });
    }

    protected final String tableName;
    protected final Class<T> entityClass;

    protected BaseRepository(String tableName, Class<T> entityClass) {
        this.tableName = tableName;
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(I id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return executeQueryWithParams(sql, rs -> rs.next() ? Optional.of(mapResultSet(rs)) : Optional.empty(), Optional.empty(), "GET BY ID " + tableName, id);
    }

    @Override
    public List<T> findAll(int limit, int offset) {
        String sql = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
        return executeQueryWithParams(sql, this::mapResultSetList, new ArrayList<>(),
                "GET ALL PAGINATED " + tableName.toUpperCase(), limit, offset);
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        return executeQuery(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0, "COUNT " + tableName);
    }

    @Override
    public boolean delete(I id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        return executeUpdate(sql, "DELETE " + tableName.toUpperCase(), id) != 0;
    }

    public List<T> mapResultSetList(ResultSet rs) throws SQLException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    @Override
    public T save(T entity) {
        I idValue = entity.getId();

        if (idValue == null) {
            return create(entity);
        }
        return update(entity);

    }

    protected T update(T entity) {
        Class<?> clazz = entity.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " must be annotated with @Table");
        }

        String tableName = tableAnnotation.name();
        DynamicUpdateBuilder builder = new DynamicUpdateBuilder(tableName);
        Object idValue = null;

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null) {
                I value = entity.getId();
                field.setAccessible(true);
                String columnName = columnAnnotation.name();
                if (columnName.equals("id")) {
                    idValue = value; // Stocke l'ID pour la clause WHERE
                } else if (value != null && columnAnnotation.updatable()) {
                    builder.set(columnName, value);
                }
            }
        }

        if (idValue != null) {
            builder.where("id = ?", idValue);
        }

        if (builder.hasUpdates()) {
            executeUpdate(builder.buildSql(), "UPDATE " + clazz.getSimpleName(), builder.buildParams());
        }
        return entity;
    }

    protected T create(T entity) {
        Class<?> clazz = entity.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        if (tableAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getSimpleName() + " must be annotated with @Table");
        }

        String tableName = tableAnnotation.name();
        DynamicInsertBuilder builder = new DynamicInsertBuilder(tableName);

        // Utilise la réflexion pour remplir le builder
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            if (columnAnnotation != null && !columnAnnotation.name().equals("id")) {
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    // Pour les insertions, on peut inclure les valeurs null si nécessaire
                    if (value != null) {
                        builder.set(columnAnnotation.name(), value);
                    }
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage());
                }
            }
        }

        if (builder.hasValues()) {
            String sql = builder.buildSql();
            Object[] params = builder.buildParams();

            Integer generatedId = executeUpdateWithGeneratedKeys(sql,
                    "CREATE " + clazz.getSimpleName(), params);

            // Définit l'ID généré sur l'entité
            setEntityId(entity, generatedId);
        }

        return entity;
    }

    private void setEntityId(T entity, Integer id) {
        entity.setId((I) id);
    }

    protected T mapResultSet(ResultSet rs) throws SQLException {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();

            Field[] fields = entityClass.getDeclaredFields();

            for (Field field : fields) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null) {
                    field.setAccessible(true);
                    String columnName = columnAnnotation.name();

                    try {
                        Object value = getValueFromResultSet(rs, columnName, field.getType());
                        field.set(entity, value);
                    } catch (SQLException e) {
                        logger.error(e.getMessage());
                    }
                }
            }

            return entity;
        } catch (Exception e) {
            throw new SQLException("Error mapping ResultSet to entity", e);
        }
    }

    private Object getValueFromResultSet(ResultSet rs, String columnName, Class<?> type) throws SQLException {
        TypeHandler handler = TYPE_HANDLERS.get(type);

        if (handler != null) {
            return handler.handle(rs, columnName);
        }

        if (type.isPrimitive()) {
            return handlePrimitiveType(rs, columnName);
        }

        return rs.getObject(columnName);
    }

    private Object handlePrimitiveType(ResultSet rs, String columnName) throws SQLException {
        return rs.getObject(columnName);
    }

    @FunctionalInterface
    private interface TypeHandler {
        Object handle(ResultSet rs, String columnName) throws SQLException;
    }

}
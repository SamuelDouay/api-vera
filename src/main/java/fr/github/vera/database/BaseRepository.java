package fr.github.vera.database;

import fr.github.vera.repository.IRepository;

import java.lang.reflect.Field;
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
        return executeQueryWithParams(sql, this::mapResultSetList, new ArrayList<>(),
                "GET ALL PAGINATED " + tableName.toUpperCase(), limit, offset);
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        return executeQuery(sql, rs -> rs.next() ? rs.getInt(1) : 0, 0, "COUNT " + tableName);
    }

    @Override
    public boolean delete(ID id) {
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
        try {
            // Vérifie si l'entité a un ID
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object idValue = idField.get(entity);

            if (idValue == null) {
                return create(entity);
            } else {
                return update(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving entity", e);
        }
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
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    String columnName = columnAnnotation.name();

                    if (columnName.equals("id")) {
                        idValue = value; // Stocke l'ID pour la clause WHERE
                    } else if (value != null && columnAnnotation.updatable()) {
                        builder.set(columnName, value);
                    }
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage());
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
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Could not set generated ID", e);
        }
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
        if (type == Integer.class || type == int.class) {
            return rs.getInt(columnName);
        } else if (type == String.class) {
            return rs.getString(columnName);
        } else if (type == Boolean.class || type == boolean.class) {
            return rs.getBoolean(columnName);
        } else if (type == Long.class || type == long.class) {
            return rs.getLong(columnName);
        } else if (type == Double.class || type == double.class) {
            return rs.getDouble(columnName);
        } else if (type == Float.class || type == float.class) {
            return rs.getFloat(columnName);
        } else if (type == java.util.Date.class) {
            return rs.getTimestamp(columnName);
        } else if (type == java.time.LocalDateTime.class) {
            java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } else if (type == java.time.LocalDate.class) {
            java.sql.Date date = rs.getDate(columnName);
            return date != null ? date.toLocalDate() : null;
        } else if (type == java.time.Instant.class) {
            java.sql.Timestamp timestamp = rs.getTimestamp(columnName);
            return timestamp != null ? timestamp.toInstant() : null;
        } else {
            return rs.getObject(columnName);
        }
    }

}
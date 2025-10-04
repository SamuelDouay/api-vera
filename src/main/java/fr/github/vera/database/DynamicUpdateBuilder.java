package fr.github.vera.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DynamicUpdateBuilder {
    private static final Logger log = LogManager.getLogger(DynamicUpdateBuilder.class);
    private final String tableName;
    private final Map<String, Object> updates = new HashMap<>();
    private String whereClause;
    private Object[] whereParams;

    public DynamicUpdateBuilder(String tableName) {
        this.tableName = tableName;
    }

    public DynamicUpdateBuilder set(String column, Object value) {
        if (value != null) {
            updates.put(column, value);
        }
        log.info(updates);
        return this;
    }

    public DynamicUpdateBuilder setIfNotNull(String column, Object value) {
        return set(column, value);
    }

    public DynamicUpdateBuilder where(String whereClause, Object... whereParams) {
        this.whereClause = whereClause;
        this.whereParams = whereParams;
        return this;
    }

    public boolean hasUpdates() {
        return !updates.isEmpty();
    }

    public String buildSql() {
        if (!hasUpdates()) {
            throw new IllegalStateException("No fields to update");
        }

        StringBuilder sql = new StringBuilder("UPDATE ")
                .append(tableName)
                .append(" SET ");

        // Ajoute les colonnes à mettre à jour
        List<String> setClauses = new ArrayList<>();
        for (String column : updates.keySet()) {
            setClauses.add(column + " = ?");
        }
        sql.append(String.join(", ", setClauses));

        // Ajoute la clause WHERE
        if (whereClause != null) {
            sql.append(" WHERE ").append(whereClause);
        }

        return sql.toString();
    }

    public Object[] buildParams() {
        List<Object> params = new ArrayList<>(updates.values());
        if (whereParams != null) {
            Collections.addAll(params, whereParams);
        }
        return params.toArray();
    }
}
package fr.github.vera.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DynamicInsertBuilder {
    private static final Logger log = LogManager.getLogger(DynamicInsertBuilder.class);
    private final String tableName;
    private final Map<String, Object> values = new LinkedHashMap<>(); // LinkedHashMap pour conserver l'ordre

    public DynamicInsertBuilder(String tableName) {
        this.tableName = tableName;
    }

    public DynamicInsertBuilder set(String column, Object value) {
        if (value != null) {
            values.put(column, value);
        }
        log.debug("Added value for column {}: {}", column, value);
        return this;
    }

    public DynamicInsertBuilder setIfNotNull(String column, Object value) {
        return set(column, value);
    }

    public boolean hasValues() {
        return !values.isEmpty();
    }

    public String buildSql() {
        if (!hasValues()) {
            throw new IllegalStateException("No values to insert");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ")
                .append(tableName)
                .append(" (");

        // Colonnes
        sql.append(String.join(", ", values.keySet()));

        // Values
        sql.append(") VALUES (");

        List<String> placeholders = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            placeholders.add("?");
        }
        sql.append(String.join(", ", placeholders));
        sql.append(")");

        return sql.toString();
    }

    public Object[] buildParams() {
        return values.values().toArray();
    }

    public List<String> getColumns() {
        return new ArrayList<>(values.keySet());
    }
}
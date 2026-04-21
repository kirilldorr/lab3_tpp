package com.example.lab3;

import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SecureCommandService {

    private static final List<String> ALLOWED_TABLES = List.of("regions", "oblasts", "settlements");

    public String executeSecureCommand(String command) {
        command = command.trim();
        
        // Регулярний вираз для розбору: тип команди + назва таблиці + параметри в дужках
        Pattern pCommand = Pattern.compile("(?i)^(insert|update|delete|read)\\s+(\\w+)(?:\\s*\\((.*)\\))?$");
        Matcher mCommand = pCommand.matcher(command);

        if (!mCommand.find()) {
            return "Помилка формату. Приклад: insert regions (name='Київський')";
        }

        String action = mCommand.group(1).toLowerCase();
        String table = mCommand.group(2).toLowerCase();
        String paramsStr = mCommand.group(3) != null ? mCommand.group(3) : "";

        if (!ALLOWED_TABLES.contains(table)) {
            return "Помилка: Операції з таблицею '" + table + "' заборонені.";
        }

        Map<String, String> params = parseParams(paramsStr);

        try (Connection conn = DatabaseConnection.getConnection()) {
            switch (action) {
                case "insert": return handleInsert(conn, table, params);
                case "update": return handleUpdate(conn, table, params);
                case "delete": return handleDelete(conn, table, params);
                case "read":   return handleRead(conn, table, params);
                default:       return "Невідома команда.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Помилка бази даних: " + e.getMessage();
        }
    }

    // Метод для розбору параметрів типу id='1', name='Taras'
    private Map<String, String> parseParams(String paramsStr) {
        Map<String, String> map = new HashMap<>();
        Pattern p = Pattern.compile("(\\w+)=['‘’](.*?)['‘’]");
        Matcher m = p.matcher(paramsStr);
        while (m.find()) {
            map.put(m.group(1).toLowerCase(), m.group(2));
        }
        return map;
    }

    private String handleInsert(Connection conn, String table, Map<String, String> params) throws Exception {
        if (params.isEmpty()) return "Помилка: відсутні параметри для додавання.";

        StringBuilder cols = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (cols.length() > 0) {
                cols.append(", ");
                placeholders.append(", ");
            }
            cols.append(entry.getKey());
            placeholders.append("?");
            values.add(castValue(entry.getKey(), entry.getValue()));
        }

        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                pstmt.setObject(i + 1, values.get(i));
            }
            pstmt.executeUpdate();
            return "Запис успішно додано до таблиці " + table + ".";
        }
    }

    private String handleUpdate(Connection conn, String table, Map<String, String> params) throws Exception {
        if (!params.containsKey("id")) return "Помилка: для update обов'язковий параметр id='...'";
        
        int id = Integer.parseInt(params.get("id"));
        params.remove("id");

        if (params.isEmpty()) return "Помилка: відсутні дані для оновлення.";

        StringBuilder setClause = new StringBuilder();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (setClause.length() > 0) setClause.append(", ");
            setClause.append(entry.getKey()).append(" = ?");
            values.add(castValue(entry.getKey(), entry.getValue()));
        }

        String sql = "UPDATE " + table + " SET " + setClause + " WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object val : values) {
                pstmt.setObject(i++, val);
            }
            pstmt.setInt(i, id);
            int rows = pstmt.executeUpdate();
            return rows > 0 ? "Запис з ID=" + id + " успішно оновлено." : "Запис з ID=" + id + " не знайдено.";
        }
    }

    private String handleDelete(Connection conn, String table, Map<String, String> params) throws Exception {
        if (!params.containsKey("id")) return "Помилка: для delete обов'язковий параметр id='...'";
        
        int id = Integer.parseInt(params.get("id"));
        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0 ? "Запис з ID=" + id + " успішно видалено." : "Запис не знайдено.";
        }
    }

    private String handleRead(Connection conn, String table, Map<String, String> params) throws Exception {
        String sql = "SELECT * FROM " + table;
        boolean hasId = params.containsKey("id");
        if (hasId) {
            sql += " WHERE id = ?";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (hasId) pstmt.setInt(1, Integer.parseInt(params.get("id")));

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                StringBuilder sb = new StringBuilder();
                
                // Формування заголовків
                for (int i = 1; i <= columnCount; i++) {
                    sb.append(metaData.getColumnName(i)).append(" | ");
                }
                sb.append("\n-------------------------------------------------\n");

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    for (int i = 1; i <= columnCount; i++) {
                        sb.append(rs.getString(i)).append(" | ");
                    }
                    sb.append("\n");
                }

                if (rowCount == 0) return "Таблиця порожня або запис не знайдено.";
                return sb.toString();
            }
        }
    }

    // Допоміжний метод для автоматичного перетворення типів
    private Object castValue(String key, String value) {
        if (key.equals("region_id") || key.equals("population") || key.equals("oblast_id") || key.equals("id")) {
            return Integer.parseInt(value);
        }
        return value;
    }
}
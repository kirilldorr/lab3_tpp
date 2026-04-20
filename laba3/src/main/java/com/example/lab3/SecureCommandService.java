package com.example.lab3;

import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SecureCommandService {

    private static final List<String> ALLOWED_TABLES = List.of("regions", "oblasts", "settlements");

    public String executeSecureCommand(String command) {
        command = command.trim();
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (command.toLowerCase().startsWith("insert")) {
                return handleInsert(conn, command);
            } else if (command.toLowerCase().startsWith("delete")) {
                return handleDelete(conn, command);
            } else {
                return "Невідома команда. Використовуйте 'insert' або 'delete'";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Помилка виконання: " + e.getMessage();
        }
    }

    private String handleInsert(Connection conn, String command) throws Exception {

        if (command.toLowerCase().contains("regions")) {
            Pattern p = Pattern.compile("name=['‘’](.*?)['‘’]");
            Matcher m = p.matcher(command);

            if (m.find()) {
                String name = m.group(1);
                String sql = "INSERT INTO regions (name) VALUES (?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.executeUpdate();
                    return "Регіон '" + name + "' додано успішно.";
                }
            }
        }

        else if (command.toLowerCase().contains("oblasts")) {
            Pattern pName = Pattern.compile("name=['‘’](.*?)['‘’]");
            Pattern pRegionId = Pattern.compile("region_id=['‘’](\\d+)['‘’]");

            Matcher mName = pName.matcher(command);
            Matcher mRegionId = pRegionId.matcher(command);

            if (mName.find() && mRegionId.find()) {
                String name = mName.group(1);
                int regionId = Integer.parseInt(mRegionId.group(1));

                String sql = "INSERT INTO oblasts (name, region_id) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setInt(2, regionId);
                    pstmt.executeUpdate();
                    return "Область '" + name + "' додано успішно до регіону ID=" + regionId;
                }
            } else {
                return "Помилка: для oblasts потрібні параметри name='...' та region_id='...'";
            }
        }

        else if (command.toLowerCase().contains("settlements")) {
            Pattern pName = Pattern.compile("name=['‘’](.*?)['‘’]");
            Pattern pPop = Pattern.compile("population=['‘’](\\d+)['‘’]");
            Pattern pOblastId = Pattern.compile("oblast_id=['‘’](\\d+)['‘’]");

            Matcher mName = pName.matcher(command);
            Matcher mPop = pPop.matcher(command);
            Matcher mOblastId = pOblastId.matcher(command);

            if (mName.find() && mPop.find() && mOblastId.find()) {
                String name = mName.group(1);
                int population = Integer.parseInt(mPop.group(1));
                int oblastId = Integer.parseInt(mOblastId.group(1));

                String sql = "INSERT INTO settlements (name, population, oblast_id) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, name);
                    pstmt.setInt(2, population);
                    pstmt.setInt(3, oblastId);
                    pstmt.executeUpdate();
                    return "Населений пункт '" + name + "' додано успішно.";
                }
            } else {
                return "Помилка: для settlements потрібні name, population та oblast_id";
            }
        }

        return "Не розпізнано таблицю або параметри команди insert.";
    }

    private String handleDelete(Connection conn, String command) throws Exception {
        Pattern pTable = Pattern.compile("delete\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Pattern pId = Pattern.compile("id=['‘’](\\d+)['‘’]");

        Matcher mTable = pTable.matcher(command);
        Matcher mId = pId.matcher(command);

        if (mTable.find() && mId.find()) {
            String table = mTable.group(1).toLowerCase();
            int id = Integer.parseInt(mId.group(1));

            if (!ALLOWED_TABLES.contains(table)) {
                return "Помилка: Видалення з таблиці '" + table + "' заборонено.";
            }

            String sql = "DELETE FROM " + table + " WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int rows = pstmt.executeUpdate();
                return rows > 0 ? "Запис з ID=" + id + " видалено з " + table + "." : "Запис не знайдено.";
            }
        }
        return "Помилка формату delete. Приклад: delete regions(id='1')";
    }
}
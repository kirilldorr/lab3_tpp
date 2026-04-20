package com.example.lab3;

import org.springframework.beans.factory.annotation.Autowired; // Додайте цей імпорт
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    @Autowired
    private SecureCommandService secureCommandService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/execute")
    public String executeSql(@RequestParam("sql") String sql, Model model) {
        model.addAttribute("lastQuery", sql);
        /*
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // ВРАЗЛИВЕ МІСЦЕ: виконання рядка напряму
            boolean hasResultSet = stmt.execute(sql);

            if (hasResultSet) {
                ResultSet rs = stmt.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<String> headers = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    headers.add(metaData.getColumnName(i));
                }

                List<List<String>> rows = new ArrayList<>();
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getString(i));
                    }
                    rows.add(row);
                }
                model.addAttribute("headers", headers);
                model.addAttribute("rows", rows);
            } else {
                model.addAttribute("message", "Запит виконано успішно (Updates: " + stmt.getUpdateCount() + ")");
            }

        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
        }
        */

        String resultMessage = secureCommandService.executeSecureCommand(sql);

        model.addAttribute("message", resultMessage);

        return "index";
    }
}
package com.example.lab3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

        /* // ==========================================
        // FOR TEST WITH SQL INJECTION (NOT SECURE)
        // ==========================================
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {

            boolean hasResultSet = stmt.execute(sql);
            if (hasResultSet) {
                java.sql.ResultSet rs = stmt.getResultSet();
                java.sql.ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        sb.append(metaData.getColumnName(i)).append(": ").append(rs.getString(i)).append(" | ");
                    }
                    sb.append("\n");
                }
                model.addAttribute("message", sb.toString());
            } else {
                model.addAttribute("message", "Запит виконано успішно (Updates: " + stmt.getUpdateCount() + ")");
            }

        } catch (java.sql.SQLException e) {
            model.addAttribute("message", "Помилка: " + e.getMessage());
        }
        */

        // ==========================================
        // FOR SECURE COMMAND PARSER
        // ==========================================
        String resultMessage = secureCommandService.executeSecureCommand(sql);
        model.addAttribute("message", resultMessage);

        return "index";
    }
}
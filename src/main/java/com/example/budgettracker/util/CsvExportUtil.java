package com.example.budgettracker.util;

import com.example.budgettracker.dto.ExpenseDTO;
import java.io.PrintWriter;
import java.util.List;

public class CsvExportUtil {

    public static void writeExpensesToCsv(PrintWriter writer, List<ExpenseDTO> expenses) {
        writer.write("ID,Description,Amount,Category,Payment Mode,Date\n");
        for (ExpenseDTO expense : expenses) {
            writer.write(expense.getId() + ",");
            writer.write(escapeSpecialCharacters(expense.getDescription()) + ",");
            writer.write(expense.getAmount() + ",");
            writer.write(expense.getCategory() + ",");
            writer.write(expense.getPaymentMode() + ",");
            writer.write(expense.getDate() + "\n");
        }
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}

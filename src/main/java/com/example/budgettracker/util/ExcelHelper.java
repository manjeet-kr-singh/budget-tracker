package com.example.budgettracker.util;

import com.example.budgettracker.entity.Expense;
import com.example.budgettracker.enums.Category;
import com.example.budgettracker.enums.PaymentMode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERS = { "Date (YYYY-MM-DD)", "Description", "Category", "Payment Mode", "Amount" };
    static String SHEET = "Expenses";

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public static ByteArrayInputStream startApiTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET);

            // Header
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                // Bold Style
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data Validation: Category (Col 2)
            // Create Hidden Sheet for Dropdown Values
            Sheet hidden = workbook.createSheet("Hidden");
            // Hide the sheet
            workbook.setSheetHidden(workbook.getSheetIndex(hidden), true);

            // Populate Categories in Col 0
            List<String> categories = new ArrayList<>();
            for (Category c : Category.values())
                categories.add(c.getDisplayName());

            for (int i = 0; i < categories.size(); i++) {
                Row row = hidden.getRow(i + 1);
                if (row == null)
                    row = hidden.createRow(i + 1);
                row.createCell(0).setCellValue(categories.get(i));
            }

            // Populate Payment Modes in Col 1
            List<String> paymentModes = new ArrayList<>();
            for (PaymentMode pm : PaymentMode.values())
                paymentModes.add(pm.getDisplayName());

            for (int i = 0; i < paymentModes.size(); i++) {
                Row row = hidden.getRow(i + 1);
                if (row == null)
                    row = hidden.createRow(i + 1);
                row.createCell(1).setCellValue(paymentModes.get(i));
            }

            // Create Named Range for Categories
            Name namedCellCat = workbook.createName();
            namedCellCat.setNameName("Categories");
            // String catRef = "Hidden!$A$1:$A$" + categories.size();
            String catRef = "Hidden!$A$2:$A$" + (categories.size() + 1);
            namedCellCat.setRefersToFormula(catRef);

            // Create Named Range for Payment Modes
            Name namedCellMode = workbook.createName();
            namedCellMode.setNameName("PaymentModes");
            // String modeRef = "Hidden!$B$1:$B$" + paymentModes.size();
            String modeRef = "Hidden!$B$2:$B$" + (paymentModes.size() + 1);
            namedCellMode.setRefersToFormula(modeRef);

            // Data Validation: Category (Col 2)
            DataValidationHelper validationHelper = sheet.getDataValidationHelper();
            DataValidationConstraint catConstraint = validationHelper.createFormulaListConstraint("Categories");
            CellRangeAddressList catAddrList = new CellRangeAddressList(1, 100, 2, 2);
            DataValidation catValidation = validationHelper.createValidation(catConstraint, catAddrList);
            // catValidation.setSuppressDropDownArrow(false);
            catValidation.setShowErrorBox(true);
            sheet.addValidationData(catValidation);

            // Data Validation: Payment Mode (Col 3)
            DataValidationConstraint payConstraint = validationHelper.createFormulaListConstraint("PaymentModes");
            CellRangeAddressList payAddrList = new CellRangeAddressList(1, 100, 3, 3);
            DataValidation payValidation = validationHelper.createValidation(payConstraint, payAddrList);
            sheet.addValidationData(payValidation);

            // Auto Size Columns
            // Set fixed column width to avoid AWT dependency (headless mode issue)
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.setColumnWidth(i, 15 * 256); // Fixed width approx 15 chars
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }

    public static List<Expense> excelToExpenses(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);
            if (sheet == null)
                sheet = workbook.getSheetAt(0); // Fallback

            Iterator<Row> rows = sheet.iterator();
            List<Expense> expenses = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip Header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Expense expense = new Expense();
                // Check if empty row
                if (currentRow.getCell(0) == null)
                    break;

                // 0: Date
                Cell dateCell = currentRow.getCell(0);
                if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                    Date d = dateCell.getDateCellValue();
                    expense.setDate(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                } else {
                    // Try parsing string? For now assume strict Excel date
                    continue; // Skip invalid
                }

                // 1: Description
                expense.setDescription(getCellValueAsString(currentRow.getCell(1)));

                // 2: Category
                String catStr = getCellValueAsString(currentRow.getCell(2));
                // Match display name to Enum
                for (Category c : Category.values()) {
                    if (c.getDisplayName().equalsIgnoreCase(catStr)) {
                        expense.setCategory(c.getDisplayName());
                        break;
                    }
                }

                // 3: Payment Mode
                String payStr = getCellValueAsString(currentRow.getCell(3));
                for (com.example.budgettracker.enums.PaymentMode pm : com.example.budgettracker.enums.PaymentMode
                        .values()) {
                    if (pm.getDisplayName().equalsIgnoreCase(payStr) || pm.name().equalsIgnoreCase(payStr)) {
                        // Still storing as String in DB for now as per plan, but ensuring it matches an
                        // Enum
                        expense.setPaymentMode(pm.name()); // Or getDisplayName() depending on DB preference.
                        // Per existing code, it seems to store raw string.
                        // But for consistency let's store the DISPLAY NAME if that's what Category
                        // does?
                        // Category stores Display Name: expense.setCategory(c.getDisplayName());
                        // Let's check Expense.java... it stores String.
                        // The prompt implies we want consistency.
                        // Let's stick to what Category does:
                        expense.setPaymentMode(pm.getDisplayName());
                        break;
                    }
                }

                // 4: Amount
                Cell amountCell = currentRow.getCell(4);
                if (amountCell != null && amountCell.getCellType() == CellType.NUMERIC) {
                    expense.setAmount(new BigDecimal(amountCell.getNumericCellValue()));
                }

                expenses.add(expense);
                rowNumber++;
            }
            workbook.close();
            return expenses;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        if (cell.getCellType() == CellType.STRING)
            return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC)
            return String.valueOf(cell.getNumericCellValue());
        return "";
    }
}

package com.example.budgettracker.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelHelperTest {

    @Test
    void testStartApiTemplateHasValidations() throws Exception {
        ByteArrayInputStream bis = ExcelHelper.startApiTemplate();
        Workbook workbook = new XSSFWorkbook(bis);
        Sheet sheet = workbook.getSheet("Expenses");

        assertNotNull(sheet, "Sheet 'Expenses' should exist");

        List<? extends DataValidation> validations = sheet.getDataValidations();
        assertFalse(validations.isEmpty(), "Sheet should have data validations");

        // We expect at least 2 validations (Category and Payment Mode)
        // Note: The size might be 2, but let's just ensure we find the ones we care
        // about.

        boolean hasCategoryValidation = false;
        boolean hasPaymentModeValidation = false;

        for (DataValidation dv : validations) {
            CellRangeAddress[] addresses = dv.getRegions().getCellRangeAddresses();
            for (CellRangeAddress address : addresses) {
                // Category is Column 2 (0-indexed)
                if (address.getFirstColumn() == 2 && address.getLastColumn() == 2) {
                    hasCategoryValidation = true;
                }
                // Payment Mode is Column 3 (0-indexed)
                if (address.getFirstColumn() == 3 && address.getLastColumn() == 3) {
                    hasPaymentModeValidation = true;
                }
            }
        }

        assertTrue(hasCategoryValidation, "Category column (index 2) should have validation");
        assertTrue(hasPaymentModeValidation, "Payment Mode column (index 3) should have validation");

        workbook.close();
    }
}

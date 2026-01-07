package com.example.budgettracker.controller;

import com.example.budgettracker.dto.ExpenseDTO;
import com.example.budgettracker.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import com.example.budgettracker.util.ExcelHelper;
import com.example.budgettracker.entity.Expense;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // --- MVC Endpoints for Thymeleaf Views ---

    @GetMapping("/")
    public String home(Model model) {
        List<ExpenseDTO> expenses = expenseService.getAllExpenses(null);
        BigDecimal totalAmount = expenses.stream()
                .map(ExpenseDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("activePage", "dashboard");
        return "index";
    }

    @GetMapping("/transactions")
    public String transactions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String paymentMode,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "desc") String dir,
            Model model) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                dir.equals("asc") ? org.springframework.data.domain.Sort.by(sort).ascending()
                        : org.springframework.data.domain.Sort.by(sort).descending());

        org.springframework.data.domain.Page<ExpenseDTO> expensePage = expenseService.getExpensesPaginated(keyword,
                category, paymentMode, startDate, endDate, pageable);

        model.addAttribute("expenses", expensePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", expensePage.getTotalPages());
        model.addAttribute("totalItems", expensePage.getTotalElements());
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("paymentMode", paymentMode);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Pass enums for filters
        model.addAttribute("categories", com.example.budgettracker.enums.Category.values());
        model.addAttribute("paymentModes", com.example.budgettracker.enums.PaymentMode.values());

        model.addAttribute("activePage", "transactions");
        return "transactions";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activePage", "settings");
        return "settings";
    }

    @GetMapping("/showNewExpenseForm")
    public String showNewExpenseForm(Model model) {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        // Set default date to today
        expenseDTO.setDate(java.time.LocalDate.now());
        model.addAttribute("expense", expenseDTO);
        model.addAttribute("categories", com.example.budgettracker.enums.Category.values());
        model.addAttribute("paymentModes", com.example.budgettracker.enums.PaymentMode.values());
        return "add-expense";
    }

    @GetMapping("/export")
    public void exportToCsv(javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setCharacterEncoding("UTF-8"); // Ensure UTF-8
        String headerKey = "Content-Disposition";
        // Header with quotes around filename for better compatibility
        String headerValue = "attachment; filename=\"expenses.csv\"";
        response.setHeader(headerKey, headerValue);

        List<ExpenseDTO> listExpenses = expenseService.getAllExpenses(null);

        com.example.budgettracker.util.CsvExportUtil.writeExpensesToCsv(response.getWriter(), listExpenses);
        response.getWriter().flush(); // Explicit flush
        response.getWriter().close();
    }

    @PostMapping("/saveExpense")
    public String saveExpense(@Valid @ModelAttribute("expense") ExpenseDTO expenseDTO,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "add-expense";
        }
        // If ID is present, service updates; if null, service saves new.
        if (expenseDTO.getId() != null) {
            expenseService.updateExpense(expenseDTO.getId(), expenseDTO);
        } else {
            expenseService.saveExpense(expenseDTO);
        }
        return "redirect:/transactions";
    }

    @GetMapping("/showEditExpenseForm/{id}")
    public String showEditExpenseForm(@PathVariable(value = "id") Long id, Model model) {
        ExpenseDTO expenseDTO = expenseService.getExpenseById(id);
        model.addAttribute("expense", expenseDTO);
        model.addAttribute("categories", com.example.budgettracker.enums.Category.values());
        model.addAttribute("paymentModes", com.example.budgettracker.enums.PaymentMode.values());
        return "edit-expense";
    }

    @GetMapping("/deleteExpense/{id}")
    public String deleteExpense(@PathVariable(value = "id") Long id) {
        expenseService.deleteExpense(id);
        return "redirect:/transactions";
    }

    @GetMapping("/downloadTemplate")
    public ResponseEntity<Resource> downloadTemplate() {
        String filename = "expenses_template.xlsx";
        InputStreamResource file = new InputStreamResource(ExcelHelper.startApiTemplate());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @PostMapping("/import")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes inputFlash) {
        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                List<Expense> expenses = ExcelHelper.excelToExpenses(file.getInputStream());
                expenseService.saveAll(expenses);
                inputFlash.addFlashAttribute("message",
                        "Uploaded the file successfully: " + file.getOriginalFilename());
            } catch (Exception e) {
                inputFlash.addFlashAttribute("error",
                        "Could not upload the file: " + file.getOriginalFilename() + "!" + e.getMessage());
            }
        } else {
            inputFlash.addFlashAttribute("error", "Please upload an excel file!");
        }
        return "redirect:/transactions";
    }
}

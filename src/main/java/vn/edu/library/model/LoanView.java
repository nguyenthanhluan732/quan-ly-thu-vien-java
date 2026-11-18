package vn.edu.library.model;

public record LoanView(int loanId, String readerName, String cardNumber,
                       String bookTitle, int quantity, String loanDate,
                       String dueDate, String returnDate, String status) {}

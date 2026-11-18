package vn.edu.library.model;

public record Book(int id, int categoryId, String categoryName, String title,
                   String author, String publisher, Integer publishYear,
                   int totalQuantity, int availableQuantity, String shelfLocation) {}

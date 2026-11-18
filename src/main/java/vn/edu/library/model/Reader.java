package vn.edu.library.model;

public record Reader(int id, String cardNumber, String fullName, String birthDate,
                     String gender, String phone, String email, String address) {}

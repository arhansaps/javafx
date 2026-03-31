package com.example;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class BillingRecord {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final String invoiceNumber;
    private final Integer roomNumber;
    private final String customerName;
    private final String roomType;
    private final LocalDate checkInDate;
    private final LocalDate checkOutDate;
    private final Integer nights;
    private final Double roomCharge;
    private final Double serviceCharge;
    private final Double totalAmount;
    private final String billingStatus;
    private final String paymentMethod;
    private final String paymentStatus;
    private final String paymentReference;
    private final LocalDate generatedOn;

    public BillingRecord(
            String invoiceNumber,
            Integer roomNumber,
            String customerName,
            String roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            Integer nights,
            Double roomCharge,
            Double serviceCharge,
            Double totalAmount,
            String billingStatus,
            String paymentMethod,
            String paymentStatus,
            String paymentReference,
            LocalDate generatedOn) {
        this.invoiceNumber = invoiceNumber;
        this.roomNumber = roomNumber;
        this.customerName = customerName;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.roomCharge = roomCharge;
        this.serviceCharge = serviceCharge;
        this.totalAmount = totalAmount;
        this.billingStatus = billingStatus;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentReference = paymentReference;
        this.generatedOn = generatedOn;
    }

    public static BillingRecord createActiveRecord(
            String invoiceNumber,
            Room room,
            String customerName,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            PaymentMethod paymentMethod,
            String paymentReference) {
        Integer nights = calculateNights(checkInDate, checkOutDate);
        Double roomCharge = room.calculatePrice() * nights;
        Double serviceCharge = roomCharge * 0.12;
        Double totalAmount = roomCharge + serviceCharge;

        return new BillingRecord(
                invoiceNumber,
                room.getRoomNumber(),
                customerName,
                room.getRoomType().name(),
                checkInDate,
                checkOutDate,
                nights,
                roomCharge,
                serviceCharge,
                totalAmount,
                "Active",
                paymentMethod == null ? "-" : paymentMethod.getDisplayName(),
                paymentMethod == null ? "Pending" : "Paid",
                paymentReference == null || paymentReference.isBlank() ? "-" : paymentReference,
                LocalDate.now());
    }

    public BillingRecord markClosed() {
        return new BillingRecord(
                invoiceNumber,
                roomNumber,
                customerName,
                roomType,
                checkInDate,
                checkOutDate,
                nights,
                roomCharge,
                serviceCharge,
                totalAmount,
                "Closed",
                paymentMethod,
                paymentStatus,
                paymentReference,
                LocalDate.now());
    }

    private static Integer calculateNights(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return (int) Math.max(1, nights);
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getRoomType() {
        return roomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public Integer getNights() {
        return nights;
    }

    public Double getRoomCharge() {
        return roomCharge;
    }

    public Double getServiceCharge() {
        return serviceCharge;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public LocalDate getGeneratedOn() {
        return generatedOn;
    }

    public String getCheckInText() {
        return checkInDate == null ? "-" : checkInDate.format(DATE_FORMATTER);
    }

    public String getCheckOutText() {
        return checkOutDate == null ? "-" : checkOutDate.format(DATE_FORMATTER);
    }

    public String getGeneratedOnText() {
        return generatedOn == null ? "-" : generatedOn.format(DATE_FORMATTER);
    }

    public String getRoomChargeText() {
        return String.format("%.2f", roomCharge);
    }

    public String getServiceChargeText() {
        return String.format("%.2f", serviceCharge);
    }

    public String getTotalAmountText() {
        return String.format("%.2f", totalAmount);
    }
}

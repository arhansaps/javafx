package com.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class BookingSystem {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static class ReportRow {
        private final Integer roomNumber;
        private final String roomType;
        private final Double price;
        private final Boolean available;
        private final String customerName;
        private final String customerPhone;
        private final LocalDate checkInDate;
        private final LocalDate checkOutDate;

        public ReportRow(
                Integer roomNumber,
                String roomType,
                Double price,
                Boolean available,
                String customerName,
                String customerPhone,
                LocalDate checkInDate,
                LocalDate checkOutDate) {
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.price = price;
            this.available = available;
            this.customerName = customerName;
            this.customerPhone = customerPhone;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
        }

        public Integer getRoomNumber() {
            return roomNumber;
        }

        public String getRoomType() {
            return roomType;
        }

        public Double getPrice() {
            return price;
        }

        public Boolean getAvailable() {
            return available;
        }

        public String getStatusText() {
            return Boolean.TRUE.equals(available) ? "Available" : "Occupied";
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public String getCheckInText() {
            return checkInDate == null ? "-" : checkInDate.format(DATE_FORMATTER);
        }

        public String getCheckOutText() {
            return checkOutDate == null ? "-" : checkOutDate.format(DATE_FORMATTER);
        }
    }

    // Demonstrating collections: ArrayList and HashMap
    private final ArrayList<Room> rooms = new ArrayList<>();
    private final HashMap<Integer, String> roomToCustomerMap = new HashMap<>();
    private final HashMap<Integer, String> roomToCustomerPhoneMap = new HashMap<>();
    private final HashMap<Integer, Pair<LocalDate, LocalDate>> roomBookingDatesMap = new HashMap<>();
    private final ArrayList<BillingRecord> billingRecords = new ArrayList<>();
    private Integer nextInvoiceNumber = 1001;

    public String addRoom(Room room) {
        if (findRoomByNumber(room.getRoomNumber()) != null) {
            return "Room already exists.";
        }
        room.setPrice(room.calculatePrice());
        rooms.add(room);
        return "Room added successfully.";
    }

    public String displayAllRooms() {
        if (rooms.isEmpty()) {
            return "No rooms found.";
        }

        StringBuilder builder = new StringBuilder();
        // Demonstrating Iterator traversal
        Iterator<Room> iterator = rooms.iterator();
        while (iterator.hasNext()) {
            Room room = iterator.next();
            String customerName = roomToCustomerMap.get(room.getRoomNumber());
            String customerPhone = roomToCustomerPhoneMap.get(room.getRoomNumber());
            Pair<LocalDate, LocalDate> bookingDates = roomBookingDatesMap.get(room.getRoomNumber());

            builder.append("Room ")
                    .append(room.getRoomNumber())
                    .append(" | Type: ")
                    .append(room.getRoomType())
                    .append(" | Price: ")
                    .append(String.format("%.2f", room.calculatePrice()))
                    .append(" | Status: ");

            if (customerName != null && bookingDates != null && !Boolean.TRUE.equals(room.getAvailable())) {
                builder.append("Booked")
                        .append(" | Guest: ")
                        .append(customerName)
                        .append(" | Phone: ")
                        .append(customerPhone == null ? "-" : customerPhone)
                        .append(" | Check-in: ")
                        .append(bookingDates.getFirst().format(DATE_FORMATTER))
                        .append(" | Checkout: ")
                        .append(bookingDates.getSecond().format(DATE_FORMATTER));
            } else if (!Boolean.TRUE.equals(room.getAvailable())) {
                builder.append("Not Available");
            } else {
                builder.append("Available");
            }

            builder.append(" | Amenities: ")
                    .append(room.getAmenities());

            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    public String displayBookings() {
        if (roomToCustomerMap.isEmpty()) {
            return "No active bookings.";
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, String> entry : roomToCustomerMap.entrySet()) {
            // Demonstrating generics class Pair<T, U>
            Pair<Integer, String> pair = new Pair<>(entry.getKey(), entry.getValue());
            Pair<LocalDate, LocalDate> datePair = roomBookingDatesMap.get(entry.getKey());
            String customerPhone = roomToCustomerPhoneMap.getOrDefault(entry.getKey(), "-");

            String fromDate = "-";
            String toDate = "-";
            if (datePair != null) {
                fromDate = Pair.displayValue(datePair.getFirst().format(DATE_FORMATTER));
                toDate = Pair.displayValue(datePair.getSecond().format(DATE_FORMATTER));
            }

            BillingRecord billingRecord = findLatestBillingRecordByRoom(entry.getKey());
            String paymentSummary = "";
            if (billingRecord != null) {
                paymentSummary = " | Payment: "
                        + billingRecord.getPaymentStatus()
                        + " ("
                        + billingRecord.getPaymentMethod()
                        + ")";
            }

            builder.append("Room ")
                    .append(Pair.displayValue(pair.getFirst()))
                    .append(" -> ")
                    .append(pair.getSecond())
                    .append(" | Phone: ")
                    .append(customerPhone)
                    .append(" | From: ")
                    .append(fromDate)
                    .append(" | To: ")
                    .append(toDate)
                    .append(paymentSummary)
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    public synchronized Integer getTotalRooms() {
        return rooms.size();
    }

    public synchronized Integer getAvailableRoomsCount() {
        Integer count = 0;
        for (Room room : rooms) {
            if (Boolean.TRUE.equals(room.getAvailable())) {
                count++;
            }
        }
        return count;
    }

    public synchronized Integer getOccupiedRoomsCount() {
        return getTotalRooms() - getAvailableRoomsCount();
    }

    public synchronized Integer getActiveBookingsCount() {
        return roomToCustomerMap.size();
    }

    public synchronized Double getOccupancyRate() {
        if (rooms.isEmpty()) {
            return 0.0;
        }
        return (getOccupiedRoomsCount() * 100.0) / getTotalRooms();
    }

    public synchronized ArrayList<ReportRow> getDetailedRoomReport() {
        ArrayList<ReportRow> reportRows = new ArrayList<>();

        // Demonstrating Iterator traversal for report generation
        Iterator<Room> iterator = rooms.iterator();
        while (iterator.hasNext()) {
            Room room = iterator.next();
            String customerName = roomToCustomerMap.getOrDefault(room.getRoomNumber(), "-");
            String customerPhone = roomToCustomerPhoneMap.getOrDefault(room.getRoomNumber(), "-");
            Pair<LocalDate, LocalDate> bookingDates = roomBookingDatesMap.get(room.getRoomNumber());

            LocalDate checkInDate = null;
            LocalDate checkOutDate = null;
            if (bookingDates != null) {
                checkInDate = bookingDates.getFirst();
                checkOutDate = bookingDates.getSecond();
            }

            reportRows.add(new ReportRow(
                    room.getRoomNumber(),
                    room.getRoomType().name(),
                    room.calculatePrice(),
                    room.getAvailable(),
                    customerName,
                    customerPhone,
                    checkInDate,
                    checkOutDate));
        }
        return reportRows;
    }

    // Demonstrating synchronization
    public synchronized String bookRoom(
            String customerName,
            String customerPhone,
            Integer roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            PaymentMethod paymentMethod,
            String paymentReference) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        if (customerPhone == null || customerPhone.isBlank()) {
            return "Enter customer phone number.";
        }

        if (checkInDate == null || checkOutDate == null) {
            return "Select both check-in and checkout dates.";
        }

        if (checkOutDate.isBefore(checkInDate)) {
            return "Checkout date must be on or after check-in date.";
        }

        if (paymentMethod == null) {
            return "Select a payment method.";
        }

        if (!room.getAvailable()) {
            return "Room is not available.";
        }

        room.setAvailable(Boolean.FALSE);
        roomToCustomerMap.put(roomNumber, customerName);
        roomToCustomerPhoneMap.put(roomNumber, customerPhone);
        roomBookingDatesMap.put(roomNumber, new Pair<>(checkInDate, checkOutDate));
        BillingRecord billingRecord = BillingRecord.createActiveRecord(
                generateInvoiceNumber(),
                room,
                customerName,
                checkInDate,
                checkOutDate,
                paymentMethod,
                paymentReference);
        billingRecords.add(billingRecord);

        Pair<Integer, String> bookingPair = new Pair<>(roomNumber, customerName);
        return "Booking successful: Room "
                + bookingPair.getFirst()
                + " for "
                + bookingPair.getSecond()
                + " ("
                + checkInDate.format(DATE_FORMATTER)
                + " to "
                + checkOutDate.format(DATE_FORMATTER)
                + ")"
                + " | Payment received via "
                + paymentMethod.getDisplayName()
                + " | Invoice "
                + billingRecord.getInvoiceNumber();
    }

    public void bookRoomAsync(
            String customerName,
            String customerPhone,
            Integer roomNumber,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            PaymentMethod paymentMethod,
            String paymentReference,
            Consumer<String> callback) {
        // Demonstrating multithreading
        Thread bookingThread = new Thread(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            String result = bookRoom(customerName, customerPhone, roomNumber, checkInDate, checkOutDate, paymentMethod, paymentReference);
            callback.accept(result);
        });
        bookingThread.start();
    }

    public String checkoutRoom(Integer roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        if (room.getAvailable()) {
            return "Room is already available.";
        }

        room.setAvailable(Boolean.TRUE);
        roomToCustomerMap.remove(roomNumber);
        roomToCustomerPhoneMap.remove(roomNumber);
        roomBookingDatesMap.remove(roomNumber);
        closeBillingRecord(roomNumber);
        return "Checkout complete. Cleaning started for room " + roomNumber + ".";
    }

    public void checkoutRoomAsync(Integer roomNumber, Consumer<String> callback) {
        // Demonstrating multithreading
        Thread checkoutThread = new Thread(() -> {
            String result = checkoutRoom(roomNumber);
            callback.accept(result);

            // Room cleaning simulation with delay
            Thread cleaningThread = new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    callback.accept("Room " + roomNumber + " cleaning completed.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
            cleaningThread.start();
        });
        checkoutThread.start();
    }

    public String saveRoomsToTextFile(String filePath) {
        // Demonstrating file handling with FileWriter
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Room room : rooms) {
                writer.write(room.getRoomNumber() + ","
                        + room.getRoomType() + ","
                        + room.getPrice() + ","
                        + room.getAvailable() + System.lineSeparator());
            }
            return "Room data saved to text file.";
        } catch (IOException exception) {
            return "Error saving text file: " + exception.getMessage();
        }
    }

    public String loadRoomsFromTextFile(String filePath) {
        // Demonstrating file handling with FileReader
        try (FileReader reader = new FileReader(filePath); BufferedReader bufferedReader = new BufferedReader(reader)) {
            rooms.clear();
            roomToCustomerMap.clear();
            roomToCustomerPhoneMap.clear();
            roomBookingDatesMap.clear();
            billingRecords.clear();
            nextInvoiceNumber = 1001;

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    continue;
                }

                Integer roomNumber = Integer.valueOf(parts[0].trim());
                RoomType roomType = RoomType.valueOf(parts[1].trim());
                Double price = Double.valueOf(parts[2].trim());
                Boolean available = Boolean.valueOf(parts[3].trim());

                rooms.add(createRoomByType(roomType, roomNumber, price, available));
            }
            return "Room data loaded from text file.";
        } catch (IOException | IllegalArgumentException exception) {
            return "Error loading text file: " + exception.getMessage();
        }
    }

    public String saveRoomsSerialized(String filePath) {
        // Demonstrating serialization save
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath))) {
            outputStream.writeObject(rooms);
            return "Room objects serialized successfully.";
        } catch (IOException exception) {
            return "Error in serialization save: " + exception.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public String loadRoomsSerialized(String filePath) {
        // Demonstrating serialization load
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            Object object = inputStream.readObject();
            if (object instanceof ArrayList<?>) {
                rooms.clear();
                roomToCustomerMap.clear();
                roomToCustomerPhoneMap.clear();
                roomBookingDatesMap.clear();
                billingRecords.clear();
                nextInvoiceNumber = 1001;

                for (Object item : (ArrayList<?>) object) {
                    if (item instanceof Room) {
                        rooms.add((Room) item);
                    }
                }
                return "Room objects loaded from serialized file.";
            }
            return "Serialized data format is invalid.";
        } catch (IOException | ClassNotFoundException exception) {
            return "Error in serialization load: " + exception.getMessage();
        }
    }

    public synchronized ArrayList<Room> getRoomsSnapshot() {
        ArrayList<Room> snapshot = new ArrayList<>();
        for (Room room : rooms) {
            Room copy = createRoomByType(room.getRoomType(), room.getRoomNumber(), room.getPrice(), room.getAvailable());
            snapshot.add(copy);
        }
        return snapshot;
    }

    public synchronized HashMap<Integer, String> getRoomToCustomerSnapshot() {
        return new HashMap<>(roomToCustomerMap);
    }

    public synchronized HashMap<Integer, String> getCustomerPhoneSnapshot() {
        return new HashMap<>(roomToCustomerPhoneMap);
    }

    public synchronized HashMap<Integer, Pair<LocalDate, LocalDate>> getBookingDatesSnapshot() {
        return new HashMap<>(roomBookingDatesMap);
    }

    public synchronized ArrayList<BillingRecord> getBillingRecordsSnapshot() {
        return new ArrayList<>(billingRecords);
    }

    public synchronized void loadSnapshot(
            ArrayList<Room> loadedRooms,
            HashMap<Integer, String> loadedRoomCustomers,
            HashMap<Integer, String> loadedCustomerPhones,
            HashMap<Integer, Pair<LocalDate, LocalDate>> loadedBookingDates,
            ArrayList<BillingRecord> loadedBillingRecords) {
        rooms.clear();
        rooms.addAll(loadedRooms);

        roomToCustomerMap.clear();
        roomToCustomerMap.putAll(loadedRoomCustomers);

        roomToCustomerPhoneMap.clear();
        roomToCustomerPhoneMap.putAll(loadedCustomerPhones);

        roomBookingDatesMap.clear();
        roomBookingDatesMap.putAll(loadedBookingDates);

        billingRecords.clear();
        billingRecords.addAll(loadedBillingRecords);

        updateInvoiceCounter();
    }

    private Room findRoomByNumber(Integer roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber)) {
                return room;
            }
        }
        return null;
    }

    private String generateInvoiceNumber() {
        return "INV-" + nextInvoiceNumber++;
    }

    private void closeBillingRecord(Integer roomNumber) {
        for (int index = 0; index < billingRecords.size(); index++) {
            BillingRecord record = billingRecords.get(index);
            if (record.getRoomNumber().equals(roomNumber) && "Active".equalsIgnoreCase(record.getBillingStatus())) {
                billingRecords.set(index, record.markClosed());
                return;
            }
        }
    }

    private BillingRecord findLatestBillingRecordByRoom(Integer roomNumber) {
        for (int index = billingRecords.size() - 1; index >= 0; index--) {
            BillingRecord record = billingRecords.get(index);
            if (record.getRoomNumber().equals(roomNumber)) {
                return record;
            }
        }
        return null;
    }

    private void updateInvoiceCounter() {
        int maxInvoiceNumber = 1000;
        for (BillingRecord record : billingRecords) {
            String invoiceNumber = record.getInvoiceNumber();
            if (invoiceNumber != null && invoiceNumber.startsWith("INV-")) {
                try {
                    maxInvoiceNumber = Math.max(maxInvoiceNumber, Integer.parseInt(invoiceNumber.substring(4)));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed invoice numbers and keep the safe default.
                }
            }
        }
        nextInvoiceNumber = maxInvoiceNumber + 1;
    }

    private Room createRoomByType(RoomType roomType, Integer roomNumber, Double price, Boolean available) {
        return switch (roomType) {
            case STANDARD -> new StandardRoom(roomNumber, price, available);
            case DELUXE -> new DeluxeRoom(roomNumber, price, available);
            case SUITE -> new SuiteRoom(roomNumber, price, available);
            case VILLA -> new VillaRoom(roomNumber, price, available);
        };
    }
}

package com.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class BookingSystem {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static class SnapshotData implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ArrayList<Room> rooms;
        private final HashMap<Integer, String> roomCustomers;
        private final HashMap<Integer, String> customerPhones;
        private final HashMap<Integer, Pair<LocalDate, LocalDate>> bookingDates;
        private final HashMap<Integer, OperationalStatus> operationalStatuses;
        private final ArrayList<OperationTask> operationTasks;
        private final ArrayList<BillingRecord> billingRecords;

        private SnapshotData(
                ArrayList<Room> rooms,
                HashMap<Integer, String> roomCustomers,
                HashMap<Integer, String> customerPhones,
                HashMap<Integer, Pair<LocalDate, LocalDate>> bookingDates,
                HashMap<Integer, OperationalStatus> operationalStatuses,
                ArrayList<OperationTask> operationTasks,
                ArrayList<BillingRecord> billingRecords) {
            this.rooms = rooms;
            this.roomCustomers = roomCustomers;
            this.customerPhones = customerPhones;
            this.bookingDates = bookingDates;
            this.operationalStatuses = operationalStatuses;
            this.operationTasks = operationTasks;
            this.billingRecords = billingRecords;
        }
    }

    public static class ReportRow {
        private final Integer roomNumber;
        private final String roomType;
        private final Double price;
        private final OperationalStatus operationalStatus;
        private final String customerName;
        private final String customerPhone;
        private final LocalDate checkInDate;
        private final LocalDate checkOutDate;

        public ReportRow(
                Integer roomNumber,
                String roomType,
                Double price,
                OperationalStatus operationalStatus,
                String customerName,
                String customerPhone,
                LocalDate checkInDate,
                LocalDate checkOutDate) {
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.price = price;
            this.operationalStatus = operationalStatus;
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

        public OperationalStatus getOperationalStatus() {
            return operationalStatus;
        }

        public String getStatusText() {
            return operationalStatus == null ? "-" : operationalStatus.getDisplayName();
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

    private final ArrayList<Room> rooms = new ArrayList<>();
    private final HashMap<Integer, String> roomToCustomerMap = new HashMap<>();
    private final HashMap<Integer, String> roomToCustomerPhoneMap = new HashMap<>();
    private final HashMap<Integer, Pair<LocalDate, LocalDate>> roomBookingDatesMap = new HashMap<>();
    private final HashMap<Integer, OperationalStatus> roomOperationalStatusMap = new HashMap<>();
    private final ArrayList<OperationTask> operationTasks = new ArrayList<>();
    private final ArrayList<BillingRecord> billingRecords = new ArrayList<>();
    private Integer nextInvoiceNumber = 1001;
    private Integer nextTaskId = 1;

    public String addRoom(Room room) {
        if (findRoomByNumber(room.getRoomNumber()) != null) {
            return "Room already exists.";
        }

        room.setPrice(room.calculatePrice());
        rooms.add(room);
        roomOperationalStatusMap.put(room.getRoomNumber(), deriveInitialStatus(room.getAvailable()));
        return "Room added successfully.";
    }

    public String displayAllRooms() {
        if (rooms.isEmpty()) {
            return "No rooms found.";
        }

        StringBuilder builder = new StringBuilder();
        Iterator<Room> iterator = rooms.iterator();
        while (iterator.hasNext()) {
            Room room = iterator.next();
            String customerName = roomToCustomerMap.get(room.getRoomNumber());
            String customerPhone = roomToCustomerPhoneMap.get(room.getRoomNumber());
            Pair<LocalDate, LocalDate> bookingDates = roomBookingDatesMap.get(room.getRoomNumber());
            OperationalStatus operationalStatus = getOperationalStatusForRoom(room.getRoomNumber());

            builder.append("Room ")
                    .append(room.getRoomNumber())
                    .append(" | Type: ")
                    .append(room.getRoomType())
                    .append(" | Price: ")
                    .append(String.format("%.2f", room.calculatePrice()))
                    .append(" | Status: ")
                    .append(operationalStatus.getDisplayName());

            if (customerName != null && bookingDates != null && operationalStatus == OperationalStatus.OCCUPIED) {
                builder.append(" | Guest: ")
                        .append(customerName)
                        .append(" | Phone: ")
                        .append(customerPhone == null ? "-" : customerPhone)
                        .append(" | Check-in: ")
                        .append(bookingDates.getFirst().format(DATE_FORMATTER))
                        .append(" | Checkout: ")
                        .append(bookingDates.getSecond().format(DATE_FORMATTER));
            }

            long openTaskCount = operationTasks.stream()
                    .filter(task -> task.getRoomNumber().equals(room.getRoomNumber()))
                    .filter(task -> task.getTaskState() != TaskState.COMPLETED)
                    .count();

            builder.append(" | Open Tasks: ")
                    .append(openTaskCount)
                    .append(" | Amenities: ")
                    .append(room.getAmenities())
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    public String displayBookings() {
        if (roomToCustomerMap.isEmpty()) {
            return "No active bookings.";
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, String> entry : roomToCustomerMap.entrySet()) {
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
        int count = 0;
        for (Room room : rooms) {
            if (getOperationalStatusForRoom(room.getRoomNumber()).isSellable()) {
                count++;
            }
        }
        return count;
    }

    public synchronized Integer getOccupiedRoomsCount() {
        int count = 0;
        for (Room room : rooms) {
            if (getOperationalStatusForRoom(room.getRoomNumber()) == OperationalStatus.OCCUPIED) {
                count++;
            }
        }
        return count;
    }

    public synchronized Integer getActiveBookingsCount() {
        return roomToCustomerMap.size();
    }

    public synchronized Integer getOperationalStatusCount(OperationalStatus operationalStatus) {
        int count = 0;
        for (Room room : rooms) {
            if (getOperationalStatusForRoom(room.getRoomNumber()) == operationalStatus) {
                count++;
            }
        }
        return count;
    }

    public synchronized Integer getTaskCount(TaskState taskState) {
        int count = 0;
        for (OperationTask task : operationTasks) {
            if (task.getTaskState() == taskState) {
                count++;
            }
        }
        return count;
    }

    public synchronized Double getOccupancyRate() {
        if (rooms.isEmpty()) {
            return 0.0;
        }
        return (getOccupiedRoomsCount() * 100.0) / getTotalRooms();
    }

    public synchronized ArrayList<ReportRow> getDetailedRoomReport() {
        ArrayList<ReportRow> reportRows = new ArrayList<>();

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
                    getOperationalStatusForRoom(room.getRoomNumber()),
                    customerName,
                    customerPhone,
                    checkInDate,
                    checkOutDate));
        }
        return reportRows;
    }

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

        if (!getOperationalStatusForRoom(roomNumber).isSellable()) {
            return "Room is not available for booking.";
        }

        roomToCustomerMap.put(roomNumber, customerName);
        roomToCustomerPhoneMap.put(roomNumber, customerPhone);
        roomBookingDatesMap.put(roomNumber, new Pair<>(checkInDate, checkOutDate));
        setRoomOperationalStatusInternal(room, OperationalStatus.OCCUPIED);

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

    public synchronized String checkoutRoom(Integer roomNumber) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        if (!roomToCustomerMap.containsKey(roomNumber)) {
            return "Room is already available.";
        }

        roomToCustomerMap.remove(roomNumber);
        roomToCustomerPhoneMap.remove(roomNumber);
        roomBookingDatesMap.remove(roomNumber);
        setRoomOperationalStatusInternal(room, OperationalStatus.DIRTY);
        createAutomaticTask(roomNumber, TaskCategory.HOUSEKEEPING, "Housekeeping Team", "Post-checkout turnover cleaning");
        closeBillingRecord(roomNumber);
        return "Checkout complete. Room " + roomNumber + " marked Dirty and sent to housekeeping queue.";
    }

    public void checkoutRoomAsync(Integer roomNumber, Consumer<String> callback) {
        Thread checkoutThread = new Thread(() -> {
            String result = checkoutRoom(roomNumber);
            callback.accept(result);

            Thread housekeepingDispatchThread = new Thread(() -> {
                try {
                    Thread.sleep(1200);
                    callback.accept("Room " + roomNumber + " is now visible on the housekeeping command board.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
            housekeepingDispatchThread.start();
        });
        checkoutThread.start();
    }

    public synchronized String updateRoomOperationalStatus(Integer roomNumber, OperationalStatus status, String staffName, String notes) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        if (status == null) {
            return "Select an operational status.";
        }

        if (roomToCustomerMap.containsKey(roomNumber) && status != OperationalStatus.OCCUPIED) {
            return "Booked rooms cannot move to " + status.getDisplayName() + " until checkout.";
        }

        setRoomOperationalStatusInternal(room, status);

        String normalizedStaff = staffName == null ? "" : staffName.trim();
        String normalizedNotes = notes == null ? "" : notes.trim();
        if (!normalizedStaff.isEmpty() && !normalizedNotes.isEmpty()) {
            TaskCategory category = status == OperationalStatus.MAINTENANCE || status == OperationalStatus.OUT_OF_ORDER
                    ? TaskCategory.MAINTENANCE
                    : TaskCategory.HOUSEKEEPING;
            createAutomaticTask(roomNumber, category, normalizedStaff, normalizedNotes);
        }

        return "Room " + roomNumber + " updated to " + status.getDisplayName() + ".";
    }

    public synchronized String createOperationTask(Integer roomNumber, TaskCategory category, String assignedTo, String notes) {
        Room room = findRoomByNumber(roomNumber);
        if (room == null) {
            return "Room not found.";
        }

        if (category == null) {
            return "Select a task category.";
        }

        String staffName = assignedTo == null ? "" : assignedTo.trim();
        if (staffName.isEmpty()) {
            return "Enter the staff member name.";
        }

        String taskNotes = notes == null || notes.trim().isEmpty()
                ? defaultTaskNote(category, roomNumber)
                : notes.trim();

        OperationTask task = new OperationTask(
                nextTaskId++,
                roomNumber,
                category,
                staffName,
                taskNotes,
                LocalDateTime.now(),
                TaskState.OPEN,
                null);
        operationTasks.add(task);

        if (!roomToCustomerMap.containsKey(roomNumber)) {
            setRoomOperationalStatusInternal(room, category == TaskCategory.HOUSEKEEPING ? OperationalStatus.CLEANING : OperationalStatus.MAINTENANCE);
        }

        return category.getDisplayName() + " task created for room " + roomNumber + ".";
    }

    public synchronized String markTaskInProgress(Integer taskId) {
        OperationTask task = findTaskById(taskId);
        if (task == null) {
            return "Task not found.";
        }

        if (task.getTaskState() == TaskState.COMPLETED) {
            return "Task is already completed.";
        }

        replaceTask(task.markInProgress());

        Room room = findRoomByNumber(task.getRoomNumber());
        if (room != null && !roomToCustomerMap.containsKey(task.getRoomNumber())) {
            setRoomOperationalStatusInternal(room, task.getCategory() == TaskCategory.HOUSEKEEPING ? OperationalStatus.CLEANING : OperationalStatus.MAINTENANCE);
        }

        return "Task #" + taskId + " moved to In Progress.";
    }

    public synchronized String completeTask(Integer taskId) {
        OperationTask task = findTaskById(taskId);
        if (task == null) {
            return "Task not found.";
        }

        OperationTask completedTask = task.markCompleted();
        replaceTask(completedTask);

        Room room = findRoomByNumber(task.getRoomNumber());
        if (room != null && !roomToCustomerMap.containsKey(task.getRoomNumber())) {
            setRoomOperationalStatusInternal(room, OperationalStatus.VACANT_CLEAN);
        }

        return "Task #" + taskId + " completed at " + completedTask.getCompletedAtText() + ".";
    }

    public synchronized ArrayList<String> getOperationsBoardLines() {
        ArrayList<String> boardLines = new ArrayList<>();
        ArrayList<Room> sortedRooms = new ArrayList<>(rooms);
        sortedRooms.sort(Comparator.comparing(Room::getRoomNumber));

        for (Room room : sortedRooms) {
            OperationalStatus status = getOperationalStatusForRoom(room.getRoomNumber());
            String guestName = roomToCustomerMap.getOrDefault(room.getRoomNumber(), "-");
            long openTasks = operationTasks.stream()
                    .filter(task -> task.getRoomNumber().equals(room.getRoomNumber()))
                    .filter(task -> task.getTaskState() != TaskState.COMPLETED)
                    .count();

            boardLines.add("Room "
                    + room.getRoomNumber()
                    + " | "
                    + room.getRoomType()
                    + " | "
                    + status.getDisplayName()
                    + " | Guest: "
                    + guestName
                    + " | Open Tasks: "
                    + openTasks);
        }

        return boardLines;
    }

    public synchronized ArrayList<OperationTask> getSortedOperationTasks() {
        ArrayList<OperationTask> sortedTasks = new ArrayList<>(operationTasks);
        sortedTasks.sort(Comparator
                .comparing(OperationTask::getTaskState)
                .thenComparing(OperationTask::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(OperationTask::getTaskId));
        return sortedTasks;
    }

    public String saveRoomsToTextFile(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Room room : rooms) {
                writer.write(room.getRoomNumber() + ","
                        + room.getRoomType() + ","
                        + room.getPrice() + ","
                        + room.getAvailable() + ","
                        + getOperationalStatusForRoom(room.getRoomNumber()).name()
                        + System.lineSeparator());
            }
            return "Room data saved to text file.";
        } catch (IOException exception) {
            return "Error saving text file: " + exception.getMessage();
        }
    }

    public String loadRoomsFromTextFile(String filePath) {
        try (FileReader reader = new FileReader(filePath); BufferedReader bufferedReader = new BufferedReader(reader)) {
            rooms.clear();
            roomToCustomerMap.clear();
            roomToCustomerPhoneMap.clear();
            roomBookingDatesMap.clear();
            roomOperationalStatusMap.clear();
            operationTasks.clear();
            billingRecords.clear();
            nextInvoiceNumber = 1001;
            nextTaskId = 1;

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    continue;
                }

                Integer roomNumber = Integer.valueOf(parts[0].trim());
                RoomType roomType = RoomType.valueOf(parts[1].trim());
                Double price = Double.valueOf(parts[2].trim());
                Boolean available = Boolean.valueOf(parts[3].trim());
                OperationalStatus operationalStatus = parts.length >= 5
                        ? OperationalStatus.valueOf(parts[4].trim())
                        : deriveInitialStatus(available);

                Room room = createRoomByType(roomType, roomNumber, price, available);
                rooms.add(room);
                setRoomOperationalStatusInternal(room, operationalStatus);
            }
            return "Room data loaded from text file.";
        } catch (IOException | IllegalArgumentException exception) {
            return "Error loading text file: " + exception.getMessage();
        }
    }

    public String saveRoomsSerialized(String filePath) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(filePath))) {
            outputStream.writeObject(new SnapshotData(
                    getRoomsSnapshot(),
                    getRoomToCustomerSnapshot(),
                    getCustomerPhoneSnapshot(),
                    getBookingDatesSnapshot(),
                    getOperationalStatusSnapshot(),
                    getOperationTasksSnapshot(),
                    getBillingRecordsSnapshot()));
            return "Room objects serialized successfully.";
        } catch (IOException exception) {
            return "Error in serialization save: " + exception.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    public String loadRoomsSerialized(String filePath) {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filePath))) {
            Object object = inputStream.readObject();
            if (object instanceof SnapshotData snapshotData) {
                loadSnapshot(
                        snapshotData.rooms,
                        snapshotData.roomCustomers,
                        snapshotData.customerPhones,
                        snapshotData.bookingDates,
                        snapshotData.operationalStatuses,
                        snapshotData.operationTasks,
                        snapshotData.billingRecords);
                return "Room objects loaded from serialized file.";
            }

            if (object instanceof ArrayList<?>) {
                rooms.clear();
                roomToCustomerMap.clear();
                roomToCustomerPhoneMap.clear();
                roomBookingDatesMap.clear();
                roomOperationalStatusMap.clear();
                operationTasks.clear();
                billingRecords.clear();
                nextInvoiceNumber = 1001;
                nextTaskId = 1;

                for (Object item : (ArrayList<?>) object) {
                    if (item instanceof Room room) {
                        rooms.add(room);
                        setRoomOperationalStatusInternal(room, deriveInitialStatus(room.getAvailable()));
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
            snapshot.add(createRoomByType(room.getRoomType(), room.getRoomNumber(), room.getPrice(), room.getAvailable()));
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

    public synchronized HashMap<Integer, OperationalStatus> getOperationalStatusSnapshot() {
        return new HashMap<>(roomOperationalStatusMap);
    }

    public synchronized ArrayList<OperationTask> getOperationTasksSnapshot() {
        return new ArrayList<>(operationTasks);
    }

    public synchronized ArrayList<BillingRecord> getBillingRecordsSnapshot() {
        return new ArrayList<>(billingRecords);
    }

    public synchronized void loadSnapshot(
            ArrayList<Room> loadedRooms,
            HashMap<Integer, String> loadedRoomCustomers,
            HashMap<Integer, String> loadedCustomerPhones,
            HashMap<Integer, Pair<LocalDate, LocalDate>> loadedBookingDates,
            HashMap<Integer, OperationalStatus> loadedOperationalStatuses,
            ArrayList<OperationTask> loadedOperationTasks,
            ArrayList<BillingRecord> loadedBillingRecords) {
        rooms.clear();
        rooms.addAll(loadedRooms);

        roomToCustomerMap.clear();
        roomToCustomerMap.putAll(loadedRoomCustomers);

        roomToCustomerPhoneMap.clear();
        roomToCustomerPhoneMap.putAll(loadedCustomerPhones);

        roomBookingDatesMap.clear();
        roomBookingDatesMap.putAll(loadedBookingDates);

        roomOperationalStatusMap.clear();
        roomOperationalStatusMap.putAll(loadedOperationalStatuses);
        for (Room room : rooms) {
            setRoomOperationalStatusInternal(room, roomOperationalStatusMap.getOrDefault(room.getRoomNumber(), deriveInitialStatus(room.getAvailable())));
        }

        operationTasks.clear();
        operationTasks.addAll(loadedOperationTasks);

        billingRecords.clear();
        billingRecords.addAll(loadedBillingRecords);

        updateInvoiceCounter();
        updateTaskCounter();
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
                    // Keep safe default when invoice numbers are malformed.
                }
            }
        }
        nextInvoiceNumber = maxInvoiceNumber + 1;
    }

    private void updateTaskCounter() {
        int maxTaskId = 0;
        for (OperationTask task : operationTasks) {
            if (task.getTaskId() != null) {
                maxTaskId = Math.max(maxTaskId, task.getTaskId());
            }
        }
        nextTaskId = maxTaskId + 1;
    }

    private OperationalStatus deriveInitialStatus(Boolean available) {
        return Boolean.TRUE.equals(available) ? OperationalStatus.VACANT_CLEAN : OperationalStatus.OUT_OF_ORDER;
    }

    private OperationalStatus getOperationalStatusForRoom(Integer roomNumber) {
        return roomOperationalStatusMap.getOrDefault(roomNumber, OperationalStatus.VACANT_CLEAN);
    }

    private void setRoomOperationalStatusInternal(Room room, OperationalStatus status) {
        roomOperationalStatusMap.put(room.getRoomNumber(), status);
        room.setAvailable(status.isSellable());
    }

    private void createAutomaticTask(Integer roomNumber, TaskCategory category, String assignedTo, String notes) {
        operationTasks.add(new OperationTask(
                nextTaskId++,
                roomNumber,
                category,
                assignedTo,
                notes,
                LocalDateTime.now(),
                TaskState.OPEN,
                null));
    }

    private String defaultTaskNote(TaskCategory category, Integer roomNumber) {
        return category == TaskCategory.HOUSEKEEPING
                ? "Turnover cleaning scheduled for room " + roomNumber
                : "Maintenance inspection scheduled for room " + roomNumber;
    }

    private OperationTask findTaskById(Integer taskId) {
        for (OperationTask task : operationTasks) {
            if (task.getTaskId().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    private void replaceTask(OperationTask updatedTask) {
        for (int index = 0; index < operationTasks.size(); index++) {
            if (operationTasks.get(index).getTaskId().equals(updatedTask.getTaskId())) {
                operationTasks.set(index, updatedTask);
                return;
            }
        }
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

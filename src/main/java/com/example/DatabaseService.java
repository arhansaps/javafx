package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class DatabaseService {
    private DatabaseService() {
    }

    public static String saveSnapshot(BookingSystem bookingSystem, String databaseFile) {
        String jdbcUrl = "jdbc:sqlite:" + databaseFile;

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            connection.setAutoCommit(false);
            createSchema(connection);
            clearTables(connection);

            ArrayList<Room> rooms = bookingSystem.getRoomsSnapshot();
            HashMap<Integer, String> bookings = bookingSystem.getRoomToCustomerSnapshot();
            HashMap<Integer, String> customerPhones = bookingSystem.getCustomerPhoneSnapshot();
            HashMap<Integer, Pair<LocalDate, LocalDate>> bookingDates = bookingSystem.getBookingDatesSnapshot();
            ArrayList<BillingRecord> billingRecords = bookingSystem.getBillingRecordsSnapshot();

            saveRooms(connection, rooms);
            saveBookings(connection, bookings, customerPhones, bookingDates);
            saveBillingRecords(connection, billingRecords);

            connection.commit();
            return "Data saved to JDBC database successfully.";
        } catch (SQLException exception) {
            return "Error saving JDBC data: " + exception.getMessage();
        }
    }

    public static String loadSnapshot(BookingSystem bookingSystem, String databaseFile) {
        String jdbcUrl = "jdbc:sqlite:" + databaseFile;

        try (Connection connection = DriverManager.getConnection(jdbcUrl)) {
            createSchema(connection);

            ArrayList<Room> loadedRooms = loadRooms(connection);
            HashMap<Integer, String> loadedBookings = loadBookings(connection);
            HashMap<Integer, String> loadedCustomerPhones = loadCustomerPhones(connection);
            HashMap<Integer, Pair<LocalDate, LocalDate>> loadedBookingDates = loadBookingDates(connection);
            ArrayList<BillingRecord> loadedBillingRecords = loadBillingRecords(connection);

            bookingSystem.loadSnapshot(loadedRooms, loadedBookings, loadedCustomerPhones, loadedBookingDates, loadedBillingRecords);
            return "Data loaded from JDBC database successfully.";
        } catch (SQLException exception) {
            return "Error loading JDBC data: " + exception.getMessage();
        }
    }

    private static void createSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS rooms (
                        room_number INTEGER PRIMARY KEY,
                        room_type TEXT NOT NULL,
                        price REAL NOT NULL,
                        available INTEGER NOT NULL
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        room_number INTEGER PRIMARY KEY,
                        customer_name TEXT NOT NULL,
                        customer_phone TEXT NOT NULL DEFAULT '-',
                        check_in_date TEXT,
                        check_out_date TEXT
                    )
                    """);

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS billing_records (
                        invoice_number TEXT PRIMARY KEY,
                        room_number INTEGER NOT NULL,
                        customer_name TEXT NOT NULL,
                        room_type TEXT NOT NULL,
                        check_in_date TEXT,
                        check_out_date TEXT,
                        nights INTEGER NOT NULL,
                        room_charge REAL NOT NULL,
                        service_charge REAL NOT NULL,
                        total_amount REAL NOT NULL,
                        billing_status TEXT NOT NULL,
                        payment_method TEXT NOT NULL DEFAULT '-',
                        payment_status TEXT NOT NULL DEFAULT 'Pending',
                        payment_reference TEXT NOT NULL DEFAULT '-',
                        generated_on TEXT
                    )
                    """);
        }

        ensureColumn(connection, "bookings", "customer_phone", "TEXT NOT NULL DEFAULT '-'");
        ensureColumn(connection, "billing_records", "payment_method", "TEXT NOT NULL DEFAULT '-'");
        ensureColumn(connection, "billing_records", "payment_status", "TEXT NOT NULL DEFAULT 'Pending'");
        ensureColumn(connection, "billing_records", "payment_reference", "TEXT NOT NULL DEFAULT '-'");
    }

    private static void clearTables(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM billing_records");
            statement.executeUpdate("DELETE FROM bookings");
            statement.executeUpdate("DELETE FROM rooms");
        }
    }

    private static void saveRooms(Connection connection, ArrayList<Room> rooms) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO rooms (room_number, room_type, price, available) VALUES (?, ?, ?, ?)")) {
            for (Room room : rooms) {
                statement.setInt(1, room.getRoomNumber());
                statement.setString(2, room.getRoomType().name());
                statement.setDouble(3, room.getPrice());
                statement.setInt(4, Boolean.TRUE.equals(room.getAvailable()) ? 1 : 0);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void saveBookings(
            Connection connection,
            HashMap<Integer, String> bookings,
            HashMap<Integer, String> customerPhones,
            HashMap<Integer, Pair<LocalDate, LocalDate>> bookingDates) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO bookings (room_number, customer_name, customer_phone, check_in_date, check_out_date) VALUES (?, ?, ?, ?, ?)")) {
            for (Integer roomNumber : bookings.keySet()) {
                Pair<LocalDate, LocalDate> dates = bookingDates.get(roomNumber);
                statement.setInt(1, roomNumber);
                statement.setString(2, bookings.get(roomNumber));
                statement.setString(3, customerPhones.getOrDefault(roomNumber, "-"));
                statement.setString(4, dates == null || dates.getFirst() == null ? null : dates.getFirst().toString());
                statement.setString(5, dates == null || dates.getSecond() == null ? null : dates.getSecond().toString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void saveBillingRecords(Connection connection, ArrayList<BillingRecord> billingRecords) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO billing_records (
                    invoice_number,
                    room_number,
                    customer_name,
                    room_type,
                    check_in_date,
                    check_out_date,
                    nights,
                    room_charge,
                    service_charge,
                    total_amount,
                    billing_status,
                    payment_method,
                    payment_status,
                    payment_reference,
                    generated_on
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """)) {
            for (BillingRecord record : billingRecords) {
                statement.setString(1, record.getInvoiceNumber());
                statement.setInt(2, record.getRoomNumber());
                statement.setString(3, record.getCustomerName());
                statement.setString(4, record.getRoomType());
                statement.setString(5, record.getCheckInDate() == null ? null : record.getCheckInDate().toString());
                statement.setString(6, record.getCheckOutDate() == null ? null : record.getCheckOutDate().toString());
                statement.setInt(7, record.getNights());
                statement.setDouble(8, record.getRoomCharge());
                statement.setDouble(9, record.getServiceCharge());
                statement.setDouble(10, record.getTotalAmount());
                statement.setString(11, record.getBillingStatus());
                statement.setString(12, record.getPaymentMethod());
                statement.setString(13, record.getPaymentStatus());
                statement.setString(14, record.getPaymentReference());
                statement.setString(15, record.getGeneratedOn() == null ? null : record.getGeneratedOn().toString());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static ArrayList<Room> loadRooms(Connection connection) throws SQLException {
        ArrayList<Room> loadedRooms = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT room_number, room_type, price, available FROM rooms ORDER BY room_number")) {
            while (resultSet.next()) {
                Integer roomNumber = resultSet.getInt("room_number");
                RoomType roomType = RoomType.valueOf(resultSet.getString("room_type"));
                Double price = resultSet.getDouble("price");
                Boolean available = resultSet.getInt("available") == 1;

                Room room = createRoomByType(roomType, roomNumber, price, available);
                loadedRooms.add(room);
            }
        }

        return loadedRooms;
    }

    private static HashMap<Integer, String> loadBookings(Connection connection) throws SQLException {
        HashMap<Integer, String> loadedBookings = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT room_number, customer_name FROM bookings")) {
            while (resultSet.next()) {
                loadedBookings.put(resultSet.getInt("room_number"), resultSet.getString("customer_name"));
            }
        }

        return loadedBookings;
    }

    private static HashMap<Integer, String> loadCustomerPhones(Connection connection) throws SQLException {
        HashMap<Integer, String> loadedCustomerPhones = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT room_number, customer_phone FROM bookings")) {
            while (resultSet.next()) {
                loadedCustomerPhones.put(resultSet.getInt("room_number"), resultSet.getString("customer_phone"));
            }
        }

        return loadedCustomerPhones;
    }

    private static HashMap<Integer, Pair<LocalDate, LocalDate>> loadBookingDates(Connection connection) throws SQLException {
        HashMap<Integer, Pair<LocalDate, LocalDate>> loadedBookingDates = new HashMap<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT room_number, check_in_date, check_out_date FROM bookings")) {
            while (resultSet.next()) {
                LocalDate checkInDate = parseDate(resultSet.getString("check_in_date"));
                LocalDate checkOutDate = parseDate(resultSet.getString("check_out_date"));
                loadedBookingDates.put(resultSet.getInt("room_number"), new Pair<>(checkInDate, checkOutDate));
            }
        }

        return loadedBookingDates;
    }

    private static ArrayList<BillingRecord> loadBillingRecords(Connection connection) throws SQLException {
        ArrayList<BillingRecord> loadedBillingRecords = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("""
                     SELECT invoice_number, room_number, customer_name, room_type, check_in_date, check_out_date,
                            nights, room_charge, service_charge, total_amount, billing_status, payment_method,
                            payment_status, payment_reference, generated_on
                     FROM billing_records
                     ORDER BY generated_on DESC, invoice_number DESC
                     """)) {
            while (resultSet.next()) {
                loadedBillingRecords.add(new BillingRecord(
                        resultSet.getString("invoice_number"),
                        resultSet.getInt("room_number"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("room_type"),
                        parseDate(resultSet.getString("check_in_date")),
                        parseDate(resultSet.getString("check_out_date")),
                        resultSet.getInt("nights"),
                        resultSet.getDouble("room_charge"),
                        resultSet.getDouble("service_charge"),
                        resultSet.getDouble("total_amount"),
                        resultSet.getString("billing_status"),
                        resultSet.getString("payment_method"),
                        resultSet.getString("payment_status"),
                        resultSet.getString("payment_reference"),
                        parseDate(resultSet.getString("generated_on"))));
            }
        }

        return loadedBillingRecords;
    }

    private static void ensureColumn(Connection connection, String tableName, String columnName, String definition) throws SQLException {
        Set<String> columnNames = new HashSet<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                columnNames.add(resultSet.getString("name"));
            }
        }

        if (!columnNames.contains(columnName)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
            }
        }
    }

    private static Room createRoomByType(RoomType roomType, Integer roomNumber, Double price, Boolean available) {
        return switch (roomType) {
            case STANDARD -> new StandardRoom(roomNumber, price, available);
            case DELUXE -> new DeluxeRoom(roomNumber, price, available);
            case SUITE -> new SuiteRoom(roomNumber, price, available);
            case VILLA -> new VillaRoom(roomNumber, price, available);
        };
    }

    private static LocalDate parseDate(String dateValue) {
        return dateValue == null || dateValue.isBlank() ? null : LocalDate.parse(dateValue);
    }
}

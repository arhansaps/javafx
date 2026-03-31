package com.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static final String TEXT_FILE = "rooms-data.txt";
    private static final String SERIALIZED_FILE = "rooms-data.ser";
    private static final String DATABASE_FILE = "hotel-data.db";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final BookingSystem bookingSystem = new BookingSystem();
    private final Label statusLabel = new Label("Status: Application started. Add rooms to begin.");
    private final ObservableList<String> activityItems = FXCollections.observableArrayList();

    // Home dashboard counters
    private final Label totalRoomsValue = new Label("0");
    private final Label availableRoomsValue = new Label("0");
    private final Label occupiedRoomsValue = new Label("0");
    private final Label activeBookingsValue = new Label("0");

    // Reports dashboard counters
    private final Label reportTotalRoomsValue = new Label("0");
    private final Label reportAvailableRoomsValue = new Label("0");
    private final Label reportOccupiedRoomsValue = new Label("0");
    private final Label reportOccupancyRateValue = new Label("0.00%");

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("Hotel Management System");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Professional dashboard for room and customer operations");
        subtitleLabel.getStyleClass().add("subtitle-label");

        statusLabel.getStyleClass().add("status-label");

        ListView<String> roomListView = new ListView<>();
        roomListView.setPlaceholder(new Label("No rooms to display."));
        roomListView.setPrefHeight(320);

        ListView<String> bookingListView = new ListView<>();
        bookingListView.setPlaceholder(new Label("No bookings to display."));
        bookingListView.setPrefHeight(220);

        ListView<String> activityListView = new ListView<>(activityItems);
        activityListView.setPlaceholder(new Label("Activity updates will appear here."));
        activityListView.setPrefHeight(220);

        GridPane roomGrid = new GridPane();
        roomGrid.getStyleClass().add("section-card");
        roomGrid.setHgap(10);
        roomGrid.setVgap(10);
        roomGrid.setMaxWidth(Double.MAX_VALUE);

        Label roomNumberLabel = new Label("Room Number:");
        TextField roomNumberField = new TextField();

        Label roomTypeLabel = new Label("Room Type:");
        ComboBox<RoomType> roomTypeCombo = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        roomTypeCombo.setValue(RoomType.STANDARD);

        Label priceLabel = new Label("Price:");
        TextField priceField = new TextField();
        priceField.setPromptText("Leave empty for default");

        Label availabilityLabel = new Label("Availability:");
        ComboBox<String> availabilityCombo = new ComboBox<>(FXCollections.observableArrayList("Available", "Not Available"));
        availabilityCombo.setValue("Available");

        Button addRoomButton = new Button("Add Room");
        Button displayRoomsButton = new Button("Display All Rooms");
        addRoomButton.getStyleClass().add("primary-button");
        displayRoomsButton.getStyleClass().add("secondary-button");

        roomGrid.add(roomNumberLabel, 0, 0);
        roomGrid.add(roomNumberField, 1, 0);
        roomGrid.add(roomTypeLabel, 0, 1);
        roomGrid.add(roomTypeCombo, 1, 1);
        roomGrid.add(priceLabel, 0, 2);
        roomGrid.add(priceField, 1, 2);
        roomGrid.add(availabilityLabel, 0, 3);
        roomGrid.add(availabilityCombo, 1, 3);
        roomGrid.add(addRoomButton, 0, 4);
        roomGrid.add(displayRoomsButton, 1, 4);

        Label roomListTitle = new Label("Rooms Overview");
        roomListTitle.getStyleClass().add("section-title");
        VBox roomDisplayBox = new VBox(8, roomListTitle, roomListView);
        roomDisplayBox.getStyleClass().add("section-card");
        roomDisplayBox.setMaxWidth(Double.MAX_VALUE);

        GridPane bookingGrid = new GridPane();
        bookingGrid.getStyleClass().add("section-card");
        bookingGrid.setHgap(10);
        bookingGrid.setVgap(10);
        bookingGrid.setMaxWidth(Double.MAX_VALUE);

        Label customerNameLabel = new Label("Customer Name:");
        TextField customerNameField = new TextField();

        Label bookingRoomLabel = new Label("Room Number:");
        TextField bookingRoomField = new TextField();

        Label checkInLabel = new Label("Check-in Date:");
        DatePicker checkInPicker = new DatePicker();

        Label checkOutLabel = new Label("Checkout Date:");
        DatePicker checkOutPicker = new DatePicker();

        Button bookButton = new Button("Book Room");
        Button checkoutButton = new Button("Checkout Room");
        Button showBookingsButton = new Button("Show Bookings");
        bookButton.getStyleClass().add("primary-button");
        checkoutButton.getStyleClass().add("warning-button");
        showBookingsButton.getStyleClass().add("secondary-button");

        bookingGrid.add(customerNameLabel, 0, 0);
        bookingGrid.add(customerNameField, 1, 0);
        bookingGrid.add(bookingRoomLabel, 0, 1);
        bookingGrid.add(bookingRoomField, 1, 1);
        bookingGrid.add(checkInLabel, 0, 2);
        bookingGrid.add(checkInPicker, 1, 2);
        bookingGrid.add(checkOutLabel, 0, 3);
        bookingGrid.add(checkOutPicker, 1, 3);
        bookingGrid.add(bookButton, 0, 4);
        bookingGrid.add(checkoutButton, 1, 4);
        bookingGrid.add(showBookingsButton, 0, 5);

        Label bookingListTitle = new Label("Active Bookings (Date-wise)");
        bookingListTitle.getStyleClass().add("section-title");
        VBox bookingDisplayBox = new VBox(8, bookingListTitle, bookingListView);
        bookingDisplayBox.getStyleClass().add("section-card");
        bookingDisplayBox.setMaxWidth(Double.MAX_VALUE);

        GridPane fileGrid = new GridPane();
        fileGrid.getStyleClass().add("section-card");
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);
        fileGrid.setMaxWidth(Double.MAX_VALUE);

        Button saveTextButton = new Button("Save (Text)");
        Button loadTextButton = new Button("Load (Text)");
        Button saveSerializedButton = new Button("Save (Serialized)");
        Button loadSerializedButton = new Button("Load (Serialized)");
        Button saveJdbcButton = new Button("Save (JDBC)");
        Button loadJdbcButton = new Button("Load (JDBC)");
        saveTextButton.getStyleClass().add("secondary-button");
        loadTextButton.getStyleClass().add("secondary-button");
        saveSerializedButton.getStyleClass().add("secondary-button");
        loadSerializedButton.getStyleClass().add("secondary-button");
        saveJdbcButton.getStyleClass().add("secondary-button");
        loadJdbcButton.getStyleClass().add("secondary-button");

        fileGrid.add(saveTextButton, 0, 0);
        fileGrid.add(loadTextButton, 1, 0);
        fileGrid.add(saveSerializedButton, 0, 1);
        fileGrid.add(loadSerializedButton, 1, 1);
        fileGrid.add(saveJdbcButton, 0, 2);
        fileGrid.add(loadJdbcButton, 1, 2);

        Label dashboardTitle = new Label("Dashboard Home");
        dashboardTitle.getStyleClass().add("section-title");

        Label dashboardHint = new Label("Track room availability in real time and navigate using action cards.");
        dashboardHint.getStyleClass().add("dashboard-hint");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(10);
        statsGrid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints statsLeftColumn = new ColumnConstraints();
        statsLeftColumn.setHgrow(Priority.ALWAYS);
        statsLeftColumn.setFillWidth(true);
        statsLeftColumn.setPercentWidth(50);
        ColumnConstraints statsRightColumn = new ColumnConstraints();
        statsRightColumn.setHgrow(Priority.ALWAYS);
        statsRightColumn.setFillWidth(true);
        statsRightColumn.setPercentWidth(50);
        statsGrid.getColumnConstraints().addAll(statsLeftColumn, statsRightColumn);
        statsGrid.add(createStatCard("Total Rooms", totalRoomsValue, "stat-blue"), 0, 0);
        statsGrid.add(createStatCard("Available Rooms", availableRoomsValue, "stat-green"), 1, 0);
        statsGrid.add(createStatCard("Occupied Rooms", occupiedRoomsValue, "stat-orange"), 0, 1);
        statsGrid.add(createStatCard("Active Bookings", activeBookingsValue, "stat-purple"), 1, 1);

        VBox statsCard = new VBox(10, new Label("Live Hotel Snapshot"), statsGrid);
        statsCard.getChildren().get(0).getStyleClass().add("section-title");
        statsCard.getStyleClass().add("section-card");
        statsCard.setMaxWidth(Double.MAX_VALUE);

        Button roomManagementButton = new Button("Room Management");
        Button customerManagementButton = new Button("Customer Management");
        Button bookingManagementButton = new Button("Booking Management");
        Button reportsButton = new Button("Reports & Analytics");

        roomManagementButton.getStyleClass().addAll("quick-button", "quick-room");
        customerManagementButton.getStyleClass().addAll("quick-button", "quick-guest");
        bookingManagementButton.getStyleClass().addAll("quick-button", "quick-booking");
        reportsButton.getStyleClass().addAll("quick-button", "quick-reports");
        roomManagementButton.setMaxWidth(Double.MAX_VALUE);
        customerManagementButton.setMaxWidth(Double.MAX_VALUE);
        bookingManagementButton.setMaxWidth(Double.MAX_VALUE);
        reportsButton.setMaxWidth(Double.MAX_VALUE);

        VBox quickActionsBox = new VBox(
                10,
                new Label("Quick Actions"),
                roomManagementButton,
                customerManagementButton,
                bookingManagementButton,
                reportsButton
        );
        quickActionsBox.getChildren().get(0).getStyleClass().add("section-title");
        quickActionsBox.getStyleClass().add("section-card");
        quickActionsBox.setMaxWidth(Double.MAX_VALUE);

        VBox activityCard = new VBox(8, new Label("Live Activity"), activityListView);
        activityCard.getChildren().get(0).getStyleClass().add("section-title");
        activityCard.getStyleClass().add("section-card");
        activityCard.setMaxWidth(Double.MAX_VALUE);

        GridPane homeGrid = new GridPane();
        homeGrid.setHgap(12);
        homeGrid.setVgap(12);
        homeGrid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints homeLeftColumn = new ColumnConstraints();
        homeLeftColumn.setPercentWidth(40);
        homeLeftColumn.setHgrow(Priority.ALWAYS);
        homeLeftColumn.setFillWidth(true);
        ColumnConstraints homeRightColumn = new ColumnConstraints();
        homeRightColumn.setPercentWidth(60);
        homeRightColumn.setHgrow(Priority.ALWAYS);
        homeRightColumn.setFillWidth(true);
        homeGrid.getColumnConstraints().addAll(homeLeftColumn, homeRightColumn);
        homeGrid.add(quickActionsBox, 0, 0, 1, 2);
        homeGrid.add(statsCard, 1, 0);
        homeGrid.add(activityCard, 1, 1);
        GridPane.setHgrow(quickActionsBox, Priority.ALWAYS);
        GridPane.setVgrow(quickActionsBox, Priority.ALWAYS);
        GridPane.setHgrow(statsCard, Priority.ALWAYS);
        GridPane.setHgrow(activityCard, Priority.ALWAYS);

        VBox homeTabContent = new VBox(12, dashboardTitle, dashboardHint, homeGrid);
        homeTabContent.setPadding(new Insets(12));
        homeTabContent.setFillWidth(true);
        homeTabContent.setMaxWidth(Double.MAX_VALUE);

        Label roomSectionTitle = new Label("Room Management");
        roomSectionTitle.getStyleClass().add("section-title");
        Label fileSectionTitle = new Label("File, Serialization and JDBC");
        fileSectionTitle.getStyleClass().add("section-title");

        VBox roomTabContent = new VBox(12, roomSectionTitle, roomGrid, roomDisplayBox, fileSectionTitle, fileGrid);
        roomTabContent.setPadding(new Insets(12));
        roomTabContent.setFillWidth(true);
        roomTabContent.setMaxWidth(Double.MAX_VALUE);

        ScrollPane roomScrollPane = new ScrollPane(roomTabContent);
        roomScrollPane.setFitToWidth(true);
        roomScrollPane.getStyleClass().add("content-scroll");

        Label customerSectionTitle = new Label("Customer Booking");
        customerSectionTitle.getStyleClass().add("section-title");

        VBox customerTabContent = new VBox(12, customerSectionTitle, bookingGrid, bookingDisplayBox);
        customerTabContent.setPadding(new Insets(12));
        customerTabContent.setFillWidth(true);
        customerTabContent.setMaxWidth(Double.MAX_VALUE);

        Label reportsTitle = new Label("Reports & Analytics");
        reportsTitle.getStyleClass().add("section-title");

        Label reportsHint = new Label("Comprehensive report including room status, customer allocation, check-in, and checkout dates.");
        reportsHint.getStyleClass().add("dashboard-hint");

        GridPane reportSnapshotGrid = new GridPane();
        reportSnapshotGrid.setHgap(10);
        reportSnapshotGrid.setVgap(10);
        reportSnapshotGrid.setMaxWidth(Double.MAX_VALUE);
        for (int index = 0; index < 4; index++) {
            ColumnConstraints reportColumn = new ColumnConstraints();
            reportColumn.setPercentWidth(25);
            reportColumn.setHgrow(Priority.ALWAYS);
            reportColumn.setFillWidth(true);
            reportSnapshotGrid.getColumnConstraints().add(reportColumn);
        }
        reportSnapshotGrid.add(createStatCard("Total Rooms", reportTotalRoomsValue, "stat-blue"), 0, 0);
        reportSnapshotGrid.add(createStatCard("Available Rooms", reportAvailableRoomsValue, "stat-green"), 1, 0);
        reportSnapshotGrid.add(createStatCard("Occupied Rooms", reportOccupiedRoomsValue, "stat-orange"), 2, 0);
        reportSnapshotGrid.add(createStatCard("Occupancy Rate", reportOccupancyRateValue, "stat-purple"), 3, 0);

        VBox reportSnapshotCard = new VBox(8, new Label("Report Snapshot"), reportSnapshotGrid);
        reportSnapshotCard.getChildren().get(0).getStyleClass().add("section-title");
        reportSnapshotCard.getStyleClass().add("section-card");
        reportSnapshotCard.setMaxWidth(Double.MAX_VALUE);

        GridPane reportTableGrid = new GridPane();
        reportTableGrid.getStyleClass().add("report-grid");

        ScrollPane reportScrollPane = new ScrollPane(reportTableGrid);
        reportScrollPane.setFitToWidth(true);
        reportScrollPane.setPrefHeight(320);
        reportScrollPane.getStyleClass().add("report-scroll");

        VBox reportTableCard = new VBox(8, new Label("Detailed Room Allocation Report"), reportScrollPane);
        reportTableCard.getChildren().get(0).getStyleClass().add("section-title");
        reportTableCard.getStyleClass().add("section-card");
        reportTableCard.setMaxWidth(Double.MAX_VALUE);

        Button refreshReportButton = new Button("Refresh Report");
        refreshReportButton.getStyleClass().add("secondary-button");

        VBox reportsTabContent = new VBox(12, reportsTitle, reportsHint, reportSnapshotCard, reportTableCard, refreshReportButton);
        reportsTabContent.setPadding(new Insets(12));
        reportsTabContent.setFillWidth(true);
        reportsTabContent.setMaxWidth(Double.MAX_VALUE);

        FXMLLoader billingLoader = new FXMLLoader(getClass().getResource("/billing-view.fxml"));
        VBox billingTabContent;
        BillingController billingController;
        try {
            billingTabContent = billingLoader.load();
            billingController = billingLoader.getController();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load billing FXML view.", exception);
        }

        billingTabContent.setFillWidth(true);
        billingTabContent.setMaxWidth(Double.MAX_VALUE);
        billingController.configure(bookingSystem, this::updateStatus);

        Tab homeTab = new Tab("Home", homeTabContent);
        homeTab.setClosable(false);

        Tab roomTab = new Tab("Rooms", roomScrollPane);
        roomTab.setClosable(false);

        Tab customerTab = new Tab("Customers", customerTabContent);
        customerTab.setClosable(false);

        Tab reportsTab = new Tab("Reports", reportsTabContent);
        reportsTab.setClosable(false);

        Tab billingTab = new Tab("Billing", billingTabContent);
        billingTab.setClosable(false);

        TabPane tabPane = new TabPane(homeTab, roomTab, customerTab, reportsTab, billingTab);

        Runnable refreshAllViews = () -> {
            refreshAllSections(roomListView, bookingListView, reportTableGrid);
            billingController.refreshBillingData();
        };

        addRoomButton.setOnAction(event -> {
            Integer roomNumber = parseInteger(roomNumberField.getText(), "Room Number");
            if (roomNumber == null) {
                return;
            }

            RoomType roomType = roomTypeCombo.getValue();
            if (roomType == null) {
                updateStatus("Select a room type.");
                return;
            }

            Double price = parsePriceWithDefault(priceField.getText(), roomType.getBasePrice());
            if (price == null) {
                return;
            }

            Boolean available = "Available".equals(availabilityCombo.getValue());

            Room room;
            if (roomType == RoomType.STANDARD) {
                room = new StandardRoom(roomNumber, price, available);
            } else {
                room = new DeluxeRoom(roomNumber, price, available);
            }

            updateStatus(bookingSystem.addRoom(room));
            roomNumberField.clear();
            priceField.clear();
            refreshAllViews.run();
        });

        displayRoomsButton.setOnAction(event -> {
            refreshAllViews.run();
            updateStatus("Room list refreshed.");
        });

        bookButton.setOnAction(event -> {
            String customerName = customerNameField.getText().trim();
            if (customerName.isEmpty()) {
                updateStatus("Enter customer name.");
                return;
            }

            Integer roomNumber = parseInteger(bookingRoomField.getText(), "Room Number");
            if (roomNumber == null) {
                return;
            }

            LocalDate checkInDate = checkInPicker.getValue();
            LocalDate checkOutDate = checkOutPicker.getValue();
            if (checkInDate == null || checkOutDate == null) {
                updateStatus("Select both check-in and checkout dates.");
                return;
            }

            bookingSystem.bookRoomAsync(customerName, roomNumber, checkInDate, checkOutDate, message ->
                    Platform.runLater(() -> {
                        updateStatus(message);
                        refreshAllViews.run();

                        if (message.startsWith("Booking successful")) {
                            customerNameField.clear();
                            bookingRoomField.clear();
                            checkInPicker.setValue(null);
                            checkOutPicker.setValue(null);
                        }
                    }));
        });

        checkoutButton.setOnAction(event -> {
            Integer roomNumber = parseInteger(bookingRoomField.getText(), "Room Number");
            if (roomNumber == null) {
                return;
            }

            bookingSystem.checkoutRoomAsync(roomNumber, message ->
                    Platform.runLater(() -> {
                        updateStatus(message);
                        refreshAllViews.run();
                    }));
        });

        showBookingsButton.setOnAction(event -> {
            refreshAllViews.run();
            updateStatus("Booking list refreshed.");
        });

        saveTextButton.setOnAction(event -> {
            updateStatus(bookingSystem.saveRoomsToTextFile(TEXT_FILE));
            refreshAllViews.run();
        });

        loadTextButton.setOnAction(event -> {
            updateStatus(bookingSystem.loadRoomsFromTextFile(TEXT_FILE));
            refreshAllViews.run();
        });

        saveSerializedButton.setOnAction(event -> {
            updateStatus(bookingSystem.saveRoomsSerialized(SERIALIZED_FILE));
            refreshAllViews.run();
        });

        loadSerializedButton.setOnAction(event -> {
            updateStatus(bookingSystem.loadRoomsSerialized(SERIALIZED_FILE));
            refreshAllViews.run();
        });

        saveJdbcButton.setOnAction(event -> {
            updateStatus(DatabaseService.saveSnapshot(bookingSystem, DATABASE_FILE));
            refreshAllViews.run();
        });

        loadJdbcButton.setOnAction(event -> {
            updateStatus(DatabaseService.loadSnapshot(bookingSystem, DATABASE_FILE));
            refreshAllViews.run();
        });

        refreshReportButton.setOnAction(event -> {
            refreshAllViews.run();
            updateStatus("Comprehensive report refreshed.");
        });

        roomManagementButton.setOnAction(event -> tabPane.getSelectionModel().select(roomTab));
        customerManagementButton.setOnAction(event -> tabPane.getSelectionModel().select(customerTab));
        bookingManagementButton.setOnAction(event -> tabPane.getSelectionModel().select(customerTab));
        reportsButton.setOnAction(event -> {
            tabPane.getSelectionModel().select(reportsTab);
            refreshAllViews.run();
            updateStatus("Reports and analytics loaded.");
        });

        VBox root = new VBox(12, titleLabel, subtitleLabel, statusLabel, tabPane);
        root.setPadding(new Insets(16));
        root.setFillWidth(true);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 1360, 860);
        // CSS styling applied here
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Hotel Management System - Dashboard");
        stage.setScene(scene);
        stage.show();

        refreshAllViews.run();
        updateStatus("Dashboard ready. Use quick actions or tabs to manage operations.");
    }

    private VBox createStatCard(String title, Label valueLabel, String colorClass) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");

        valueLabel.getStyleClass().add("stat-value");

        VBox card = new VBox(6, titleLabel, valueLabel);
        card.getStyleClass().addAll("stat-card", colorClass);
        card.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private Integer parseInteger(String value, String fieldName) {
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException exception) {
            updateStatus("Invalid " + fieldName + ". Enter a number.");
            return null;
        }
    }

    private Double parsePriceWithDefault(String value, Double defaultValue) {
        String text = value.trim();
        if (text.isEmpty()) {
            return defaultValue;
        }

        try {
            return Double.valueOf(text);
        } catch (NumberFormatException exception) {
            updateStatus("Invalid Price. Enter a decimal number.");
            return null;
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);

        String entry = LocalTime.now().format(TIME_FORMATTER) + " - " + message;
        activityItems.add(0, entry);
        if (activityItems.size() > 25) {
            activityItems.remove(activityItems.size() - 1);
        }
    }

    private void refreshAllSections(ListView<String> roomListView, ListView<String> bookingListView, GridPane reportTableGrid) {
        refreshRoomList(roomListView);
        refreshBookingList(bookingListView);
        refreshDashboardCounters();
        refreshReportTable(reportTableGrid);
    }

    private void refreshDashboardCounters() {
        totalRoomsValue.setText(String.valueOf(bookingSystem.getTotalRooms()));
        availableRoomsValue.setText(String.valueOf(bookingSystem.getAvailableRoomsCount()));
        occupiedRoomsValue.setText(String.valueOf(bookingSystem.getOccupiedRoomsCount()));
        activeBookingsValue.setText(String.valueOf(bookingSystem.getActiveBookingsCount()));

        reportTotalRoomsValue.setText(String.valueOf(bookingSystem.getTotalRooms()));
        reportAvailableRoomsValue.setText(String.valueOf(bookingSystem.getAvailableRoomsCount()));
        reportOccupiedRoomsValue.setText(String.valueOf(bookingSystem.getOccupiedRoomsCount()));
        reportOccupancyRateValue.setText(String.format("%.2f%%", bookingSystem.getOccupancyRate()));
    }

    private void refreshRoomList(ListView<String> roomListView) {
        roomListView.getItems().setAll(splitToLines(bookingSystem.displayAllRooms()));
    }

    private void refreshBookingList(ListView<String> bookingListView) {
        bookingListView.getItems().setAll(splitToLines(bookingSystem.displayBookings()));
    }

    private void refreshReportTable(GridPane reportTableGrid) {
        reportTableGrid.getChildren().clear();
        reportTableGrid.setHgap(0);
        reportTableGrid.setVgap(0);

        reportTableGrid.add(createReportHeaderCell("Room", 90.0), 0, 0);
        reportTableGrid.add(createReportHeaderCell("Type", 130.0), 1, 0);
        reportTableGrid.add(createReportHeaderCell("Price", 110.0), 2, 0);
        reportTableGrid.add(createReportHeaderCell("Status", 130.0), 3, 0);
        reportTableGrid.add(createReportHeaderCell("Customer", 190.0), 4, 0);
        reportTableGrid.add(createReportHeaderCell("Check-in", 150.0), 5, 0);
        reportTableGrid.add(createReportHeaderCell("Checkout", 150.0), 6, 0);

        ArrayList<BookingSystem.ReportRow> reportRows = bookingSystem.getDetailedRoomReport();
        if (reportRows.isEmpty()) {
            Label emptyLabel = new Label("No room records found. Add rooms to generate a report.");
            emptyLabel.getStyleClass().add("report-empty");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            reportTableGrid.add(emptyLabel, 0, 1, 7, 1);
            return;
        }

        Integer rowIndex = 1;
        for (BookingSystem.ReportRow reportRow : reportRows) {
            reportTableGrid.add(createReportDataCell(String.valueOf(reportRow.getRoomNumber()), 90.0), 0, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getRoomType(), 130.0), 1, rowIndex);
            reportTableGrid.add(createReportDataCell(String.format("%.2f", reportRow.getPrice()), 110.0), 2, rowIndex);

            String statusClass = Boolean.TRUE.equals(reportRow.getAvailable()) ? "status-available" : "status-occupied";
            reportTableGrid.add(createReportDataCell(reportRow.getStatusText(), 130.0, statusClass), 3, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getCustomerName(), 190.0), 4, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getCheckInText(), 150.0), 5, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getCheckOutText(), 150.0), 6, rowIndex);
            rowIndex++;
        }
    }

    private Label createReportHeaderCell(String text, Double width) {
        Label headerCell = new Label(text);
        headerCell.getStyleClass().add("report-header-cell");
        headerCell.setPrefWidth(width);
        headerCell.setMinWidth(width);
        return headerCell;
    }

    private Label createReportDataCell(String text, Double width, String... additionalStyleClasses) {
        Label dataCell = new Label(text);
        dataCell.getStyleClass().add("report-cell");
        dataCell.setPrefWidth(width);
        dataCell.setMinWidth(width);

        for (String styleClass : additionalStyleClasses) {
            dataCell.getStyleClass().add(styleClass);
        }
        return dataCell;
    }

    private ObservableList<String> splitToLines(String text) {
        ObservableList<String> lines = FXCollections.observableArrayList();
        if (text == null || text.isBlank()) {
            return lines;
        }

        for (String line : text.split(System.lineSeparator())) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                lines.add(trimmed);
            }
        }
        return lines;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

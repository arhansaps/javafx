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
import javafx.scene.layout.HBox;
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
    private final Label headerTotalRoomsValue = new Label("0");
    private final Label headerAvailableRoomsDisplay = new Label("0");
    private final Label headerActiveBookingsDisplay = new Label("0");
    private final Label totalRoomsValue = new Label("0");
    private final Label availableRoomsValue = new Label("0");
    private final Label occupiedRoomsValue = new Label("0");
    private final Label activeBookingsValue = new Label("0");

    // Reports dashboard counters
    private final Label reportTotalRoomsValue = new Label("0");
    private final Label reportAvailableRoomsValue = new Label("0");
    private final Label reportOccupiedRoomsValue = new Label("0");
    private final Label reportOccupancyRateValue = new Label("0.00%");

    // Operations dashboard counters
    private final Label dirtyRoomsValue = new Label("0");
    private final Label cleaningRoomsValue = new Label("0");
    private final Label maintenanceRoomsValue = new Label("0");
    private final Label openTasksValue = new Label("0");

    @Override
    public void start(Stage stage) {
        Label eyebrowLabel = new Label("Boutique Operations Suite");
        eyebrowLabel.getStyleClass().add("eyebrow-label");

        Label titleLabel = new Label("Hotel Management System");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("A cinematic control room for rooms, bookings, billing, and reporting.");
        subtitleLabel.getStyleClass().add("subtitle-label");

        statusLabel.getStyleClass().add("status-label");
        statusLabel.setMaxWidth(Double.MAX_VALUE);

        ListView<String> roomListView = new ListView<>();
        roomListView.setPlaceholder(new Label("No rooms to display."));
        roomListView.setPrefHeight(320);

        ListView<String> bookingListView = new ListView<>();
        bookingListView.setPlaceholder(new Label("No bookings to display."));
        bookingListView.setPrefHeight(220);

        ListView<String> activityListView = new ListView<>(activityItems);
        activityListView.setPlaceholder(new Label("Activity updates will appear here."));
        activityListView.setPrefHeight(220);
        ListView<String> operationsBoardListView = new ListView<>();
        operationsBoardListView.setPlaceholder(new Label("Rooms will appear here once inventory is created."));
        operationsBoardListView.setPrefHeight(320);
        operationsBoardListView.getStyleClass().add("operations-board-list");

        ListView<OperationTask> operationsTaskListView = new ListView<>();
        operationsTaskListView.setPlaceholder(new Label("Open housekeeping and maintenance tasks will appear here."));
        operationsTaskListView.setPrefHeight(320);
        operationsTaskListView.getStyleClass().add("operations-task-list");
        VBox.setVgrow(roomListView, Priority.ALWAYS);
        VBox.setVgrow(bookingListView, Priority.ALWAYS);
        VBox.setVgrow(activityListView, Priority.ALWAYS);
        VBox.setVgrow(operationsBoardListView, Priority.ALWAYS);
        VBox.setVgrow(operationsTaskListView, Priority.ALWAYS);

        GridPane roomGrid = new GridPane();
        roomGrid.getStyleClass().addAll("section-card", "form-card");
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
        roomDisplayBox.getStyleClass().addAll("section-card", "list-card");
        roomDisplayBox.setMaxWidth(Double.MAX_VALUE);

        GridPane bookingGrid = new GridPane();
        bookingGrid.getStyleClass().addAll("section-card", "form-card");
        bookingGrid.setHgap(10);
        bookingGrid.setVgap(10);
        bookingGrid.setMaxWidth(Double.MAX_VALUE);

        Label customerNameLabel = new Label("Customer Name:");
        TextField customerNameField = new TextField();
        customerNameField.setPromptText("Guest full name");

        Label customerPhoneLabel = new Label("Phone Number:");
        TextField customerPhoneField = new TextField();
        customerPhoneField.setPromptText("10-digit mobile number");

        Label bookingRoomLabel = new Label("Room Number:");
        TextField bookingRoomField = new TextField();

        Label checkInLabel = new Label("Check-in Date:");
        DatePicker checkInPicker = new DatePicker();

        Label checkOutLabel = new Label("Checkout Date:");
        DatePicker checkOutPicker = new DatePicker();

        Label paymentMethodLabel = new Label("Payment Method:");
        ComboBox<PaymentMethod> paymentMethodCombo = new ComboBox<>(FXCollections.observableArrayList(PaymentMethod.values()));
        paymentMethodCombo.setPromptText("Select payment method");

        Label paymentDetailsLabel = new Label("Payment Details:");
        TextField cardNumberField = new TextField();
        cardNumberField.setPromptText("Card number");
        TextField cardHolderField = new TextField();
        cardHolderField.setPromptText("Name on card");
        TextField upiIdField = new TextField();
        upiIdField.setPromptText("example@upi");

        Label cardNumberPrompt = new Label("Card Number");
        cardNumberPrompt.getStyleClass().add("dashboard-hint");
        Label cardHolderPrompt = new Label("Name on Card");
        cardHolderPrompt.getStyleClass().add("dashboard-hint");
        Label upiPrompt = new Label("UPI ID");
        upiPrompt.getStyleClass().add("dashboard-hint");
        Label cashHintLabel = new Label("Select a payment method. Cash requires no extra verification details.");
        cashHintLabel.getStyleClass().add("dashboard-hint");

        VBox cardPaymentBox = new VBox(6, cardNumberPrompt, cardNumberField, cardHolderPrompt, cardHolderField);
        VBox upiPaymentBox = new VBox(6, upiPrompt, upiIdField);
        VBox paymentDetailsBox = new VBox(8, cashHintLabel, cardPaymentBox, upiPaymentBox);
        paymentDetailsBox.getStyleClass().add("payment-details-box");

        Button bookButton = new Button("Book Room");
        Button checkoutButton = new Button("Checkout Room");
        Button showBookingsButton = new Button("Show Bookings");
        bookButton.getStyleClass().add("primary-button");
        checkoutButton.getStyleClass().add("warning-button");
        showBookingsButton.getStyleClass().add("secondary-button");

        bookingGrid.add(customerNameLabel, 0, 0);
        bookingGrid.add(customerNameField, 1, 0);
        bookingGrid.add(customerPhoneLabel, 0, 1);
        bookingGrid.add(customerPhoneField, 1, 1);
        bookingGrid.add(bookingRoomLabel, 0, 2);
        bookingGrid.add(bookingRoomField, 1, 2);
        bookingGrid.add(checkInLabel, 0, 3);
        bookingGrid.add(checkInPicker, 1, 3);
        bookingGrid.add(checkOutLabel, 0, 4);
        bookingGrid.add(checkOutPicker, 1, 4);
        bookingGrid.add(paymentMethodLabel, 0, 5);
        bookingGrid.add(paymentMethodCombo, 1, 5);
        bookingGrid.add(paymentDetailsLabel, 0, 6);
        bookingGrid.add(paymentDetailsBox, 1, 6);
        bookingGrid.add(bookButton, 0, 7);
        bookingGrid.add(checkoutButton, 1, 7);
        bookingGrid.add(showBookingsButton, 0, 8);

        updatePaymentFields(paymentMethodCombo.getValue(), cardPaymentBox, upiPaymentBox, cashHintLabel);
        paymentMethodCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            updatePaymentFields(newValue, cardPaymentBox, upiPaymentBox, cashHintLabel);
            cardNumberField.clear();
            cardHolderField.clear();
            upiIdField.clear();
        });

        Label bookingListTitle = new Label("Active Bookings (Date-wise)");
        bookingListTitle.getStyleClass().add("section-title");
        VBox bookingDisplayBox = new VBox(8, bookingListTitle, bookingListView);
        bookingDisplayBox.getStyleClass().addAll("section-card", "list-card");
        bookingDisplayBox.setMaxWidth(Double.MAX_VALUE);

        GridPane fileGrid = new GridPane();
        fileGrid.getStyleClass().addAll("section-card", "utility-card");
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
        statsCard.getStyleClass().addAll("section-card", "summary-card");
        statsCard.setMaxWidth(Double.MAX_VALUE);

        Button roomManagementButton = new Button("Room Management\nInventory, pricing, and availability");
        Button customerManagementButton = new Button("Customer Management\nGuest check-in and allocation");
        Button bookingManagementButton = new Button("Booking Operations\nLive reservations and payment flow");
        Button operationsButton = new Button("Operations Board\nHousekeeping, maintenance, and dispatch");
        Button reportsButton = new Button("Reports & Analytics\nOccupancy, allocation, and review");

        roomManagementButton.getStyleClass().addAll("quick-button", "quick-room");
        customerManagementButton.getStyleClass().addAll("quick-button", "quick-guest");
        bookingManagementButton.getStyleClass().addAll("quick-button", "quick-booking");
        operationsButton.getStyleClass().addAll("quick-button", "quick-operations");
        reportsButton.getStyleClass().addAll("quick-button", "quick-reports");
        roomManagementButton.setWrapText(true);
        customerManagementButton.setWrapText(true);
        bookingManagementButton.setWrapText(true);
        operationsButton.setWrapText(true);
        reportsButton.setWrapText(true);
        roomManagementButton.setMaxWidth(Double.MAX_VALUE);
        customerManagementButton.setMaxWidth(Double.MAX_VALUE);
        bookingManagementButton.setMaxWidth(Double.MAX_VALUE);
        operationsButton.setMaxWidth(Double.MAX_VALUE);
        reportsButton.setMaxWidth(Double.MAX_VALUE);

        Label quickActionsTitle = new Label("Quick Actions");
        quickActionsTitle.getStyleClass().add("section-title");

        Label quickActionsHint = new Label("Move between the most-used workflows without breaking the operational flow.");
        quickActionsHint.getStyleClass().add("dashboard-hint");

        GridPane quickActionsGrid = new GridPane();
        quickActionsGrid.setHgap(12);
        quickActionsGrid.setVgap(12);
        ColumnConstraints quickLeftColumn = new ColumnConstraints();
        quickLeftColumn.setPercentWidth(50);
        quickLeftColumn.setHgrow(Priority.ALWAYS);
        quickLeftColumn.setFillWidth(true);
        ColumnConstraints quickRightColumn = new ColumnConstraints();
        quickRightColumn.setPercentWidth(50);
        quickRightColumn.setHgrow(Priority.ALWAYS);
        quickRightColumn.setFillWidth(true);
        quickActionsGrid.getColumnConstraints().addAll(quickLeftColumn, quickRightColumn);
        quickActionsGrid.add(roomManagementButton, 0, 0);
        quickActionsGrid.add(customerManagementButton, 1, 0);
        quickActionsGrid.add(bookingManagementButton, 0, 1);
        quickActionsGrid.add(operationsButton, 1, 1);
        quickActionsGrid.add(reportsButton, 0, 2, 2, 1);

        VBox quickActionsBox = new VBox(12, quickActionsTitle, quickActionsHint, quickActionsGrid);
        quickActionsBox.getStyleClass().addAll("section-card", "command-card");
        quickActionsBox.setMaxWidth(Double.MAX_VALUE);

        Label activityTitle = new Label("Live Activity");
        activityTitle.getStyleClass().add("section-title");

        Label activityHint = new Label("Every booking, checkout, billing refresh, and persistence action lands here in sequence.");
        activityHint.getStyleClass().add("dashboard-hint");

        VBox activityCard = new VBox(10, activityTitle, activityHint, activityListView);
        activityCard.getStyleClass().addAll("section-card", "activity-card");
        activityCard.setMaxWidth(Double.MAX_VALUE);

        GridPane homeGrid = new GridPane();
        homeGrid.setHgap(12);
        homeGrid.setVgap(12);
        homeGrid.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints homeLeftColumn = new ColumnConstraints();
        homeLeftColumn.setPercentWidth(58);
        homeLeftColumn.setHgrow(Priority.ALWAYS);
        homeLeftColumn.setFillWidth(true);
        ColumnConstraints homeRightColumn = new ColumnConstraints();
        homeRightColumn.setPercentWidth(42);
        homeRightColumn.setHgrow(Priority.ALWAYS);
        homeRightColumn.setFillWidth(true);
        homeGrid.getColumnConstraints().addAll(homeLeftColumn, homeRightColumn);
        homeGrid.add(quickActionsBox, 0, 0);
        homeGrid.add(activityCard, 1, 0);
        GridPane.setHgrow(quickActionsBox, Priority.ALWAYS);
        GridPane.setVgrow(quickActionsBox, Priority.ALWAYS);
        GridPane.setHgrow(activityCard, Priority.ALWAYS);
        GridPane.setVgrow(activityCard, Priority.ALWAYS);

        dashboardTitle.getStyleClass().add("page-title");
        dashboardHint.getStyleClass().add("page-copy");
        VBox homeLead = new VBox(4, dashboardTitle, dashboardHint);

        VBox homeTabContent = new VBox(16, homeLead, statsCard, homeGrid);
        homeTabContent.setPadding(new Insets(12));
        homeTabContent.setFillWidth(true);
        homeTabContent.setMaxWidth(Double.MAX_VALUE);
        ScrollPane homeScrollPane = new ScrollPane(homeTabContent);
        homeScrollPane.setFitToWidth(true);
        homeScrollPane.getStyleClass().add("content-scroll");

        Label roomSectionTitle = new Label("Room Management");
        roomSectionTitle.getStyleClass().add("section-title");

        Label roomSectionHint = new Label("Shape your room inventory, review status at a glance, and manage persistence utilities below.");
        roomSectionHint.getStyleClass().add("dashboard-hint");

        GridPane roomWorkspace = new GridPane();
        roomWorkspace.setHgap(14);
        roomWorkspace.setVgap(14);
        roomWorkspace.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints roomFormColumn = new ColumnConstraints();
        roomFormColumn.setPercentWidth(38);
        roomFormColumn.setHgrow(Priority.ALWAYS);
        roomFormColumn.setFillWidth(true);
        ColumnConstraints roomListColumn = new ColumnConstraints();
        roomListColumn.setPercentWidth(62);
        roomListColumn.setHgrow(Priority.ALWAYS);
        roomListColumn.setFillWidth(true);
        roomWorkspace.getColumnConstraints().addAll(roomFormColumn, roomListColumn);
        roomWorkspace.add(roomGrid, 0, 0);
        roomWorkspace.add(roomDisplayBox, 1, 0);
        roomWorkspace.add(fileGrid, 0, 1, 2, 1);
        GridPane.setHgrow(roomGrid, Priority.ALWAYS);
        GridPane.setHgrow(roomDisplayBox, Priority.ALWAYS);
        GridPane.setHgrow(fileGrid, Priority.ALWAYS);

        VBox roomTabContent = new VBox(14, roomSectionTitle, roomSectionHint, roomWorkspace);
        roomTabContent.setPadding(new Insets(12));
        roomTabContent.setFillWidth(true);
        roomTabContent.setMaxWidth(Double.MAX_VALUE);

        ScrollPane roomScrollPane = new ScrollPane(roomTabContent);
        roomScrollPane.setFitToWidth(true);
        roomScrollPane.getStyleClass().add("content-scroll");

        Label customerSectionTitle = new Label("Customer Booking");
        customerSectionTitle.getStyleClass().add("section-title");

        Label customerSectionHint = new Label("Handle guest details, stay dates, payment capture, and booking review in one operational lane.");
        customerSectionHint.getStyleClass().add("dashboard-hint");

        GridPane customerWorkspace = new GridPane();
        customerWorkspace.setHgap(14);
        customerWorkspace.setVgap(14);
        customerWorkspace.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints customerFormColumn = new ColumnConstraints();
        customerFormColumn.setPercentWidth(38);
        customerFormColumn.setHgrow(Priority.ALWAYS);
        customerFormColumn.setFillWidth(true);
        ColumnConstraints customerListColumn = new ColumnConstraints();
        customerListColumn.setPercentWidth(62);
        customerListColumn.setHgrow(Priority.ALWAYS);
        customerListColumn.setFillWidth(true);
        customerWorkspace.getColumnConstraints().addAll(customerFormColumn, customerListColumn);
        customerWorkspace.add(bookingGrid, 0, 0);
        customerWorkspace.add(bookingDisplayBox, 1, 0);
        GridPane.setHgrow(bookingGrid, Priority.ALWAYS);
        GridPane.setHgrow(bookingDisplayBox, Priority.ALWAYS);

        VBox customerTabContent = new VBox(14, customerSectionTitle, customerSectionHint, customerWorkspace);
        customerTabContent.setPadding(new Insets(12));
        customerTabContent.setFillWidth(true);
        customerTabContent.setMaxWidth(Double.MAX_VALUE);
        ScrollPane customerScrollPane = new ScrollPane(customerTabContent);
        customerScrollPane.setFitToWidth(true);
        customerScrollPane.getStyleClass().add("content-scroll");

        Label operationsTitle = new Label("Housekeeping + Maintenance Command Board");
        operationsTitle.getStyleClass().add("section-title");

        Label operationsHint = new Label("Route rooms through housekeeping and maintenance states, assign owners, and close tasks with visible timestamps.");
        operationsHint.getStyleClass().add("dashboard-hint");

        GridPane operationsSnapshotGrid = new GridPane();
        operationsSnapshotGrid.setHgap(10);
        operationsSnapshotGrid.setVgap(10);
        operationsSnapshotGrid.setMaxWidth(Double.MAX_VALUE);
        for (int index = 0; index < 4; index++) {
            ColumnConstraints operationsColumn = new ColumnConstraints();
            operationsColumn.setPercentWidth(25);
            operationsColumn.setHgrow(Priority.ALWAYS);
            operationsColumn.setFillWidth(true);
            operationsSnapshotGrid.getColumnConstraints().add(operationsColumn);
        }
        operationsSnapshotGrid.add(createStatCard("Dirty Rooms", dirtyRoomsValue, "stat-orange"), 0, 0);
        operationsSnapshotGrid.add(createStatCard("Cleaning", cleaningRoomsValue, "stat-blue"), 1, 0);
        operationsSnapshotGrid.add(createStatCard("Maintenance", maintenanceRoomsValue, "stat-purple"), 2, 0);
        operationsSnapshotGrid.add(createStatCard("Open Tasks", openTasksValue, "stat-green"), 3, 0);

        VBox operationsSnapshotCard = new VBox(8, new Label("Operations Snapshot"), operationsSnapshotGrid);
        operationsSnapshotCard.getChildren().get(0).getStyleClass().add("section-title");
        operationsSnapshotCard.getStyleClass().addAll("section-card", "summary-card");
        operationsSnapshotCard.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Integer> operationsRoomCombo = new ComboBox<>();
        operationsRoomCombo.setPromptText("Select room");

        ComboBox<OperationalStatus> operationsStatusCombo = new ComboBox<>(FXCollections.observableArrayList(OperationalStatus.values()));
        operationsStatusCombo.setValue(OperationalStatus.VACANT_CLEAN);

        ComboBox<TaskCategory> taskCategoryCombo = new ComboBox<>(FXCollections.observableArrayList(TaskCategory.values()));
        taskCategoryCombo.setValue(TaskCategory.HOUSEKEEPING);

        TextField assignedStaffField = new TextField();
        assignedStaffField.setPromptText("Assigned staff member");

        TextField taskNotesField = new TextField();
        taskNotesField.setPromptText("Task notes or dispatch instruction");

        Button updateRoomStatusButton = new Button("Update Room Status");
        updateRoomStatusButton.getStyleClass().add("secondary-button");
        Button createTaskButton = new Button("Create Task");
        createTaskButton.getStyleClass().add("primary-button");
        Button markTaskInProgressButton = new Button("Mark In Progress");
        markTaskInProgressButton.getStyleClass().add("secondary-button");
        Button completeTaskButton = new Button("Complete Task");
        completeTaskButton.getStyleClass().add("warning-button");
        Button refreshOperationsButton = new Button("Refresh Board");
        refreshOperationsButton.getStyleClass().add("secondary-button");

        updateRoomStatusButton.setMaxWidth(Double.MAX_VALUE);
        createTaskButton.setMaxWidth(Double.MAX_VALUE);
        markTaskInProgressButton.setMaxWidth(Double.MAX_VALUE);
        completeTaskButton.setMaxWidth(Double.MAX_VALUE);
        refreshOperationsButton.setMaxWidth(Double.MAX_VALUE);

        GridPane operationsControlGrid = new GridPane();
        operationsControlGrid.getStyleClass().addAll("section-card", "form-card");
        operationsControlGrid.setHgap(10);
        operationsControlGrid.setVgap(10);
        operationsControlGrid.setMaxWidth(Double.MAX_VALUE);
        operationsControlGrid.add(new Label("Room:"), 0, 0);
        operationsControlGrid.add(operationsRoomCombo, 1, 0);
        operationsControlGrid.add(new Label("Room Status:"), 0, 1);
        operationsControlGrid.add(operationsStatusCombo, 1, 1);
        operationsControlGrid.add(new Label("Task Category:"), 0, 2);
        operationsControlGrid.add(taskCategoryCombo, 1, 2);
        operationsControlGrid.add(new Label("Assign To:"), 0, 3);
        operationsControlGrid.add(assignedStaffField, 1, 3);
        operationsControlGrid.add(new Label("Notes:"), 0, 4);
        operationsControlGrid.add(taskNotesField, 1, 4);
        operationsControlGrid.add(updateRoomStatusButton, 0, 5);
        operationsControlGrid.add(createTaskButton, 1, 5);
        operationsControlGrid.add(markTaskInProgressButton, 0, 6);
        operationsControlGrid.add(completeTaskButton, 1, 6);
        operationsControlGrid.add(refreshOperationsButton, 0, 7, 2, 1);

        Label operationsBoardTitle = new Label("Room Command Board");
        operationsBoardTitle.getStyleClass().add("section-title");
        Label operationsBoardHint = new Label("Sellable, occupied, dirty, and maintenance states update here as the front desk works.");
        operationsBoardHint.getStyleClass().add("dashboard-hint");
        VBox operationsBoardCard = new VBox(10, operationsBoardTitle, operationsBoardHint, operationsBoardListView);
        operationsBoardCard.getStyleClass().addAll("section-card", "command-card");
        operationsBoardCard.setMaxWidth(Double.MAX_VALUE);

        Label operationsTaskTitle = new Label("Task Queue");
        operationsTaskTitle.getStyleClass().add("section-title");
        Label operationsTaskHint = new Label("Every task carries category, assigned staff, notes, created time, and completion time.");
        operationsTaskHint.getStyleClass().add("dashboard-hint");
        VBox operationsTaskCard = new VBox(10, operationsTaskTitle, operationsTaskHint, operationsTaskListView);
        operationsTaskCard.getStyleClass().addAll("section-card", "activity-card");
        operationsTaskCard.setMaxWidth(Double.MAX_VALUE);

        VBox operationsRightColumn = new VBox(14, operationsControlGrid, operationsTaskCard);
        operationsRightColumn.setFillWidth(true);
        operationsRightColumn.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(operationsTaskCard, Priority.ALWAYS);

        GridPane operationsWorkspace = new GridPane();
        operationsWorkspace.setHgap(14);
        operationsWorkspace.setVgap(14);
        operationsWorkspace.setMaxWidth(Double.MAX_VALUE);
        ColumnConstraints operationsLeftColumn = new ColumnConstraints();
        operationsLeftColumn.setPercentWidth(54);
        operationsLeftColumn.setHgrow(Priority.ALWAYS);
        operationsLeftColumn.setFillWidth(true);
        ColumnConstraints operationsRightGridColumn = new ColumnConstraints();
        operationsRightGridColumn.setPercentWidth(46);
        operationsRightGridColumn.setHgrow(Priority.ALWAYS);
        operationsRightGridColumn.setFillWidth(true);
        operationsWorkspace.getColumnConstraints().addAll(operationsLeftColumn, operationsRightGridColumn);
        operationsWorkspace.add(operationsBoardCard, 0, 0);
        operationsWorkspace.add(operationsRightColumn, 1, 0);
        GridPane.setHgrow(operationsBoardCard, Priority.ALWAYS);
        GridPane.setVgrow(operationsBoardCard, Priority.ALWAYS);
        GridPane.setHgrow(operationsRightColumn, Priority.ALWAYS);
        GridPane.setVgrow(operationsRightColumn, Priority.ALWAYS);

        VBox operationsTabContent = new VBox(16, operationsTitle, operationsHint, operationsSnapshotCard, operationsWorkspace);
        operationsTabContent.setPadding(new Insets(12));
        operationsTabContent.setFillWidth(true);
        operationsTabContent.setMaxWidth(Double.MAX_VALUE);
        ScrollPane operationsScrollPane = new ScrollPane(operationsTabContent);
        operationsScrollPane.setFitToWidth(true);
        operationsScrollPane.getStyleClass().add("content-scroll");

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
        reportSnapshotCard.getStyleClass().addAll("section-card", "summary-card");
        reportSnapshotCard.setMaxWidth(Double.MAX_VALUE);

        GridPane reportTableGrid = new GridPane();
        reportTableGrid.getStyleClass().add("report-grid");

        ScrollPane reportScrollPane = new ScrollPane(reportTableGrid);
        reportScrollPane.setFitToWidth(true);
        reportScrollPane.setPrefHeight(320);
        reportScrollPane.getStyleClass().add("report-scroll");

        VBox reportTableCard = new VBox(8, new Label("Detailed Room Allocation Report"), reportScrollPane);
        reportTableCard.getChildren().get(0).getStyleClass().add("section-title");
        reportTableCard.getStyleClass().addAll("section-card", "table-card");
        reportTableCard.setMaxWidth(Double.MAX_VALUE);

        Button refreshReportButton = new Button("Refresh Report");
        refreshReportButton.getStyleClass().add("secondary-button");
        HBox reportsHeader = new HBox(16, new VBox(4, reportsTitle, reportsHint), refreshReportButton);
        reportsHeader.getStyleClass().add("toolbar-row");
        HBox.setHgrow(reportsHeader.getChildren().get(0), Priority.ALWAYS);

        VBox reportsTabContent = new VBox(16, reportsHeader, reportSnapshotCard, reportTableCard);
        reportsTabContent.setPadding(new Insets(12));
        reportsTabContent.setFillWidth(true);
        reportsTabContent.setMaxWidth(Double.MAX_VALUE);
        ScrollPane reportsScrollPane = new ScrollPane(reportsTabContent);
        reportsScrollPane.setFitToWidth(true);
        reportsScrollPane.getStyleClass().add("content-scroll");

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
        ScrollPane billingScrollPane = new ScrollPane(billingTabContent);
        billingScrollPane.setFitToWidth(true);
        billingScrollPane.getStyleClass().add("content-scroll");
        billingController.configure(bookingSystem, this::updateStatus);

        Tab homeTab = new Tab("Home", homeScrollPane);
        homeTab.setClosable(false);

        Tab roomTab = new Tab("Rooms", roomScrollPane);
        roomTab.setClosable(false);

        Tab customerTab = new Tab("Customers", customerScrollPane);
        customerTab.setClosable(false);

        Tab operationsTab = new Tab("Operations", operationsScrollPane);
        operationsTab.setClosable(false);

        Tab reportsTab = new Tab("Reports", reportsScrollPane);
        reportsTab.setClosable(false);

        Tab billingTab = new Tab("Billing", billingScrollPane);
        billingTab.setClosable(false);

        TabPane tabPane = new TabPane(homeTab, roomTab, customerTab, operationsTab, reportsTab, billingTab);

        Runnable refreshAllViews = () -> {
            refreshAllSections(roomListView, bookingListView, reportTableGrid, operationsRoomCombo, operationsBoardListView, operationsTaskListView);
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

            Room room = createRoomByType(roomType, roomNumber, price, available);

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

            String customerPhone = parseCustomerPhone(customerPhoneField.getText());
            if (customerPhone == null) {
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

            PaymentMethod paymentMethod = paymentMethodCombo.getValue();
            if (paymentMethod == null) {
                updateStatus("Select a payment method.");
                return;
            }

            String paymentReference = resolvePaymentReference(
                    paymentMethod,
                    cardNumberField.getText(),
                    cardHolderField.getText(),
                    upiIdField.getText());
            if (paymentReference == null) {
                return;
            }

            bookingSystem.bookRoomAsync(customerName, customerPhone, roomNumber, checkInDate, checkOutDate, paymentMethod, paymentReference, message ->
                    Platform.runLater(() -> {
                        updateStatus(message);
                        refreshAllViews.run();

                        if (message.startsWith("Booking successful")) {
                            customerNameField.clear();
                            customerPhoneField.clear();
                            bookingRoomField.clear();
                            checkInPicker.setValue(null);
                            checkOutPicker.setValue(null);
                            paymentMethodCombo.setValue(null);
                            cardNumberField.clear();
                            cardHolderField.clear();
                            upiIdField.clear();
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

        operationsTaskListView.getSelectionModel().selectedItemProperty().addListener((observable, oldTask, newTask) -> {
            if (newTask == null) {
                return;
            }

            operationsRoomCombo.setValue(newTask.getRoomNumber());
            taskCategoryCombo.setValue(newTask.getCategory());
            assignedStaffField.setText(newTask.getAssignedTo());
            taskNotesField.setText(newTask.getNotes());
        });

        updateRoomStatusButton.setOnAction(event -> {
            Integer roomNumber = operationsRoomCombo.getValue();
            if (roomNumber == null) {
                updateStatus("Select a room for the operations board.");
                return;
            }

            updateStatus(bookingSystem.updateRoomOperationalStatus(
                    roomNumber,
                    operationsStatusCombo.getValue(),
                    assignedStaffField.getText(),
                    taskNotesField.getText()));
            refreshAllViews.run();
        });

        createTaskButton.setOnAction(event -> {
            Integer roomNumber = operationsRoomCombo.getValue();
            if (roomNumber == null) {
                updateStatus("Select a room before creating a task.");
                return;
            }

            updateStatus(bookingSystem.createOperationTask(
                    roomNumber,
                    taskCategoryCombo.getValue(),
                    assignedStaffField.getText(),
                    taskNotesField.getText()));
            refreshAllViews.run();
        });

        markTaskInProgressButton.setOnAction(event -> {
            OperationTask selectedTask = operationsTaskListView.getSelectionModel().getSelectedItem();
            if (selectedTask == null) {
                updateStatus("Select a task from the operations queue.");
                return;
            }

            updateStatus(bookingSystem.markTaskInProgress(selectedTask.getTaskId()));
            refreshAllViews.run();
        });

        completeTaskButton.setOnAction(event -> {
            OperationTask selectedTask = operationsTaskListView.getSelectionModel().getSelectedItem();
            if (selectedTask == null) {
                updateStatus("Select a task from the operations queue.");
                return;
            }

            updateStatus(bookingSystem.completeTask(selectedTask.getTaskId()));
            refreshAllViews.run();
            operationsTaskListView.getSelectionModel().clearSelection();
        });

        refreshOperationsButton.setOnAction(event -> {
            refreshAllViews.run();
            updateStatus("Operations command board refreshed.");
        });

        roomManagementButton.setOnAction(event -> tabPane.getSelectionModel().select(roomTab));
        customerManagementButton.setOnAction(event -> tabPane.getSelectionModel().select(customerTab));
        bookingManagementButton.setOnAction(event -> tabPane.getSelectionModel().select(customerTab));
        operationsButton.setOnAction(event -> {
            tabPane.getSelectionModel().select(operationsTab);
            refreshAllViews.run();
            updateStatus("Operations board loaded.");
        });
        reportsButton.setOnAction(event -> {
            tabPane.getSelectionModel().select(reportsTab);
            refreshAllViews.run();
            updateStatus("Reports and analytics loaded.");
        });

        Label controlTitle = new Label("Tonight's Control Room");
        controlTitle.getStyleClass().add("hero-panel-title");

        Label controlCopy = new Label("Monitor live inventory, booking flow, billing capture, and persistence health from one polished desktop workspace.");
        controlCopy.getStyleClass().add("hero-panel-copy");

        HBox heroMetrics = new HBox(
                12,
                createHeroMetric("Rooms Online", headerTotalRoomsValue),
                createHeroMetric("Available Now", headerAvailableRoomsDisplay),
                createHeroMetric("Active Bookings", headerActiveBookingsDisplay)
        );

        HBox heroChips = new HBox(
                8,
                createHeroChip("JavaFX"),
                createHeroChip("FXML Billing"),
                createHeroChip("Operations Board"),
                createHeroChip("JDBC Persistence"),
                createHeroChip("Live Payment Capture")
        );

        VBox brandColumn = new VBox(8, eyebrowLabel, titleLabel, subtitleLabel, statusLabel);
        brandColumn.getStyleClass().add("hero-brand");
        VBox detailColumn = new VBox(12, controlTitle, controlCopy, heroMetrics, heroChips);
        detailColumn.getStyleClass().add("hero-detail");
        HBox headerShell = new HBox(24, brandColumn, detailColumn);
        headerShell.getStyleClass().add("hero-shell");
        HBox.setHgrow(brandColumn, Priority.ALWAYS);
        HBox.setHgrow(detailColumn, Priority.ALWAYS);

        VBox root = new VBox(18, headerShell, tabPane);
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

    private String parseCustomerPhone(String value) {
        String digitsOnly = value == null ? "" : value.replaceAll("\\D", "");
        if (digitsOnly.length() != 10) {
            updateStatus("Enter a valid 10-digit phone number.");
            return null;
        }
        return digitsOnly;
    }

    private String resolvePaymentReference(PaymentMethod paymentMethod, String cardNumber, String cardHolderName, String upiId) {
        if (paymentMethod == PaymentMethod.CASH) {
            return "Front Desk Cash";
        }

        if (paymentMethod == PaymentMethod.CARD) {
            String digitsOnly = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
            if (digitsOnly.length() < 12 || digitsOnly.length() > 19) {
                updateStatus("Enter a valid card number.");
                return null;
            }

            String holderName = cardHolderName == null ? "" : cardHolderName.trim();
            if (holderName.isEmpty()) {
                updateStatus("Enter the name on the card.");
                return null;
            }

            String lastFourDigits = digitsOnly.substring(digitsOnly.length() - 4);
            return "Card ending " + lastFourDigits + " / " + holderName;
        }

        String normalizedUpiId = upiId == null ? "" : upiId.trim();
        if (normalizedUpiId.isEmpty() || !normalizedUpiId.contains("@") || normalizedUpiId.contains(" ")) {
            updateStatus("Enter a valid UPI ID.");
            return null;
        }
        return normalizedUpiId;
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);

        String entry = LocalTime.now().format(TIME_FORMATTER) + " - " + message;
        activityItems.add(0, entry);
        if (activityItems.size() > 25) {
            activityItems.remove(activityItems.size() - 1);
        }
    }

    private void refreshAllSections(
            ListView<String> roomListView,
            ListView<String> bookingListView,
            GridPane reportTableGrid,
            ComboBox<Integer> operationsRoomCombo,
            ListView<String> operationsBoardListView,
            ListView<OperationTask> operationsTaskListView) {
        refreshRoomList(roomListView);
        refreshBookingList(bookingListView);
        refreshDashboardCounters();
        refreshOperationRoomChoices(operationsRoomCombo);
        refreshOperationsBoard(operationsBoardListView);
        refreshOperationsTaskList(operationsTaskListView);
        refreshOperationsCounters();
        refreshReportTable(reportTableGrid);
    }

    private void refreshDashboardCounters() {
        totalRoomsValue.setText(String.valueOf(bookingSystem.getTotalRooms()));
        availableRoomsValue.setText(String.valueOf(bookingSystem.getAvailableRoomsCount()));
        occupiedRoomsValue.setText(String.valueOf(bookingSystem.getOccupiedRoomsCount()));
        activeBookingsValue.setText(String.valueOf(bookingSystem.getActiveBookingsCount()));
        headerTotalRoomsValue.setText(String.valueOf(bookingSystem.getTotalRooms()));
        headerAvailableRoomsDisplay.setText(String.valueOf(bookingSystem.getAvailableRoomsCount()));
        headerActiveBookingsDisplay.setText(String.valueOf(bookingSystem.getActiveBookingsCount()));

        reportTotalRoomsValue.setText(String.valueOf(bookingSystem.getTotalRooms()));
        reportAvailableRoomsValue.setText(String.valueOf(bookingSystem.getAvailableRoomsCount()));
        reportOccupiedRoomsValue.setText(String.valueOf(bookingSystem.getOccupiedRoomsCount()));
        reportOccupancyRateValue.setText(String.format("%.2f%%", bookingSystem.getOccupancyRate()));
    }

    private void refreshOperationsCounters() {
        dirtyRoomsValue.setText(String.valueOf(bookingSystem.getOperationalStatusCount(OperationalStatus.DIRTY)));
        cleaningRoomsValue.setText(String.valueOf(bookingSystem.getOperationalStatusCount(OperationalStatus.CLEANING)));
        maintenanceRoomsValue.setText(String.valueOf(
                bookingSystem.getOperationalStatusCount(OperationalStatus.MAINTENANCE)
                        + bookingSystem.getOperationalStatusCount(OperationalStatus.OUT_OF_ORDER)));
        openTasksValue.setText(String.valueOf(
                bookingSystem.getTaskCount(TaskState.OPEN) + bookingSystem.getTaskCount(TaskState.IN_PROGRESS)));
    }

    private void refreshRoomList(ListView<String> roomListView) {
        roomListView.getItems().setAll(splitToLines(bookingSystem.displayAllRooms()));
    }

    private void refreshBookingList(ListView<String> bookingListView) {
        bookingListView.getItems().setAll(splitToLines(bookingSystem.displayBookings()));
    }

    private void refreshOperationRoomChoices(ComboBox<Integer> operationsRoomCombo) {
        Integer selectedRoom = operationsRoomCombo.getValue();
        ArrayList<Integer> roomNumbers = new ArrayList<>();
        for (Room room : bookingSystem.getRoomsSnapshot()) {
            roomNumbers.add(room.getRoomNumber());
        }
        roomNumbers.sort(Integer::compareTo);
        operationsRoomCombo.setItems(FXCollections.observableArrayList(roomNumbers));
        if (selectedRoom != null && roomNumbers.contains(selectedRoom)) {
            operationsRoomCombo.setValue(selectedRoom);
        }
    }

    private void refreshOperationsBoard(ListView<String> operationsBoardListView) {
        operationsBoardListView.getItems().setAll(bookingSystem.getOperationsBoardLines());
    }

    private void refreshOperationsTaskList(ListView<OperationTask> operationsTaskListView) {
        operationsTaskListView.getItems().setAll(bookingSystem.getSortedOperationTasks());
    }

    private void refreshReportTable(GridPane reportTableGrid) {
        reportTableGrid.getChildren().clear();
        reportTableGrid.setHgap(0);
        reportTableGrid.setVgap(0);

        reportTableGrid.add(createReportHeaderCell("Room", 90.0), 0, 0);
        reportTableGrid.add(createReportHeaderCell("Type", 130.0), 1, 0);
        reportTableGrid.add(createReportHeaderCell("Price", 110.0), 2, 0);
        reportTableGrid.add(createReportHeaderCell("Status", 130.0), 3, 0);
        reportTableGrid.add(createReportHeaderCell("Customer", 180.0), 4, 0);
        reportTableGrid.add(createReportHeaderCell("Phone", 150.0), 5, 0);
        reportTableGrid.add(createReportHeaderCell("Check-in", 150.0), 6, 0);
        reportTableGrid.add(createReportHeaderCell("Checkout", 150.0), 7, 0);

        ArrayList<BookingSystem.ReportRow> reportRows = bookingSystem.getDetailedRoomReport();
        if (reportRows.isEmpty()) {
            Label emptyLabel = new Label("No room records found. Add rooms to generate a report.");
            emptyLabel.getStyleClass().add("report-empty");
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            reportTableGrid.add(emptyLabel, 0, 1, 8, 1);
            return;
        }

        Integer rowIndex = 1;
        for (BookingSystem.ReportRow reportRow : reportRows) {
            reportTableGrid.add(createReportDataCell(String.valueOf(reportRow.getRoomNumber()), 90.0), 0, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getRoomType(), 130.0), 1, rowIndex);
            reportTableGrid.add(createReportDataCell(String.format("%.2f", reportRow.getPrice()), 110.0), 2, rowIndex);

            String statusClass = getReportStatusClass(reportRow.getOperationalStatus());
            reportTableGrid.add(createReportDataCell(reportRow.getStatusText(), 130.0, statusClass), 3, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getCustomerName(), 180.0), 4, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getCustomerPhone(), 150.0), 5, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getCheckInText(), 150.0), 6, rowIndex);
            reportTableGrid.add(createReportDataCell(reportRow.getCheckOutText(), 150.0), 7, rowIndex);
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

    private String getReportStatusClass(OperationalStatus operationalStatus) {
        if (operationalStatus == null) {
            return "status-maintenance";
        }

        return switch (operationalStatus) {
            case VACANT_CLEAN -> "status-available";
            case OCCUPIED -> "status-occupied";
            case DIRTY -> "status-dirty";
            case CLEANING -> "status-cleaning";
            case OUT_OF_ORDER, MAINTENANCE -> "status-maintenance";
        };
    }

    private void updatePaymentFields(PaymentMethod paymentMethod, VBox cardPaymentBox, VBox upiPaymentBox, Label cashHintLabel) {
        boolean showCardFields = paymentMethod == PaymentMethod.CARD;
        boolean showUpiField = paymentMethod == PaymentMethod.UPI;
        boolean showCashHint = paymentMethod == PaymentMethod.CASH || paymentMethod == null;

        cardPaymentBox.setManaged(showCardFields);
        cardPaymentBox.setVisible(showCardFields);
        upiPaymentBox.setManaged(showUpiField);
        upiPaymentBox.setVisible(showUpiField);
        cashHintLabel.setManaged(showCashHint);
        cashHintLabel.setVisible(showCashHint);
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

    private VBox createHeroMetric(String labelText, Label valueLabel) {
        Label metricLabel = new Label(labelText);
        metricLabel.getStyleClass().add("hero-metric-label");

        valueLabel.getStyleClass().add("hero-metric-value");

        VBox metricCard = new VBox(4, metricLabel, valueLabel);
        metricCard.getStyleClass().add("hero-metric");
        metricCard.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(metricCard, Priority.ALWAYS);
        return metricCard;
    }

    private Label createHeroChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("hero-chip");
        return chip;
    }

    private Room createRoomByType(RoomType roomType, Integer roomNumber, Double price, Boolean available) {
        return switch (roomType) {
            case STANDARD -> new StandardRoom(roomNumber, price, available);
            case DELUXE -> new DeluxeRoom(roomNumber, price, available);
            case SUITE -> new SuiteRoom(roomNumber, price, available);
            case VILLA -> new VillaRoom(roomNumber, price, available);
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}

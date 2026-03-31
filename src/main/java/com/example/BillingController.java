package com.example;

import java.util.Comparator;
import java.util.function.Consumer;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class BillingController {
    @FXML
    private Label totalInvoicesValue;

    @FXML
    private Label activeInvoicesValue;

    @FXML
    private Label projectedRevenueValue;

    @FXML
    private TableView<BillingRecord> billingTable;

    @FXML
    private TableColumn<BillingRecord, String> invoiceColumn;

    @FXML
    private TableColumn<BillingRecord, Integer> roomColumn;

    @FXML
    private TableColumn<BillingRecord, String> guestColumn;

    @FXML
    private TableColumn<BillingRecord, String> roomTypeColumn;

    @FXML
    private TableColumn<BillingRecord, String> statusColumn;

    @FXML
    private TableColumn<BillingRecord, Integer> nightsColumn;

    @FXML
    private TableColumn<BillingRecord, String> totalColumn;

    @FXML
    private TableColumn<BillingRecord, String> checkInColumn;

    @FXML
    private TableColumn<BillingRecord, String> checkOutColumn;

    @FXML
    private TableColumn<BillingRecord, String> generatedOnColumn;

    private BookingSystem bookingSystem;
    private Consumer<String> statusConsumer;

    @FXML
    public void initialize() {
        invoiceColumn.setCellValueFactory(new PropertyValueFactory<>("invoiceNumber"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        guestColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("billingStatus"));
        nightsColumn.setCellValueFactory(new PropertyValueFactory<>("nights"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmountText"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInText"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutText"));
        generatedOnColumn.setCellValueFactory(new PropertyValueFactory<>("generatedOnText"));

        billingTable.setPlaceholder(new Label("Billing records will appear after room bookings are created."));
        billingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void configure(BookingSystem bookingSystem, Consumer<String> statusConsumer) {
        this.bookingSystem = bookingSystem;
        this.statusConsumer = statusConsumer;
        refreshBillingData();
    }

    @FXML
    private void handleRefreshBilling() {
        refreshBillingData();
        if (statusConsumer != null) {
            statusConsumer.accept("Billing dashboard refreshed.");
        }
    }

    public void refreshBillingData() {
        if (bookingSystem == null) {
            return;
        }

        var billingRecords = bookingSystem.getBillingRecordsSnapshot();
        billingRecords.sort(Comparator.comparing(BillingRecord::getGeneratedOn, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(BillingRecord::getInvoiceNumber, Comparator.reverseOrder()));

        billingTable.setItems(FXCollections.observableArrayList(billingRecords));

        long activeInvoices = billingRecords.stream()
                .filter(record -> "Active".equalsIgnoreCase(record.getBillingStatus()))
                .count();
        double projectedRevenue = billingRecords.stream()
                .mapToDouble(BillingRecord::getTotalAmount)
                .sum();

        totalInvoicesValue.setText(String.valueOf(billingRecords.size()));
        activeInvoicesValue.setText(String.valueOf(activeInvoices));
        projectedRevenueValue.setText(String.format("%.2f", projectedRevenue));
    }
}

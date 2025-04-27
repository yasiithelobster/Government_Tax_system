// This controller manages the TableView displaying transaction data
// It handles file import, data validation, deletion of records, and tax calculation.

package lk.gov.taxdepartmentapp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TaxDepartmentController {

    @FXML
    private TableView<TransactionWithBillInfo> tableView;
    @FXML
    private Label summaryLabel;
    @FXML
    private TextField taxRateField;
    @FXML
    private Label finalTaxLabel;
    @FXML
    private TextField filePathField;
    @FXML
    private Button importPathButton;

    private ObservableList<TransactionWithBillInfo> transactionsWithBillInfo = FXCollections.observableArrayList();
    private ObservableList<Bill> bills = FXCollections.observableArrayList();

    public void initialize() {
        System.out.println("Initializing controller...");

        // Setup table columns
        TableColumn<TransactionWithBillInfo, String> itemCodeCol = new TableColumn<>("Item Code");
        itemCodeCol.setCellValueFactory(new PropertyValueFactory<>("itemCode"));

        TableColumn<TransactionWithBillInfo, Double> internalPriceCol = new TableColumn<>("Internal Price");
        internalPriceCol.setCellValueFactory(new PropertyValueFactory<>("internalPrice"));

        TableColumn<TransactionWithBillInfo, Double> discountCol = new TableColumn<>("Discount");
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));

        TableColumn<TransactionWithBillInfo, Double> salePriceCol = new TableColumn<>("Sale Price");
        salePriceCol.setCellValueFactory(new PropertyValueFactory<>("salePrice"));

        TableColumn<TransactionWithBillInfo, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<TransactionWithBillInfo, Integer> checksumCol = new TableColumn<>("Checksum");
        checksumCol.setCellValueFactory(new PropertyValueFactory<>("checksum"));

        TableColumn<TransactionWithBillInfo, Double> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));

        TableColumn<TransactionWithBillInfo, String> validationStatusCol = new TableColumn<>("Item Status");
        validationStatusCol.setCellValueFactory(new PropertyValueFactory<>("validationStatus"));

        // Add a "Refresh" button column for manually re-validating a row
        TableColumn<TransactionWithBillInfo, String> editCol = new TableColumn<>("Refresh");
        editCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Refresh");

            {
                editButton.setOnAction(event -> {
                    TransactionWithBillInfo transaction = getTableRow().getItem();
                    if (transaction != null) {
                        refreshTransaction(transaction);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(editButton);
                }
            }
        });

        tableView.getColumns().addAll(itemCodeCol, internalPriceCol, discountCol, salePriceCol, quantityCol, checksumCol, profitCol, validationStatusCol, editCol);
        tableView.setItems(transactionsWithBillInfo);
        tableView.setEditable(true); // Make the table view editable to allow in-place editing

        // Make the columns editable using TextFieldTableCell for direct modification
        itemCodeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        internalPriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        discountCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        salePriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));

        // Define the actions to be performed when a cell edit is committed
        itemCodeCol.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setItemCode(e.getNewValue());
            updateTransaction(e.getTableView().getItems().get(e.getTablePosition().getRow()));
        });
        internalPriceCol.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setInternalPrice(e.getNewValue());
            updateTransaction(e.getTableView().getItems().get(e.getTablePosition().getRow()));
        });
        discountCol.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setDiscount(e.getNewValue());
            updateTransaction(e.getTableView().getItems().get(e.getTablePosition().getRow()));
        });
        salePriceCol.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setSalePrice(e.getNewValue());
            updateTransaction(e.getTableView().getItems().get(e.getTablePosition().getRow()));
        });
        quantityCol.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setQuantity(e.getNewValue());
            updateTransaction(e.getTableView().getItems().get(e.getTablePosition().getRow()));
        });
    }

    // Updates a single transaction after an edit in the TableView
    void updateTransaction(TransactionWithBillInfo transaction) {
        int index = transactionsWithBillInfo.indexOf(transaction);
        if (index == -1) {
            System.out.println("Transaction not found for editing.");
            return;
        }

        // Recalculate the checksum based on the updated values
        int calculatedChecksum = calculateItemChecksum(transaction.getItemCode(), transaction.getQuantity(), transaction.getInternalPrice(), transaction.getDiscount(), transaction.getSalePrice());
        transaction.setChecksum(calculatedChecksum);

        // Determine the validation status based on the recalculated checksum and item code format
        String validationStatus = "Valid";
        if (calculatedChecksum != transaction.getChecksum()) {
            validationStatus = "Invalid";
        }
        if (containsSpecialCharacters(transaction.getItemCode())) {
            validationStatus = "Invalid";
        }
        if (transaction.getInternalPrice() < 0 || transaction.getSalePrice() < 0) {
            validationStatus = "Invalid";
        }
        transaction.setValidationStatus(validationStatus);

        // Update the transaction in the ObservableList and refresh the TableView
        transactionsWithBillInfo.set(index, transaction);
        tableView.refresh(); // Refresh the row to show the updated validation status and profit

    }


    // Handles the action when the path is given.
    @FXML
    protected void onImportPathButtonClick() {
        String filePath = filePathField.getText();
        if (filePath != null && !filePath.trim().isEmpty()) {
            File file = new File(filePath);
            importFile(file);
        } else {
            // Optionally, show an error message to the user
            System.err.println("File path cannot be empty.");
            // You might want to use an Alert here for better user feedback
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a valid file path.");
            alert.showAndWait();
        }
    }

    // Handles the action when the "Validate" button is clicked
    @FXML
    protected void onValidateButtonClick() {
        validateTransactions();
    }

    // Handles the action when the "Delete Invalid" button is clicked
    @FXML
    protected void onDeleteInvalidButtonClick() {
        deleteInvalidRecords();
    }

    // Handles the action when the "Delete Zero Profit" button is clicked
    @FXML
    protected void onDeleteZeroProfitButtonClick() {
        deleteZeroProfitRecords();
    }

    // Handles the action when the "Calculate Tax" button is clicked
    @FXML
    protected void onCalculateTaxButtonClick() {
        calculateFinalTax();
    }


    // Imports transaction data from the selected file
    public void importFile(File file) {
        System.out.println("importFile() called with file: " + file.getAbsolutePath());
        try {
            if (file == null || !file.exists()) {
                System.out.println("File does not exist or was not selected.");
                return;
            }

            ObservableList<TransactionWithBillInfo> transactions = FXCollections.observableArrayList();
            ObservableList<Bill> billsList = FXCollections.observableArrayList();

            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                try {
                    JSONObject json = new JSONObject(line);
                    JSONArray itemsArray = json.getJSONArray("items");
                    double currentTotalAmount = json.getDouble("total_amount");

                    Bill currentBill = new Bill(currentTotalAmount, 0);
                    ArrayList<TransactionWithBillInfo> billTransactions = new ArrayList<>();

                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject item = itemsArray.getJSONObject(i);

                        String itemCode = item.getString("item_code");
                        int quantity = item.getInt("quantity");
                        double internalPrice = item.getDouble("internal_price");
                        double discount = item.getDouble("discount");
                        double salePrice = item.getDouble("sale_price");
                        double lineTotal = item.getDouble("line_total");
                        int fileChecksum = item.getInt("checksum");

                        // Create a temporary Transaction object to calculate the checksum correctly based on the item data
                        Transaction tempTransaction = new Transaction(itemCode, internalPrice, discount, salePrice, quantity, 0);
                        int calculatedChecksum = currentBill.calculateItemChecksum(tempTransaction);

                        // Initial validation based on checksum and item code format upon import
                        String validationStatus = "Valid";
                        if (calculatedChecksum != fileChecksum) {
                            validationStatus = "Invalid";
                        }
                        if (containsSpecialCharacters(itemCode)) {
                            validationStatus = "Invalid";
                        }
                        if (internalPrice < 0 || salePrice < 0) {
                            validationStatus = "Invalid";
                        }

                        // Create the TransactionWithBillInfo object with the calculated checksum and initial validation status
                        TransactionWithBillInfo transaction = new TransactionWithBillInfo(
                                itemCode, internalPrice, discount, salePrice, quantity, calculatedChecksum, 0.0, validationStatus // Initial profit is set to 0, will be calculated later if needed
                        );

                        transactions.add(transaction);
                        billTransactions.add(transaction);
                        currentBill.addItem(new Transaction(itemCode, internalPrice, discount, salePrice, quantity, calculatedChecksum));
                    }

                    billsList.add(currentBill);
                    transactionsWithBillInfo.addAll(billTransactions);

                } catch (JSONException e) {
                    System.err.println("Error parsing JSON object on line " + lineNumber + ": " + line);
                    e.printStackTrace();
                }
                lineNumber++;
            }
            reader.close();

            tableView.setItems(transactionsWithBillInfo);

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Checks if a given text contains special characters (excluding alphanumeric and underscore)
    boolean containsSpecialCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        Pattern specialChars = Pattern.compile("[^a-zA-Z0-9_ ]");
        Matcher matcher = specialChars.matcher(text);
        return matcher.find();
    }


    // Validates all transactions in the table and updates the summary label
    void validateTransactions() {
        int totalTransactions = transactionsWithBillInfo.size();
        int validTransactions = 0;
        int invalidTransactions = 0;

        // Iterate through each transaction to check its validation status
        for (TransactionWithBillInfo transaction : transactionsWithBillInfo) {
            if (transaction.getValidationStatus().equals("Valid")) {
                validTransactions++;
            } else {
                invalidTransactions++;
            }
        }
        // Update the summary label in the UI with the validation results
        summaryLabel.setText("Total Transactions: " + totalTransactions + ", Valid Transactions: " + validTransactions + ", Invalid Transactions: " + invalidTransactions);
    }


    // Deletes all transactions marked as "Invalid" from the table
    void deleteInvalidRecords() {
        transactionsWithBillInfo.removeIf(transaction -> transaction.getValidationStatus().equals("Invalid"));
        tableView.setItems(transactionsWithBillInfo);
    }


    // Deletes all transactions with a profit of zero from the table
    void deleteZeroProfitRecords() {
        transactionsWithBillInfo.removeIf(transaction -> transaction.getProfit() == 0);
        tableView.setItems(transactionsWithBillInfo);
    }


    // Calculates the final tax based on the total profit/loss and the entered tax rate
    void calculateFinalTax() {
        double taxRate = Double.parseDouble(taxRateField.getText()) / 100.0;
        double totalProfitLoss = transactionsWithBillInfo.stream()
                .mapToDouble(TransactionWithBillInfo::getProfit)
                .sum();
        double finalTax = totalProfitLoss * taxRate;
        finalTaxLabel.setText("Final Tax: " + finalTax);
    }


    // Refreshes (re-validates) a single transaction
    void refreshTransaction(TransactionWithBillInfo transaction) {
        int index = transactionsWithBillInfo.indexOf(transaction);
        if (index == -1) {
            System.out.println("Transaction not found for refreshing.");
            return;
        }


        // Recalculate checksum and profit
        int calculatedChecksum = calculateItemChecksum(transaction.getItemCode(), transaction.getQuantity(), transaction.getInternalPrice(), transaction.getDiscount(), transaction.getSalePrice());
        transaction.setChecksum(calculatedChecksum);
        double profit = (transaction.getSalePrice() - transaction.getInternalPrice()) * transaction.getQuantity() - transaction.getDiscount();
        transaction.setProfit(profit);


        // Re-evaluate validation status
        String validationStatus = "Valid";
        if (calculatedChecksum != transaction.getChecksum()) {
            validationStatus = "Invalid";
        }
        if (containsSpecialCharacters(transaction.getItemCode())) {
            validationStatus = "Invalid";
        }
        if (transaction.getInternalPrice() < 0 || transaction.getSalePrice() < 0) {
            validationStatus = "Invalid";
        }
        transaction.setValidationStatus(validationStatus);

        // Update the transaction in the list and refresh the table
        transactionsWithBillInfo.set(index, transaction);
        tableView.refresh();
    }


    // Calculates the checksum for a single item based on its properties
    int calculateItemChecksum(String itemCode, int quantity, double internalPrice, double discount, double salePrice) {
        NumberFormat nf = new DecimalFormat("#0.00");
        JSONObject itemData = new JSONObject();
        itemData.put("item_code", itemCode);
        itemData.put("quantity", quantity);
        itemData.put("sale_price", nf.format(salePrice));
        itemData.put("line_total", nf.format(salePrice * quantity));
        String jsonString = itemData.toString(2); // Use toString(2) for better readability in case of debugging
        return calculateStringChecksum(jsonString);
    }


    // Calculates a simple checksum for a given string based on character types
    int calculateStringChecksum(String dataString) {
        int uppercaseCount = 0;
        int lowercaseCount = 0;
        int numbersDecimalsCount = 0;

        for (int i = 0; i < dataString.length(); i++) {
            char ch = dataString.charAt(i);
            if (Character.isUpperCase(ch)) {
                uppercaseCount++;
            } else if (Character.isLowerCase(ch)) {
                lowercaseCount++;
            } else if (Character.isDigit(ch)) {
                numbersDecimalsCount++;
            }
        }
        return uppercaseCount + lowercaseCount + numbersDecimalsCount;
    }
}


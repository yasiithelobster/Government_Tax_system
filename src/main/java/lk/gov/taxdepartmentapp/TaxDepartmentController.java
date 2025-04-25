package lk.gov.taxdepartmentapp;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    private Button importButton;
    @FXML
    private Button validateButton;
    @FXML
    private Button deleteInvalidButton;
    @FXML
    private Button deleteZeroProfitButton;
    @FXML
    private Button calculateTaxButton;

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

        // Add an "Edit" button column
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
        tableView.setEditable(true);

        // Make the columns editable
        itemCodeCol.setCellFactory(TextFieldTableCell.forTableColumn());
        internalPriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        discountCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        salePriceCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.DoubleStringConverter()));
        quantityCol.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));

        // Commit changes
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

        File defaultFile = new File("tax_transactions.txt");
        if (defaultFile.exists()) {
            importFile(defaultFile);
        } else {
            System.out.println("No tax_transactions.txt found in current directory.");
        }
    }

    void updateTransaction(TransactionWithBillInfo transaction) {
        int index = transactionsWithBillInfo.indexOf(transaction);
        if (index == -1) {
            System.out.println("Transaction not found for editing.");
            return;
        }

        int calculatedChecksum = calculateItemChecksum(transaction.getItemCode(), transaction.getQuantity(), transaction.getInternalPrice(), transaction.getDiscount(), transaction.getSalePrice());
        transaction.setChecksum(calculatedChecksum);

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

        transactionsWithBillInfo.set(index, transaction);
        tableView.refresh(); // This will trigger the getProfit() method to be called
        validateTransactions();
    }

    @FXML
    protected void onImportButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Tax Transactions File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            importFile(selectedFile);
        } else {
            System.out.println("No file selected.");
        }
    }

    @FXML
    protected void onValidateButtonClick() {
        validateTransactions();
    }

    @FXML
    protected void onDeleteInvalidButtonClick() {
        deleteInvalidRecords();
    }

    @FXML
    protected void onDeleteZeroProfitButtonClick() {
        deleteZeroProfitRecords();
    }

    @FXML
    protected void onCalculateTaxButtonClick() {
        calculateFinalTax();
    }

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

                        Transaction tempTransaction = new Transaction(itemCode, internalPrice, discount, salePrice, quantity, 0);
                        int calculatedChecksum = currentBill.calculateItemChecksum(tempTransaction);

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

                        TransactionWithBillInfo transaction = new TransactionWithBillInfo(
                                itemCode, internalPrice, discount, salePrice, quantity, calculatedChecksum, 0.0, validationStatus // Initial profit can be 0
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
            validateTransactions();
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    boolean containsSpecialCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        Pattern specialChars = Pattern.compile("[^a-zA-Z0-9_ ]");
        Matcher matcher = specialChars.matcher(text);
        return matcher.find();
    }

    void validateTransactions() {
        int totalTransactions = transactionsWithBillInfo.size();
        int validTransactions = 0;
        int invalidTransactions = 0;

        for (TransactionWithBillInfo transaction : transactionsWithBillInfo) {
            if (transaction.getValidationStatus().equals("Valid")) {
                validTransactions++;
            } else {
                invalidTransactions++;
            }
        }
        summaryLabel.setText("Total Transactions: " + totalTransactions + ", Valid Transactions: " + validTransactions + ", Invalid Transactions: " + invalidTransactions);
    }

    void deleteInvalidRecords() {
        transactionsWithBillInfo.removeIf(transaction -> transaction.getValidationStatus().equals("Invalid"));
        tableView.setItems(transactionsWithBillInfo);
    }

    void deleteZeroProfitRecords() {
        transactionsWithBillInfo.removeIf(transaction -> transaction.getProfit() == 0);
        tableView.setItems(transactionsWithBillInfo);
    }

    void calculateFinalTax() {
        double taxRate = Double.parseDouble(taxRateField.getText()) / 100.0;
        double totalProfitLoss = transactionsWithBillInfo.stream()
                .mapToDouble(TransactionWithBillInfo::getProfit)
                .sum();
        double finalTax = totalProfitLoss * taxRate;
        finalTaxLabel.setText("Final Tax: " + finalTax);
    }

    void refreshTransaction(TransactionWithBillInfo transaction) {
        int index = transactionsWithBillInfo.indexOf(transaction);
        if (index == -1) {
            System.out.println("Transaction not found for refreshing.");
            return;
        }

        int calculatedChecksum = calculateItemChecksum(transaction.getItemCode(), transaction.getQuantity(), transaction.getInternalPrice(), transaction.getDiscount(), transaction.getSalePrice());
        transaction.setChecksum(calculatedChecksum);
        double profit = (transaction.getSalePrice() - transaction.getInternalPrice()) * transaction.getQuantity() - transaction.getDiscount();
        transaction.setProfit(profit);

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

        transactionsWithBillInfo.set(index, transaction);
        tableView.refresh();
        validateTransactions();
    }

    int calculateItemChecksum(String itemCode, int quantity, double internalPrice, double discount, double salePrice) {
        NumberFormat nf = new DecimalFormat("#0.00");
        JSONObject itemData = new JSONObject();
        itemData.put("item_code", itemCode);
        itemData.put("quantity", quantity);
        itemData.put("sale_price", nf.format(salePrice));
        itemData.put("line_total", nf.format(salePrice * quantity));
        String jsonString = itemData.toString(2);
        return calculateStringChecksum(jsonString);
    }

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

    // Getter methods for the FXML injected fields
    public TableView<TransactionWithBillInfo> getTableView() {
        return tableView;
    }

    public Label getSummaryLabel() {
        return summaryLabel;
    }

    public TextField getTaxRateField() {
        return taxRateField;
    }

    public Label getFinalTaxLabel() {
        return finalTaxLabel;
    }

    public Button getImportButton() {
        return importButton;
    }

    public Button getValidateButton() {
        return validateButton;
    }

    public Button getDeleteInvalidButton() {
        return deleteInvalidButton;
    }

    public Button getDeleteZeroProfitButton() {
        return deleteZeroProfitButton;
    }

    public Button getCalculateTaxButton() {
        return calculateTaxButton;
    }

    public void setTableView(TableView<TransactionWithBillInfo> tableView) {
        this.tableView = tableView;
    }

    public void setSummaryLabel(Label summaryLabel) {
        this.summaryLabel = summaryLabel;
    }

    public void setTaxRateField(TextField taxRateField) {
        this.taxRateField = taxRateField;
    }

    public void setFinalTaxLabel(Label finalTaxLabel) {
        this.finalTaxLabel = finalTaxLabel;
    }

    public void setImportButton(Button importButton) {
        this.importButton = importButton;
    }

    public void setValidateButton(Button validateButton) {
        this.validateButton = validateButton;
    }

    public void setDeleteInvalidButton(Button deleteInvalidButton) {
        this.deleteInvalidButton = deleteInvalidButton;
    }

    public void setDeleteZeroProfitButton(Button deleteZeroProfitButton) {
        this.deleteZeroProfitButton = deleteZeroProfitButton;
    }

    public void setCalculateTaxButton(Button calculateTaxButton) {
        this.calculateTaxButton = calculateTaxButton;
    }
}


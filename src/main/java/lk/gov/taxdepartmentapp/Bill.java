// This is a model class representing a bill or a collection of transactions.

package lk.gov.taxdepartmentapp;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class Bill {
    private List<Transaction> items;
    private double totalAmount;
    private int checksum;

    public Bill(double totalAmount, int checksum) {
        this.items = new ArrayList<>();
        this.totalAmount = totalAmount;
        this.checksum = checksum;
    }

    public Bill() {
        this.items = new ArrayList<>();
    }

    public void addItem(Transaction item) {
        this.items.add(item);
    }

    public List<Transaction> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public int getChecksum() {
        return checksum;
    }

    public int calculateStringChecksum(String dataString) {
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

    public int calculateItemChecksum(Transaction item) {
        NumberFormat nf = new DecimalFormat("#0.00"); // Force 2 decimal places
        JSONObject itemData = new JSONObject();
        itemData.put("item_code", item.getItemCode());
        itemData.put("quantity", item.getQuantity());
        itemData.put("sale_price", nf.format(item.getSalePrice()));
        itemData.put("line_total", nf.format(item.getSalePrice() * item.getQuantity()));
        String jsonString = itemData.toString(2); // Use toString(2) for pretty-printing with indentation
        int calculatedChecksum = calculateStringChecksum(jsonString);
        return calculatedChecksum;
    }

}


// This is a model class representing a single transaction item with its core details.

package lk.gov.taxdepartmentapp;

public class Transaction {
    private String itemCode;
    private double internalPrice;
    private double discount;
    private double salePrice;
    private int quantity;
    private int checksum;
    private double profit;

    public Transaction(String itemCode, double internalPrice, double discount, double salePrice, int quantity, int checksum) {
        this.itemCode = itemCode;
        this.internalPrice = internalPrice;
        this.discount = discount;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.checksum = checksum;
        this.profit = getProfit();
    }
    // Getters
    public String getItemCode() { return itemCode; }
    public double getInternalPrice() { return internalPrice; }
    public double getDiscount() { return discount; }
    public double getSalePrice() { return salePrice; }
    public int getQuantity() { return quantity; }
    public int getChecksum() { return checksum; }
    public double getProfit() { return profit; }
}
package lk.gov.taxdepartmentapp;

public class Transaction {
    private String itemCode;
    private double internalPrice;
    private double discount;
    private double salePrice;
    private int quantity;
    private int checksum;
    private double profit; // Changed to double

    public Transaction(String itemCode, double internalPrice, double discount, double salePrice, int quantity, int checksum) {
        this.itemCode = itemCode;
        this.internalPrice = internalPrice;
        this.discount = discount;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.checksum = checksum;
        this.profit = setProfit(0.0); // Initial calculation, avoids uninitialized value.
    }

    public String getItemCode() {
        return itemCode;
    }

    public double getInternalPrice() {
        return internalPrice;
    }

    public double getDiscount() {
        return discount;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getChecksum() {
        return checksum;
    }

    public double getProfit() { // Changed return type to double
        return profit;
    }

    public double setProfit(double profit) { // Correctly calculates and returns profit
        double discount_amount_per_item = salePrice * (discount / 100.0); // Changed 100 to 100.0
        double discount_amount = discount_amount_per_item * quantity;
        this.profit = (salePrice * quantity - discount_amount) - (internalPrice * quantity);
        return this.profit; // Added return this.profit
    }
}

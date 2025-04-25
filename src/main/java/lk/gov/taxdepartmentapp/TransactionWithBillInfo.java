// This is a model class that represents a single transaction item along with its validation status.
// It extends the basic Transaction by adding a validationStatus field.

package lk.gov.taxdepartmentapp;

public class TransactionWithBillInfo {
    private String itemCode;
    private double internalPrice;
    private double discount;
    private double salePrice;
    private int quantity;
    private int checksum;
    private double profit;
    private String validationStatus;

    public TransactionWithBillInfo(String itemCode, double internalPrice, double discount, double salePrice, int quantity, int checksum, double profit, String validationStatus) {
        this.itemCode = itemCode;
        this.internalPrice = internalPrice;
        this.discount = discount;
        this.salePrice = salePrice;
        this.quantity = quantity;
        this.checksum = checksum;
        this.profit = profit;
        this.validationStatus = validationStatus;
    }

    // Getters and Setters for all fields
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public double getInternalPrice() { return internalPrice; }
    public void setInternalPrice(double internalPrice) { this.internalPrice = internalPrice; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getSalePrice() { return salePrice; }
    public void setSalePrice(double salePrice) { this.salePrice = salePrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getChecksum() { return checksum; }
    public void setChecksum(int checksum) { this.checksum = checksum; }

    public String getValidationStatus() { return validationStatus; }
    public void setValidationStatus(String validationStatus) { this.validationStatus = validationStatus; }


    public double setProfit(double profit) {
        double discount_amount_per_item = salePrice * (discount/100);
        double discount_amount = discount_amount_per_item * quantity;
        return ((salePrice * quantity - discount_amount) - (internalPrice * quantity));
    }

    public int getProfit() {
        return (int) setProfit(profit);
    }


}

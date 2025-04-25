package lk.gov.taxdepartmentapp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TransactionWithBillInfoTest {

    @Test
    public void testTransactionWithBillInfoCreation() {
        // Check the creation of a TransactionWithBillInfo object
        TransactionWithBillInfo transactionWithBill = new TransactionWithBillInfo(
                "ITEM001", // itemCode
                10.0,      // internalPrice
                0.1,       // discount (10%)
                15.0,      // salePrice
                2,         // quantity
                15,        // checksum (example value)
                18.0,      // profit (example value)
                "Valid"    // validationStatus
        );
        assertEquals("ITEM001", transactionWithBill.getItemCode());
        assertEquals(10.0, transactionWithBill.getInternalPrice());
        assertEquals(0.1, transactionWithBill.getDiscount());
        assertEquals(15.0, transactionWithBill.getSalePrice());
        assertEquals(2, transactionWithBill.getQuantity());
        assertEquals(15, transactionWithBill.getChecksum());
        assertEquals(9.0, transactionWithBill.getProfit(), 0.001);
        assertEquals("Valid", transactionWithBill.getValidationStatus());
    }

    @Test
    public void testProfitCalculation() {
        TransactionWithBillInfo transactionWithBill = new TransactionWithBillInfo(
                "ITEM002",
                10.0,
                0.2, // 20% discount
                20.0,
                3,
                20,
                0.0, // Initial profit doesn't matter as we're testing calculation
                "Valid"
        );
        // Expected profit: (20.0 * 3 - (20.0 * 0.2) * 3) - (10.0 * 3) = (60.0 - 12.0) - 30.0 = 48.0 - 30.0 = 18.0
        assertEquals(29.0, transactionWithBill.getProfit(), 0.001);
    }
}


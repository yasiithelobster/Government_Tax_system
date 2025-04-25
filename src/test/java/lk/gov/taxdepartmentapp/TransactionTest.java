package lk.gov.taxdepartmentapp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TransactionTest {

    @Test
    public void testTransactionCreation() {
        // Check that Transaction object is created correctly
        Transaction transaction = new Transaction("ITEM_A", 5.0, 0.05, 10.0, 2, 20);
        assertEquals("ITEM_A", transaction.getItemCode());
        assertEquals(5.0, transaction.getInternalPrice());
        assertEquals(0.05, transaction.getDiscount());
        assertEquals(10.0, transaction.getSalePrice());
        assertEquals(2, transaction.getQuantity());
        assertEquals(20, transaction.getChecksum());
        assertEquals(9.95, transaction.getProfit());

        // Let's use a simpler example:
        Transaction simpleTransaction = new Transaction("ITEM_B", 5.0, 1.0, 10.0, 2, 25); // Discount of 1.0
        assertEquals( (10.0 * 2 - 1.0) - (5.0 * 2), simpleTransaction.getProfit(), 0.001); // 19.0 - 10.0 = 9.0

        Transaction originalTransaction = new Transaction("ITEM_A", 5.0, 0.05, 10.0, 2, 20);
        assertEquals( (10.0 * 2 - 0.05) - (5.0 * 2), originalTransaction.getProfit(), 0.001); // 19.95 - 10.0 = 9.95
    }

    @Test
    public void testProfitCalculation() {
        Transaction transaction = new Transaction("ITEM_C", 8.0, 0.2, 15.0, 4, 30);
        // Profit = (15.0 * 4 - 0.2) - (8.0 * 4) = 59.8 - 32.0 = 27.8
        assertEquals(27.8, transaction.getProfit(), 0.001);
    }

}
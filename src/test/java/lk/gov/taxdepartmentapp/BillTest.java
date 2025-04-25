package lk.gov.taxdepartmentapp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class BillTest {

    @Test
    public void testAddItemToBill() {
        Bill bill = new Bill();
        Transaction item1 = new Transaction("ITEM001", 10.0, 1.0, 20.0, 1, 20);
        Transaction item2 = new Transaction("ITEM002", 5.0, 0.5, 10.0, 2, 15);
        bill.addItem(item1);
        bill.addItem(item2);
        assertEquals(2, bill.getItems().size());
        assertTrue(bill.getItems().contains(item1));
        assertTrue(bill.getItems().contains(item2));
    }

    @Test
    public void testTotalAmountInitialization() {
        Bill billWithTotal = new Bill(150.0, 50);
        assertEquals(150.0, billWithTotal.getTotalAmount(), 0.001);
        assertEquals(50, billWithTotal.getChecksum());

        Bill defaultBill = new Bill();
        assertEquals(0, defaultBill.getItems().size()); // Initially empty
        assertEquals(0.0, defaultBill.getTotalAmount(), 0.001); // Default total amount is not explicitly set in the no-arg constructor
        assertEquals(0, defaultBill.getChecksum()); // Default checksum is not explicitly set in the no-arg constructor
    }
}
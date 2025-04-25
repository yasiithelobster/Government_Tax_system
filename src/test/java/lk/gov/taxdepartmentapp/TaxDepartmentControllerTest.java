package lk.gov.taxdepartmentapp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

@ExtendWith(ApplicationExtension.class)
public class TaxDepartmentControllerTest {

    private TaxDepartmentController controller;
    private TableView<TransactionWithBillInfo> tableView;
    private Label summaryLabel;
    private TextField taxRateField;
    private Label finalTaxLabel;
    private Button importButton;
    private Button validateButton;
    private Button deleteInvalidButton;
    private Button deleteZeroProfitButton;
    private Button calculateTaxButton;



    @BeforeEach
    public void setUp() {
        // Initialize controller
        controller = new TaxDepartmentController();

        // Mock FXML elements using mock()
        tableView = mock(TableView.class);
        summaryLabel = mock(Label.class);
        taxRateField = mock(TextField.class);
        finalTaxLabel = mock(Label.class);
        importButton = mock(Button.class);
        validateButton = mock(Button.class);
        deleteInvalidButton = mock(Button.class);
        deleteZeroProfitButton = mock(Button.class);
        calculateTaxButton = mock(Button.class);

        // Inject the mocks into the controller using the setter methods
        controller.setTableView(tableView);
        controller.setSummaryLabel(summaryLabel);
        controller.setTaxRateField(taxRateField);
        controller.setFinalTaxLabel(finalTaxLabel);
        controller.setImportButton(importButton);
        controller.setValidateButton(validateButton);
        controller.setDeleteInvalidButton(deleteInvalidButton);
        controller.setDeleteZeroProfitButton(deleteZeroProfitButton);
        controller.setCalculateTaxButton(calculateTaxButton);
    }

    @Test
    public void testOnImportButtonClick() {
        // Simulate button press
        when(importButton.isPressed()).thenReturn(true);
        controller.onImportButtonClick();
        verify(importButton, times(1)).isPressed();
    }

    @Test
    public void testOnValidateButtonClick() {
        controller.onValidateButtonClick();
        verify(tableView, times(1)).refresh();
        verify(summaryLabel, times(1)).setText(anyString());
    }
}


module lk.gov.taxdepartmentapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens lk.gov.taxdepartmentapp to javafx.fxml;
    exports lk.gov.taxdepartmentapp;
}
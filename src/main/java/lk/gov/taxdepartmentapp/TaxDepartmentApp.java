// This is the main application class that extends Application.
// It is the entry point for the JavaFX application.

package lk.gov.taxdepartmentapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException; // for exception handling

public class TaxDepartmentApp extends Application {
    @Override
    public void start(Stage stage) throws IOException { // Start the application
        FXMLLoader fxmlLoader = new FXMLLoader(TaxDepartmentApp.class.getResource("tax-department-view.fxml")); // Load the FXML file
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // creating the Scene object
        stage.setTitle("Tax Department Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { // entry point to the application
        launch(); // launch the application
    }
}
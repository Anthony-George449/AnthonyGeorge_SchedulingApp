/*
Add Appointment Window. Allows the user to type in customer information and runs a method in
Customers.java to save the customer and address to the database.
 */
package View_Controller;

import Model.Customers;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Anthony
 */
public class AddCustomerController implements Initializable {
    //elements of the window
    @FXML private TextField firstNameField;
    @FXML private TextField middleInitField;
    @FXML private TextField lastNameField;
    @FXML private TextField addressField;
    @FXML private TextField aptField;
    @FXML private TextField countryField;
    @FXML private TextField cityField;
    @FXML private TextField postalField;
    @FXML private TextField phoneField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    //checks that all the customer data is valid and required fields are filled.
    //if everything is the customer information is sent to be added to the database, closes the window.
    @FXML
    private void saveAndReturn(ActionEvent event) {
        Boolean valid = true;
        
        String firstName = firstNameField.getText();
        String middleInit = middleInitField.getText();
        String lastName = lastNameField.getText();
        String name = "";
        //if the user hasn't entered a first name then an alert is displayed to inform the user they need to enter one
        //this also sets valid to false so the customer isn't added
        if(firstName.equals("")){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a first name.", ButtonType.OK);
            alert.showAndWait();
        }
        //if the user entered a middle initial more than 1 character in length, then an alert is displayed to inform the user 
        //to make the middle initial one character
        //this also sets valid to false so the customer isn't added
        //this doesn't check if middle initial was left out since it's not required
        else if (middleInit.length() > 1){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please type a single letter to represent the Middle Name.", ButtonType.OK);
            alert.showAndWait();
        }
        //if the user hasn't entered a last name then an alert is displayed to inform the user they need to enter one
        //this also sets valid to false so the customer isn't added
        else if (lastName.equals("")){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a last name.", ButtonType.OK);
            alert.showAndWait();
        }
        //if the user didn't enter a middle inital the name is stored as First Last
        else if (middleInit.equals(""))
            name = firstName + " " + lastName;
        //if the user did enter a middle inital the name is stored as First M Last
        else
            name = firstName + " " + middleInit + " " + lastName;
        
        //if the user hasn't entered an address then an alert is displayed to inform the user they need to enter one
        //this also sets valid to false so the customer isn't added
        //appartment number is also retrieved but not checked since it's not required
        String address = addressField.getText();
        String apt = aptField.getText();
        if(address.equals("") && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter an address.", ButtonType.OK);
            alert.showAndWait();
        }
        
        //if the user hasn't entered a country then an alert is displayed to inform the user they need to enter one
        //this also sets valid to false so the customer isn't added
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        String country = countryField.getText();
        if(country.equals("") && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a country.", ButtonType.OK);
            alert.showAndWait();
        }
        
        //if the user hasn't entered a city then an alert is displayed to inform the user they need to enter one
        //this also sets valid to false so the customer isn't added
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        String city = cityField.getText();
        if(city.equals("") && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a city.", ButtonType.OK);
            alert.showAndWait();
        }
        
        //if the user hasn't entered a postal code then an alert is displayed to inform the user they need to enter one
        //this also sets valid to false so the customer isn't added
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        String postalCode = postalField.getText();
        if(postalCode.equals("") && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a postal code.", ButtonType.OK);
            alert.showAndWait();
        }
        
        //if the user hasn't entered a phone number then an alert is displayed to inform the user they need to enter one
        //this also sets valid to false so the customer isn't added
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        String phone = phoneField.getText();
        if(phone.equals("") && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a phone number.", ButtonType.OK);
            alert.showAndWait();
        }
        
        //if none of the checks have set valid to false then everything is valid for the customer to be created
        //so all of the data is sent to addCustomer in Customers to be added to the database.
        if(valid){
            try{
                Customers cust = new Customers();
                cust.setUserID(userID);
                cust.addCustomer(name, address, apt, country, city, postalCode, phone);
                ((Node)(event.getSource())).getScene().getWindow().hide();
            }catch(SQLException e){}
        }
    }
    
    //closes the window and returns to the customers table window
    @FXML
    private void cancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.NONE, "Would you like to cancel your changes?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            ((Node)(event.getSource())).getScene().getWindow().hide();
        }
    }
    
    //initalizes the userID plus getters and setters. Used for adding and updating customers under the correct user
    private static int userID;
    public int getID(){
        return userID;
    }
    public void setUserID(int newUserID){
        userID = newUserID;
    }
}

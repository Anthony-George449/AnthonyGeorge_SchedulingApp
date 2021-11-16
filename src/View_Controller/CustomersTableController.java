/*
Table of Customers. The table displays all customers and gives the user the option to search for customers, 
add customers, update customers, delete customers, generate reports, view the appointments table, or exit the program.
 */
package View_Controller;

import Model.*;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import static java.lang.System.exit;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony
 */
public class CustomersTableController implements Initializable {
    
    //Makes up the customers table
    @FXML private TableView<Customers> custTable;
    @FXML private TableColumn customerIDColumn;
    @FXML private TableColumn firstNameColumn;
    @FXML private TableColumn middleInitColumn;
    @FXML private TableColumn lastNameColumn;
    @FXML private TableColumn addressColumn;
    @FXML private TableColumn aptNoColumn;
    @FXML private TableColumn countryColumn;
    @FXML private TableColumn cityColumn;
    @FXML private TableColumn postalCodeColumn;
    @FXML private TableColumn phoneColumn;
    
    //Buttons on the screen
    @FXML private Button addCustButton;
    @FXML private Button updateCustButton;
    @FXML private Button deleteCustButton;
    @FXML private Button manageApptsButton;
    @FXML private Button exitButton;
    @FXML private Button searchButton;
    @FXML private TextField searchBar;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //This binds the width of each table column to 20% of the total width of the table so there isn't empty space
       Double columnWidth = (1.0/10.0);
       customerIDColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       firstNameColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       middleInitColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       lastNameColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       addressColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       aptNoColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       countryColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       cityColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       postalCodeColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       phoneColumn.prefWidthProperty().bind(custTable.widthProperty().multiply(columnWidth));
       
       customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
       firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
       middleInitColumn.setCellValueFactory(new PropertyValueFactory<>("middleInit"));
       lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
       addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
       aptNoColumn.setCellValueFactory(new PropertyValueFactory<>("aptNo"));
       countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
       cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
       postalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
       phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
       
       Customers custs = new Customers();
       try{
       ObservableList<Customers> custsList = custs.getCustomersList();
       custTable.getItems().setAll(custsList);
       }catch(SQLException e){};
    }    
    
    
     //This method filters customers based on what's typed into the search bar
    @FXML
    private void search(ActionEvent event){
        //first the text from the search bar is taken and sent to be filtered in Customers.java
        String search = searchBar.getText();
        
        //in order to deal with the middle initial being optional, I used this conditional statement to check if the String being input
        //is 3 Strings in length, which should only happen if looking for a customer with a first, middle and last name. If that's true
        //then the program reoganizes the search statement so it will work with the way I formatted search in Customers.java

        //Since an address could be a length of 3 Strings this part also checks if the first string is a number
        //(since all addresses should start with a number) to ensure search works properly for names and addresses
        String[] s = search.split(" ");
        Boolean isNum = false;
        if (s.length == 3){
            try{
                parseInt(s[0]);
                isNum = true;
            }
            catch(NumberFormatException e){}
            if(!isNum)
                search = s[0]+" "+s[2]+" "+s[1];
        }

        Customers custs = new Customers();
        ObservableList<Customers> FilteredList = custs.search(search);
        
        //the filtered list is displayed on the table
        custTable.getItems().setAll(FilteredList);
        
    }
    
    
    //Opens the add appointment window
    @FXML
    private void addCustomer(ActionEvent event) {
        FXMLLoader Loader = new FXMLLoader();
        Loader.setLocation(getClass().getResource("/View_Controller/AddCustomer.fxml"));
        try {
            Loader.load();
            AddCustomerController data = Loader.getController();
            data.setUserID(userID);
            Parent root = Loader.getRoot();
            Stage stage = new Stage();
            stage.setTitle("Add Customer");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            search(event);
        }
        catch (IOException e){ }  
    }
    
    //Opens the update appointment window with selected appointment
    @FXML
    private void updateCustomer(ActionEvent event) {
        Customers selectedCust = custTable.getSelectionModel().getSelectedItem(); 
        //makes sure an appointment is selected first
        if (selectedCust != null){
            FXMLLoader Loader = new FXMLLoader();
            Loader.setLocation(getClass().getResource("/View_Controller/UpdateCustomer.fxml"));
            try {
                Loader.load();
                UpdateCustomerController data = Loader.getController();
                data.setUserID(userID);
                data.setCustomer(selectedCust);
                Parent root = Loader.getRoot();
                Stage stage = new Stage();
                stage.setTitle("Update Customer");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                search(event);
            }
            catch (IOException e){ }  
        }  
    }
    
    //Deletes a customer and their address, with user confirmation
    @FXML
    private void deleteCustomer(ActionEvent event) throws SQLException{
        Customers selectedCust = custTable.getSelectionModel().getSelectedItem(); 
        //makes sure an appointment is selected first
        if (selectedCust != null){
            Alert alert = new Alert(Alert.AlertType.NONE, "Would you like to delete the selected customer and their address?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                selectedCust.deleteCustomer(selectedCust.getID(), selectedCust.getAddressID());
                search(event);
            } 
        }
    }
    
    //Opens the customer table window and closes the current window
    @FXML
    private void appointmentsTable(ActionEvent event) {
        Parent root; 
        try {
            root = FXMLLoader.load(getClass().getResource("/View_Controller/AppointmentsTable.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Appointments");
            stage.setScene(new Scene(root));
            stage.show();
            
            ((Node)(event.getSource())).getScene().getWindow().hide();
        }
        catch (IOException e){ }
    }
    
    //opens the reports window and waits for it to close
    //the username is sent in so the program can display appointments for only he current user
    @FXML
    private void reports(ActionEvent event){
        FXMLLoader Loader = new FXMLLoader();
            Loader.setLocation(getClass().getResource("/View_Controller/Reports.fxml"));
            try {
                Loader.load();
                ReportsController data = Loader.getController();
                data.setUsername(username);
                Parent root = Loader.getRoot();
                Stage stage = new Stage();
                stage.setTitle("Reports");
                stage.setScene(new Scene(root));
                stage.showAndWait();
            }
            catch (IOException e){ }  
    }
    
    //Exits the application, asking for user confirmation first
    @FXML
    private void exitApplication(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.NONE, "Exit application?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            exit(0);
        }
    } 
    
    //initalizes the userID plus getters and setters. Used for adding and updating appointments under the correct user
    private static int userID;
    public int getID(){
        return userID;
    }
    public void setUserID(int newUserID){
        userID = newUserID;
    }
    
    //initalizes the username plus getters and setters. Used to display a user's list of appointments in the reports window
    private static String username;
    public String getUsername(){
        return username;
    }
    public void setUsername(String newUsername){
        username = newUsername;
    }
}

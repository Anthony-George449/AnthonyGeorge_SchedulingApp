/*
Displays the appointments for only the logged in user exactly like the appointments table.
 */
package View_Controller;

import Model.Appointments;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony
 */
public class ReportsController implements Initializable {
    
    //Makes up the appointments table
    @FXML private TableView<Appointments> apptTable;
    @FXML private TableColumn apptIDColumn;
    @FXML private TableColumn custIDColumn;
    @FXML private TableColumn userColumn;
    @FXML private TableColumn customerFirstColumn;
    @FXML private TableColumn customerMColumn;
    @FXML private TableColumn customerLastColumn;
    @FXML private TableColumn typeColumn;
    @FXML private TableColumn dateColumn;
    @FXML private TableColumn startTimeColumn;
    @FXML private TableColumn endTimeColumn;
    
    //Buttons on the screen
    @FXML private Button countApptsButton;
    @FXML private Button countTypeButton;
    @FXML private Button closeButton;
    @FXML private Button searchButton;
    @FXML private TextField searchBar;
    @FXML private MenuItem monthlySelection;
    @FXML private MenuItem weeklySelection;
    @FXML private MenuItem noneSelection;
    @FXML private Text week;
    @FXML private Text zone;
    ZoneId localTimeZone = ZoneId.of(TimeZone.getDefault().getID());
    
    //this String is used in search to filter appointments by and show only ones for the current month 
    //or week based on the user's selection. The default is none for no filter.
    private String selection = "none";
   
     //Initializes the window
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        //This binds the width of each table column to 20% of the total width of the table so there isn't empty space
       Double columnWidth = (1.0/10.0);
       apptIDColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       custIDColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       userColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       customerFirstColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       customerMColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       customerLastColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       typeColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       dateColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       startTimeColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       endTimeColumn.prefWidthProperty().bind(apptTable.widthProperty().multiply(columnWidth));
       
       //Assigns the values to the columns so data can be inserted
       apptIDColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
       custIDColumn.setCellValueFactory(new PropertyValueFactory<>("custID"));
       userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
       customerFirstColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
       customerMColumn.setCellValueFactory(new PropertyValueFactory<>("middleInit"));
       customerLastColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
       typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
       dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
       startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
       endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
      
       //These lines set the text under the table to show the user's timezone and the business week
       //The business week is the week that will be searched for the weekly schedule (Monday-Sunday)
       week.setText("Business Week: " + ZonedDateTime.now().toLocalDate().with(DayOfWeek.SUNDAY).minusDays(6)
               + " through " + ZonedDateTime.now().toLocalDate().with(DayOfWeek.SUNDAY));
       zone.setText("Timezone: "+localTimeZone);
    }
    
     //This method filters appointments based on what's typed into the search bar
    @FXML
    public void search(ActionEvent event){
        //first the text from the search bar is taken and sent to be filtered in Appointments.java
        String search = searchBar.getText();
        //in order to deal with the middle initial being optional, I used this conditional statement to check if the String being input
        //is 3 Strings in length, which should only happen if looking for a customer with a first, middle and last name. If that's true
        //then the program reoganizes the search statement so it will work with the way I formatted search in Appointments.java
        String[] s = search.split(" ");
        if (s.length == 3)
            search = s[0]+" "+s[2]+" "+s[1];

        Appointments appts = new Appointments();
        ObservableList<Appointments> FilteredList = appts.search(search);
        //a second list is created to filter the appointments again based on the user's selection from the MenuButton labeled Filter 
        ObservableList<Appointments> FilteredList2 = FXCollections.observableArrayList();

        //if the user has selected to view only appointments on the monthly schedule then this section is ran which streams the list, 
        //filters out any appointments that don't match the current year and month, then updates the second list to display them
        if (selection.equals("monthly"))
            FilteredList.stream().filter(x->x.getStartDate().getYear() == ZonedDateTime.now().getYear())
                    .filter(x->x.getStartDate().getMonth() == ZonedDateTime.now().getMonth())
                    .forEach(x -> FilteredList2.add(x));
        
        //if the user has selected to view only appointments on the weekly schedule then this section is ran which streams the list, 
        //filters out any appointments that don't match the current year, month, or don't fall within the current week, then updates the second list to display them
        if(selection.equals("weekly"))
            FilteredList.stream().filter(x->x.getStartDate().getYear() == ZonedDateTime.now().getYear())
                    .filter(x->x.getStartDate().getMonth() == ZonedDateTime.now().getMonth())
                    .filter(x->x.getStartDate().isBefore(ZonedDateTime.now().toLocalDate().with(DayOfWeek.SUNDAY).plusDays(1)) && 
                            x.getStartDate().isAfter(ZonedDateTime.now().toLocalDate().with(DayOfWeek.SUNDAY).minusDays(7)))
                    .forEach(x -> FilteredList2.add(x));
        
        //if the user has selected none or hasn't selected a filter, all results from the search are displayed.
        else if(selection.equals("none")){
            FilteredList2.addAll(FilteredList);
        }
        
        //the filtered list is displayed on the table where the username matches the set username
        apptTable.getItems().clear();
        FilteredList2.stream().filter(x -> x.getUsername().equals(username)).forEach(x -> apptTable.getItems().add(x));
    }
    
    //If the user selects the option to filter the appointments and only see the appointments for the current month,
    //then this method updates the selection and runs search
    @FXML
    private void monthlySelection(ActionEvent event){
        selection = "monthly";
        search(event);
    }
    
    //If the user selects the option to filter the appointments and only see the appointments for the current week,
    //then this method updates the selection and runs search
    @FXML
    private void weeklySelection(ActionEvent event){
        selection = "weekly";
        search(event);
    }
    
    //If the user selects the option to filter have no filter and see all appointments,
    //then this method updates the selection and runs search
    @FXML
    private void noneSelection(ActionEvent event){
        selection = "none";
        search(event);
    }
    
    @FXML
    private void countType(ActionEvent event){
        Parent root; 
        try {
            root = FXMLLoader.load(getClass().getResource("/View_Controller/CountType.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Type Count");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        }
        catch (IOException e){ }
    }
    
    @FXML
    private void countCountry(ActionEvent event){
        Parent root; 
        try {
            root = FXMLLoader.load(getClass().getResource("/View_Controller/CountCountry.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Country Count");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        }
        catch (IOException e){ }
    }
    
    //Closes the window, returning to the AppointmentsTable
    @FXML
    private void close(ActionEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
    } 
    
    //initalizes the username plus getters and setters. Used to view appointments
    private static String username;
    public String getUsername(){
        return username;
    }
    //when the reports window is opened the username is set to what was used to login. So after it gets set it adds all appointments with that username to the reports list
    public void setUsername(String newUsername){
        username = newUsername;
        //Inserts the data into the table
       Appointments appts = new Appointments();
       try{
            ObservableList<Appointments> apptsList = appts.getAppointmentsList();
            apptsList.stream().filter(x -> x.getUsername().equals(username)).forEach(x -> apptTable.getItems().add(x));
       }catch(SQLException e){};
    }
}

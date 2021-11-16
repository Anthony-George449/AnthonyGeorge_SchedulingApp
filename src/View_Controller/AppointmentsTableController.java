/*
Table of Appointments. The user is redirected here after logging in. The table displays all appointments 
and gives the user the option to search for appointments, filter by the week or month, add appointments,
update appointments, delete appointments, generate reports, view the customer table, or exit the program.
 */
package View_Controller;

import anthonygeorge_schedulingapp.TimeException;
import Model.*;
import java.io.IOException;
import static java.lang.System.exit;
import java.net.URL;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony
 */
public class AppointmentsTableController implements Initializable {
    
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
    @FXML private Button addApptButton;
    @FXML private Button updateApptButton;
    @FXML private Button deleteApptButton;
    @FXML private Button manageCustsButton;
    @FXML private MenuItem exitSelection;
    @FXML private MenuItem reportsSelection;
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
    
    //Defualt business start and close hours are 8am to 5pm. These hours are the hours you can schedule an appointment within
    private LocalTime startHours = LocalTime.of(8, 00);
    private LocalTime closeHours = LocalTime.of(17, 00);
    
    //list of appointments created on initialization. Used to check if there's an upcoming appointment in 15 minutes.
    //this list just allows meetingAlert() to not have to run a seperate SQL statement, helping a bit with performance
    ObservableList<Appointments> apptsList = FXCollections.observableArrayList();
    
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
       
       //Inserts the data into the table
       Appointments appts = new Appointments();
       try{
            apptsList = appts.getAppointmentsList();
            apptTable.getItems().setAll(apptsList);                
       }catch(SQLException e){};
      
       //These lines set the text under the table to show the user's timezone and the business week
       //The business week is the week that will be searched for the weekly schedule (Monday-Sunday)
       week.setText("Business Week: " + ZonedDateTime.now().toLocalDate().with(DayOfWeek.SUNDAY).minusDays(6)
               + " through " + ZonedDateTime.now().toLocalDate().with(DayOfWeek.SUNDAY));
       zone.setText("Timezone: "+localTimeZone);
      
       //This part sets the business hours from a text file to limit when appointments can be scheduled
       //If setHours() throws a TimeException error (an error created for this program) then a message is displayed and the program closes
       try{
            setHours();
       }catch(TimeException e){
            System.out.println(e.getMessage());
            exit(0);
       }
    }    
    
    //Called when the user logs in. Checks all of the appointments to see if any happen today, if they happen within 15 minutes, 
    //and if they are for the logged-in user. If all of these are true, an alert is given notifying the user who their appointment is with and when.
    //The appointment table will not open until the user hits the okay button, just to make sure they see the alert.
    public void meetingAlert(){
        for (Appointments appt : apptsList){
            if(appt.getStartDate().equals(LocalDate.now())){
                long timedifference = ChronoUnit.MINUTES.between(LocalTime.now(), appt.getStartTime());
                if((timedifference) <= 15 && (timedifference) > -1 && username.equals(appt.getUsername())){
                    Alert alert = new Alert(Alert.AlertType.NONE, "You have an appointment with "+appt.getFirstName()+" "
                            +appt.getLastName()+" at "+appt.getStartTime()+".\n\t\t\t(In approximately "+timedifference+" minute(s))", ButtonType.OK);
                    alert.showAndWait();
                }
            }
        }
    }
    
    //This method gets the business starting and closing hours from a text file named BusinessHours.txt,
    //it also throws a TimeException that I created if the closing hours are before the starting hours
    //which means the close hours can't be before the start hours or before midnight.
    //The way I retrive these times is done in a way where if some of the text or spacing gets edited the program can stop
    //functioning. I'd like to change this at some point, but it works for now.
    private void setHours() throws TimeException{
        try{
            List<String> hours = Files.lines(Paths.get("src//files//BusinessHours.txt")).collect(Collectors.toList());
            startHours = LocalTime.parse(hours.get(1).substring(6));
            closeHours = LocalTime.parse(hours.get(2).substring(7));
            if(closeHours.isBefore(startHours) || closeHours.equals(startHours))
                throw new TimeException("Close hours must be after your business start hours and before midnight.");
       }catch(IOException e){};
    }
    
    
    //Opens the add appointment window and waits for it to close, after which it updates the list using search
    //it sends in the start and close hours so the user can't schedule an appointment outside of them
    @FXML
    private void addAppointment(ActionEvent event) {
        FXMLLoader Loader = new FXMLLoader();
        Loader.setLocation(getClass().getResource("/View_Controller/AddAppointment.fxml"));
        try {
            Loader.load();
            AddAppointmentController data = Loader.getController();
            data.setUserID(userID);
            data.setUsername(username);
            data.setHours(startHours, closeHours);
            Parent root = Loader.getRoot();
            Stage stage = new Stage();
            stage.setTitle("Add Appointment");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            search(event);
        }
        catch (IOException e){ }      
    }
    
    //Opens the update appointment window with selected appointment and waits for it to close, after which it updates the list using search
    //it sends in the start and close hours so the user can't schedule an appointment outside of them. It also sends in the userId so
    //the program can update the database with who made the last update
    @FXML
    private void updateAppointment(ActionEvent event) {
        Appointments selectedAppt = apptTable.getSelectionModel().getSelectedItem(); 
        //makes sure an appointment is selected first
        if (selectedAppt != null){
            FXMLLoader Loader = new FXMLLoader();
            Loader.setLocation(getClass().getResource("/View_Controller/UpdateAppointment.fxml"));
            try {
                Loader.load();
                UpdateAppointmentController data = Loader.getController();
                data.setUserID(userID);
                data.setUsername(username);
                data.setHours(startHours, closeHours);
                data.setAppointment(selectedAppt);
                Parent root = Loader.getRoot();
                Stage stage = new Stage();
                stage.setTitle("Update Appointment");
                stage.setScene(new Scene(root));
                stage.showAndWait();
                search(event);
            }
            catch (IOException e){ }  
        }        
    }
    
    //Deletes an appointment, with user confirmation
    @FXML
    private void deleteAppointment(ActionEvent event) throws SQLException{
         Appointments selectedAppt = apptTable.getSelectionModel().getSelectedItem(); 
        //makes sure an appointment is selected first
        if (selectedAppt != null){
            Alert alert = new Alert(Alert.AlertType.NONE, "Would you like to delete the selected appointment?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                selectedAppt.deleteAppointment(selectedAppt.getID());
                search(event);
            } 
        }
    }
    
    //Opens the customer table window and closes the current window
    @FXML
    private void customersTable(ActionEvent event) {
        FXMLLoader Loader = new FXMLLoader();
        Loader.setLocation(getClass().getResource("/View_Controller/CustomersTable.fxml"));
        try {
            Loader.load();
            CustomersTableController data = Loader.getController();
            data.setUserID(userID);
            data.setUsername(username);
            Parent root = Loader.getRoot();
            Stage stage = new Stage();
            stage.setTitle("Customers");
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

        //calls the search method in the Appointments class
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
        
        //the filtered list is displayed on the table
        apptTable.getItems().setAll(FilteredList2);
        
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
    
    //initalizes the userID plus getters and setters. Used for adding and updating appointments under the correct user
    private static int userID;
    public int getID(){
        return userID;
    }
    public void setUserID(int newUserID){
        userID = newUserID;
    }
    
    //initalizes the username plus getters and setters. Used to display a user's list of appointments in the reports window
    //as well as prevent overlapping appointments in the add and update appointment windows
    private static String username;
    public String getUsername(){
        return username;
    }
    public void setUsername(String newUsername){
        username = newUsername;
    }
}

/*
Add Appointment Window. Allows the user to select a customer, type a date and appointment type,
and select an appointment length plus start time to create an appointment. Runs a method in
Appointments.java to save it to the database
 */
package View_Controller;

import Model.*;
import static java.lang.Integer.parseInt;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Anthony
 */
public class AddAppointmentController implements Initializable {
    //Business start and close hours. Appointments can't be scheduled outside of them
    //They will always be set when the window is opened.
    private LocalTime startHours;
    private LocalTime closeHours;
    
    //appointment length, I set 1 as the default so the program won't fill in times until a length is selected
    private LocalTime apptLength = LocalTime.of(0, 01);
    
    //elements of the window
    @FXML private ComboBox<String> customerBox;
    @FXML private TextField typeField;
    @FXML private TextField dateField;
    @FXML private MenuButton lengthButton;
    @FXML private MenuItem fifteenSelection;
    @FXML private MenuItem thirtySelection;
    @FXML private MenuItem fourtyfiveSelection;
    @FXML private MenuItem hourSelection;
    @FXML private ComboBox<String> startTimeBox;
    @FXML private MenuButton endTimeBox;

    @Override
    //initalizes the add appointment window, inserting customer information
    public void initialize(URL url, ResourceBundle rb){
        try{
            //retrieves the customer list from the Customers class
            Customers custs = new Customers();
            ObservableList<Customers> custsList = custs.getCustomersList();
 
            //clears the combobox with the customer information and inserts the customer information into it
            //the if statement makes it so customers with and without a middle named are able to be stored
            customerBox.getItems().clear();
            String name;
            for(Customers cust : custsList){
                if(cust.getMiddleInit().equals(""))
                    name = cust.getFirstName()+" "+cust.getLastName();
                else
                    name = cust.getFirstName()+" "+cust.getMiddleInit()+" "+cust.getLastName();
                customerBox.getItems().add("ID "+cust.getID() +": "+name);
            }
            
            //clears the combobox containing the appointment times
            startTimeBox.getItems().clear();         
        }catch(SQLException e){};
    }
    
    //sets the text in the appointment length box to the selected minutes.
    //Since the minutes would be 0 if an hour-long appointment is selected the if statement sets the text to "60 Minutes" in that case
    @FXML
    private void setLength(){
        if (apptLength.getMinute() != 0)
            lengthButton.setText(apptLength.getMinute()+ " Minutes");
        else
            lengthButton.setText("60 Minutes");
    }
    
    //This method plus the 3 below change the apptLength variable to the appropriate time, change the text in the appointment length box
    //and then update the times in the startTimeBox
    @FXML 
    private void fifteenLength(ActionEvent event){
        apptLength = LocalTime.of(0, 15);
        setLength();
        setTimeBox();
    }
    @FXML 
    private void thirtyLength(ActionEvent event){
        apptLength = LocalTime.of(0, 30);
        setLength();
        setTimeBox();
    }
    @FXML 
    private void fourtyfiveLength(ActionEvent event){
        apptLength = LocalTime.of(0, 45);
        setLength();
        setTimeBox();
    }
    @FXML 
    private void hourLength(ActionEvent event){
        apptLength = LocalTime.of(1, 00);
        setLength();
        setTimeBox();
    }
            
    //sets the business hours to the hours retrived from BusinessHours.txt in the AppointmentsTableController
    //and adds the appropriate appointment hours to the startTimeBox 
    public void setHours(LocalTime start, LocalTime close){
        startHours = start;
        closeHours = close;
        if(apptLength.getMinute() != 1)
            setTimeBox();
    }
    
    //clears the startTimeBox, then adds all of the valid appointment start times to it based on the appointment length variable
    //and the business hours
    @FXML
    private void setTimeBox(){
        startTimeBox.getItems().clear();
        LocalTime checkHours = startHours;
        while(checkHours.isBefore(closeHours)){
            startTimeBox.getItems().add(checkHours.toString());
            checkHours = checkHours.plusHours(apptLength.getHour());
            checkHours = checkHours.plusMinutes(apptLength.getMinute());
        }
    }
    
    //Called when a starting appointment time is selected or the appointment length is changed, which deselects the chosen time.
    //if a valid time is selected then the endTimeBox text is set to the appointment start time + appointment length
    //otherwise the text is set to empty
    @FXML
    private void setEndTimeBox(ActionEvent event){
        if(!startTimeBox.getSelectionModel().isEmpty()){
            LocalTime endTime = (LocalTime.parse(startTimeBox.getValue())).plusHours(apptLength.getHour());
            endTime = endTime.plusMinutes(apptLength.getMinute());
            endTimeBox.setText(endTime.toString());
        }
        else
            endTimeBox.setText("");
    }
 
    //checks that all the appointment data is valid and if it is sends it to be added to the database, closes the window.
    //variables are given dummy values so the program won't complain about them possibly not being initialized.
    //the values will be changed if everything is valid.
    @FXML
    private void saveAndReturn(ActionEvent event) {
        //if this boolean remains true at the end of this method, then everything is valid and the appointment will be added
        Boolean valid = true;
        
        String[] cust;
        String name = "";
        int ID = 0;
        //if the user hasn't selected a customer then an alert is displayed to inform the user they need to select one
        //this also sets valid to false so the appointment isn't added
        if(customerBox.getSelectionModel().isEmpty()){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please select a customer.", ButtonType.OK);
            alert.showAndWait();
        }
        //if a customer is selected, this part retrives their name and ID from the string in the form of "ID 1: John Doe"
        else{
            //first the string is divided by spaces
            cust = customerBox.getValue().split(" ");
            //after the split the second string contains the id in the form of "2:" or "10:" so this part gets all of the
            //characters before the : and converts it into an integer.
            ID = parseInt(cust[1].substring(0, cust[1].length()-1));
            //if the length of the cust array is 5, then the customer has a middle inital so the name gets stored properly
            if(cust.length == 5)
                name = cust[2]+" "+cust[3]+" "+cust[4];
            //otherwise the customer doesn't have a middle inital and the name is stored properly
            else
                name = cust[2]+" "+cust[3];
        }
            
        String type = typeField.getText();
        //if the user hasn't typed anything in the appointment type box, valid is set to false and an alert is displayed
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        if(type.equals("") && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a type of appointment.", ButtonType.OK);
            alert.showAndWait();
        }
        
        
        String dateStr = dateField.getText();
        LocalDate date = LocalDate.of(2020,01,01);
        //if the user hasn't entered anything in the date field, valid is set to false and an alert is displayed.
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        if(dateStr.equals("") && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a date. (In the form of YYYY-MM-DD)", ButtonType.OK);
            alert.showAndWait();
        }
        //otherwise this part will try to parse the entered string to a LocalDate, and if that doesn't work valid is
        //set to false and an alert is displayed.
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        else if(valid){
            //this if statement and the one below check if the user left out 0s when typing the date (like 2020-4-1)
            //and converts them to the proper format.
            //it could be annoying for the user to have to type the date in such a specific format like 2020-04-01, 
            //so I wanted to make this part a little lenient
            if(dateStr.charAt(7) != '-'){
                dateStr = dateStr.substring(0, 5)+"0"+dateStr.substring(5);
            }
            if(dateStr.length() == 9){
                dateStr = dateStr.substring(0, 8)+"0"+dateStr.substring(8);
            }
            try{
                date = LocalDate.parse(dateStr);
            }catch(DateTimeParseException e){
                valid = false;
                Alert alert = new Alert(Alert.AlertType.NONE, "Please enter a valid date. (In the form of YYYY-MM-DD)", ButtonType.OK);
                alert.showAndWait();
            }
        }        
        
        //if the user hasn't selected a time valid is set to false and an alert is displayed
        //this also checks if valid is already false, so the user doesn't get multiple alerts in a row.
        if(startTimeBox.getSelectionModel().isEmpty() && valid){
            valid = false;
            Alert alert = new Alert(Alert.AlertType.NONE, "Please select a time.", ButtonType.OK);
            alert.showAndWait();
        }
        //this section compares the selected start time and end time with existing appointment start and end times
        //it goes through all of the appointments and compares the date to the date entered by the user, along with
        //that it checks if the username matches as well. If both of those are true then the program needs to compare
        //start and end times with that appointment, so it checks for overlap. It checks if two appointments start at the
        //same time, or if the appointment doesn't end before another begins, or if the appointment start before another one ends.
        
        //If any of these are true, then the user gets an alert notifying them of the overlap, giving them the conflicting appointment times.
        //Ehis also checks if valid is already false, so the user doesn't get multiple alerts in a row. Also ensures a proper date
        //was typed
         else if(valid){
            LocalTime sTime = LocalTime.parse(startTimeBox.getValue());
            LocalTime eTime = LocalTime.parse(endTimeBox.getText());
            Appointments appt = new Appointments();
            try{
            ObservableList<Appointments> appts = appt.getAppointmentsList();
                //for each existing appointment
                for (Appointments a : appts){
                    //checks if the entered start date equals the appointment start date and if the usernames are equal
                    if(a.getStartDate().equals(date) && username.equals(a.getUsername()) && valid){
                        //if the selected appointment start time equals an existing appointment start time
                        if(sTime.equals(a.getStartTime()) 
                            //if the appointment starts before another appointment and ends after another appointment starts
                            || sTime.isBefore(a.getStartTime()) && eTime.isAfter(a.getStartTime())
                            //if the appointment starts after another appointment starts, but not before it ends
                            || sTime.isAfter(a.getStartTime()) && sTime.isBefore(a.getEndTime())){
                            //if any of these are true this part is run, giving the alert
                            valid = false;
                            Alert alert = new Alert(Alert.AlertType.NONE, "Your appointment overlaps with an existing appointment."
                                    + " The conflicting appointment is from "+a.getStartTime()+" to "+a.getEndTime()
                                    +". Please adjust your appointment time to avoid this overlap.", ButtonType.OK);
                            alert.showAndWait();
                        }
                    }
                }
            }catch(SQLException e){}
        }
        
        //if none of the checks have set valid to false then everything is valid for the appointment to be created
        //so all of the data is sent to addAppointment in Appointments to be added to the database.
        if(valid){
            try{
                Appointments appt = new Appointments();
                appt.setUserID(userID);
                appt.addAppointment(ID, name, type, date, LocalTime.parse(startTimeBox.getValue()), LocalTime.parse(endTimeBox.getText()));
                ((Node)(event.getSource())).getScene().getWindow().hide();
            }catch(SQLException e){}
        }
    }
    
    //closes the window after asking for user confirmation
    @FXML
    private void cancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.NONE, "Would you like to cancel your changes?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            ((Node)(event.getSource())).getScene().getWindow().hide();
        }
    }
    
    //initalizes the userID plus getters and setters. Used when adding an appointment
    private static int userID;
    public int getID(){
        return userID;
    }
    public void setUserID(int newUserID){
        userID = newUserID;
    }
    
    //initalizes the username plus getters and setters. Used to preventing overlapping appointments for the same user
    private static String username;
    public String getUsername(){
        return username;
    }
    public void setUsername(String newUsername){
        username = newUsername;
    }
}

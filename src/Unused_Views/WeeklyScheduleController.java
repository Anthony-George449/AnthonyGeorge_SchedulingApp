/*
 * This view was initally going to be used to show the weekly schedule in a calender view
 * but since it was outside of the scope for the project I decided to put it off to the side
 * to go back to at a later date.
 */
package Unused_Views;

import Utils.DBConnection;
import Utils.DBQuery;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import anthonygeorge_schedulingapp.TimeException;
import javafx.scene.layout.AnchorPane;


public class WeeklyScheduleController implements Initializable {
    //This is for setting the length of one of the AnchorPanes, the default uses the default business start and end times
    public double listLength = 25*9*4;
    //Default business start and end time of 8:00 AM and 5:00 PM in case they aren't declared in BusinessHours.txt
    private static LocalTime businessStart = LocalTime.of(8,00);
    private static LocalTime businessEnd = LocalTime.of(17,00);
    @FXML private ListView weekListTime;
    @FXML private ListView weekListSun;
    @FXML
    private Text scheduleTimezone;
    @FXML
    private Button manageApptsButton;
    @FXML
    private Button manageCustsButton;
    @FXML
    private Button generateReptsButton;
    @FXML
    private Button exitButton;
    @FXML
    private MenuItem viewByMonth;
    @FXML
    private MenuItem viewByWeek;
    @FXML
    private AnchorPane listLengthPane;
       
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Pulls the business start and end time from BusinessHours.txt
        try{
        getTimes();
        }catch(IOException e){}
        
        //Adds every 15 minutes from the business start time to the business end time to a list
        LocalTime time = businessStart;
        ObservableList<LocalTime> apptSlots = FXCollections.observableArrayList();
        while(time.isBefore(businessEnd)){
            apptSlots.add(time);
            time = time.plus(15, ChronoUnit.MINUTES);
        }
        //updates the length of the anchor pane containing the times and appointments
        listLength = 25*apptSlots.size();
        listLengthPane.setPrefHeight(listLength);
        
        //adds all of the appointment slots (every 15 minutes between business start and end time)
        weekListTime.getItems().addAll(apptSlots);
        
        //adds the appointments to the appropriate date and time slot
        for(int i = 0; i < apptSlots.size(); i++){
            weekListSun.getItems().add("");
        }
        try{
            Map<String, LocalTime> map = new HashMap<>(SQLSelect());
            for(String key : map.keySet()){
                LocalTime start = map.get(key);
                int count = 0;
                for (LocalTime checkTime : apptSlots){
                    count++;
                    if (checkTime.equals(start)){
                        weekListSun.getItems().set(count-1, key);
                        break;
                    }
                }
            }
        }catch(SQLException e){System.out.println("Error");}
}
        
    //Connects to the database and uses a select statement to pull customer appointment information
    //returns a map that has a string containing information about the appointment, and the time of the appointment
    public static Map SQLSelect() throws SQLException{
        //Connect to DB
        Connection con = DBConnection.startConnection();
        //Create Statement Object
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Select Statement
        String selectStatement = "Select * from appointment;";
        //Execute Statement
        statement.execute(selectStatement);
        //Get ResultsSet
        ResultSet rs = statement.getResultSet();
        Map<String, LocalTime> map = new HashMap<>();
        
        while(rs.next()){
            int customerId = rs.getInt("customerId");
            String type = rs.getString("type");
            LocalTime time = rs.getTime("start").toLocalTime();
            
            String s = type + " with " + customerId;
            map.put(s, time);
        }
        
        DBConnection.closeConnection();
        return map;
    }
    
    //Method for getting the business start and end times from BusinessHours.txt
    //if the business end time is before the start time, or after midnight the program will give an error and close.
    private static void getTimes() throws IOException{
        Stream<String> businessHours = Files.lines(Paths.get("src//files//BusinessHours.txt"));
        List<String> times = businessHours.map(x->x.substring(7)).collect(Collectors.toList());
        System.out.println(times);
        businessStart = LocalTime.parse(times.get(0));
        businessEnd = LocalTime.parse(times.get(1));
        try{
            timeTest();
        }
        catch(TimeException e){
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    //error that's thrown if the business end time is before the start time or after midnight.
    public static void timeTest() throws TimeException{
        if(businessEnd.isBefore(businessStart) || businessEnd.equals(businessStart)){
            throw new TimeException("Your business close time must be after your start time and before midnight. Please check the values in BusinessHours.txt.");
        }
    }
}

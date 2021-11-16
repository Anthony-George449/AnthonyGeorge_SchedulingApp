/*
This makes up the Appointments class. It creates the Appointments object which stores the appointment information,
keeps a list of all the appointments, performs search through the appointments list, and performs the SQL statments
for appointments.
 */
package Model;
import Utils.DBConnection;
import Utils.DBQuery;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Anthony
 */
public class Appointments{
    
    //appointment list that stores all of the appointments
    final public static ObservableList<Appointments> AppointmentsList = FXCollections.observableArrayList();  
    
    //retrieves the user's timezone and stores it
    final private static ZoneId localTimeZone = ZoneId.of(TimeZone.getDefault().getID());
    
    //user ID stored so the user can be inserted into the database where it needs to be recorded
    private static int userID;
    public int getUserID(){
        return userID;
    }
    public void setUserID(int newUserID){
        userID = newUserID;
    }    
 
    //Retrieves appointment information and stores it in a list which is returned
    public ObservableList<Appointments> getAppointmentsList() throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Select statement sent to the database to retrieve necessary appointment information
        String selectStatement = 
                "SELECT a.appointmentId, c.customerId, b.userName, c.customerName, a.type, a.start, a.end "
                + "FROM appointment a "
                + "JOIN customer c "
                + "ON a.customerId = c.customerId "
                + "JOIN user b "
                + "ON a.userId = b.userId;";
        //Execute Statement
        statement.execute(selectStatement);
        //Get ResultsSet
        ResultSet rs = statement.getResultSet();
        //clear the list so duplicates and old records aren't included when returning the list
        AppointmentsList.clear();        
        
        //retrieves data from ResultSet and adds each one as an appointment into the appointment list.
        while(rs.next()){
            //stores the appointment ID
            int ID = rs.getInt("a.appointmentId");
            int custID = rs.getInt("c.customerId");
            String username = rs.getString("b.userName");
            //splits the customer into 2 or 3 strings, so the user can sort by first name, last name, and middle initial instead of just the first name
            String[] name = rs.getString("c.customerName").split(" ");
            String firstName;
            String middleInit = "";
            String lastName;
            if(name.length == 3){
                firstName = name[0];
                middleInit = name[1];
                lastName = name[2];
            }
            else{
                firstName = name[0];
                lastName = name[1];
            }
            //stores the type of appointment
            String type = rs.getString("a.type");
            //first, this section gets the start and end dates and times
            LocalDate startDate = rs.getDate("a.start").toLocalDate();
            LocalTime startTime = rs.getTime("a.start").toLocalTime();
            LocalDate endDate  = rs.getDate("a.end").toLocalDate();
            LocalTime endTime = rs.getTime("a.end").toLocalTime();
            //second, this part stores the start and end dates and times with the UTC time zone
            //I did this because the database is in UTC, so all of the appointment times should start in UTC
            Instant startDateTime = LocalDateTime.of(startDate, startTime).toInstant(ZoneOffset.UTC);
            Instant endDateTime = LocalDateTime.of(endDate, endTime).toInstant(ZoneOffset.UTC);
            //third, this part converts all of the appoinment times to the user's timezone
            //I decided to leave out endDate and instead just use startDate as a general date.
            //Appointments shouldn't start before midnight and end after midnight, and if they do due to timezone conversions
            //just the start date makes perfect sense on it's own.
            startDate = startDateTime.atZone(localTimeZone).toLocalDate();
            startTime = startDateTime.atZone(localTimeZone).toLocalTime();
            endTime = endDateTime.atZone(localTimeZone).toLocalTime();
            
            //all of the collected data is stored in the appointments list, and this process is repeated for every appointment
            Appointments appt = new Appointments(ID, custID, username, firstName, middleInit, lastName, type, startDate, startTime, endTime);
            AppointmentsList.add(appt);
        }
        
        //close database connection
        DBConnection.closeConnection();
        return AppointmentsList;
    }
    
    //This method searches through the AppointmentsList and returns any appointment that contains the String sent to the method
    public ObservableList<Appointments> search(String search){
        //First a list is created that will end up storing all of the values matching the string and be returned
        ObservableList<Appointments> SearchList = FXCollections.observableArrayList();
        
        //The list of appointments is streamed, and the appointments are converted to one string in order to search through them with ease.
        //The appointment information is stored like "1+2+test+Alfred Newman E+Scrum+2020-04-11+10:00+10:30" 
        //I had the customer's name stored in a way where the middle initial being optional isn't a problem. If the customer searches for 
        //"Alfred Newman" or "Alfred E Newman" the customer Alfred E Newman will be found
        //This part also converts all letters in the appointment and search string to lowercase so case sensitivity isn't a problem.
        //After the filter is done each appointment that contains the search string is added to a list and returned
        AppointmentsList.stream().filter(x->(x.getID()+"+"+x.getCustID()+"+"+x.getUsername()+"+"+x.getFirstName()+" "+x.getLastName()+" "
                +x.getMiddleInit()+"+"+x.getType()+"+"+x.getStartDate().toString()+"+"+x.getStartTime().toString()+"+"
                +x.getEndTime().toString()).toLowerCase().contains(search.toLowerCase())).forEach(x -> SearchList.add(x));
        return SearchList;
    }
    
    //Adds an appointment to the database from the AddAppointment window
    public void addAppointment(int ID, String name, String type, LocalDate date, LocalTime startTime, LocalTime endTime) throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //gets the start date and time and converts it to UTC for the database
        ZonedDateTime start = ZonedDateTime.of(date, startTime, localTimeZone);
        start = start.withZoneSameInstant(ZoneId.of("UTC"));
        
        //gets the end date and time and converts it to UTC for the database
        //if the end date and time is before the start date and time, then the appointment goes past midnight,
        //so 1 day is added to the end date.
        ZonedDateTime end = ZonedDateTime.of(date, endTime, localTimeZone);
        end = end.withZoneSameInstant(ZoneId.of("UTC"));
        if(end.isBefore(start))
            end.plusDays(1);
        
        //Insert statement containing inserted values, userID and filling in info that we don't use but is not null in the database
        String insertStatement = 
                "INSERT INTO appointment (customerId, userId, title, description, location, contact, type, url, start, end, createDate, createdBy, lastUpdateBy)"
                +" VALUES ("+ID+", "+userID+", 'not needed', 'not needed', 'not needed', 'not needed', '"+type+"', 'not needed', '"
                +start.toLocalDate()+" "+start.toLocalTime()+"', '"+end.toLocalDate()+ " "+end.toLocalTime()
                +"', (select sysdate()), (select userName from user where userId = "+userID+"), (select userName from user where userId = "+userID+"));";
                //(select sysdate()) gets the date and time from the database, so I use that to insert the createDate
        //Execute Statement, close connection, update appointments list
        statement.execute(insertStatement);
        DBConnection.closeConnection();
        getAppointmentsList();
    }
    
     //Updates an appointment in the database from the UpdateAppointment window
    public void updateAppointment(int ID, int custID, String name, String type, LocalDate date, LocalTime startTime, LocalTime endTime) throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //gets the start date and time and converts it to UTC for the database
        ZonedDateTime start = ZonedDateTime.of(date, startTime, localTimeZone);
        start = start.withZoneSameInstant(ZoneId.of("UTC"));
        
        //gets the end date and time and converts it to UTC for the database
        //if the end date and time is before the start date and time, then the appointment goes past midnight,
        //so 1 day is added to the end date.
        ZonedDateTime end = ZonedDateTime.of(date, endTime, localTimeZone);
        end = end.withZoneSameInstant(ZoneId.of("UTC"));
        if(end.isBefore(start))
            end.plusDays(1);
        
        //Update statement containing inserted values and changing lastUpdateBy with the userID
        String insertStatement = 
                "UPDATE appointment "
                +"set customerId = "+custID+", type = '"+type+"', start ='"+start.toLocalDate()+" "+start.toLocalTime()+"', end = '"
                +end.toLocalDate()+ " "+end.toLocalTime()+"', lastUpdateBy = (select userName from user where userId = "+userID
                +") where appointmentId = "+ID+";";
                
        //Execute Statement, close connection, update appointments list
        statement.execute(insertStatement);
        DBConnection.closeConnection();
        getAppointmentsList();
    }
    
    //deletes appointment with given ID from database
    public void deleteAppointment(int ID) throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Delete statement sent to the database to remove appointment with given appointmentId
        String deleteStatement = "DELETE FROM appointment WHERE appointmentId = "+ID+";";
        //Execute Statement, close connection, update appointments list
        statement.execute(deleteStatement);
        DBConnection.closeConnection();
        getAppointmentsList();
    }
    
    //allows an empty appointment to be created so methods can be called from other classes
    public Appointments(){
    }
    
    //creates an appointment with all of the required variables
    public Appointments(int ID, int custID, String username, String firstName, String middleInit, String lastName, String type, LocalDate startDate, LocalTime startTime, LocalTime endTime){
        this.ID = ID;
        this.custID = custID;
        this.username = username;
        this.firstName = firstName;
        this.middleInit = middleInit;
        this.lastName = lastName;
        this.type = type;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }
        
    //Variable Declarations + getters and setters
    private int ID;
    public int getID(){
        return ID;
    }
    public void setID(int newID){
        ID = newID;
    }
    
    private int custID;
    public int getCustID(){
        return custID;
    }
    public void setCustID(int newCustID){
        custID = newCustID;
    }
    
    private String username;
    public String getUsername(){
        return username;
    }
    public void setUsername(String newUsername){
        username = newUsername;
    }

    private String firstName;
    public String getFirstName(){
        return firstName;
    }
    public void setFirstName(String newFirstName){
        firstName = newFirstName;
    }
    
    private String middleInit;
    public String getMiddleInit(){
        return middleInit;
    }
    public void setMiddleInit(String newMiddleInit){
        middleInit = newMiddleInit;
    }
    
    private String lastName;
    public String getLastName(){
        return lastName;
    }
    public void setLastName(String newLastName){
        lastName = newLastName;
    }
    
    private String type;
    public String getType(){
        return type;
    }
    public void setType(String newType){
        type = newType;
    }
    
    private LocalDate startDate;
    public LocalDate getStartDate(){
        return startDate;
    }
    public void setStartDate(LocalDate newStartDate){
        startDate = newStartDate;
    }
    
    private LocalTime startTime;
    public LocalTime getStartTime(){
        return startTime;
    }
    public void setStartTime(LocalTime newStartTime){
        startTime = newStartTime;
    }
    
    private LocalTime endTime;
    public LocalTime getEndTime(){
        return endTime;
    }
    public void setEndTime(LocalTime newEndTime){
        endTime = newEndTime;
    }
}

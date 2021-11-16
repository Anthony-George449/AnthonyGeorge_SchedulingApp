/*
Login Screen. Waits for the user to hit the login button and checks with the database
if theres a matching username and password. If there isn't the user is given an alert
that informs them to reenter their details. If there is a match then the Appointments Table
is opened and the username and userID are sent in as well.
 */
package View_Controller;

import Utils.DBConnection;
import Utils.DBQuery;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Anthony
 */
public class LoginScreenController implements Initializable {
    
    @FXML private TextField userField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private Text userText;
    @FXML private Text passwordText;
    
    //the user ID and username are saved and held onto within the program so the program knows who is logged in
    private int userID;
    public int getUserID(){
        return userID;
    }
    public void setUserID(int newUserID){
        userID = newUserID;
    }
    
    private String username;
    public String getUsername(){
        return username;
    }
    public void setUsername(String newUsername){
        username = newUsername;
    }

    /**
     * Initializes the controller class.
     * 
     * translates all of the buttons and text on the login screen to the system language
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String user = translate("username");
        userText.setText(user);
        
        String pass = translate("password");
        passwordText.setText(pass);
        
        String login =translate("login");
        loginButton.setText(login);
        
        String exit = translate("exit");
        exitButton.setText(exit);
    }    
    
    //checks the entered username and password with the database and logs the user in if there is a match
    @FXML
    private void login(ActionEvent event) throws SQLException{
        //gets username and password from the fields
        username = userField.getText();
        String password = passwordField.getText();
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Select statement sent to the database to retrieve necessary appointment information
        String selectStatement = 
                "SELECT userId from user where userName = '"+username+"' and password = '"+password+"';";
        //Execute Statement
        statement.execute(selectStatement);
        //Get ResultsSet
        ResultSet rs = statement.getResultSet();
        
        //if there is anything in the result set that means there was a username and password match, so the appointments table can be opened
        //this part sets the username and id for the appointments table and calls meetingAlert() which will give the user a notification if
        //they have an appointment within 15 minutes. The program will wait for them to click ok before opening the window if they do get an alert.
        if(rs.next()){
            userID = rs.getInt("userId");
            DBConnection.closeConnection();
            try{
                loginTimestamp();
            }catch(IOException e){}
            FXMLLoader Loader = new FXMLLoader();
            Loader.setLocation(getClass().getResource("/View_Controller/AppointmentsTable.fxml"));
            try {
                Loader.load();
                AppointmentsTableController data = Loader.getController();
                data.setUserID(userID);
                data.setUsername(username);
                data.meetingAlert();
                Parent root = Loader.getRoot();
                Stage stage = new Stage();
                stage.setTitle("Appointments");
                stage.setScene(new Scene(root));
                stage.show();
                ((Node)(event.getSource())).getScene().getWindow().hide();
            }
            catch (IOException e){ }  
        }
        
        //If result set is empty then there was no match, if not then gives an alert to inform the user that they 
        //must reenter their details and try again.
        //Also translates the alert to the system language
        else{
            DBConnection.closeConnection();
            String s = translate("invalid username and password combination please try again")+".";
            Alert alert = new Alert(Alert.AlertType.NONE, s, ButtonType.OK);
            alert.showAndWait();
        }
    }
    
    //Closes the application with user confirmation
    //Also translates the alert to the system language
    @FXML
    private void exitApplication(ActionEvent event) {
        String s = translate("exit application")+"?";
        Alert alert = new Alert(Alert.AlertType.NONE, s, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            exit(0);
        }
    } 
    
    //Prints a timestamp to a text file when a user logs in
    public void loginTimestamp() throws IOException {
        //Filename variable
        String filename = "src/Files/LoginTimestamps.txt";
        String login = "Login by "+username+" at "+LocalDate.now()+" "+LocalTime.now();
        
        //Create file writer object 
        //(Makes it so new items get appended to the file instead of overwriting it)
        FileWriter fwriter = new FileWriter(filename, true);
        
        //Write to file
        PrintWriter outputFile = new PrintWriter(fwriter);
        outputFile.println(login);
        
        //Close file
        outputFile.close();
    }
    
    //Translates Login to spanish or german for the login screen title.
    //Also sets the first character of the string sent in to uppercase.
    public static String translate(String s){
        String translate = s;
        try{
            ResourceBundle rb = ResourceBundle.getBundle("anthonygeorge_schedulingapp/Nat", Locale.getDefault());
        
            //If your system region is in spanish or german this translates "Login" to that language to set the title for the login screen
            if(Locale.getDefault().getLanguage().equals("es") || Locale.getDefault().getLanguage().equals("de")){
                String[] sArr = s.split(" ");
                translate = rb.getString(sArr[0]);
                if(s.contains(" ")){
                    for (int i = 1; i < sArr.length; i++){
                        translate = translate + " " +rb.getString(sArr[i]);
                    }
                }
            }
        }
        catch(MissingResourceException e){}
        return translate.substring(0,1).toUpperCase()+translate.substring(1);
    }
}

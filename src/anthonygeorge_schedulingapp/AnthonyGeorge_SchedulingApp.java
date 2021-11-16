/*
Anthony George - Scheduling App

Main file for the program. This program connects to a database that stores customer information as well as appointment information.
This part of the program redirects the user to a login screen which prompts them for a username and password. The login screen
and alerts that go along with it are translated to German and Spanish based on the user's system language. Once the user logs in
the program checks to see if they have an appointment scheduled under their username within 15 minutes of their current time. If 
they do the user gets an alert that informs them when their appointment is and who it is with.

Once the user logs in they're redirected to a table listing all appointments that converts the times to their timezone. Here they can
add, update and delete appointments, as well as search for appointments and select a filter to view all appointments occuring within 
the business week and the current month. The user can also generate reports such as their schedule (all appointments under their username).
On that screen they can also click buttons to see a count of all appointment types in that month, as well as a count of customers located in each country.

The user can also access a customers screen which is very similar to the appointments screen in which they can add, update and delete customers,
as well as view the same reports.
 */
package anthonygeorge_schedulingapp;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Anthony
 */
public class AnthonyGeorge_SchedulingApp extends Application {
    
    //opens the login window
    @Override
    public void start(Stage stage) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/View_Controller/LoginScreen.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle(translate());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }    

    //Translates Login to spanish or german for the login screen title
    public static String translate(){
        String login = "login";
        try{
            ResourceBundle rb = ResourceBundle.getBundle("anthonygeorge_schedulingapp/Nat", Locale.getDefault());
        
            //If your system region is in spanish or german this translates "Login" to that language to set the title for the login screen
            if(Locale.getDefault().getLanguage().equals("es") || Locale.getDefault().getLanguage().equals("de")){
                login = rb.getString("login");
            }
        }
        catch(MissingResourceException e){}
        //changes the first character to uppercase
        return login.substring(0,1).toUpperCase()+login.substring(1);
    }
}

/*
Counts the number of types of appoinments for the month and displays it in a table
 */
package View_Controller;

import Model.Type;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * FXML Controller class
 *
 * @author Anthony
 */
public class CountTypeController implements Initializable {

    @FXML private TableView<Type> typeTable;
    @FXML private TableColumn typeColumn;
    @FXML private TableColumn countColumn;
    @FXML private Button closeButton;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb){
        //This binds the width of each table column to 20% of the total width of the table so there isn't empty space
       Double columnWidth = (1.0/2.0);
       typeColumn.prefWidthProperty().bind(typeTable.widthProperty().multiply(columnWidth));
       countColumn.prefWidthProperty().bind(typeTable.widthProperty().multiply(columnWidth));
       
       //Assigns the values to the columns so data can be inserted
       typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
       countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
       
         try{
             Type type = new Type();
             ObservableList<Type> typeList = type.getTypeCount();
             typeTable.getItems().setAll(typeList);
         }catch(SQLException e){};
        
    }    
    //Closes the window, returning to the Reports window
    @FXML
    private void close(ActionEvent event) {
        ((Node)(event.getSource())).getScene().getWindow().hide();
    }    
}

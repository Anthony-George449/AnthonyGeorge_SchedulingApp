/*
Makes up the Type class which stores all of the types of appointments and the count of each type for the current month
 */
package Model;

import Utils.DBConnection;
import Utils.DBQuery;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Anthony
 */
public class Type {
    
     //appointment list that stores all of the appointments
    final public static ObservableList<Type> TypeList = FXCollections.observableArrayList();  
    
    public ObservableList<Type> getTypeCount() throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Select statement sent to the database to retrieve necessary appointment information
        String selectStatement = "select distinct type, count(type) from appointment "
            +"where MONTH(sysdate()) = MONTH(start) group by type;";
        //Execute Statement
        statement.execute(selectStatement);
        //Get ResultsSet
        ResultSet rs = statement.getResultSet();
        
        while(rs.next()){
            String type = rs.getString("type");
            int count = rs.getInt("count(type)");
            
            Type data = new Type(type, count);
            TypeList.add(data);
        }
        
        //close database connection
        DBConnection.closeConnection();
        return TypeList;
    }
    
    //allows an empty Type to be created so methods can be called from other classes
    public Type(){
    }
    
    //creates a Type with all of the required variables
    public Type(String type, int count){
        this.type = type;
        this.count = count;
    }
    
    //Variable Declarations + getters and setters
    private String type;
    public String getType(){
        return type;
    }
    public void setType(String newType){
        type = newType;
    }
    private int count;
    public int getCount(){
        return count;
    }
    public void setCount(int newCount){
        count = newCount;
    }
}

/*
Makes up the Country class which stores all of the countries and the count of customers per country
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
public class Country {
    
     //appointment list that stores all of the appointments
    final public static ObservableList<Country> CountryList = FXCollections.observableArrayList();  
    
    public ObservableList<Country> getCountryCount() throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Select statement sent to the database to retrieve necessary appointment information
        String selectStatement = "select distinct d.country, count(c.customerId) " +
            "from customer c join address a " +
            "on c.addressId = a.addressId " +
            "join city b " +
            "on a.cityId = b.cityId " +
            "join country d " +
            "on b.countryId = d.countryId " +
            "group by country;";
        //Execute Statement
        statement.execute(selectStatement);
        //Get ResultsSet
        ResultSet rs = statement.getResultSet();
        
        while(rs.next()){
            String country = rs.getString("d.country");
            int count = rs.getInt("count(c.customerId)");
            
            Country data = new Country(country, count);
            CountryList.add(data);
        }
        
        //close database connection
        DBConnection.closeConnection();
        return CountryList;
    }
    
    //allows an empty Country to be created so methods can be called from other classes
    public Country(){
    }
    
    //creates a Country with all of the required variables
    public Country(String country, int count){
        this.country = country;
        this.count = count;
    }
    
    //Variable Declarations + getters and setters
    private String country;
    public String getCountry(){
        return country;
    }
    public void setCountry(String newCountry){
        country = newCountry;
    }
    private int count;
    public int getCount(){
        return count;
    }
    public void setCount(int newCount){
        count = newCount;
    }
}

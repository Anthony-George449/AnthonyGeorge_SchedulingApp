/*
This makes up the Customers class. It creates the Customers object which stores the customer information,
keeps a list of all the customers, performs search through the customers list, and performs the SQL statments
for customers.
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
public class Customers {
    
    //customer list that stores all of the customers
    final private static ObservableList<Customers> CustomersList = FXCollections.observableArrayList(); 
    
    //user ID stored so the user can be inserted into the database where it needs to be recorded
    private static int userID;
    public int getUserID(){
        return userID;
    }
    public void setUserID(int newUserID){
        userID = newUserID;
    }
    
    public ObservableList<Customers> search(String search){
        //First a list is created that will end up storing all of the values matching the string and be returned
        ObservableList<Customers> SearchList = FXCollections.observableArrayList();
        
        //The list of customer information is streamed, and the customer information is converted to one string in order to search through them with ease.
        //The customer information is stored like "Alfred Newman E+123 Elm B24+Canada Toronto+11112+555-1213" 
        //I had the customer's name stored in a way where the middle initial being optional isn't a problem. If the customer searches for 
        //"Alfred Newman" or "Alfred E Newman" the customer Alfred E Newman will be found
        //I also had some parts stored with spaces so the user can search "123 Elm B24" or "Canada Toronto", allowing a better range of search options
        //This part also converts all letters in the customer information and search string to lowercase so case sensitivity isn't a problem.
        //After the filter is done each customer information that contains the search string is added to a list and returned
        CustomersList.stream().filter(x->(x.getID()+"+"+x.getFirstName()+" "+x.getLastName()+" "+x.getMiddleInit()+"+"
                +x.getAddress()+" "+x.getAptNo()+"+"+x.getCountry()+" "+x.getCity()+"+"+x.getPostalCode()+x.getPhone())
                .toLowerCase().contains(search.toLowerCase())).forEach(x -> SearchList.add(x));
        return SearchList;
    }
    
    public ObservableList<Customers> getCustomersList() throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Select statement sent to the database to retrieve necessary customer information
        String selectStatement = 
                "select c.customerId, c.addressId, c.customerName, a.address, a.address2, b.country, d.city, a.postalCode, a.phone " +
                "from customer c join address a " +
                "on c.addressId = a.addressId " +
                "join city d " +
                "on a.cityId = d.cityId " +
                "join country b " +
                "on d.countryId = b.countryId;";
        //Execute Statement
        statement.execute(selectStatement);
        //Get ResultsSet
        ResultSet rs = statement.getResultSet();
        //clear the list so duplicates and old records aren't included when returning the list
        CustomersList.clear();
        
        //retrieves data from ResultSet and adds them to the list of customers
        while(rs.next()){
            int ID = rs.getInt("c.customerId");
            int addressID = rs.getInt("c.addressId");
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
            String address = rs.getString("a.address");
            String aptNo = rs.getString("a.address2");
            String country = rs.getString("b.country");
            String city = rs.getString("d.city");
            String postalCode = rs.getString("a.postalCode");
            String phone = rs.getString("a.phone");
            
            Customers cust = new Customers(ID, addressID, firstName, middleInit, lastName, address, aptNo, country, city, postalCode, phone);
            CustomersList.add(cust);
        }
        
        DBConnection.closeConnection();
        return CustomersList;
    }
    
    //Adds a customer to the database from the AddCustomer window
    public void addCustomer (String name, String address, String apt, String country, String city, String postalCode, String phone) throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //checks to see if the country, city and address exist, if they don't they're created, and the id is returned for the existing or new one
        int countryID = checkCountry(country);
        int cityID = checkCity(city, countryID);
        int addressID = checkAddress(address, apt, postalCode, phone, cityID);
        
        //Insert statement containing inserted values, userID and filling in info that we don't use but is not null in the database
        String insertStatement = "insert into customer (customerName, addressId, active, createDate, createdBy, lastUpdateBy) "
                +"values ('"+name+"', "+addressID+", 1, sysdate(), (select userName from user where userId = "+userID+"), (select userName from user where userId = "+userID+"));";
                //sysdate() gets the date and time from the database, so I use that to insert the createDate
        //Execute Statement, close connection, update customers list
        System.out.println(insertStatement);
        statement.execute(insertStatement);
        DBConnection.closeConnection();
        getCustomersList();
    }
    
    //updates existing customer and address information in the database from the UpdateCustomer window
    public void updateCustomer(int ID, int addressID, String name, String address, String apt, String country, String city, String postalCode, String phone) throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //checks to see if the country and city exist, if they don't they're created, and the id is returned for the existing or new one
        int countryID = checkCountry(country);
        int cityID = checkCity(city, countryID);
        //Update statement containing inserted values and changing lastUpdateBy with the userID
        String updateStatement = "update address set address = '"+address+"', address2 = '"+apt+"', cityId = "
                +cityID+", postalCode = '"+postalCode+"', phone = '"+phone
                +"', lastUpdateBy = (select userName from user where userId = "+userID+") where addressId = "+addressID+";";
        //Execute statement
        statement.execute(updateStatement);
        
        //Update statement containing inserted values and changing lastUpdateBy with the userID
        updateStatement = "update customer set customerName = '"+name+"', addressId = "+addressID
                +", lastUpdateBy = (select userName from user where userId = "+userID+") where customerId = "+ID+";";
        
        //Execute Statement, close connection, update customers list
        statement.execute(updateStatement);
        DBConnection.closeConnection();
        getCustomersList();
    }
    
    //checks to see if the entered country exists in the database. If it does the country ID is retrieved, but if it doesn't
    //then the country is created and the new country ID is retrieved
    private int checkCountry(String country) throws SQLException{
        Statement statement = DBQuery.getStatement();
        //attempts to get the country Id from the country table
        String countryCheck = "Select countryId from country where country = '"+country+"';";
        statement.execute(countryCheck);
        ResultSet rs = statement.getResultSet();
        int countryID;
        
        //if the country Id is retrived then it is set and returned
        if(rs.next()){
            countryID = rs.getInt("countryId");
        }
        //otherwise the country is created and the Id is retrived from the new country
        else{
            String insertCountry = "Insert Into country (country, createDate, createdBy, lastUpdateBy) values ('"
                    +country+"', sysdate(), (select userName from user where userId = "+userID+"), (select userName from user where userId = "+userID+"));";
            statement.execute(insertCountry);
            statement.execute(countryCheck);
            rs = statement.getResultSet();
            rs.next();
            countryID = rs.getInt("countryId");
        }
        return countryID;
    }
    
    //checks to see if the entered city exists in the database. If it does the city ID is retrieved, but if it doesn't
    //then the city is created and the new city ID is retrieved
    private int checkCity(String city, int countryID) throws SQLException{
        Statement statement = DBQuery.getStatement();
        //attempts to get the city Id from the city table
        String cityCheck = "Select cityId from city where city = '"+city+"';";
        statement.execute(cityCheck);
        ResultSet rs = statement.getResultSet();
        int cityID;
        
        //if the city Id is retrived then it is set and returned
        if(rs.next()){
            cityID = rs.getInt("cityId");
        }
        //otherwise the city is created and the Id is retrived from the new city
        else{
            String insertCity = "Insert Into city (city, countryId, createDate, createdBy, lastUpdateBy) values ('"
                    +city+"', "+countryID+", sysdate(), (select userName from user where userId = "+userID+"), (select userName from user where userId = "+userID+"));";
            statement.execute(insertCity);
            statement.execute(cityCheck);
            rs = statement.getResultSet();
            rs.next();
            cityID = rs.getInt("cityId");
        }
        return cityID;
    }
    
     //checks to see if the entered address exists in the database using the entered address, apt, postal code and phone. 
    //If it does the address ID is retrieved, but if it doesn't then the address is created and the new address ID is retrieved
    private int checkAddress(String address, String apt, String postalCode, String phone, int cityID) throws SQLException{
        Statement statement = DBQuery.getStatement();
         //attempts to get the address Id from the address table
        String addressCheck = "Select addressId from address where address = '"+address+"' and address2 = '"+apt+"' and postalCode = '"+postalCode+"' and phone = '"+phone+"';";
        statement.execute(addressCheck);
        ResultSet rs = statement.getResultSet();
        int addressID;
        
        //if the address Id is retrived then it is set and returned
        if(rs.next()){
            addressID = rs.getInt("addressId");
        }
        //otherwise the address is created and the Id is retrived from the new address
        else{
            String insertAddress = "Insert Into address (address, address2, cityId, postalCode, phone, createDate, createdBy, lastUpdateBy) values ('"
                    +address+"', '"+apt+"', "+cityID+", '"+postalCode+"', '"+phone+"', sysdate(), (select userName from user where userId = "+userID+"), (select userName from user where userId = "+userID+"));";
            statement.execute(insertAddress);
            statement.execute(addressCheck);
            rs = statement.getResultSet();
            rs.next();
            addressID = rs.getInt("addressId");
        }
        return addressID;
    }
    
    //Removes a selected address and customer from the database
    public void deleteCustomer(int custID, int addressID) throws SQLException{
        //Conenct to the database
        Connection con = DBConnection.startConnection();
        //Create Statement 
        DBQuery.setStatement(con);
        //Get Statement reference
        Statement statement = DBQuery.getStatement();
        
        //Delete statement sent to the database to remove customer with given customerId 
        //(happens before address since address is a foreign key in customer)
        String deleteStatement = "DELETE FROM customer WHERE customerId = "+custID+";";
        //Execute statement
        statement.execute(deleteStatement);
        
        //Delete statement sent to the database to remove address with given addressId
        deleteStatement = "DELETE FROM address WHERE addressId = "+addressID+";";
        
        //Execute Statement, close connection, update customers list
        statement.execute(deleteStatement);
        DBConnection.closeConnection();
        getCustomersList();
    }
    
    //allows an empty Customers to be created so methods can be called from other classes
    public Customers(){
    }
    
    //creates a Customers with all of the required variables
    public Customers(int ID, int addressID, String firstName, String middleInit, String lastName, String address, String aptNo, String country, String city, String postalCode, String phone){
        this.ID = ID;
        this.addressID = addressID;
        this.firstName = firstName;
        this.middleInit = middleInit;
        this.lastName = lastName;
        this.address = address;
        this.aptNo = aptNo;
        this.country = country;
        this.city = city;
        this.postalCode = postalCode;
        this.phone = phone;
    }
    
    //Variable Declartions + getters and setters
    private int ID;
    public int getID(){
        return ID;
    }
    public void setID(int newID){
        ID = newID;
    }
    
    private int addressID;
    public int getAddressID(){
        return addressID;
    }
    public void setAddressID(int newAddressID){
        addressID = newAddressID;
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
    
    private String address;
    public String getAddress(){
        return address;
    }
    public void setAddress(String newAddress){
        address = newAddress;
    }
    
    private String aptNo;
    public String getAptNo(){
        return aptNo;
    }
    public void setAptNo(String newAptNo){
        aptNo = newAptNo;
    }
    
    private String country;
    public String getCountry(){
        return country;
    }
    public void setCountry(String newCountry){
        country = newCountry;
    }
    
    private String city;
    public String getCity(){
        return city;
    }
    public void setCity(String newCity){
        city = newCity;
    }
    
    private String postalCode;
    public String getPostalCode(){
        return postalCode;
    }
    public void setPostalCode(String newPostalCode){
        postalCode = newPostalCode;
    }
    
    private String phone;
    public String getPhone(){
        return phone;
    }
    public void setPhone(String newPhone){
        phone = newPhone;
    }
    
}

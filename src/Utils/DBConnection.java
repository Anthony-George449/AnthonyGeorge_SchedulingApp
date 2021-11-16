/*
Has the ability to connect to the database and close the connection so SQL statements can be performed
 */
package Utils;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Anthony
 */
public class DBConnection {
    
//JDBC URL parts
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String IPAddress = "//3.227.166.251/U05xgd";

//Full JDBC URL
    private static final String jdbcURL = protocol + vendorName + IPAddress;

//Driver and Connection interface reference
    private static final String mysql_jdbc_driver = "com.mysql.jdbc.Driver";
    private static Connection con = null;
    
//Username
    private static final String username = "U05xgd";   
//Password
    private static String password = "53688636881";
    
    
    public static Connection startConnection(){
        try{
            Class.forName(mysql_jdbc_driver);
            con = (Connection)DriverManager.getConnection(jdbcURL, username, password);
        }
        catch(ClassNotFoundException e){
            System.out.println(e.getMessage());    
        }
        catch(SQLException e){
            System.out.println(e.getMessage());    
        }
        return con;   
    }
    
    public static void closeConnection(){
        try{
            con.close();
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
}

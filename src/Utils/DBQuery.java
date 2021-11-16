/*
Allows an SQL statement to be created and retrived
 */
package Utils;

import java.sql.*;

/**
 *
 * @author Anthony
 */
public class DBQuery {
    
    private static Statement statement;
    
    //Create statement object
    public static void setStatement(Connection conn) throws SQLException{
        statement = conn.createStatement();
    }
    
    //Return Statement object
    public static Statement getStatement(){
        return statement;
    }
}


package caguioa.bank;

import java.sql.*;

public class DB {
    public static Connection connect(){
        try{
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/lawbank",
                "root",
                ""
            );
        }catch(Exception e){
            System.out.println("DB Error: " + e);
            return null;
        }
    }
}

  

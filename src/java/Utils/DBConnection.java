package Utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;"
      + "databaseName=FifaFieldDB;"
      + "encrypt=true;"
      + "trustServerCertificate=true";

    private static final String USER = "sa";      
    private static final String PASSWORD = "123";

    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(
            getConnection() != null
                ? "✅ CONNECT SQL SERVER OK"
                : "❌ CONNECT FAIL"
        );
    }
}

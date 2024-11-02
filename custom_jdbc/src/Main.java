import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        ExampleJDBCRequest exampleJDBCRequest = new ExampleJDBCRequest();
        exampleJDBCRequest.runJDBC();;
    }
}
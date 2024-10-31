import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        Class <Driver> driverClass = Driver.class;
        String sql = """
                CREATE SCHEMA IF NOT EXISTS game_repository;
                CREATE TABLE IF NOT EXISTS game_repository.info (id INT GENERATED ALWAYS AS IDENTITY, name VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL, PRIMARY KEY (id));
                INSERT INTO game_repository.info (name, email) VALUES ('John Doe', 'john.doe@example.com'), ('Ivan Ivanov', 'ivan.ivanov@example.com'), ('Petr Sidorov', 'petr.sidorov@example.com');
                UPDATE game_repository.info SET email = 'john2.doe@example.com' WHERE id = 1;
                """; // DML и DDL операции
        String sql2 = """
                SELECT * FROM game_repository.info;
                """; // DML получение данных
        String sql3 = """
               INSERT INTO game_repository.info (name, email) VALUES ('Fedor Fedorov', 'fedr.fedorov@example.com');
                """; // DML вставка данных
        String sql4 = """
                CREATE TABLE IF NOT EXISTS game;
                """; // DDL создание сущностей
        String sql5 = """
                DROP TABLE game_repository.info;
                """; // DDL удаление таблиц

        try(var connection = ConnectionManager.open(); var statement = connection.createStatement();) {
//            var executeResult = statement.execute(sql); - запуск любой операции sql



//            var executeResult2 = statement.executeQuery(sql2); - sql запрос данных
//            while(executeResult2.next()) {
//                System.out.println(executeResult2.getLong("id")); - получение данных из запроса
//                System.out.println(executeResult2.getString("name"));
//                System.out.println(executeResult2.getString("name"));
//                System.out.println("---------");
//            }



//              var executeResult3 = statement.executeUpdate(sql3, Statement.RETURN_GENERATED_KEYS); - sql запрос обновления данных
//              var generatedkeys = statement.getGeneratedKeys(); - только что созданные записи
//              while(generatedkeys.next()){
//                  System.out.println(generatedkeys.getLong("id"));
//                  System.out.println(generatedkeys.getString("name"));
//                  System.out.println(generatedkeys.getString("email"));
//                  System.out.println("---------");
//              }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
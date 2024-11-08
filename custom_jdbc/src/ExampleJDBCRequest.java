import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;

public class ExampleJDBCRequest {
    public void runJDBC() {
        Class <Driver> driverClass = Driver.class;
        try(var connection = ConnectionManager.open();) {
            customCreateTable(connection);
            customGetMetaData(connection);
            customInsertData(connection);
            customUpdateData(connection);
            customSelectData(connection);
            customSelectWithInjectData(connection);
            customShowTransactionData(connection);
            customShowBatchData(connection);
            customBlobMaker(connection);
            customDropTable(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customCreateTable(Connection connection){
        try(var statement = connection.createStatement();) {
            String sql = """
                    CREATE SCHEMA IF NOT EXISTS game_repository;
                    CREATE TABLE IF NOT EXISTS game_repository.info (id INT GENERATED ALWAYS AS IDENTITY, name VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL, image BYTEA, PRIMARY KEY (id));
            """;
            statement.execute(sql); // - запуск любой операции sql
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customInsertData(Connection connection){
        try(var statement = connection.createStatement();) {
            String sql = "INSERT INTO game_repository.info (name, email, image) VALUES ('John Doe', 'john.doe@example.com', NULL), ('Ivan Ivanov', 'ivan.ivanov@example.com', NULL), ('Petr Sidorov', 'petr.sidorov@example.com', NULL);";
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customUpdateData(Connection connection){
        try(var statement = connection.createStatement();) {
            String sql = "UPDATE game_repository.info SET email = 'john2.doe@example.com' WHERE id = 1;";
            System.out.println("CustomUpdateData: ");
            statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS); //- sql запрос обновления данных
            var generatedkeys = statement.getGeneratedKeys();
            while(generatedkeys.next()){
                System.out.println(generatedkeys.getLong("id"));
                System.out.println(generatedkeys.getString("name"));
                System.out.println(generatedkeys.getString("email"));
                System.out.println("---------");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void customGetMetaData(Connection connection){
        try {
            System.out.println("CustomGetMetaData: ");
            var metaData = connection.getMetaData();
            var catalogs = metaData.getCatalogs();
            System.out.println("Catalogs: " + catalogs);
            while(catalogs.next()){
                var catalog = catalogs.getString("TABLE_CAT");
                if (catalog.equals("dbSql")) {
                    var schemas = metaData.getSchemas(catalog, "%");
                    System.out.println(catalog);
                    while(schemas.next()){
                        var schema = schemas.getString("TABLE_SCHEM");
                        var tables = metaData.getTables(catalog, schema, "%", new String[] {"TABLE"});
                        System.out.println("   " + schema);
                        while(tables.next()){
                            if (schema.equals(tables.getString("TABLE_SCHEM"))) {
                                System.out.println("      " + tables.getString("TABLE_NAME"));
                                System.out.println("---------");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customSelectData(Connection connection){
        try(var statement = connection.createStatement();) {
            String sql = "SELECT id, name, email FROM game_repository.info";
            System.out.println("CustomSelectData: ");
            var executeResult = statement.executeQuery(sql); //- sql запрос данных
            statement.setFetchSize(1);  // - сколько записей из БД тянуть за раз
            statement.setQueryTimeout(100);  // - время ожидания ответа из БД
            statement.setMaxRows(100);  // - аналог LIMIT в sql запросе

            while(executeResult.next()){
                System.out.println(executeResult.getLong("id"));
                System.out.println(executeResult.getString("name"));
                System.out.println(executeResult.getString("email"));
                System.out.println("---------");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customSelectWithInjectData(Connection connection){
        var infoId1 = 1;
        var sql1 = "SELECT id, name, email FROM game_repository.info WHERE id = %s;".formatted(infoId1);
        var infoId2 = 2;
        var sql2 = "SELECT id, name, email, image FROM game_repository.info WHERE id = ?;";

        try(var statement = connection.createStatement(); var preparedStatement = connection.prepareStatement(sql2);) {
            System.out.println("customSelectWithInjectData: ");
            var executeResultStatement = statement.executeQuery(sql1);
            while(executeResultStatement.next()){
                System.out.println(executeResultStatement.getLong("id"));
                System.out.println(executeResultStatement.getString("name"));
                System.out.println(executeResultStatement.getString("email"));
                System.out.println("---------");
            }

            System.out.println("customPreparedSelectWithInjectData: ");
            preparedStatement.setLong(1, infoId2); // - защита от sql injection
            var executeResult = preparedStatement.executeQuery();
            while(executeResult.next()){
                System.out.println(executeResult.getObject("id", Integer.class)); // Null safe
                System.out.println(executeResult.getObject("name", String.class));
                System.out.println("---------");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customShowTransactionData(Connection connection) throws SQLException {
        var infoId = 2;
        var sql = "UPDATE game_repository.info SET email = 'ivan2.ivanov@example.com' WHERE id = ?;";
        var sql2 = "SELECT id, name, email FROM game_repository.info;";
        try {
            connection.setAutoCommit(false); // - отключение автокоммита
            var preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, infoId);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            if(true){
                throw new RuntimeException("Commit failed");
            }
           connection.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            connection.rollback();
        }
        finally {
            try(var preparedStatement2 = connection.prepareStatement(sql2);) {
                var executeResult2= preparedStatement2.executeQuery();
                System.out.println("ShowTransaction: ");
                while(executeResult2.next()){
                    System.out.println(executeResult2.getObject("id", Integer.class)); // Null safe
                    System.out.println(executeResult2.getObject("name", String.class));
                    System.out.println(executeResult2.getObject("email", String.class));
                    System.out.println("---------");
                }
                connection.setAutoCommit(true);
            } catch (SQLException e2) {
                throw new RuntimeException(e2);
            }
        }
    }

    public static void customShowBatchData(Connection connection){
        var sql1 = "DELETE FROM game_repository.info WHERE id = 1;";
        var sql2 = "DELETE FROM game_repository.info WHERE id = 2;";
        var sql3 = "SELECT id, name, email FROM game_repository.info;";
        try (var statement = connection.createStatement(); var preparedStatement = connection.prepareStatement(sql3);){
            statement.addBatch(sql1);
            statement.addBatch(sql2);
            var executeResult = statement.executeBatch();
            System.out.println("customShowBatchData: ");
            var executeResult2= preparedStatement.executeQuery();
            while(executeResult2.next()){
                System.out.println(executeResult2.getObject("id", Integer.class)); // Null safe
                System.out.println(executeResult2.getObject("name", String.class));
                System.out.println(executeResult2.getObject("email", String.class));
                System.out.println("---------");
            }
        } catch (SQLException e2) {
            throw new RuntimeException(e2);
        }
    }

    public static void customBlobMaker(Connection connection){
        var sql1 = "UPDATE game_repository.info SET image = ? WHERE id = ?;";
        var sql2 = "SELECT image FROM game_repository.info WHERE id = ?;";
        try(var preparedStatement1 = connection.prepareStatement(sql1); var preparedStatement2 = connection.prepareStatement(sql2)){
            // var blob = connection.createBlob();
            // blob.setBytes(1, Files.readAllBytes(Path.of("resources", "images", "cat.jpg")));
            // preparedStatement.setBlob(1, blob); - для других БД
            preparedStatement1.setBytes(1, Files.readAllBytes(Path.of("resources", "images", "cat.jpg")));
            preparedStatement1.setLong(2, 3);
            preparedStatement2.setLong(1, 3);
            preparedStatement1.executeUpdate();
            var resultSet = preparedStatement2.executeQuery();
            if(resultSet.next()){
                var image = resultSet.getBytes("image");
                Files.write(Path.of("resources", "images", "cat_copy.jpg"), image, StandardOpenOption.CREATE);
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customDropTable(Connection connection){
        try(var statement = connection.createStatement()) {
            String sql = "DROP TABLE game_repository.info;";
            statement.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

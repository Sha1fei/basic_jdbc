import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;

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
            customDropTable(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customCreateTable(Connection connection){
        try(var statement = connection.createStatement();) {
            String sql = """
                    CREATE SCHEMA IF NOT EXISTS game_repository;
                    CREATE TABLE IF NOT EXISTS game_repository.info (id INT GENERATED ALWAYS AS IDENTITY, name VARCHAR(255) NOT NULL, email VARCHAR(255) NOT NULL, PRIMARY KEY (id));
            """;
            statement.execute(sql); // - запуск любой операции sql
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void customInsertData(Connection connection){
        try(var statement = connection.createStatement();) {
            String sql = "INSERT INTO game_repository.info (name, email) VALUES ('John Doe', 'john.doe@example.com'), ('Ivan Ivanov', 'ivan.ivanov@example.com'), ('Petr Sidorov', 'petr.sidorov@example.com');";
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
        var sql2 = "SELECT id, name, email FROM game_repository.info WHERE id = ?;";

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
                System.out.println(executeResult.getObject("email", String.class));
                System.out.println("---------");
            }
        } catch (SQLException e) {
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
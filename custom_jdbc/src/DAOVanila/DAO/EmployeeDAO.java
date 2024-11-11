package DAOVanila.DAO;

import DAOVanila.DTO.CompanyFilter;
import DAOVanila.DTO.EmployeeFilter;
import DAOVanila.Entity.Employee;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeDAO {
    private static Connection connection;
    private static EmployeeDAO INSATNCE;
    private EmployeeDAO(Connection connection){
        this.connection = connection;
    }
    public static EmployeeDAO getInstance(Connection connection){
        if(INSATNCE == null){
            INSATNCE = new EmployeeDAO(connection);
        }
        return INSATNCE;
    };

    public static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS game_repository.employee (id INT GENERATED ALWAYS AS IDENTITY, name VARCHAR(255) NOT NULL, surname VARCHAR(255) NOT NULL, salary INT NOT NULL, company_id INT REFERENCES game_repository.company (id) ON DELETE CASCADE, PRIMARY KEY (id));
    """;

    public static final String DELETE_TABLE_SQL = """
        DROP TABLE game_repository.employee;
    """;

    public static final String CREATE_SQL = """
       INSERT INTO game_repository.employee (name, surname, salary, company_id) VALUES (?, ?, ?, ?);
    """;

    public static final String UPDATE_SQL = """
       UPDATE game_repository.employee SET name = ?, surname = ?, salary = ?, company_id = ? WHERE id = ?;
    """;

    public static final String FIND_BY_ID_SQL = """
       SELECT * FROM game_repository.employee WHERE id = ?;
    """;

    public static final String FIND_ALL_SQL = """
       SELECT * FROM game_repository.employee;
    """;

    public static final String DELETE_SQL = """
       DELETE FROM game_repository.employee WHERE id = ?;
    """;

    public boolean createTable(){
        try(var preparedStatement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteTable(){
        try(var preparedStatement = connection.prepareStatement(DELETE_TABLE_SQL)) {
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Employee create(Employee employee){
        try(var preparedStatement = connection.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, employee.getName());
            preparedStatement.setString(2, employee.getSurname());
            preparedStatement.setLong(3, employee.getSalary());
            preparedStatement.setLong(4, employee.getCompany_id());
            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                employee.setId(generatedKeys.getLong("id"));
            }
            return employee;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Employee employee){
        try(var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, employee.getName());
            preparedStatement.setString(2, employee.getSurname());
            preparedStatement.setLong(3, employee.getSalary());
            preparedStatement.setLong(4, employee.getCompany_id());
            preparedStatement.setLong(5, employee.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Employee> findById(Long id){
        try(var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            var resultSet =  preparedStatement.executeQuery();
            Employee employee = null;
            if(resultSet.next()){
                employee = new Employee(
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
                        resultSet.getLong("salary"),
                        resultSet.getLong("company_id"));
                employee.setId(resultSet.getLong("id"));
            }
            return Optional.ofNullable(employee);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Employee> findAll(){
        try(var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet =  preparedStatement.executeQuery();
            List<Employee> employees = new ArrayList<>();
            while(resultSet.next()){
                var employee = new Employee(
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
                        resultSet.getLong("salary"),
                        resultSet.getLong("company_id"));
                employee.setId(resultSet.getLong("id"));
                employees.add(employee);
            }
            return employees;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Employee> findAll(EmployeeFilter filter){
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if(filter.name() !=  null){
            whereSql.add("name = ?");
            parameters.add(filter.name());
        }
        if(filter.surname() !=  null){
            whereSql.add("surname = ?");
            parameters.add(filter.name());
        }
        if(filter.salary() !=  null){
            whereSql.add("salary = ?");
            parameters.add(filter.name());
        }
        if(filter.company_id() !=  null){
            whereSql.add("company_id = ?");
            parameters.add(filter.name());
        }

        parameters.add(filter.limit());
        parameters.add(filter.offset());
        var where = whereSql.size() > 0 ? whereSql.stream().collect(Collectors.joining(" AND ", " WHERE ", "" )) : "";
        var sql = "SELECT * FROM game_repository.employee" + where + " LIMIT ? OFFSET ?";
        try(var preparedStatement = connection.prepareStatement(sql)) {
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            var resultSet =  preparedStatement.executeQuery();
            List<Employee> employees = new ArrayList<>();
            while(resultSet.next()){
                var employee = new Employee(
                        resultSet.getString("name"),
                        resultSet.getString("surname"),
                        resultSet.getLong("salary"),
                        resultSet.getLong("company_id"));
                employee.setId(resultSet.getLong("id"));
                employees.add(employee);
            }
            return employees;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(Long id){
        try(var preparedStatement = connection.prepareStatement(DELETE_SQL)) {
            preparedStatement.setLong(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

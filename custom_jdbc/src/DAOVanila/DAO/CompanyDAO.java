package DAOVanila.DAO;

import java.sql.Connection;

import DAOVanila.DTO.CompanyFilter;
import DAOVanila.Entity.Company;
import DAOVanila.Entity.Employee;
import Util.ConnectionManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompanyDAO {
    private static Connection connection;
    private static CompanyDAO INSATNCE;
    private CompanyDAO(Connection connection){
        this.connection = connection;
    }
    public static CompanyDAO getInstance(Connection connection){
        if(INSATNCE == null){
            INSATNCE = new CompanyDAO(connection);
        }
        return INSATNCE;
    };

    public static final String CREATE_TABLE_SQL = """
       CREATE TABLE IF NOT EXISTS game_repository.company (id INT GENERATED ALWAYS AS IDENTITY, name VARCHAR(255) NOT NULL, PRIMARY KEY (id));
    """;

    public static final String DELETE_TABLE_SQL = """
        DROP TABLE game_repository.company CASCADE;
    """;

    public static final String CREATE_SQL = """
       INSERT INTO game_repository.company (name) VALUES (?);
    """;

    public static final String UPDATE_SQL = """
       UPDATE game_repository.company SET name = ? WHERE id = ?;
    """;

    public static final String FIND_BY_ID_SQL = """
       SELECT * FROM game_repository.company WHERE id = ?;
    """;

    public static final String FIND_ALL_SQL = """
       SELECT * FROM game_repository.company;
    """;

    public static final String DELETE_SQL = """
       DELETE FROM game_repository.company WHERE id = ?;
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

    public Company create(Company company){
        try(var preparedStatement = connection.prepareStatement(CREATE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, company.getName());
            preparedStatement.executeUpdate();

            var generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                company.setId(generatedKeys.getLong("id"));
            }
            return company;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(Company company){
        try(var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setString(1, company.getName());
            preparedStatement.setLong(2, company.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Company> findById(Long id){
        try(var preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            preparedStatement.setLong(1, id);
            var resultSet =  preparedStatement.executeQuery();
            Company company = null;
            if(resultSet.next()){
                company = new Company(resultSet.getString("name"));
                company.setId(resultSet.getLong("id"));
            }
            return Optional.ofNullable(company);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Company> findAll(){
        try(var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
            var resultSet =  preparedStatement.executeQuery();
            List<Company> companies = new ArrayList<>();
            while(resultSet.next()){
                var company = new Company(resultSet.getString("name"));
                company.setId(resultSet.getLong("id"));
                companies.add(company);
            }
            return companies;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Company> findAll(CompanyFilter filter){
        List<Object> parameters = new ArrayList<>();
        List<String> whereSql = new ArrayList<>();
        if(filter.name() !=  null){
            whereSql.add("name = ?");
            parameters.add(filter.name());
        }
        parameters.add(filter.limit());
        parameters.add(filter.offset());
        var where = whereSql.size() > 0 ? whereSql.stream().collect(Collectors.joining(" AND ", " WHERE ", "" )) : "";
        var sql = "SELECT * FROM game_repository.company" + where + " LIMIT ? OFFSET ?";
        try(var preparedStatement = connection.prepareStatement(sql)) {
            for(int i = 0; i < parameters.size(); i++){
                preparedStatement.setObject(i + 1, parameters.get(i));
            }
            var resultSet =  preparedStatement.executeQuery();
            List<Company> companies = new ArrayList<>();
            while(resultSet.next()){
                var company = new Company(resultSet.getString("name"));
                company.setId(resultSet.getLong("id"));
                companies.add(company);
            }
            return companies;
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

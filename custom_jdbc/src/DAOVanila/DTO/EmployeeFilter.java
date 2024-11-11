package DAOVanila.DTO;

public record EmployeeFilter(int limit, int offset, String name, String surname, Long salary, Long company_id) {} //record создает геттеры и сетттеры, hashcode, construtor, toString

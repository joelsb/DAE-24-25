package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.Employee;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDTO extends UserDTO{

    private String warehouse;

    public EmployeeDTO() {
    }

    public EmployeeDTO(String username,String password, String name, String email, String warehouse) {
        super(username,password, name, email, null);
        this.warehouse = warehouse;
    }

    public static EmployeeDTO from(Employee employee) {
        return new EmployeeDTO(
                employee.getUsername(),
                null,
                employee.getName(),
                employee.getEmail(),
                employee.getWarehouse()
        );
    }

    public static List<EmployeeDTO> from(List<Employee> employees) {
        return employees.stream().map(EmployeeDTO::from).collect(Collectors.toList());
    }



    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }
}

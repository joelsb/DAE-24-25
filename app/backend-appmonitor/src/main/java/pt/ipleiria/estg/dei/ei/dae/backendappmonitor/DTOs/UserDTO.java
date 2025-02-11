package pt.ipleiria.estg.dei.ei.dae.backendappmonitor.DTOs;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.Hibernate;
import pt.ipleiria.estg.dei.ei.dae.backendappmonitor.Entities.User;

import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    public String username;
    public String password;
    public String name;
    public String email;
    public String role;

    public UserDTO() {
    }

    public UserDTO(String username,String password, String name, String email, String role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;

    }

    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getUsername(),
                null,
                user.getName(),
                user.getEmail(),
                Hibernate.getClass(user).getSimpleName()
        );
    }

    public static List<UserDTO> fromUsers(List<User> users) {
        return users.stream().map(UserDTO::fromUser).collect(Collectors.toList());
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

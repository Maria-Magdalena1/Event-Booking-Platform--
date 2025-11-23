package main.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDTO {

    @Size(min = 5, max = 30, message = "Username length must be between 5 and 30 symbols.")
    private String username;

    @Size(min = 6, max = 12, message = "Password length must be between 6 and 12 symbols.")
    private String password;
}

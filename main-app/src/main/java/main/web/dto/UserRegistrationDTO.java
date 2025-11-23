package main.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDTO {

    @Size(min = 5, max = 30, message = "Username length must be between 5 and 30 symbols.")
    private String username;

    @Email
    @NotBlank
    private String email;

    @Size(min = 6, max = 12, message = "Password length must be between 6 and 12 symbols.")
    private String password;

    @NotBlank
    private String confirmPassword;
}

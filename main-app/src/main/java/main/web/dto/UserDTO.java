package main.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private UUID id;
    @NotBlank
    @Size(min = 5, max = 30)
    private String username;

    @Email
    @NotBlank
    private String email;

    private String role;

    private String name;

    private Integer age;

    private LocalDateTime joinedAt;
}

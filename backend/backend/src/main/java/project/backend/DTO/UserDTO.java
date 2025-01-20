package project.backend.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {
    private String id;
    private String userid;
    private String password;
    private String name;
    private String email;
    private String phone;
    private String token;
}

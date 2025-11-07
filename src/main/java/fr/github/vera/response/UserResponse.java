package fr.github.vera.response;

import fr.github.vera.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponse extends Response<User> {
    public UserResponse(User data) {
        super(data);
    }

    @Schema(description = "User")
    @Override
    public User getData() {
        return super.getData();
    }
}

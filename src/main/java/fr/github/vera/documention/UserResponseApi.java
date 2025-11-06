package fr.github.vera.documention;

import fr.github.vera.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponseApi extends ResponseApi<User> {
    public UserResponseApi(User data) {
        super(data);
    }

    @Schema(description = "User")
    @Override
    public User getData() {
        return super.getData();
    }
}

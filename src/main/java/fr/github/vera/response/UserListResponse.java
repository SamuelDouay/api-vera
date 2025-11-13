package fr.github.vera.response;

import fr.github.vera.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public class UserListResponse extends Response<List<User>> {

    public UserListResponse(List<User> data, Map<String, Object> meta) {
        super(data, meta);
    }

    public UserListResponse(List<User> users) {
        super(users);
    }

    @Schema(description = "Liste des utilisateurs", type = "array")
    @Override
    public List<User> getData() {
        return super.getData();
    }
}

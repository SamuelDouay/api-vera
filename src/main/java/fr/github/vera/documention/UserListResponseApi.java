package fr.github.vera.documention;

import fr.github.vera.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public class UserListResponseApi extends ResponseApi<List<User>> {

    public UserListResponseApi(List<User> data, Map<String, Object> meta) {
        super(data, meta);
    }

    @Schema(description = "Liste des utilisateurs", type = "array")
    @Override
    public List<User> getData() {
        return super.getData();
    }
}

package fr.github.vera.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class User {
    private Integer id;

    @NotBlank(message = "name cannot be blank")
    @Size(min = 2, max = 255, message = "name must be between 2 and 255 characters")
    private String name;

    @NotBlank(message = "email cannot be blank")
    @Email(message = "email should be valid")
    private String email;

    // Constructeurs
    public User() {
    }

    public User(Integer id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s'}",
                id, name, email);
    }
}

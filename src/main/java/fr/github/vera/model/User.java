package fr.github.vera.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class User {
    private Integer id;

    @NotBlank(message = "name cannot be blank")
    @Size(max = 255, message = "name must max 255 characters")
    private String name;

    @NotBlank(message = "surname cannot be blank")
    @Size(max = 255, message = "surname must max 255 characters")
    private String surname;

    @NotBlank(message = "email cannot be blank")
    @Email(message = "email should be valid")
    private String email;

    // Constructeurs
    public User() {
    }

    public User(Integer id, String name, String surname, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.surname = surname;
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

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s'}",
                id, name, email);
    }
}

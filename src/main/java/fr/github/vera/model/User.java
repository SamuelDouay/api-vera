package fr.github.vera.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private boolean admin;
    @JsonIgnore
    private String password;

    // Constructeurs
    public User() {
    }

    public User(Integer id, String name, String surname, String email, boolean admin, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.surname = surname;
        this.admin = admin;
        this.password = password;
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', isAdmin='%s'}",
                id, name, email, admin);
    }
}

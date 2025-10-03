package fr.github.vera.model;

public class ResetPasswordRequest {
    private String email;
    private String newPassword;
    private String resetToken;

    public ResetPasswordRequest() {
        
    }

    public ResetPasswordRequest(String email, String newPassword, String resetToken) {
        this.email = email;
        this.newPassword = newPassword;
        this.resetToken = resetToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}

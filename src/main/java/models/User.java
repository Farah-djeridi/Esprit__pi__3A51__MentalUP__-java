package models;

import java.util.Date;

public class User {

    private int id;
    private String prenom;
    private String nom;
    private String email;
    private String motDePasse;
    private String role;
    private String roles;  // JSON string
    private String resetToken;
    private Date resetTokenExpiresAt;
    private String avatarFilename;
    private Date updatedAt;
    private Date createdAt;
    private String githubUsername;
    private String telephone;
    private Date deletedAt;
    private int failedLoginAttempts;
    private Date lockedUntil;

    // Constructeur par défaut
    public User() {
    }

    // Constructeur avec tous les champs
    public User(int id, String prenom, String nom, String email, String motDePasse,
                String role, String roles, String resetToken, Date resetTokenExpiresAt,
                String avatarFilename, Date updatedAt, Date createdAt,
                String githubUsername, String telephone, Date deletedAt,
                int failedLoginAttempts, Date lockedUntil) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.roles = roles;
        this.resetToken = resetToken;
        this.resetTokenExpiresAt = resetTokenExpiresAt;
        this.avatarFilename = avatarFilename;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
        this.githubUsername = githubUsername;
        this.telephone = telephone;
        this.deletedAt = deletedAt;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public Date getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(Date resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public String getAvatarFilename() {
        return avatarFilename;
    }

    public void setAvatarFilename(String avatarFilename) {
        this.avatarFilename = avatarFilename;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getGithubUsername() {
        return githubUsername;
    }

    public void setGithubUsername(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Date getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Date lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", prenom='" + prenom + '\'' +
                ", nom='" + nom + '\'' +
                ", email='" + email + '\'' +
                ", motDePasse='" + motDePasse + '\'' +
                ", role='" + role + '\'' +
                ", roles='" + roles + '\'' +
                ", resetToken='" + resetToken + '\'' +
                ", resetTokenExpiresAt=" + resetTokenExpiresAt +
                ", avatarFilename='" + avatarFilename + '\'' +
                ", updatedAt=" + updatedAt +
                ", createdAt=" + createdAt +
                ", githubUsername='" + githubUsername + '\'' +
                ", telephone='" + telephone + '\'' +
                ", deletedAt=" + deletedAt +
                ", failedLoginAttempts=" + failedLoginAttempts +
                ", lockedUntil=" + lockedUntil +
                "}\n";
    }
}
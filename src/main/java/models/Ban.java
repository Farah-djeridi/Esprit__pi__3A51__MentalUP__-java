package models;

import java.sql.Date;
import java.time.LocalDate;

public class Ban {
    private int id;
    private int userId;
    private String userName;
    private String banReason;
    private Date banDate;
    private Date banExpiryDate;
    private boolean isActive;
    private int bannedBy;
    private String bannedByName;

    // Constructeurs
    public Ban() {}

    public Ban(int userId, String banReason, Date banExpiryDate, int bannedBy) {
        this.userId = userId;
        this.banReason = banReason;
        this.banDate = Date.valueOf(LocalDate.now());
        this.banExpiryDate = banExpiryDate;
        this.isActive = true;
        this.bannedBy = bannedBy;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    public Date getBanDate() { return banDate; }
    public void setBanDate(Date banDate) { this.banDate = banDate; }

    public Date getBanExpiryDate() { return banExpiryDate; }
    public void setBanExpiryDate(Date banExpiryDate) { this.banExpiryDate = banExpiryDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getBannedBy() { return bannedBy; }
    public void setBannedBy(int bannedBy) { this.bannedBy = bannedBy; }

    public String getBannedByName() { return bannedByName; }
    public void setBannedByName(String bannedByName) { this.bannedByName = bannedByName; }

    public boolean isExpired() {
        return banExpiryDate != null && banExpiryDate.toLocalDate().isBefore(LocalDate.now());
    }
}
package models;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private String type;
    private String title;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;
    private int userId;
    private Integer objectifId;
    private Integer suiviId;

    public Notification() {
    }

    public Notification(int id, String type, String title, String message, boolean isRead,
                        Timestamp createdAt, int userId, Integer objectifId, Integer suiviId) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.userId = userId;
        this.objectifId = objectifId;
        this.suiviId = suiviId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


    public Integer getObjectifId() {
        return objectifId;
    }

    public void setObjectifId(Integer objectifId) {
        this.objectifId = objectifId;
    }


    public Integer getSuiviId() {
        return suiviId;
    }

    public void setSuiviId(Integer suiviId) {
        this.suiviId = suiviId;
    }

    @Override
    public String toString() {
        return title + " - " + message;
    }
}
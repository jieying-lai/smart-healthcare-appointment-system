package com.healthcare.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model for System Notifications and Alerts.
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private String notificationId;
    private String recipientUserId;
    private String title;
    private String message;
    private String type; // e.g. "APPOINTMENT", "STATUS_CHANGE", "MEDICATION", "ALERT"
    private boolean read;
    private LocalDateTime timestamp;

    public Notification(String notificationId, String recipientUserId, String title, String message, String type) {
        this.notificationId = notificationId;
        this.recipientUserId = recipientUserId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
        this.timestamp = LocalDateTime.now();
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}

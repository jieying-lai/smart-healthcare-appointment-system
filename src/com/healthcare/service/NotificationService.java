package com.healthcare.service;

import com.healthcare.model.Notification;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Alert & Notification System managing in-app notifications and GUI pop-up dialogs.
 */
public class NotificationService {
    private final DataStorageService dataStorage;

    public NotificationService(DataStorageService dataStorage) {
        this.dataStorage = dataStorage;
    }

    public Notification createNotification(String recipientUserId, String title, String message, String type) {
        String notifId = "NOTIF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Notification notif = new Notification(notifId, recipientUserId, title, message, type);

        dataStorage.getNotifications().add(0, notif);
        dataStorage.saveData();

        return notif;
    }

    public void triggerPopupAlert(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "System Alert: " + title, JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public List<Notification> getNotificationsForUser(String userId) {
        return dataStorage.getNotifications().stream()
            .filter(n -> n.getRecipientUserId().equals(userId))
            .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId) {
        return dataStorage.getNotifications().stream()
            .filter(n -> n.getRecipientUserId().equals(userId) && !n.isRead())
            .count();
    }

    public void markAllAsRead(String userId) {
        for (Notification n : dataStorage.getNotifications()) {
            if (n.getRecipientUserId().equals(userId)) {
                n.setRead(true);
            }
        }
        dataStorage.saveData();
    }
}

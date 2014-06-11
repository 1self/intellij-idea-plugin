package org.quantifieddev.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

class EventLogger {
    private static String groupDisplayId = 'org.quantifieddev.build.plugin.id'
    public static boolean canLog = false

    private EventLogger() {
    }

    public static void logSuccess(String title, String message) {
        if(!canLog) {
            return
        }

        Notification notification = new Notification(groupDisplayId, "$groupDisplayId - $title", message, NotificationType.INFORMATION)
        Notifications.Bus.notify(notification)
    }
    public static void logError(String title, String message) {
        if(!canLog) {
            return
        }

        Notification notification = new Notification(groupDisplayId, "$groupDisplayId - $title", message, NotificationType.ERROR)
        Notifications.Bus.notify(notification)
    }
    public static void logWarn(String title, String message) {
        if(!canLog) {
            return
        }

        Notification notification = new Notification(groupDisplayId, "$groupDisplayId - $title", message, NotificationType.WARNING)
        Notifications.Bus.notify(notification)
    }
}

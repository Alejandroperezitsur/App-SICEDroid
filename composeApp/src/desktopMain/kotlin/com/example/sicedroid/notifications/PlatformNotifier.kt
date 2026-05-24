package com.example.sicedroid.notifications

import java.awt.SystemTray
import java.awt.TrayIcon

actual fun platformSendGradeNotification(title: String, message: String) {
    try {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val trayIcon = TrayIcon(java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB), "SICEDroid")
            trayIcon.isImageAutoSize = true
            tray.add(trayIcon)
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
            Thread.sleep(8000)
            tray.remove(trayIcon)
        }
    } catch (_: Exception) {
    }
}

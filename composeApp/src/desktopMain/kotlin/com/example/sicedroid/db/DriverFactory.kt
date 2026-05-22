package com.example.sicedroid.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

class DesktopDriverFactory : DriverFactory {
    override fun createDriver(): SqlDriver {
        val dbDir = File(System.getProperty("user.home"), ".sicedroid")
        dbDir.mkdirs()
        val dbFile = File(dbDir, "sicenet.db")
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        SicenetDatabase.Schema.create(driver)
        return driver
    }
}

package com.example.marsphotos.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * SessionManager - Gestiona la persistencia de la sesión del usuario
 * 
 * Esta clase guarda y recupera las credenciales del usuario usando SharedPreferences
 * para mantener la sesión activa incluso cuando la app se cierra.
 */
class SessionManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREF_NAME = "SICENET_SESSION"
        private const val KEY_MATRICULA = "matricula"
        private const val KEY_CONTRASENIA = "contrasenia"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_LOGIN = "last_login"
        private const val TAG = "SessionManager"
    }
    
    /**
     * Guarda la sesión del usuario después de un login exitoso
     */
    fun saveSession(matricula: String, contrasenia: String) {
        prefs.edit().apply {
            putString(KEY_MATRICULA, matricula)
            putString(KEY_CONTRASENIA, contrasenia)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            apply()
        }
        Log.d(TAG, "✅ Sesión guardada para matrícula: $matricula")
        Log.d(TAG, "✅ Verificación - matrícula guardada: '${getMatricula()}'")
    }
    
    /**
     * Verifica si hay una sesión activa guardada
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Obtiene la matrícula guardada
     */
    fun getMatricula(): String {
        val matricula = prefs.getString(KEY_MATRICULA, "") ?: ""
        Log.d(TAG, "getMatricula() leído: '$matricula'")
        return matricula
    }
    
    /**
     * Obtiene la contraseña guardada
     */
    fun getContrasenia(): String {
        return prefs.getString(KEY_CONTRASENIA, "") ?: ""
    }
    
    /**
     * Obtiene la fecha del último login en milisegundos
     */
    fun getLastLoginTime(): Long {
        return prefs.getLong(KEY_LAST_LOGIN, 0)
    }
    
    /**
     * Limpia la sesión guardada (logout)
     */
    fun clearSession() {
        prefs.edit().clear().apply()
        Log.d(TAG, "🚪 Sesión cerrada y limpiada")
    }
    
    /**
     * Obtiene las credenciales completas si existe sesión
     */
    fun getCredentials(): Pair<String, String>? {
        return if (isLoggedIn()) {
            val matricula = getMatricula()
            val contrasenia = getContrasenia()
            if (matricula.isNotEmpty() && contrasenia.isNotEmpty()) {
                Pair(matricula, contrasenia)
            } else null
        } else null
    }
}

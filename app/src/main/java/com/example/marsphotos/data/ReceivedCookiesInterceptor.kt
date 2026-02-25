import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ReceivedCookiesInterceptor // AddCookiesInterceptor()
    (private val context: Context) : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val existingCookies = prefs.getStringSet("PREF_COOKIES", HashSet()) ?: HashSet()
            
            // Map para asegurar que solo haya una cookie por nombre
            val cookieMap = existingCookies.associate { 
                val name = it.split("=")[0]
                name to it
            }.toMutableMap()

            for (header in originalResponse.headers("Set-Cookie")) {
                val cookieClean = header.split(";")[0].trim()
                if (cookieClean.contains("=")) {
                    val name = cookieClean.split("=")[0]
                    cookieMap[name] = cookieClean
                    Log.d("ReceivedCookies", "Updated Cookie: $cookieClean")
                }
            }
            
            prefs.edit()
                .putStringSet("PREF_COOKIES", cookieMap.values.toSet())
                .commit()
        }
        return originalResponse
    }
}
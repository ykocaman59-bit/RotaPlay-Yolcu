package com.rotaplay.yolcu

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

/** Yolcu uygulaması: sarı taksi çağırma sayfasını tam ekran açar; konum, panoya kopyalama ve Yandex Harita köprüsü sağlar. */
class MainActivity : Activity() {
    private lateinit var web: WebView
    private var pendingOrigin: String? = null
    private var pendingGeo: GeolocationPermissions.Callback? = null

    // ---- Yerel konum (WebView'in konum özelliğine bağlı kalmadan, doğrudan Android konum servisi) ----
    private val handler = Handler(Looper.getMainLooper())
    private var locListener: LocationListener? = null
    private var locTimeout: Runnable? = null

    private fun hasLocPerm() =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun sendPos(l: Location) {
        web.evaluateJavascript("window.__nativePos && window.__nativePos(${l.latitude},${l.longitude},${l.accuracy})", null)
    }

    private fun sendPosErr(code: String) {
        web.evaluateJavascript("window.__nativePosErr && window.__nativePosErr('$code')", null)
    }

    private fun stopLocation() {
        locTimeout?.let { handler.removeCallbacks(it) }
        locTimeout = null
        locListener?.let {
            try { (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(it) } catch (e: Exception) { }
        }
        locListener = null
    }

    /** Konumu alır ve sayfaya window.__nativePos(lat, lon, doğruluk) ile yollar; olmazsa __nativePosErr(kod) çağırır. */
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        if (!hasLocPerm()) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 2)
            return
        }
        stopLocation()
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER).filter {
            try { lm.isProviderEnabled(it) } catch (e: Exception) { false }
        }
        if (providers.isEmpty()) { sendPosErr("off"); return }

        // Son bilinen konum yeniyse (2 dk) hemen kullan
        var best: Location? = null
        for (p in providers) {
            try { lm.getLastKnownLocation(p)?.let { if (best == null || it.time > best!!.time) best = it } } catch (e: Exception) { }
        }
        best?.let { if (System.currentTimeMillis() - it.time < 120_000) { sendPos(it); return } }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) { sendPos(location); stopLocation() }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) { }
            override fun onProviderEnabled(provider: String) { }
            override fun onProviderDisabled(provider: String) { }
        }
        locListener = listener
        var started = false
        for (p in providers) {
            try { lm.requestLocationUpdates(p, 0L, 0f, listener, Looper.getMainLooper()); started = true } catch (e: Exception) { }
        }
        if (!started) { locListener = null; sendPosErr("denied"); return }

        val t = Runnable {
            stopLocation()
            val old = best
            if (old != null) sendPos(old) else sendPosErr("timeout")
        }
        locTimeout = t
        handler.postDelayed(t, 25_000)
    }

    /** Web sayfasından çağrılan yerel işlevler (window.RotaPlayApp). Yalnızca kendi sayfamız yüklenir. */
    inner class Bridge {
        @JavascriptInterface
        fun copy(text: String) {
            runOnUiThread {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("IBAN", text))
                Toast.makeText(this@MainActivity, "Panoya kopyalandı", Toast.LENGTH_SHORT).show()
            }
        }

        @JavascriptInterface
        fun getLocation() { runOnUiThread { fetchLocation() } }

        @JavascriptInterface
        fun openAppSettings() {
            runOnUiThread {
                try { startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) } catch (e: Exception) { }
            }
        }

        @JavascriptInterface
        fun openLocationSettings() {
            runOnUiThread {
                try { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) } catch (e: Exception) { }
            }
        }

        @JavascriptInterface
        fun openYandexRoute(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {
            runOnUiThread { openYandex(lat1, lon1, lat2, lon2) }
        }
    }

    /** Yandex Harita, yoksa Yandex Navigatör, o da yoksa tarayıcıda Yandex Harita ile rota açar. */
    private fun openYandex(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {
        val tries = listOf(
            "ru.yandex.yandexmaps" to "yandexmaps://maps.yandex.ru/?rtext=$lat1,$lon1~$lat2,$lon2&rtt=auto",
            "ru.yandex.yandexnavi" to "yandexnavi://build_route_on_map?lat_from=$lat1&lon_from=$lon1&lat_to=$lat2&lon_to=$lon2"
        )
        for ((pkg, uri) in tries) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)).setPackage(pkg))
                return
            } catch (e: ActivityNotFoundException) { }
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://yandex.com.tr/maps/?rtext=$lat1,$lon1~$lat2,$lon2&rtt=auto")))
        } catch (e: Exception) {
            Toast.makeText(this, "Yandex Harita açılamadı", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true                    // son yolculuk bilgisi burada saklanır
        web.settings.setGeolocationEnabled(true)
        web.addJavascriptInterface(Bridge(), "RotaPlayApp")

        web.webViewClient = object : WebViewClient() {
            // yandexmaps:// , tel: gibi özel bağlantıları ilgili uygulamada aç
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val scheme = request.url.scheme ?: return false
                if (scheme == "http" || scheme == "https") return false
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    true
                } catch (e: Exception) { true }
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false)
                } else {
                    pendingOrigin = origin
                    pendingGeo = callback
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1
                    )
                }
            }
        }
        if (savedInstanceState == null) web.loadUrl(SERVER_URL) else web.restoreState(savedInstanceState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
        if (requestCode == 2) {                                   // yerel konum izni
            if (granted) fetchLocation() else sendPosErr("denied")
            return
        }
        pendingGeo?.invoke(pendingOrigin, granted, false)
        pendingGeo = null
        pendingOrigin = null
    }

    override fun onDestroy() {
        stopLocation()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    /** Önce sayfadaki açık paneli kapatmayı dener, yoksa geri/çıkış. */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        web.evaluateJavascript("(window.__back && window.__back()) === true") { result ->
            if (result == "true") return@evaluateJavascript
            if (web.canGoBack()) web.goBack() else super.onBackPressed()
        }
    }
}

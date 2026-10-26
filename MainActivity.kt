package app.sieltv.tv

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.Locale

/**
 * 시엘TV TV 앱
 * - 시엘TV TV용 화면(https://siel-tv.netlify.app/tv)을 여는 창이에요.
 *   화면과 기능은 Netlify에 올린 것이 그대로 나오고, 업데이트도 저절로 돼요.
 * - 영상이 버튼을 누르지 않아도 바로 재생되게 해요.
 * - 리모컨 '뒤로'는 시엘TV 화면이 받아서 한 단계씩 돌아가고, 첫 화면에서는 앱을 닫아요.
 * - 시엘TV 밖의 사이트(유튜브 앱·웹 등)로는 나가지 못하게 막아요.
 * - 쉬는 시간·스티커·오늘의 AAC 목소리는 TV의 한국어 읽어 주기로 나와요.
 */
class MainActivity : Activity() {

    companion object {
        private const val HOME = "https://siel-tv.netlify.app/tv"
        private const val HOME_HOST = "siel-tv.netlify.app"
    }

    private lateinit var web: WebView
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var offline = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        web = WebView(this)
        setContentView(web)
        web.setBackgroundColor(Color.parseColor("#0F0F0F"))
        web.isFocusable = true
        web.isFocusableInTouchMode = true
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true            // 쉬는 시간·기록을 기억
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false   // 영상을 바로 재생
            cacheMode = WebSettings.LOAD_DEFAULT
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
        }

        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            // 시엘TV 밖으로 나가는 이동은 막아요 (영상 안의 유튜브 화면은 그대로 재생)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (!request.isForMainFrame) return false
                return request.url.host != HOME_HOST
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) showOffline()
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (url.startsWith("https://$HOME_HOST")) offline = false
            }
        }

        web.addJavascriptInterface(Bridge(), "SielTV")

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(Locale.KOREAN)
                ttsReady = true
            }
        }

        if (savedInstanceState != null) web.restoreState(savedInstanceState) else web.loadUrl(HOME)
        web.requestFocus()
    }

    /** 시엘TV 화면(tv.html)에서 부르는 기능 */
    inner class Bridge {
        @JavascriptInterface
        fun speak(text: String, rate: Float) {
            if (!ttsReady) return
            tts?.setSpeechRate(rate.coerceIn(0.5f, 1.5f))
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "siel")
        }

        @JavascriptInterface
        fun stop() { tts?.stop() }

        @JavascriptInterface
        fun isApp(): Boolean = true
    }

    /** 리모컨 '뒤로': 시엘TV가 한 단계 뒤로, 첫 화면이면 앱 닫기 */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP) {
                if (offline) { finish(); return true }
                web.evaluateJavascript("(window.sielBack && window.sielBack()) ? '1' : '0'") { result ->
                    if (result?.contains("1") != true) finish()
                }
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun showOffline() {
        offline = true
        val html = """
            <html><body style="margin:0;background:#0f0f0f;color:#fff;font-family:sans-serif;display:flex;
            flex-direction:column;align-items:center;justify-content:center;height:100vh">
            <h2 style="font-size:4vw;margin:0 0 2vw">인터넷 연결을 확인해 주세요</h2>
            <button id="b" autofocus style="font-size:2.4vw;padding:1.2vw 3vw;border-radius:3vw;border:0;background:#ffd23f"
              onclick="location.href='$HOME'">다시 열기</button>
            <script>document.getElementById('b').focus()</script></body></html>
        """.trimIndent()
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        web.saveState(outState)
    }

    override fun onPause() { super.onPause(); web.onPause() }
    override fun onResume() { super.onResume(); web.onResume() }

    override fun onDestroy() {
        tts?.shutdown()
        web.destroy()
        super.onDestroy()
    }
}

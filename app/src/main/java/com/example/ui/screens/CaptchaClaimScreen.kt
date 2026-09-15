package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CyberAmberSecondary
import com.example.ui.theme.HoneyAccentCyan
import com.example.ui.theme.HoneyGoldLight
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.HoneyMintTertiary
import com.example.ui.viewmodel.FaucetUiState

private const val PERF_TAG = "BeefaucetPerf"

private fun safePerfUrl(raw: String?): String = raw?.substringBefore('?')?.substringBefore('#') ?: "<null>"

private val PERF_SCRIPT = """
    (function() {
      if (window.__beefPerfInstalled) return;
      window.__beefPerfInstalled = true;
      const log = (msg) => console.log('[BEEF_PERF] ' + msg);

      document.addEventListener('click', function(ev) {
        const el = ev.target && ev.target.closest ? ev.target.closest('button,input[type="submit"],a') : null;
        if (!el) return;
        const label = (el.innerText || el.value || el.getAttribute('aria-label') || '').trim().replace(/\s+/g, ' ').slice(0, 80);
        if (/collect|claim/i.test(label)) {
          log('CLAIM_CLICK perf=' + Math.round(performance.now()) + 'ms label=' + label);
        }
      }, true);

      try {
        const observer = new PerformanceObserver(function(list) {
          list.getEntries().forEach(function(e) {
            if (e.entryType !== 'resource') return;
            if (e.initiatorType !== 'fetch' && e.initiatorType !== 'xmlhttprequest') return;
            if (e.duration < 300) return;
            let safe = e.name;
            try {
              const u = new URL(e.name, location.href);
              safe = u.origin + u.pathname;
            } catch (_) {}
            log('NET ' + e.initiatorType + ' start=' + Math.round(e.startTime) + 'ms duration=' + Math.round(e.duration) + 'ms ' + safe);
          });
        });
        observer.observe({entryTypes: ['resource']});
      } catch (e) {
        log('PerformanceObserver unavailable');
      }

      log('OBSERVER_READY ' + location.origin + location.pathname);
    })();
""".trimIndent()

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CaptchaClaimScreen(
    state: FaucetUiState,
    retainedWebView: WebView,
    onClaimed: (String, Context) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentFaucet = state.selectedFaucet

    var webProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var webError by remember { mutableStateOf<String?>(null) }

    // Non-Compose timing holders: updating these must not trigger recomposition.
    val pageStartElapsedMs = remember { longArrayOf(0L) }
    val lastProgressBucket = remember { intArrayOf(-1) }

    // Hardware/gesture back press navigates webview history or goes back home
    BackHandler {
        if (retainedWebView.canGoBack()) {
            retainedWebView.goBack()
        } else {
            onNavigateToHome()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
    ) {
        // Top Bar inside CaptchaClaimScreen with "I Have Claimed / Start 60s Timer"
        Card(
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181F)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("claim_screen_top_bar")
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onNavigateToHome,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("back_to_faucets_grid")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to Grid",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentFaucet.coinIcon,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentFaucet.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Claims: ${currentFaucet.dailyClaims}/${currentFaucet.maxDailyClaims} today • beefaucet.org",
                                fontSize = 11.sp,
                                color = HoneyGoldLight
                            )
                        }
                    }

                    // Reload page button
                    IconButton(
                        onClick = { webError = null; retainedWebView.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload Faucet Page",
                            tint = HoneyGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Prominent CTA Button: "I Have Claimed / Start 60s Timer"
                Button(
                    onClick = {
                        onClaimed(currentFaucet.id, context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("i_have_claimed_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentFaucet.isLimitReached) Color(0xFF333644) else HoneyGoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !currentFaucet.isLimitReached
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Claimed",
                            tint = if (currentFaucet.isLimitReached) Color(0xFF8C90A0) else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentFaucet.isLimitReached) {
                                "Daily Limit Reached (10/10)"
                            } else {
                                "I Have Claimed / Start 60s Timer"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (currentFaucet.isLimitReached) Color(0xFF8C90A0) else Color.Black
                        )
                    }
                }
            }
        }

        // Web Loading Progress Bar
        if (isLoading && webProgress < 1f) {
            LinearProgressIndicator(
                progress = { webProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = HoneyGoldPrimary,
                trackColor = Color(0xFF1E2028)
            )
        }

        webError?.let { message ->
            Text(
                text = message,
                color = Color(0xFFFF8A80),
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A1718))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        // Faucet URL Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF121419))
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secure",
                    tint = HoneyMintTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentFaucet.url,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF8C92A4)
                )
            }
        }

        // Embedded In-App WebView for real faucet site
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("faucet_webview_container")
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    // The same WebView instance is reused after leaving/returning to Claim.
                    // It is never kept hidden via View.INVISIBLE or alpha=0.
                    (retainedWebView.parent as? ViewGroup)?.removeView(retainedWebView)

                    retainedWebView.apply {
                        Log.i(PERF_TAG, "ATTACH faucet=${currentFaucet.id} currentUrl=${safePerfUrl(url)}")
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            setSupportZoom(true)
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean = false

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                webError = null
                                isLoading = true
                                pageStartElapsedMs[0] = SystemClock.elapsedRealtime()
                                lastProgressBucket[0] = -1
                                Log.i(PERF_TAG, "PAGE_START url=${safePerfUrl(url)}")
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                val elapsed = if (pageStartElapsedMs[0] > 0L) {
                                    SystemClock.elapsedRealtime() - pageStartElapsedMs[0]
                                } else 0L
                                Log.i(PERF_TAG, "PAGE_FINISH elapsed=${elapsed}ms url=${safePerfUrl(url)}")
                                view?.evaluateJavascript(PERF_SCRIPT, null)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    webError = "WebView error ${error?.errorCode}: ${error?.description ?: "unknown error"}"
                                    isLoading = false
                                    val elapsed = if (pageStartElapsedMs[0] > 0L) {
                                        SystemClock.elapsedRealtime() - pageStartElapsedMs[0]
                                    } else 0L
                                    Log.e(PERF_TAG, "MAIN_ERROR elapsed=${elapsed}ms code=${error?.errorCode} desc=${error?.description}")
                                }
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: WebResourceResponse?
                            ) {
                                super.onReceivedHttpError(view, request, errorResponse)
                                if (request?.isForMainFrame == true) {
                                    Log.w(PERF_TAG, "HTTP_ERROR status=${errorResponse?.statusCode} url=${safePerfUrl(request.url?.toString())}")
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                webProgress = (newProgress / 100f).coerceIn(0f, 1f)
                                val bucket = when {
                                    newProgress >= 100 -> 100
                                    newProgress >= 90 -> 90
                                    newProgress >= 75 -> 75
                                    newProgress >= 50 -> 50
                                    newProgress >= 25 -> 25
                                    newProgress >= 10 -> 10
                                    else -> 0
                                }
                                if (bucket > lastProgressBucket[0]) {
                                    lastProgressBucket[0] = bucket
                                    val elapsed = if (pageStartElapsedMs[0] > 0L) {
                                        SystemClock.elapsedRealtime() - pageStartElapsedMs[0]
                                    } else 0L
                                    Log.d(PERF_TAG, "PROGRESS ${bucket}% elapsed=${elapsed}ms")
                                }
                                if (newProgress >= 100) isLoading = false
                            }

                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                val message = consoleMessage?.message().orEmpty()
                                if (message.startsWith("[BEEF_PERF]")) {
                                    Log.i(PERF_TAG, message.removePrefix("[BEEF_PERF]").trim())
                                    return true
                                }
                                return super.onConsoleMessage(consoleMessage)
                            }
                        }

                        // Generic View.tag is used only by this app to remember which faucet
                        // start page was explicitly selected. Internal site redirects do not
                        // alter it, so returning to Claim does not force a reload.
                        if (tag != currentFaucet.id || url.isNullOrBlank()) {
                            tag = currentFaucet.id
                            webError = null
                            webProgress = 0f
                            isLoading = true
                            Log.i(PERF_TAG, "LOAD reason=initial_or_empty faucet=${currentFaucet.id} url=${currentFaucet.url}")
                            loadUrl(currentFaucet.url)
                        }
                    }
                },
                update = { webView ->
                    // Only an explicit switch to another faucet should change the start URL.
                    // Recomposition and normal in-site navigation are left untouched.
                    if (webView.tag != currentFaucet.id) {
                        webView.tag = currentFaucet.id
                        webError = null
                        webProgress = 0f
                        isLoading = true
                        Log.i(PERF_TAG, "LOAD reason=faucet_switch faucet=${currentFaucet.id} url=${currentFaucet.url}")
                        webView.loadUrl(currentFaucet.url)
                    }
                }
            )
        }
    }
}

package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
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
import androidx.compose.runtime.DisposableEffect
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CaptchaClaimScreen(
    state: FaucetUiState,
    onClaimed: (String, Context) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    val context = LocalContext.current
    val currentFaucet = state.selectedFaucet

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var webError by remember { mutableStateOf<String?>(null) }
    var loadedFaucetId by remember { mutableStateOf<String?>(null) }

    // The claim screen stays composed while other tabs are shown so the WebView
    // keeps its DOM, JS state, cookies and navigation history alive. Only claim
    // the system back gesture while this tab is actually visible.
    BackHandler(enabled = isActive) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onNavigateToHome()
        }
    }

    DisposableEffect(webViewInstance) {
        val ownedWebView = webViewInstance
        onDispose {
            if (ownedWebView != null && ownedWebView === webViewInstance) {
                ownedWebView.stopLoading()
                ownedWebView.webChromeClient = null
                ownedWebView.removeAllViews()
                ownedWebView.destroy()
            }
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
                        onClick = { webError = null; webViewInstance?.reload() },
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
                factory = { ctx ->
                    WebView(ctx).apply {
                        visibility = if (isActive) View.VISIBLE else View.INVISIBLE
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Preserve the normal website session inside WebView.
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // JavaScript & DOM Storage required by the faucet pages.
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
                            ): Boolean {
                                // Keep all navigation within the in-app WebView
                                return false
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                webError = null
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
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
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                webProgress = (newProgress / 100f).coerceIn(0f, 1f)
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                            }
                        }

                        loadedFaucetId = currentFaucet.id
                        loadUrl(currentFaucet.url)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                    webView.visibility = if (isActive) View.VISIBLE else View.INVISIBLE

                    // Keep the current website session alive across ordinary recompositions
                    // and tab switches. Load a start URL only when the user explicitly chooses
                    // a different faucet. Internal redirects/navigation must remain untouched.
                    if (loadedFaucetId != currentFaucet.id) {
                        loadedFaucetId = currentFaucet.id
                        webError = null
                        webProgress = 0f
                        isLoading = true
                        webView.loadUrl(currentFaucet.url)
                    }
                }
            )
        }
    }
}

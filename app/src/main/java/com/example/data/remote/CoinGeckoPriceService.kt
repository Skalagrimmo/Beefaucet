package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service fetching live cryptocurrency prices from the CoinGecko public API.
 */
class CoinGeckoPriceService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
) {

    suspend fun fetchLivePrices(
        coinIds: List<String> = listOf(
            "binancecoin",
            "ethereum",
            "bitcoin",
            "litecoin",
            "dogecoin",
            "tron",
            "solana",
            "tether"
        )
    ): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val idsParam = coinIds.joinToString(",")
            val url = "https://api.coingecko.com/api/v3/simple/price?ids=$idsParam&vs_currencies=usd"

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("User-Agent", "CryptoBeeFaucet/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("CoinGecko API returned HTTP ${response.code}: ${response.message}")
                    )
                }

                val responseBody = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response body from CoinGecko"))

                val json = JSONObject(responseBody)
                val priceMap = mutableMapOf<String, Double>()

                for (id in coinIds) {
                    if (json.has(id)) {
                        val coinObject = json.optJSONObject(id)
                        val usdPrice = coinObject?.optDouble("usd", 0.0) ?: 0.0
                        priceMap[id] = usdPrice
                    }
                }

                Result.success(priceMap)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

data class RpcNetwork(
    val id: String,
    val name: String,
    val coinSymbol: String,
    val rpcUrl: String
)

val availableRpcNetworks = listOf(
    RpcNetwork(
        id = "bsc",
        name = "BNB Smart Chain (BSC)",
        coinSymbol = "BNB",
        rpcUrl = "https://binance.llamarpc.com"
    ),
    RpcNetwork(
        id = "eth",
        name = "Ethereum Mainnet",
        coinSymbol = "ETH",
        rpcUrl = "https://cloudflare-eth.com"
    ),
    RpcNetwork(
        id = "polygon",
        name = "Polygon POS",
        coinSymbol = "POL",
        rpcUrl = "https://polygon-rpc.com"
    )
)

/**
 * Service executing real JSON-RPC queries to EVM blockchain nodes via Web3j.
 * Queries on-chain balance for user-specified addresses without any local private key custody.
 */
class Web3BalanceService {

    suspend fun getOnChainBalance(
        address: String,
        rpcUrl: String = availableRpcNetworks[0].rpcUrl
    ): Result<Double> = withContext(Dispatchers.IO) {
        try {
            val cleanAddress = address.trim()
            if (!cleanAddress.startsWith("0x") || cleanAddress.length != 42) {
                return@withContext Result.failure(
                    IllegalArgumentException("Invalid address: EVM address must start with 0x and be 42 characters")
                )
            }

            val web3 = Web3j.build(HttpService(rpcUrl))
            val ethGetBalance = web3.ethGetBalance(cleanAddress, DefaultBlockParameterName.LATEST).send()

            if (ethGetBalance.hasError()) {
                return@withContext Result.failure(
                    Exception("RPC error: ${ethGetBalance.error.message}")
                )
            }

            val weiValue: BigInteger = ethGetBalance.balance ?: BigInteger.ZERO
            val etherValue: BigDecimal = Convert.fromWei(weiValue.toString(), Convert.Unit.ETHER)
            Result.success(etherValue.toDouble())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

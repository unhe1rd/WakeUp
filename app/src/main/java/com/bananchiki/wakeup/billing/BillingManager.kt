package com.bananchiki.wakeup.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SubscriptionState {
    NOT_PURCHASED,
    PURCHASED,
    PENDING,
    ERROR
}

class BillingManager(
    private val context: Context,
    private val premiumManager: PremiumManager
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val MONTHLY_SUB = "wakeup_pro_monthly"
        const val YEARLY_SUB = "wakeup_pro_yearly"

        // Debug flag — always true on non-user/release builds
        val IS_DEBUG_BILLING = android.os.Build.TYPE != "user"
    }

    private val _subscriptionState = MutableStateFlow(SubscriptionState.NOT_PURCHASED)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _monthlyDetails = MutableStateFlow<ProductDetails?>(null)
    val monthlyDetails: StateFlow<ProductDetails?> = _monthlyDetails.asStateFlow()

    private val _yearlyDetails = MutableStateFlow<ProductDetails?>(null)
    val yearlyDetails: StateFlow<ProductDetails?> = _yearlyDetails.asStateFlow()
    private val _useMockBilling = MutableStateFlow(IS_DEBUG_BILLING)
    val useMockBilling: StateFlow<Boolean> = _useMockBilling.asStateFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected")
                    // Don't disable mock yet — wait until products actually load
                    querySubscriptions()
                    queryExistingPurchases()
                } else {
                    Log.w(TAG, "Billing setup failed (${billingResult.responseCode}): ${billingResult.debugMessage}")
                    // Keep mock mode active
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }
        })
    }

    /**
     * Called from UI when user taps "Buy" in debug mode.
     * Simulates a successful purchase without Play Store.
     */
    fun debugSimulatePurchase() {
        if (!IS_DEBUG_BILLING || !_useMockBilling.value) return
        Log.d(TAG, "DEBUG: Simulating successful purchase")
        CoroutineScope(Dispatchers.Main).launch {
            _subscriptionState.value = SubscriptionState.PURCHASED
            premiumManager.updatePremiumStatus(true)
        }
    }

    /**
     * Called from UI to reset premium in debug mode.
     */
    fun debugResetPurchase() {
        if (!IS_DEBUG_BILLING || !_useMockBilling.value) return
        Log.d(TAG, "DEBUG: Resetting purchase")
        CoroutineScope(Dispatchers.Main).launch {
            _subscriptionState.value = SubscriptionState.NOT_PURCHASED
            premiumManager.updatePremiumStatus(false)
        }
    }

    fun querySubscriptions() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MONTHLY_SUB)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(YEARLY_SUB)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { details ->
                    when (details.productId) {
                        MONTHLY_SUB -> _monthlyDetails.value = details
                        YEARLY_SUB -> _yearlyDetails.value = details
                    }
                }
                Log.d(TAG, "Found ${productDetailsList.size} products")
                if (productDetailsList.isNotEmpty()) {
                    _useMockBilling.value = false
                    Log.d(TAG, "Real products found — disabling mock billing")
                } else {
                    Log.d(TAG, "No products found in Play Console — keeping mock mode")
                }
            } else {
                Log.e(TAG, "Query failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun queryExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasActiveSub = purchasesList.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _subscriptionState.value = if (hasActiveSub) {
                    SubscriptionState.PURCHASED
                } else {
                    SubscriptionState.NOT_PURCHASED
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User cancelled purchase")
            }
            else -> {
                Log.e(TAG, "Purchase error: ${billingResult.debugMessage}")
                _subscriptionState.value = SubscriptionState.ERROR
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _subscriptionState.value = SubscriptionState.PURCHASED

            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged")
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _subscriptionState.value = SubscriptionState.PENDING
        }
    }

    fun restorePurchases() {
        if (_useMockBilling.value) {
            Log.d(TAG, "DEBUG: Mock restore — no purchases to restore")
            return
        }
        queryExistingPurchases()
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}

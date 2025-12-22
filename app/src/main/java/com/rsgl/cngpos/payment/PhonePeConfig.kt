package com.rsgl.cngpos.payment

object PhonePeConfig {

    // ===== PRODUCTION CREDENTIALS =====
    const val MERCHANT_ID = "M23VAIS6MPRP8"
    const val CLIENT_ID = "SU2511111301182712747921"
    const val CLIENT_SECRET = "cbc109b2-2036-4e2b-92d0-358f686d08da"
    const val CLIENT_VERSION = "cbc109b2-2036-4e2b-92d0-358f686d08da"

    // ===== ENVIRONMENT =====
    const val ENVIRONMENT = "PRODUCTION"

    // ===== OAUTH (AUTHORIZATION) =====
    const val AUTH_URL =
        "https://api.phonepe.com/apis/identity-manager/v1/oauth/token"

    // ===== PAYMENT APIs (PRODUCTION) =====
    const val CREATE_ORDER_URL =
        "https://api.phonepe.com/apis/checkout/v2/pay"

    const val STATUS_URL =
        "https://api.phonepe.com/apis/checkout/v2/order"

    // ===== REDIRECT =====
    const val REDIRECT_URL = "about:blank?success=true"
    const val FAILURE_REDIRECT_URL = "about:blank?success=false"
}

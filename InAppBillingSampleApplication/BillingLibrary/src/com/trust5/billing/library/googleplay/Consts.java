/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trust5.billing.library.googleplay;

/**
 * This class holds global constants that are used throughout the application to support in-app billing.
 */
class Consts {
	// The response codes for a request, defined by Android Market.
	enum ResponseCode {
		RESULT_OK,
		RESULT_USER_CANCELED,
		RESULT_SERVICE_UNAVAILABLE,
		RESULT_BILLING_UNAVAILABLE,
		RESULT_ITEM_UNAVAILABLE,
		RESULT_DEVELOPER_ERROR,
		RESULT_ERROR;

		// Converts from an ordinal value to the ResponseCode
		public static ResponseCode valueOf(int index) {
			ResponseCode[] values = ResponseCode.values();
			if (index < 0 || index >= values.length) {
				return RESULT_ERROR;
			}
			return values[index];
		}
	}

	// The possible states of an in-app purchase, as defined by Android Market.
	enum PurchaseState {
		// Responses to requestPurchase or restoreTransactions.
		PURCHASED,   // User was charged for the order.
		CANCELED,    // The charge failed on the server.
		REFUNDED;    // User received a refund for the order.

		// Converts from an ordinal value to the PurchaseState
		public static PurchaseState valueOf(int index) {
			PurchaseState[] values = PurchaseState.values();
			if (index < 0 || index >= values.length) {
				return CANCELED;
			}
			return values[index];
		}
	}

	/**
	 * This is the action we use to bind to the MarketBillingService.
	 */
	static final String MARKET_BILLING_SERVICE_ACTION =
			"com.android.vending.billing.MarketBillingService.BIND";

	// Intent actions that we send from the BillingReceiver to the
	// BillingService.  Defined by this application.
	static final String ACTION_CONFIRM_NOTIFICATION =
			"com.trust5.subscriptions.CONFIRM_NOTIFICATION";
	static final String ACTION_GET_PURCHASE_INFORMATION =
			"com.trust5.subscriptions.GET_PURCHASE_INFORMATION";
	static final String ACTION_RESTORE_TRANSACTIONS =
			"com.trust5.subscriptions.RESTORE_TRANSACTIONS";

	// Intent actions that we receive in the BillingReceiver from Market.
	// These are defined by Market and cannot be changed.
	static final String ACTION_NOTIFY = "com.android.vending.billing.IN_APP_NOTIFY";
	static final String ACTION_RESPONSE_CODE =
			"com.android.vending.billing.RESPONSE_CODE";
	static final String ACTION_PURCHASE_STATE_CHANGED =
			"com.android.vending.billing.PURCHASE_STATE_CHANGED";

	// These are the names of the extras that are passed in an intent from
	// Market to this application and cannot be changed.
	static final String NOTIFICATION_ID = "notification_id";
	static final String INAPP_SIGNED_DATA = "inapp_signed_data";
	static final String INAPP_SIGNATURE = "inapp_signature";
	static final String INAPP_REQUEST_ID = "request_id";
	static final String INAPP_RESPONSE_CODE = "response_code";

	// These are the names of the fields in the request bundle.
	static final String BILLING_REQUEST_METHOD = "BILLING_REQUEST";
	static final String BILLING_REQUEST_API_VERSION = "API_VERSION";
	static final String BILLING_REQUEST_PACKAGE_NAME = "PACKAGE_NAME";
	static final String BILLING_REQUEST_ITEM_ID = "ITEM_ID";
	static final String BILLING_REQUEST_ITEM_TYPE = "ITEM_TYPE";
	static final String BILLING_REQUEST_DEVELOPER_PAYLOAD = "DEVELOPER_PAYLOAD";
	static final String BILLING_REQUEST_NOTIFY_IDS = "NOTIFY_IDS";
	static final String BILLING_REQUEST_NONCE = "NONCE";

	static final String BILLING_RESPONSE_RESPONSE_CODE = "RESPONSE_CODE";
	static final String BILLING_RESPONSE_PURCHASE_INTENT = "PURCHASE_INTENT";
	static final String BILLING_RESPONSE_REQUEST_ID = "REQUEST_ID";
	static long BILLING_RESPONSE_INVALID_REQUEST_ID = -1;

	// These are the types supported in the IAB v2
	static final String ITEM_TYPE_INAPP = "inapp";
	static final String ITEM_TYPE_SUBSCRIPTION = "subs";

	static final boolean DEBUG = true;

	static final String SHARED_PREFS_NAME = "PlayBillingSharedPreferencesFile";

}

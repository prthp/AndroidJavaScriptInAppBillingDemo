package com.trust5.billing.library.googleplay;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import com.trust5.billing.library.IBilling;
import com.trust5.billing.library.IPurchaseResponseCallback;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Ruairi Date: 08/11/12 Time: 19:44
 */
public class PlayBilling implements IBilling {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG = PlayBilling.class.getSimpleName();

	/**
	 * The SharedPreferences key for recording whether we initialized the database.  If false, then we perform a
	 * RestoreTransactions request to get all the purchases for this user.
	 */
	private static final String DB_INITIALIZED = "db_initialized";

	// ===========================================================
	// Fields
	// ===========================================================
	private Handler mHandler;
	private PlayBillingService mPlayBillingService;
	private PlayBillingPurchaseObserver mPlayBillingPurchaseObserver;
	private PurchaseDatabase mPurchaseDatabase;
	private Cursor mOwnedItemsCursor;
	private Set<String> mOwnedItems = new HashSet<String>();

	private Context mContext;
	private IPurchaseResponseCallback mResponseCallback;

	// ===========================================================
	// Constructors
	// ===========================================================
	public PlayBilling(final Context pContext, final IPurchaseResponseCallback pResponseCallback) {
		this.mContext = pContext;
		this.mResponseCallback = pResponseCallback;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public Cursor getOwnedItemsCursor() {
		return mOwnedItemsCursor;
	}

	public Set<String> getOwnedItemsSet() {
		return mOwnedItems;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	/**
	 * Should be called in Activity onCreate()
	 */
	@Override
	public PlayBilling initialize() {
		mHandler = new Handler();
		mPlayBillingPurchaseObserver = new PlayBillingPurchaseObserver();
		mPlayBillingService = new PlayBillingService();
		mPlayBillingService.setContext(mContext);
		mPurchaseDatabase = new PurchaseDatabase(mContext);


		//TODO: Possibly move this to onStart or onResume
		ResponseHandler.register(mPlayBillingPurchaseObserver);
		initializeOwnedItems();

		return this;
	}

	@Override
	public boolean buyItem(String pItemID, String pPayload) {
		return mPlayBillingService.requestPurchase(pItemID, Consts.ITEM_TYPE_INAPP, pPayload);
	}

	@Override
	public boolean buySubscription(String pItemID, String pPayload) {
		return mPlayBillingService.requestPurchase(pItemID, Consts.ITEM_TYPE_SUBSCRIPTION, pPayload);
	}

	/**
	 * @return True if billing is currently available
	 */
	@Override
	public boolean checkBillingAvailable() {
		return mPlayBillingService.checkBillingSupported();
	}

	/**
	 * @return True if subscriptions are currently available
	 */
	@Override
	public boolean checkSubscriptionsAvailable() {
		return mPlayBillingService.checkBillingSupported(Consts.ITEM_TYPE_SUBSCRIPTION);
	}

	/**
	 * Should be called in Activity onDestroy()
	 */
	@Override
	public void dispose() {
		//TODO: Possibly move this to be called in onStop or onPause
		ResponseHandler.unregister(mPlayBillingPurchaseObserver);


		mPurchaseDatabase.close();
		mPlayBillingService.unbind();
		mContext = null;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public SimpleCursorAdapter getOwnedItemsAdapter(Context pContext, int layout, int pNameResourceID,
													int pItemQuantityResourceID) {
		String[] from = new String[]{PurchaseDatabase.PURCHASED_PRODUCT_ID_COL,
				PurchaseDatabase.PURCHASED_QUANTITY_COL
		};
		int[] to = new int[]{pNameResourceID, pItemQuantityResourceID};
		return new SimpleCursorAdapter(pContext, layout,
				mOwnedItemsCursor, from, to);
	}

	/**
	 * If the database has not been initialized, we send a RESTORE_TRANSACTIONS request to Android Market to get the list
	 * of purchased items for this user. This happens if the application has just been installed or the user wiped data. We
	 * do not want to do this on every startup, rather, we want to do only when the database needs to be initialized.
	 */
	private void restoreDatabase() {
		SharedPreferences prefs = mContext.getSharedPreferences(Consts.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		boolean initialized = prefs.getBoolean(DB_INITIALIZED, false);
		if (!initialized) {
			mPlayBillingService.restoreTransactions();
			Toast.makeText(mContext, "Restoring transactions", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Creates a background thread that reads the database and initializes the set of owned items.
	 */
	private void initializeOwnedItems() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				doInitializeOwnedItems();
			}
		}).start();
	}

	/**
	 * Reads the set of purchased items from the database in a background thread and then adds those items to the set of
	 * owned items in the main UI thread.
	 */
	private void doInitializeOwnedItems() {
		Cursor cursor = mPurchaseDatabase.queryAllPurchasedItems();
		if (cursor == null) {
			return;
		}
		mOwnedItemsCursor = cursor;
		final Set<String> ownedItems = new HashSet<String>();
		try {
			int productIdCol = cursor.getColumnIndexOrThrow(
					PurchaseDatabase.PURCHASED_PRODUCT_ID_COL);
			while (cursor.moveToNext()) {
				String productId = cursor.getString(productIdCol);
				ownedItems.add(productId);
			}

		} finally {
			cursor.close();
		}

		// We will add the set of owned items in a new Runnable that runs on
		// the UI thread so that we don't need to synchronize access to
		// mOwnedItems.
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mOwnedItems.addAll(ownedItems);
			}
		});
	}

	/**
	 * List subscriptions for this package in Google Play
	 * <p/>
	 * This allows users to unsubscribe from this apps subscriptions.
	 * <p/>
	 * Subscriptions are listed on the Google Play app detail page, so this should only be called if subscriptions are
	 * known to be present.
	 */
	public final void editSubscriptions() {
		// Get current package name
		String packageName = mContext.getPackageName();
		// Open app detail in Google Play
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse("market://details?id=" + packageName));
		mContext.startActivity(i);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	/**
	 * A {@link PurchaseObserver} is used to get callbacks when Android Market sends messages to this application so that
	 * we can update the UI.
	 */
	private class PlayBillingPurchaseObserver extends PurchaseObserver {
		public PlayBillingPurchaseObserver() {
			super(mContext, mHandler);
		}

		@Override
		public void onBillingSupported(boolean supported, String type) {
			if (Consts.DEBUG) {
				Log.i(TAG, "supported: " + supported);
			}
			if (type == null || type.equals(Consts.ITEM_TYPE_INAPP)) {
				if (supported) {
					restoreDatabase();
				}
				mResponseCallback.onBillingSupported(supported);
			}
			else if (type.equals(Consts.ITEM_TYPE_SUBSCRIPTION)) {
				mResponseCallback.onSubscriptionSupported(supported);
			}
			else {
				mResponseCallback.onSubscriptionSupported(false);
			}
		}

		@Override
		public void onPurchaseStateChange(Consts.PurchaseState purchaseState, String itemId,
										  int quantity, long purchaseTime, String developerPayload) {
			if (Consts.DEBUG) {
				Log.i(TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
			}

			if (developerPayload == null) {
				Log.i(TAG, "ItemID=" + itemId + " : Purchase state=" + purchaseState.toString());
			}
			else {
				Log.i(TAG, "ItemID=" + itemId + " : Purchase state=" + purchaseState + "\n\t" + developerPayload);
			}

			if (purchaseState == Consts.PurchaseState.PURCHASED) {
				mOwnedItems.add(itemId);
				mResponseCallback.onPurchaseSuccess(itemId, quantity, purchaseTime);
				mOwnedItemsCursor.requery();
			}
			else if (purchaseState == Consts.PurchaseState.REFUNDED) {
				mResponseCallback.onPurchaseRefunded(itemId, quantity, purchaseTime);
			}
			else if (purchaseState == Consts.PurchaseState.CANCELED) {
				mResponseCallback.onPurchaseCancelled(itemId, quantity, purchaseTime);
			}
		}

		@Override
		public void onRequestPurchaseResponse(PlayBillingService.RequestPurchase request,
											  Consts.ResponseCode responseCode) {
			if (Consts.DEBUG) {
				Log.d(TAG, request.mProductId + ": " + responseCode);
			}
			if (responseCode == Consts.ResponseCode.RESULT_OK) {
				if (Consts.DEBUG) {
					Log.i(TAG, "purchase was successfully sent to server");
				}
				mResponseCallback.onPurchaseSent(request.mProductId);
			}
			else if (responseCode == Consts.ResponseCode.RESULT_USER_CANCELED) {
				if (Consts.DEBUG) {
					Log.i(TAG, "user canceled purchase");
				}
				mResponseCallback.onPurchaseCancelled(request.mProductId, 0, new Date().getTime());
			}
			else {
				if (Consts.DEBUG) {
					Log.i(TAG, "purchase failed");
				}
				mResponseCallback.onPurchaseFail(request.mProductId, 0, new Date().getTime());
			}
		}

		@Override
		public void onRestoreTransactionsResponse(PlayBillingService.RestoreTransactions request,
												  Consts.ResponseCode responseCode) {
			if (responseCode == Consts.ResponseCode.RESULT_OK) {
				if (Consts.DEBUG) {
					Log.d(TAG, "completed RestoreTransactions request");
				}
				// Update the shared preferences so that we don't perform
				// a RestoreTransactions again.
				SharedPreferences prefs = mContext.getSharedPreferences(Consts.SHARED_PREFS_NAME,
						Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = prefs.edit();
				edit.putBoolean(DB_INITIALIZED, true);
				edit.commit();

				mResponseCallback.onRestoreTransactionsSuccess();
			}
			else {
				if (Consts.DEBUG) {
					Log.d(TAG, "RestoreTransactions error: " + responseCode);
				}
				mResponseCallback.onRestoreTransactionsError();
			}
		}
	}
}

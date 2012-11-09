package com.trust5.billing.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.trust5.billing.library.IPurchaseResponseCallback;
import com.trust5.billing.library.googleplay.PlayBilling;

import java.util.Locale;

public class MainActivity extends Activity implements View.OnClickListener,
		AdapterView.OnItemSelectedListener {

	private static final String TAG = "MainActivity";

	private static boolean sDebug;

	/**
	 * The developer payload that is sent with subsequent purchase requests.
	 */
	private String mPayloadContents = null;

	private static final int DIALOG_CANNOT_CONNECT_ID = 1;
	private static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 2;
	private static final int DIALOG_SUBSCRIPTIONS_NOT_SUPPORTED_ID = 3;

	/**
	 * Used for storing the log text.
	 */
	private static final String LOG_TEXT_KEY = "SAMPLE_APP_LOG_TEXT";

	private Button mBuyButton;
	private Button mEditPayloadButton;
	private Button mEditSubscriptionsButton;
	private TextView mLogTextView;
	private Spinner mSelectItemSpinner;
	private ListView mOwnedItemsTable;
	private SimpleCursorAdapter mOwnedItemsAdapter;

	private String mItemName;
	private String mSku;
	private Catalog.Managed mManagedType;
	private CatalogAdapter mCatalogAdapter;

	private PlayBilling mPlayBilling;
	private IPurchaseResponseCallback mPurchaseResponseCallback;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		sDebug = ((0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)));
		mPurchaseResponseCallback = new PurchaseResponseCallBack();
		mPlayBilling = new PlayBilling(this, mPurchaseResponseCallback).initialize();
		setupWidgets();
		if (!mPlayBilling.checkBillingAvailable()) {
			showDialog(DIALOG_CANNOT_CONNECT_ID);
		}

		if (!mPlayBilling.checkSubscriptionsAvailable()) {
			showDialog(DIALOG_SUBSCRIPTIONS_NOT_SUPPORTED_ID);
		}
	}

	/**
	 * Called when this activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
	}

	/**
	 * Called when this activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPlayBilling.dispose();
	}

	/**
	 * Save the context of the log so simple things like rotation will not result in the log being cleared.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(LOG_TEXT_KEY, Html.toHtml((Spanned) mLogTextView.getText()));
	}

	/**
	 * Restore the contents of the log if it has previously been saved.
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null) {
			mLogTextView.setText(Html.fromHtml(savedInstanceState.getString(LOG_TEXT_KEY)));
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CANNOT_CONNECT_ID:
				return createDialog(R.string.cannot_connect_title,
						R.string.cannot_connect_message);
			case DIALOG_BILLING_NOT_SUPPORTED_ID:
				return createDialog(R.string.billing_not_supported_title,
						R.string.billing_not_supported_message);
			case DIALOG_SUBSCRIPTIONS_NOT_SUPPORTED_ID:
				return createDialog(R.string.subscriptions_not_supported_title,
						R.string.subscriptions_not_supported_message);
			default:
				return null;
		}
	}

	private Dialog createDialog(int titleId, int messageId) {
		String helpUrl = replaceLanguageAndRegion(getString(R.string.help_url));
		if (sDebug) {
			Log.i(TAG, helpUrl);
		}

		final Uri helpUri = Uri.parse(helpUrl);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(titleId)
				.setIcon(android.R.drawable.stat_sys_warning)
				.setMessage(messageId)
				.setCancelable(false)
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(R.string.learn_more, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW, helpUri);
						startActivity(intent);
					}
				});
		return builder.create();
	}

	/**
	 * Replaces the language and/or country of the device into the given string. The pattern "%lang%" will be replaced by
	 * the device's language code and the pattern "%region%" will be replaced with the device's country code.
	 *
	 * @param str the string to replace the language/country within
	 * @return a string containing the local language and region codes
	 */
	private String replaceLanguageAndRegion(String str) {
		// Substitute language and or region if present in string
		if (str.contains("%lang%") || str.contains("%region%")) {
			Locale locale = Locale.getDefault();
			str = str.replace("%lang%", locale.getLanguage().toLowerCase());
			str = str.replace("%region%", locale.getCountry().toLowerCase());
		}
		return str;
	}

	/**
	 * Sets up the UI.
	 */
	private void setupWidgets() {
		mLogTextView = (TextView) findViewById(R.id.log);

		mBuyButton = (Button) findViewById(R.id.buy_button);
		mBuyButton.setEnabled(false);
		mBuyButton.setOnClickListener(this);

		mEditPayloadButton = (Button) findViewById(R.id.payload_edit_button);
		mEditPayloadButton.setEnabled(false);
		mEditPayloadButton.setOnClickListener(this);

		mEditSubscriptionsButton = (Button) findViewById(R.id.subscriptions_edit_button);
		mEditSubscriptionsButton.setVisibility(View.INVISIBLE);
		mEditSubscriptionsButton.setOnClickListener(this);

		mSelectItemSpinner = (Spinner) findViewById(R.id.item_choices);
		mCatalogAdapter = new CatalogAdapter(this, Catalog.CATALOG);
		mSelectItemSpinner.setAdapter(mCatalogAdapter);
		mSelectItemSpinner.setOnItemSelectedListener(this);

		Cursor ownedItems = mPlayBilling.getOwnedItemsCursor();

		startManagingCursor(ownedItems);
		mOwnedItemsAdapter = mPlayBilling.getOwnedItemsAdapter(this, R.layout.item_row, R.id.item_name,
				R.id.item_quantity);
		mOwnedItemsTable = (ListView) findViewById(R.id.owned_items);
		mOwnedItemsTable.setAdapter(mOwnedItemsAdapter);
	}

	private void prependLogEntry(CharSequence cs) {
		SpannableStringBuilder contents = new SpannableStringBuilder(cs);
		contents.append('\n');
		contents.append(mLogTextView.getText());
		mLogTextView.setText(contents);
	}

	private void logProductActivity(String product, String activity) {
		SpannableStringBuilder contents = new SpannableStringBuilder();
		contents.append(Html.fromHtml("<b>" + product + "</b>: "));
		contents.append(activity);
		prependLogEntry(contents);
	}

	/**
	 * Called when a button is pressed.
	 */
	@Override
	public void onClick(View v) {
		if (v == mBuyButton) {
			if (sDebug) {
				Log.d(TAG, "buying: " + mItemName + " sku: " + mSku);
			}

			if (mManagedType != Catalog.Managed.SUBSCRIPTION &&
					!mPlayBilling.buyItem(mSku, mPayloadContents)) {
				showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
			}
			else if (!mPlayBilling.buySubscription(mSku, mPayloadContents)) {
				// Note: mManagedType == Managed.SUBSCRIPTION
				showDialog(DIALOG_SUBSCRIPTIONS_NOT_SUPPORTED_ID);
			}
		}
		else if (v == mEditPayloadButton) {
			showPayloadEditDialog();
		}
		else if (v == mEditSubscriptionsButton) {
			mPlayBilling.editSubscriptions();
		}
	}

	/**
	 * Displays the dialog used to edit the payload dialog.
	 */
	private void showPayloadEditDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		final View view = View.inflate(this, R.layout.edit_payload, null);
		final TextView payloadText = (TextView) view.findViewById(R.id.payload_text);
		if (mPayloadContents != null) {
			payloadText.setText(mPayloadContents);
		}

		dialog.setView(view);
		dialog.setPositiveButton(
				R.string.edit_payload_accept,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mPayloadContents = payloadText.getText().toString();
					}
				});
		dialog.setNegativeButton(
				R.string.edit_payload_clear,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null) {
							mPayloadContents = null;
							dialog.cancel();
						}
					}
				});
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				if (dialog != null) {
					dialog.cancel();
				}
			}
		});
		dialog.show();
	}

	/**
	 * Called when an item in the spinner is selected.
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mItemName = getString(Catalog.CATALOG[position].nameId);
		mSku = Catalog.CATALOG[position].sku;
		mManagedType = Catalog.CATALOG[position].managed;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}


	private class PurchaseResponseCallBack implements IPurchaseResponseCallback {
		@Override
		public void onPurchaseSent(String pItemId) {
			logProductActivity(pItemId, "sending purchase request");
		}

		@Override
		public void onPurchaseSuccess(String pItemId, int pQuantity, long pPurchaseTime) {
			// If this is a subscription, then enable the "Edit
			// Subscriptions" button.
			for (Catalog.CatalogEntry e : Catalog.CATALOG) {
				if (e.sku.equals(pItemId) &&
						e.managed.equals(Catalog.Managed.SUBSCRIPTION)) {
					mEditSubscriptionsButton.setVisibility(View.VISIBLE);
				}
			}
			mCatalogAdapter.setOwnedItems(mPlayBilling.getOwnedItemsSet());
		}

		@Override
		public void onPurchaseCancelled(String pItemId, int pQuantity, long pPurchaseTime) {
			logProductActivity(pItemId, "dismissed purchase dialog");
		}

		@Override
		public void onPurchaseRefunded(String pItemId, int pQuantity, long pPurchaseTime) {
			logProductActivity(pItemId, "request purchase returned failed");
		}

		@Override
		public void onPurchaseFail(String pItemId, int pQuantity, long pPurchaseTime) {
			logProductActivity(pItemId, "onPurchaseFail");
		}

		@Override
		public void onBillingSupported(boolean pIsSupported) {
			if (pIsSupported) {
				mBuyButton.setEnabled(true);
				mEditPayloadButton.setEnabled(true);
			}
			else {
				showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
			}
		}

		@Override
		public void onSubscriptionSupported(boolean pIsSupported) {
			mCatalogAdapter.setSubscriptionsSupported(pIsSupported);
			if (!pIsSupported) {
				showDialog(DIALOG_SUBSCRIPTIONS_NOT_SUPPORTED_ID);
			}
		}

		@Override
		public void onRestoreTransactionsSuccess() {
			if (sDebug) {
				Log.d(TAG, "Restored Transactions");
			}
		}

		@Override
		public void onRestoreTransactionsError() {
			Log.d(TAG, "RestoreTransactions error");
		}
	}

}

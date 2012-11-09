package com.trust5.billing.library;

/**
 *
 */
public interface IPurchaseResponseCallback {

	public void onPurchaseSuccess(String pItemId, int pQuantity, long pPurchaseTime);

	public void onPurchaseCancelled(String pItemId, int pQuantity, long pPurchaseTime);

	public void onPurchaseRefunded(String pItemId, int pQuantity, long pPurchaseTime);

	public void onPurchaseFail(String pItemId, int pQuantity, long pPurchaseTime);

	public void onBillingSupported(boolean pIsSupported);

	public void onSubscriptionSupported(boolean pIsSupported);

	public void onRestoreTransactionsSuccess();

	public void onRestoreTransactionsError();

	public void onPurchaseSent(String pItemId);

}

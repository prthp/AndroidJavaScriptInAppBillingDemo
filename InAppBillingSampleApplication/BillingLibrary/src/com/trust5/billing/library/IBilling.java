package com.trust5.billing.library;

/**
 *
 */
public interface IBilling {

	/**
	 * Should be called in Activity onCreate()
	 */
	IBilling initialize();

	/**
	 * Should be called in Activity onDestroy()
	 */
	void dispose();

	/**
	 * @return True if billing is currently available
	 */
	boolean checkBillingAvailable();

	/**
	 * @return True if subscriptions are currently available
	 */
	boolean checkSubscriptionsAvailable();

	/**
	 * This returns a boolean indicating if the purchase request was successful. The returned value does not indicate the
	 * purchase status. Purchase status is handled using an object implementing {@link IPurchaseResponseCallback}
	 *
	 * @param pItemID  - The String value ID of the item to be purchased
	 * @param pPayload
	 * @return True if the request was successful
	 */
	boolean buyItem(String pItemID, String pPayload);

	/**
	 * This returns a boolean indicating if the purchase request was successful. The returned value does not indicate the
	 * purchase status. Purchase status is handled using an object implementing {@link IPurchaseResponseCallback}
	 *
	 * @param pItemID  - The String value ID of the Subscription item
	 * @param pPayload
	 * @return True if request was successful
	 */
	boolean buySubscription(String pItemID, String pPayload);
}

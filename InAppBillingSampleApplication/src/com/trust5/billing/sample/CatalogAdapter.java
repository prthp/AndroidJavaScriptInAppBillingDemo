package com.trust5.billing.sample;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * An adapter used for displaying a catalog of products.  If a product is managed by Android Market and already
 * purchased, then it will be "grayed-out" in the list and not selectable.
 */
public class CatalogAdapter extends ArrayAdapter<String> {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final String TAG = CatalogAdapter.class.getSimpleName();

	// ===========================================================
	// Fields
	// ===========================================================
	private Catalog.CatalogEntry[] mCatalog;
	private Set<String> mOwnedItems = new HashSet<String>();
	private boolean mIsSubscriptionsSupported = false;

	// ===========================================================
	// Constructors
	// ===========================================================
	public CatalogAdapter(Context context, Catalog.CatalogEntry[] catalog) {
		super(context, android.R.layout.simple_spinner_item);
		mCatalog = catalog;
		for (Catalog.CatalogEntry element : catalog) {
			add(context.getString(element.nameId));
		}
		setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public void setOwnedItems(Set<String> ownedItems) {
		mOwnedItems = ownedItems;
		notifyDataSetChanged();
	}

	public void setSubscriptionsSupported(boolean supported) {
		mIsSubscriptionsSupported = supported;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public boolean areAllItemsEnabled() {
		// Return false to have the adapter call isEnabled()
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// If the item at the given list position is not purchasable,
		// then prevent the list item from being selected.
		Catalog.CatalogEntry entry = mCatalog[position];
		return !(entry.managed == Catalog.Managed.MANAGED && mOwnedItems.contains(entry.sku)) && !(entry.managed == Catalog.Managed.SUBSCRIPTION && !mIsSubscriptionsSupported);
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// If the item at the given list position is not purchasable, then
		// "gray out" the list item.
		View view = super.getDropDownView(position, convertView, parent);
		view.setEnabled(isEnabled(position));
		return view;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}

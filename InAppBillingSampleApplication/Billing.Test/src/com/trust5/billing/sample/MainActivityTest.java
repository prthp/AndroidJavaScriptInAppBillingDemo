package com.trust5.billing.sample;

import android.content.Context;
import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;

/**
 *
 */
public class MainActivityTest extends ActivityUnitTestCase<MainActivity> {

	Intent mTestIntent;
	Context mTestContext;

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mTestContext = getInstrumentation().getTargetContext();
		mTestIntent = new Intent(mTestContext,
				MainActivity.class);

	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@SmallTest
	public void testSetup() {
		assertNotNull(mTestContext);
		assertNotNull(mTestIntent);
	}

}

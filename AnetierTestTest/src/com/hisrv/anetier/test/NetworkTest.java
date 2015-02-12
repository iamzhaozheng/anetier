package com.hisrv.anetier.test;

import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import com.hisrv.anetier.CurrencyRequest;
import com.hisrv.anetier.CurrencyResponse;
import com.hisrv.lib.anetier.HttpRequest.OnResponseListener;
import com.hisrv.lib.anetier.HttpResponse;

public class NetworkTest extends TestCase {
	
	public void testCurrency() throws InterruptedException {
		final CountDownLatch signal = new CountDownLatch(1);
		final int amount = 100;
		new CurrencyRequest(100).execute(new OnResponseListener() {
			
			@Override
			public void onGetResponse(HttpResponse resp) {
				CurrencyResponse r = (CurrencyResponse) resp;
				assertTrue(r.error == 0);
				assertEquals(getName(), amount * 6, r.amount, amount * 1);
				signal.countDown();
			}
		});
		signal.await();
	}
	
	
}

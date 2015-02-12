package com.hisrv.anetier;

import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.hisrv.lib.anetier.HttpRequest;
import com.hisrv.lib.anetier.HttpResponse;

public class CurrencyRequest extends HttpRequest {

	private int mAmount;
	
	public CurrencyRequest(int amount) {
		mAmount = amount;
	}
	
	@Override
	protected String getUrl() {
		return Urls.CURRENCY;
	}

	@Override
	protected HttpResponse getResponse(byte[] rst, Object tag) {
		return new CurrencyResponse(rst, tag);
	}
	
	@Override
	protected HttpResponse getMockResponse() {
		return null;
	}
	
	@Override
	protected void fillParams(List<NameValuePair> params) {
		params.add(new BasicNameValuePair("fromCurrency", "USD"));
		params.add(new BasicNameValuePair("toCurrency", "CNY"));
		params.add(new BasicNameValuePair("amount", String.valueOf(mAmount)));
	}

	@Override
	protected int postOrGet() {
		return HTTP_GET;
	}

}

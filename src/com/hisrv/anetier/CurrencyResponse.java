package com.hisrv.anetier;

import org.json.JSONException;
import org.json.JSONObject;

import com.hisrv.lib.anetier.HttpJSONResponse;

public class CurrencyResponse extends HttpJSONResponse {

	public double amount;
	
	//for mock
	public CurrencyResponse() {
		super();
		error = OK;
		amount = 100;
	}
	
	public CurrencyResponse(byte[] rst, Object tag) {
		super(rst, tag);
	}
	
	@Override
	protected void parse(JSONObject json) throws JSONException {
		error = json.getInt("errNum");
		errorMsg = json.getString("errMsg");
		if (error == 0) {
			JSONObject data = json.getJSONObject("retData");
			amount = data.getDouble("convertedamount");
		}
	}
	
}

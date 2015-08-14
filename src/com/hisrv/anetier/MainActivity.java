package com.hisrv.anetier;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.TextView;

import com.hisrv.lib.anetier.HttpRequest.OnResponseListener;
import com.hisrv.lib.anetier.HttpResponse;
import com.hisrv.lib.anetier.NetLog;

public class MainActivity extends Activity {

	private EditText mEditUsd;
	private TextView mTextCny;
	private View mBtnConvert;
	private AtomicBoolean mExcuting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		NetLog.debug(true);
		
		mEditUsd = (EditText) findViewById(R.id.usd);
		mTextCny = (TextView) findViewById(R.id.cny);
		mBtnConvert = findViewById(R.id.convert);
		mExcuting = new AtomicBoolean(false);
		
		mBtnConvert.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					int amount = Integer.parseInt(mEditUsd.getText().toString());
					if (mExcuting.getAndSet(true)) {
						return;
					}
					new CurrencyRequest(amount).execute(new OnResponseListener() {
						
						@Override
						public void onGetResponse(HttpResponse resp) {
							mExcuting.set(false);
							CurrencyResponse r = (CurrencyResponse) resp;
							if (r.error == 0) {
								mTextCny.setText(String.valueOf(r.amount));
							} else {
								mTextCny.setText(r.errorMsg);
							}
						}
					});
				} catch(NumberFormatException e) {
					mTextCny.setText("Number Error");
				}
			}
		});
	}
}

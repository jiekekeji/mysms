package com.jack.mysms.receive;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class SmsBroadcastReceive extends BroadcastReceiver{

	private static final String TAG = "SmsBroadcastReceive";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub		
		int resultCode = getResultCode();
		
		Log.i(TAG,"Activity.RESULT_OK=="+Activity.RESULT_OK);
		Log.i(TAG,"resultCode=="+resultCode);
		
		if(resultCode == Activity.RESULT_OK) {
			Toast.makeText(context, "发送成功", 0).show();
		} else {
			Toast.makeText(context, "发送失败", 0).show();
		}
	}

}

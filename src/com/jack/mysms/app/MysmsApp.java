package com.jack.mysms.app;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import android.app.Application;
/**
 * data:
 * com.android.providers.telephony    SmsProvider
 * 
 * @author Administrator
 *
 */
public class MysmsApp extends Application{

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		//初始化ifly语音
		SpeechUtility.createUtility(this, SpeechConstant.APPID+"=54429d66");
		super.onCreate();
	}
}

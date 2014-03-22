package com.jack.mssms.utils;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsManager;

public class Utils {

	private static final String TAG = "Utils";
	
	/**
	 * 根据号码获取联系人的姓名
	 * @param address
	 * @return
	 */
	public static String getContactName(ContentResolver resolver, String address) {
		/**
		 * A table that represents the result of looking up a phone number, 
		 * for example for caller ID. 
		 * To perform a lookup you must append the number you want to find to CONTENT_FILTER_URI. 
		 * This query is highly optimized.
		 * 通过联系人的电话号码查询 联系人在表Contacts中的其他信息
		 *  Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		 *  resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME,...          		 * 
		 */
		
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		
		Cursor cursor = resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
		
		if(cursor != null && cursor.moveToFirst()) {
			String contactName = cursor.getString(0);
			cursor.close();
			return contactName;
		}
		return null;
	}

	/**
	 * 根据联系人的号码查询联系人的头像
	 * @param resolver
	 * @param address
	 * @return
	 */
	public static Bitmap getContactIcon(ContentResolver resolver, String address) {
		
		// 1.根据号码取得联系人的id
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, address);
		
		Cursor cursor = resolver.query(uri, new String[]{PhoneLookup._ID}, null, null, null);
		
		if(cursor != null && cursor.moveToFirst()) {
			long id = cursor.getLong(0);
			cursor.close();
			
			// 2.根据id获取联系人的头像
			
			uri = ContentUris.withAppendedId(Contacts.CONTENT_URI, id);
			
			InputStream is = Contacts.openContactPhotoInputStream(resolver, uri);
			
			return BitmapFactory.decodeStream(is);
		}
		return null;
	}
	
	/**
	 * 发送短信
	 * @param address
	 * @param content
	 */
	public static void sendMessage(Context context, String address, String content) {
		SmsManager smsManager = SmsManager.getDefault();
		
		// 以70个字符分割短信
		ArrayList<String> divideMessage = smsManager.divideMessage(content);
		
		// 必须设置为隐士intent
		Intent intent = new Intent("com.jack.mysms.receive.SmsBroadcastReceive");
		
		PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, 
				intent, PendingIntent.FLAG_ONE_SHOT);
		
		for (String sms : divideMessage) {
			smsManager.sendTextMessage(
					address, 	// 接收人的号码
					null, 				// 短信中心的号码
					sms, 					// 短信的内容
					sentIntent, 			// 发送成功的回调广播
					null);		// 接收成功的回调广播
		}
		
		// 插入到数据库
		writeMessage(context, address, content);
	}
	
	/**
	 * 添加到数据库
	 * @param context
	 * @param address
	 * @param content
	 * 
	 * 如果不插入type列，也可以选择插入收件箱或者已发送
	 */
	public static void writeMessage(Context context, String address, String content) {
		ContentValues values = new ContentValues();
		values.put("address", address);
		values.put("type", Sms.SEND_TYPE);
		values.put("body", content);
		
		context.getContentResolver().insert(Sms.SMS_URI, values);
	}
	
	
	/**
	 * 查询联系人id
	 * @param resolver
	 * @param uri
	 * @return
	 */
	public static long getContactID(ContentResolver resolver, Uri uri) {
		Cursor cursor = resolver.query(uri, new String[]{"_id", "has_phone_number"}, null, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			int hasPhoneNumber = cursor.getInt(1);
			if(hasPhoneNumber > 0) {
				long id = cursor.getLong(0);
				cursor.close();
				return id;
			}
		}
		return -1;
	}
	
	/**
	 * 查询联系人id
	 * @param resolver
	 * @param id
	 * @return
	 */
	public static String getContactAddress(ContentResolver resolver, long id) {
		String selection = "contact_id = ?";
		String selectionArgs[] = {String.valueOf(id)};
		Cursor cursor = resolver.query(Phone.CONTENT_URI, new String[]{"data1"}, selection, selectionArgs, null);
		if(cursor != null && cursor.moveToFirst()) {
			String address = cursor.getString(0);
			cursor.close();
			return address;
		}
		return null;
	}
	
	/**
	 * 
	 * @param position
	 * @return
	 */
	public static Uri getUriFromIndex(int position) {
		switch (position) {
		case 0:
			return Sms.INBOX_URI;
		case 1:
			return Sms.OUTBOX_URI;
		case 2:
			return Sms.SENT_URI;
		case 3:
			return Sms.DRAFT_URI;
		default:
			break;
		}
		return null;
	}

}
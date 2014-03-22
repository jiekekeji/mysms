package com.jack.mysms.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.jack.mssms.utils.Sms;
import com.jack.mysms.db.GroupOpenHelper;

public class GroupContentProvider extends ContentProvider {

	
	private static final String AUTHORITY = "com.jack.mysms.provider.GroupContentProvider";
	
	private static final int GROUPS_INSERT = 001;
	private static final int GROUPS_QUERY_ALL = 002;
	private static final int THREAD_GROUP_QUERY_ALL = 003;
	private static final int THREAD_GROUP_INSERT = 004;
	private static final int GROUPS_UPDATE = 005;
	private static final int GROUPS_DELETE = 006;
	
	private static final String GROUPS_TABLE_NAME = "groups";	
	private static final String THREAD_GROUP_TABLE_NAME ="thread_group";

	private static UriMatcher uriMatcher;
	private GroupOpenHelper groupOpenHelper;
	
	static {
		uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
		
		uriMatcher.addURI(AUTHORITY, "groups/insert", GROUPS_INSERT);
		uriMatcher.addURI(AUTHORITY, "groups", GROUPS_QUERY_ALL);
		uriMatcher.addURI(AUTHORITY, "groups/update", GROUPS_UPDATE);
		uriMatcher.addURI(AUTHORITY, "groups/delete/#", GROUPS_DELETE);
		
		uriMatcher.addURI(AUTHORITY, "thread_group", THREAD_GROUP_QUERY_ALL);
		uriMatcher.addURI(AUTHORITY, "thread_group/insert", THREAD_GROUP_INSERT);
		
	}
	@Override
	public boolean onCreate() {
		groupOpenHelper = GroupOpenHelper.getInstance(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteDatabase db=groupOpenHelper.getReadableDatabase();
		
		switch (uriMatcher.match(uri)) {
		
		case GROUPS_QUERY_ALL:			
			if (db.isOpen()) {
				Cursor cursor=db.query(GROUPS_TABLE_NAME, projection, selection, selectionArgs, sortOrder, null, null, null);
				//给cursor添加一个监听，当数据改变时重新查询
				cursor.setNotificationUri(getContext().getContentResolver(), Sms.GROUPS_QUERY_ALL_URI);
				return cursor;
			}
			
		case THREAD_GROUP_QUERY_ALL:	//查询会话群组
			if(db.isOpen()) {
				Cursor cursor = db.query(THREAD_GROUP_TABLE_NAME, projection, selection, selectionArgs, sortOrder, null, null,null);
				return cursor;
			}
			break;


		default:
			new IllegalAccessException("未知uri:"+uri);
			break;
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		SQLiteDatabase db=groupOpenHelper.getWritableDatabase();
		
		switch (uriMatcher.match(uri)) {
		case GROUPS_INSERT://匹配成功			
			if (db.isOpen()) {
				long id=db.insert(GROUPS_TABLE_NAME, null, values);
				//通知查询的cursor重新查询
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI,null);
				return ContentUris.withAppendedId(uri, id);
			}
			break;
		case THREAD_GROUP_INSERT:
			if (db.isOpen()) {
				long id=db.insert(THREAD_GROUP_TABLE_NAME, null, values);
				//通知查询的cursor重新查询
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI,null);
				return ContentUris.withAppendedId(uri, id);
			}
			
		default:
			new IllegalAccessException("未知uri:"+uri);
			break;
		};
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
		case GROUPS_DELETE:
			SQLiteDatabase db=groupOpenHelper.getWritableDatabase();
			if (db.isOpen()) {
				db.beginTransaction();
				try {
					long group_id = ContentUris.parseId(uri);
					String where = "_id = " + group_id;
					//在群组表删除
					int count = db.delete(GROUPS_TABLE_NAME, where, null);				
					//在关联表删除
					where = "group_id = " + group_id;
					db.delete(THREAD_GROUP_TABLE_NAME, where, null);
					
					//通知GROUPS表发生改变֪
					getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI, null);
					db.setTransactionSuccessful();
					return count;				
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}finally{
					db.endTransaction();
				}				
			}
			break;

		default:
			new IllegalAccessException("未知uri:"+uri);
			break;
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
		case GROUPS_UPDATE:
			SQLiteDatabase db=groupOpenHelper.getWritableDatabase();
			if (db.isOpen()) {
				int count=db.update(GROUPS_TABLE_NAME, values, selection, selectionArgs);
				//通知数据改变
				getContext().getContentResolver().notifyChange(Sms.GROUPS_QUERY_ALL_URI,null);
				return count;
			}		
			break;

		default:
			new IllegalAccessException("未知uri:"+uri);
			break;
		}
		return 0;
	}
	


}

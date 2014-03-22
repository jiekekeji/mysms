package com.jack.mssms.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.CursorAdapter;

/**
 * 
 * @author Administrator
 *
 */
public class MyAsyncQuery extends AsyncQueryHandler{

	private OnQueryNotifyCompleteListener mOnQueryNotifyCompleteListener;

	public MyAsyncQuery(ContentResolver cr) {
		super(cr);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 异步查询结束的回调函数
	 */
	@Override
	protected void onQueryComplete(int token, Object cursorAdapter, Cursor cursor) {
		// TODO Auto-generated method stub		
		// 在刷新view之前, 让用户做一些准备操作
		if(mOnQueryNotifyCompleteListener != null) {
			mOnQueryNotifyCompleteListener.onPreNotify(token, cursorAdapter, cursor);
		}
				
		if (cursorAdapter!=null) {
			notifyAdapter((CursorAdapter)cursorAdapter, cursor);
		}
		
		// 通知用户刷新完成, 用户可以操作一些事情
		if(mOnQueryNotifyCompleteListener != null) {
			mOnQueryNotifyCompleteListener.onPostNotify(token, cursorAdapter, cursor);
		}
				
		super.onQueryComplete(token, cursorAdapter, cursor);
	}
	/**
	 * 
	 * @param cursorAdapter
	 * @param cursor
	 */
	private void notifyAdapter(CursorAdapter cursorAdapter,Cursor cursor) {
		// TODO Auto-generated method stub
		cursorAdapter.changeCursor(cursor);

	}

	public void setOnQueryNotifyCompleteListener(OnQueryNotifyCompleteListener l) {
		this.mOnQueryNotifyCompleteListener = l;
	}
	
	/**
	 * @author andong
	 * 当查询数据完成并且适配数据完成的监听事件
	 */
	public interface OnQueryNotifyCompleteListener {
		
		/**
		 * 当adapter更新之前回调此方法(用户做一些适配数据之前的准备操作)
		 * @param token
		 * @param cookie
		 * @param cursor
		 */
		void onPreNotify(int token, Object cookie, Cursor cursor);
		
		/**
		 * 当刷新完数据之后回调此方法
		 * @param token
		 * @param cookie
		 * @param cursor
		 */
		void onPostNotify(int token, Object cookie, Cursor cursor);
	}
	
}

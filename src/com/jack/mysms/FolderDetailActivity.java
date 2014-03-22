package com.jack.mysms;

import java.util.HashMap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jack.mssms.utils.MyAsyncQuery;
import com.jack.mssms.utils.MyAsyncQuery.OnQueryNotifyCompleteListener;
import com.jack.mssms.utils.Utils;

public class FolderDetailActivity extends Activity implements
		OnQueryNotifyCompleteListener, OnItemClickListener {

	private int index;

	private FolderDetailAdapter mAdapter;
	private final String[] projection = { "_id", "address", "date", "body" };
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int BODY_COLUMN_INDEX = 3;

	private HashMap<Integer, String> dateMap; //
	private HashMap<Integer, Integer> smsRealPositionMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder_detail);
		index = getIntent().getIntExtra("index", -1);

		initActionBar();
		initView();
		prepareData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * 
	 * @param index
	 */
	private void initActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = getActionBar();
		// 返回箭头
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		switch (index) {
		case 0:
			actionBar.setTitle("收件箱");
			break;
		case 1:
			actionBar.setTitle("发件箱");
			break;
		case 2:
			actionBar.setTitle("已发送");
			break;
		case 3:
			actionBar.setTitle("草稿箱");
			break;
		default:
			break;
		}
	}

	private void initView() {
		// TODO Auto-generated method stub
		dateMap = new HashMap<Integer, String>();
		smsRealPositionMap = new HashMap<Integer, Integer>();

		ListView mListView = (ListView) findViewById(R.id.listView_folder_detail);

		mAdapter = new FolderDetailAdapter(this, null);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

	}

	private void prepareData() {
		Uri uri = Utils.getUriFromIndex(index);
		MyAsyncQuery asyncQuery = new MyAsyncQuery(getContentResolver());
		asyncQuery.setOnQueryNotifyCompleteListener(this);
		asyncQuery.startQuery(0, mAdapter, uri, projection, null, null,
				"date desc");
	}

	private class FolderDetailAdapter extends CursorAdapter {

		private FolderDetailHolderView mHolder;

		public FolderDetailAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return dateMap.size() + smsRealPositionMap.size();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//
			if(dateMap.containsKey(position)) {	//
				TextView tvDate = new TextView(FolderDetailActivity.this);
				tvDate.setBackgroundResource(android.R.color.darker_gray);
				tvDate.setTextSize(20);
				tvDate.setTextColor(Color.WHITE);
				tvDate.setGravity(Gravity.CENTER);
				tvDate.setText(dateMap.get(position));
				return tvDate;
			}
			
			// 
			Cursor mCursor = mAdapter.getCursor();
			mCursor.moveToPosition(smsRealPositionMap.get(position));
			
	        View v;
	        if (convertView == null || convertView instanceof TextView) {
	            v = newView(FolderDetailActivity.this, mCursor, parent);
	        } else {
	            v = convertView;
	        }
	        bindView(v, FolderDetailActivity.this, mCursor);
	        return v;
	    }
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = View
					.inflate(context, R.layout.item_folder_detail, null);
			mHolder = new FolderDetailHolderView();
			mHolder.ivIcon = (ImageView) view
					.findViewById(R.id.iv_conversation_item_icon);
			mHolder.tvName = (TextView) view
					.findViewById(R.id.tv_conversation_item_name);
			mHolder.tvDate = (TextView) view
					.findViewById(R.id.tv_conversation_item_date);
			mHolder.tvBody = (TextView) view
					.findViewById(R.id.tv_conversation_item_body);
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			mHolder = (FolderDetailHolderView) view.getTag();

			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);

			String contactName = Utils.getContactName(getContentResolver(),
					address);
			if (TextUtils.isEmpty(contactName)) {
				mHolder.tvName.setText(address);
				mHolder.ivIcon
						.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				mHolder.tvName.setText(contactName);

				Bitmap contactIcon = Utils.getContactIcon(getContentResolver(),
						address);
				if (contactIcon == null) {
					mHolder.ivIcon
							.setBackgroundResource(R.drawable.ic_contact_picture);
				} else {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(
							contactIcon));
				}
			}

			String strDate = null;
			if (DateUtils.isToday(date)) {
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			mHolder.tvDate.setText(strDate);

			mHolder.tvBody.setText(body);
		}
	}

	public class FolderDetailHolderView {
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}

	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub

		if (cursor != null && cursor.getCount() > 0) {

			java.text.DateFormat dateFormat = DateFormat.getDateFormat(this);
			int listViewIndex = 0; //

			String strDate;

			while (cursor.moveToNext()) {
				//
				strDate = dateFormat.format(cursor.getLong(DATE_COLUMN_INDEX));

				//
				if (!dateMap.containsValue(strDate)) {
					dateMap.put(listViewIndex, strDate);
					listViewIndex++;
				}

				//
				smsRealPositionMap.put(listViewIndex, cursor.getPosition());
				listViewIndex++;
			}
			//
			cursor.moveToPosition(-1);
		}
	}

	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub


	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

		if(!dateMap.containsKey(position)) {
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(smsRealPositionMap.get(position));
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			Intent intent = new Intent(this, SmsDetailActivity.class);
			intent.putExtra("index", index);
			intent.putExtra("address", address);
			intent.putExtra("date", date);
			intent.putExtra("body", body);
			startActivity(intent);
		}
	}
}

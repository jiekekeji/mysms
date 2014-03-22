package com.jack.mysms;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.jack.mssms.utils.MyAsyncQuery;
import com.jack.mssms.utils.Sms;
import com.jack.mssms.utils.Utils;

public class SearchActivity extends ListActivity implements OnQueryTextListener, OnCloseListener, OnItemClickListener {

	private static final String TAG = "SearchableActivity";
	private SearchAdapter mSearchAdapter;
	/**
	 * 查询字段
	 */
	private final String projection[]={
			"thread_id as _id",
		    "address",
		    "date",
		    "body"
	};
	
	private final int BODY_COLUMN_INDEX = 3;//会话内容列
	
	private final int DATE_COLUMN_INDEX = 2;
	
	private final int ADDRESS_COLUMN_INDEX = 1;
	
	private final int THREAD_ID_COLUMN_INDEX = 0;//会话id列
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		SearchView searchView=new SearchView(this);
		searchView.setIconified(false);//false 初始即是展开的
		searchView.setQueryHint("请输入关键字查询");//默认显示的提示文字
		searchView.onActionViewExpanded();//表示在内容为空时不显示取消的x按钮，内容不为空时显示
		
		searchView.setOnQueryTextListener(this);
		searchView.setOnCloseListener(this);
		initActionBar(searchView);
		
		mSearchAdapter = new SearchAdapter(this, null);
		
		mListView = getListView();
		mListView.setAdapter(mSearchAdapter);
		mListView.setOnItemClickListener(this);
		

	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		// 把当前被点击的item
		Cursor cursor = mSearchAdapter.getCursor();
		// 移动到当前被点的索引
		cursor.moveToPosition(position);
		
		// 会话的id
		int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
		String address = cursor.getString(ADDRESS_COLUMN_INDEX);
		
		Intent intent = new Intent(SearchActivity.this, ConversationDetailActivity.class);
		intent.putExtra("thread_id", thread_id);
		intent.putExtra("address", address);
		startActivity(intent);
		finish();
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		Log.i(TAG, newText);
		String selection="body like '%"+newText+"%'";
		MyAsyncQuery myAsyncQuery=new MyAsyncQuery(getContentResolver());
		
		myAsyncQuery.startQuery(0, 
				mSearchAdapter, 
				Sms.SMS_URI, 
				projection, 
				selection, 
				null, 
				"date desc");
		return false;
	}
	
    /**
     * 初始化actionBar,将searchView加入
     * @param searchView
     */
	private void initActionBar(SearchView searchView) {
		ActionBar actionBar = getActionBar();
		//返回箭头
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		actionBar.setDisplayShowCustomEnabled(true);//
		actionBar.setCustomView(searchView);//将searchView加入到actionBar
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onClose() {
		// TODO Auto-generated method stub
		mSearchAdapter.notifyDataSetChanged();
		return false;
	}
     
    /**
     * 
     * @author Administrator
     *
     */
	private class SearchAdapter extends CursorAdapter{
		private SearchHolderView mHolder;

		public SearchAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			Log.i(TAG, cursor.getColumnName(0));
			View view = View.inflate(SearchActivity.this,
					R.layout.item_searchactivity_listview, null);
			mHolder = new SearchHolderView();
			
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
			int id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);

			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
						
			String contactName = Utils.getContactName(getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				// 显示号码
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				
				Bitmap contactIcon = Utils.getContactIcon(getContentResolver(), address);
				if(contactIcon != null) {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
				} else {
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				}
			}
			
			String strDate = null;
			if(DateUtils.isToday(date)) {
				// 显示时间
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				// 显示日期
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			mHolder.tvDate.setText(strDate);
			
			mHolder.tvBody.setText(body);
		}
		
	}
	/**
	 * 
	 * @author Administrator
	 *
	 */
	public class SearchHolderView {
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}

		
	
}

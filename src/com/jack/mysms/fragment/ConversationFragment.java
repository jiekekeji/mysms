package com.jack.mysms.fragment;
import java.util.HashSet;
import java.util.Iterator;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jack.mssms.utils.MyAsyncQuery;
import com.jack.mssms.utils.Sms;
import com.jack.mssms.utils.Utils;
import com.jack.mysms.ConversationDetailActivity;
import com.jack.mysms.CreateSmsActitivty;
import com.jack.mysms.R;

/**
 * 会话界面fragment
 * 
 * @author Administrator
 * 
 */
public class ConversationFragment extends BaseFragment implements OnClickListener,OnItemClickListener, OnItemLongClickListener{

	private static final String TAG = "ConversationFragment";
	
	/**	
	 * 会话所需的列对应的索引
	 *
	 */
	private final int THREAD_ID_COLUMN_INDEX = 0;
	
	private final int BODY_COLUMN_INDEX = 1;
	
	private final int COUNT_COLUMN_INDEX = 2;
	
	private final int DATE_COLUMN_INDEX = 3;
	
	private final int ADDRESS_COLUMN_INDEX = 4;

	/**
	 * 查询表会话所需的列://com.android.provider.telephone 下的数据库 mmssms
	 */
	private String[] projection = { "sms.thread_id AS _id", //会话id
			"sms.body AS body",//会话的最后一条信息内容
			"groups.msg_count AS count", //每个会话有几条信息
			"sms.date AS date",//最后一条信息的时间
			"sms.address AS address" };//会话的联系人地址--手机号

	private ConversationAdapter mAdapter;
	
	private final int LIST_STATE = -1;
	
	private final int EDIT_STATE = -2;
	
	private int currentState = LIST_STATE;		// 当前默认的状态为列表状态
	
	private HashSet<Integer> mMultiDeleteSet;//用于保存在编辑状态下被选中的item的id

	private Button selectAllBtn;

	private Button cancalSelectBtn;

	private Button smsNewButton;

	private Button smsEditBtn;

	private Button cancalSmsEditBtn;

	private Button deleteSmsBtn;

	private ListView mListView;
	
	private ProgressDialog mProgressDialog;
	
	private boolean isStop = false;		// 删除会话是否停止

	@Override
	protected View initView(LayoutInflater inflater) {
		// TODO Auto-generated method stub
		mMultiDeleteSet=new HashSet<Integer>();
		currentState = LIST_STATE;
		
		View view = inflater.inflate(R.layout.fragment_conversation, null);
		
		selectAllBtn = (Button) view.findViewById(R.id.select_all);		
		cancalSelectBtn = (Button) view.findViewById(R.id.cancel_select);
		smsNewButton = (Button) view.findViewById(R.id.smsnew);
		smsEditBtn = (Button) view.findViewById(R.id.smsedit);
		cancalSmsEditBtn = (Button) view.findViewById(R.id.editcancel);
		deleteSmsBtn = (Button) view.findViewById(R.id.deletesms);
		
		selectAllBtn.setOnClickListener(this);
	    cancalSelectBtn.setOnClickListener(this);
	    smsNewButton.setOnClickListener(this);
	    smsEditBtn.setOnClickListener(this);
	    cancalSmsEditBtn.setOnClickListener(this);
	    deleteSmsBtn.setOnClickListener(this);
		
		mListView = (ListView) view.findViewById(R.id.listView);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		
		mAdapter = new ConversationAdapter(ct, null);
		mListView.setAdapter(mAdapter);
				
		return view;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		prepareData();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.smsedit://编辑会话
			currentState = EDIT_STATE;
			refreshState();
			break;
		case R.id.editcancel://取消编辑会话
			currentState = LIST_STATE;
			mMultiDeleteSet.clear();
			refreshState();
			break;
		case R.id.select_all://全选
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(-1);		// 复位到初始的位置
			
			while(cursor.moveToNext()) {
				mMultiDeleteSet.add(cursor.getInt(THREAD_ID_COLUMN_INDEX));
			}
			mAdapter.notifyDataSetChanged();	// 刷新数据
			refreshState();
			break;
		case R.id.cancel_select://取消选择
			mMultiDeleteSet.clear();
			mAdapter.notifyDataSetChanged();
			refreshState();
			break;
		case R.id.deletesms:
			showConfirmDeleteDialog();
			break;
		case R.id.smsnew:
			Intent intent=new Intent(ct, CreateSmsActitivty.class);
			startActivity(intent);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// 把当前被点击的item的会话id添加到集合中, 刷新checkbox
		Cursor cursor = mAdapter.getCursor();
		// 移动到当前被点的索引
		cursor.moveToPosition(position);
		
		// 会话的id
		int thread_id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
		String address = cursor.getString(ADDRESS_COLUMN_INDEX);
		
		if(currentState == EDIT_STATE) {
			
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_conversation_item);
			
			if(checkBox.isChecked()) {
				// 移除id
				mMultiDeleteSet.remove(thread_id);
			} else {
				mMultiDeleteSet.add(thread_id);
			}
			checkBox.setChecked(!checkBox.isChecked());
			
			// 每一次点击刷新一下按钮的状态
			refreshState();
		} else {
			Intent intent = new Intent(ct, ConversationDetailActivity.class);
			intent.putExtra("thread_id", thread_id);
			intent.putExtra("address", address);
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub

				Cursor cursor = (Cursor) mAdapter.getItem(position);
				String thread_id = cursor.getString(THREAD_ID_COLUMN_INDEX);
				
				String groupName = getGroupName(thread_id);
				if(!TextUtils.isEmpty(groupName)) {
					Toast.makeText(ct, "该会话已添加到:" + groupName + "群组", 0).show();
				} else {
					// 
					showSelectGroupDialog(thread_id);
				}
				return true;		
	}

	
	private void showSelectGroupDialog(final String thread_id) {
		
		AlertDialog.Builder builder = new Builder(ct);
		builder.setTitle("将该会话添加到：");
		// 
		Cursor cursor =ct.getContentResolver().query(Sms.GROUPS_QUERY_ALL_URI, null, null, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			final String[] groupNameArray = new String[cursor.getCount()];
			final String[] groupIDArray = new String[cursor.getCount()];
			
			while(cursor.moveToNext()) {
				groupIDArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("_id"));
				groupNameArray[cursor.getPosition()] = cursor.getString(cursor.getColumnIndex("group_name"));
			}
			
			builder.setItems(groupNameArray, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {

					addGroup(groupIDArray[which], thread_id);
				}
			});
			builder.show();
		}
	}
	/**
	 * 
	 * @param group_id
	 * @param thread_id
	 */
	private void addGroup(String group_id, String thread_id) {
		// �������ϵ�������һ�����
		ContentValues values = new ContentValues();
		values.put("group_id", group_id);
		values.put("thread_id", thread_id);
		Uri uri = ct.getContentResolver().insert(Sms.THREAD_GROUP_INSERT_URI, values);
		
		if(ContentUris.parseId(uri) != -1) {
			Toast.makeText(ct, "添加成功", 0).show();
		} else {
			Toast.makeText(ct, "添加失败", 0).show();
		}
	}
	
	/**
	 * 查询会话数据
	 */
	private void prepareData() {
		MyAsyncQuery myAsyncQuery = new MyAsyncQuery(getActivity()
				.getContentResolver());
		myAsyncQuery.startQuery(0, mAdapter, Sms.CONVERSATION_URI, projection,
				null, null, "date desc");

	}
	/**
	 * 根据状态的不同刷新view
	 */
	private void refreshState() {
		if(currentState == EDIT_STATE) {//处于编辑状态
			// 新建信息隐藏, 其他按钮显示, 每一个item都要显示一个checkBox
			smsEditBtn.setVisibility(View.GONE);
			smsNewButton.setVisibility(View.GONE);
			deleteSmsBtn.setVisibility(View.VISIBLE);
			selectAllBtn.setVisibility(View.VISIBLE);
			cancalSelectBtn.setVisibility(View.VISIBLE);
			cancalSmsEditBtn.setVisibility(View.VISIBLE);
			
			if(mMultiDeleteSet.size() == 0) {
				// 没有选中任何checkbox
				cancalSelectBtn.setEnabled(false);
				deleteSmsBtn.setEnabled(false);
			} else {//
				cancalSelectBtn.setEnabled(true);
				deleteSmsBtn.setEnabled(true);
			}
			
			// 全选
			selectAllBtn.setEnabled(mMultiDeleteSet.size() != mListView.getCount());
		} else {
			// 新建信息显示, 其他的隐藏
			smsEditBtn.setVisibility(View.VISIBLE);
			smsNewButton.setVisibility(View.VISIBLE);
			deleteSmsBtn.setVisibility(View.GONE);
			selectAllBtn.setVisibility(View.GONE);
			cancalSelectBtn.setVisibility(View.GONE);
			cancalSmsEditBtn.setVisibility(View.GONE);

		}
	}
	
	/**
	 * 确认删除对话框
	 */
	private void showConfirmDeleteDialog() {
		AlertDialog.Builder builder = new Builder(ct);
		builder.setIcon(android.R.drawable.ic_dialog_alert);		// 设置图标
		builder.setTitle("删除");
		builder.setMessage("确认删除选中的会话吗?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "确认删除");
				
				// 弹出进度对话框
				showDeleteProgressDialog();
				isStop = false;
				// 开启子线程, 真正删除短信, 每删除一条短信, 更新进度条
				new Thread(new DeleteRunnable()).start();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
	
	/**
	 * 弹出删除进度对话框
	 */
	@SuppressWarnings("deprecation")
	private void showDeleteProgressDialog() {
		mProgressDialog = new ProgressDialog(ct);
		// 设置最大值
		mProgressDialog.setMax(mMultiDeleteSet.size());
		// 设置进度条的样式
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		
		mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "终止删除");
				isStop = true;
			}
		});
		
		mProgressDialog.show();
		
		//对进度条关闭的监听
		mProgressDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				currentState = LIST_STATE;
				refreshState();
			}
		});
		
		
	}
	
	/**
	 * @author andong
	 * 删除会话的任务
	 */
	private class DeleteRunnable implements Runnable {

		@Override
		public void run() {
			// 删除会话
			
			Iterator<Integer> iterator = mMultiDeleteSet.iterator();
			
			int thread_id;//根据会话id删除sms
			String where;
			String[] selectionArgs;
			while(iterator.hasNext()) {
				
				if(isStop) {
					break;
				}
				
				thread_id = iterator.next();
				where = "thread_id = ?";
				selectionArgs = new String[]{String.valueOf(thread_id)};
				ct.getContentResolver().delete(Sms.SMS_URI, where, selectionArgs);
				
				SystemClock.sleep(4000);
				
				// 更新进度条,ProgressDialog可以再子线程改变状态
				mProgressDialog.incrementProgressBy(1);
			}
			
			mMultiDeleteSet.clear();
			mProgressDialog.dismiss();
		}
		
	}
	
	/**
	 * 通过会话的id查询群组id
	 * @param thread_id
	 * @return String getGroupName
	 */
	private String getGroupName(String thread_id) {
		//
		String selection = "thread_id = " + thread_id;
		Cursor cursor = ct.getContentResolver().query(Sms.THREAD_GROUP_QUERY_ALL_URI, new String[]{"group_id"}, 
				selection, null, null);
		if(cursor != null && cursor.moveToFirst()) {
			String groupId = cursor.getString(0);
			cursor.close();
			//
			if(!TextUtils.isEmpty(groupId)) {
				//
				selection = "_id = " + groupId;
				cursor = ct.getContentResolver().query(Sms.GROUPS_QUERY_ALL_URI, new String[]{"group_name"}, 
						selection, null, null);
				if(cursor != null && cursor.moveToFirst()) {
					String groupName = cursor.getString(0);
					return groupName;
				}
			}
		}
		return null;
	}
	
	/**
	 * 数据适配
	 * 
	 * @author Administrator
	 * 
	 */
	private class ConversationAdapter extends CursorAdapter {

		private ConversationHolderView mHolder;

		public ConversationAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//
			mHolder = (ConversationHolderView) view.getTag();
			
			int id = cursor.getInt(THREAD_ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			int count = cursor.getInt(COUNT_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			// 判断当前的状态是否是编辑
			if(currentState == EDIT_STATE) {
				// 显示checkbox
				mHolder.checkBox.setVisibility(View.VISIBLE);
				Log.i(TAG,currentState+"当前状态");
				// 当前的会话id是否存在与deleteSet集合中
				mHolder.checkBox.setChecked(mMultiDeleteSet.contains(id));
			} else {
				// 隐藏checkbox
				mHolder.checkBox.setVisibility(View.GONE);
				Log.i(TAG,currentState+"当前状态");
			}
			
			String contactName = Utils.getContactName(ct.getContentResolver(), address);
			if(TextUtils.isEmpty(contactName)) {
				// 显示号码
				mHolder.tvName.setText(address + "(" + count + ")");
				mHolder.ivIcon.setBackgroundResource(R.drawable.ic_unknow_contact_picture);
			} else {
				// 显示名称
				mHolder.tvName.setText(contactName + "(" + count + ")");
				
				Bitmap contactIcon = Utils.getContactIcon(ct.getContentResolver(), address);
				if(contactIcon != null) {
					mHolder.ivIcon.setBackgroundDrawable(new BitmapDrawable(contactIcon));
				} else {
					mHolder.ivIcon.setBackgroundResource(R.drawable.ic_contact_picture);
				}
			}
			
			String strDate = null;
			/**
			 * DateUtils.isToday(date):return true if the supplied when is today else false
			 */
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

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
			//
			View view = View.inflate(getActivity(),
					R.layout.item_fragment_conversation_listview, null);
			mHolder = new ConversationHolderView();
			
			mHolder.checkBox = (CheckBox) view
					.findViewById(R.id.cb_conversation_item);
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
	}

	//
	public class ConversationHolderView {
		public CheckBox checkBox;
		public ImageView ivIcon;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
	}

}


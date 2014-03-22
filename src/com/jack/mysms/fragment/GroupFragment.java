package com.jack.mysms.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jack.mssms.utils.MyAsyncQuery;
import com.jack.mssms.utils.Sms;
import com.jack.mysms.GroupDetailActivity;
import com.jack.mysms.R;

/**
 
 */
public class GroupFragment extends BaseFragment implements OnClickListener, OnItemClickListener, OnItemLongClickListener{

	private static final String TAG = "GroupFragment";
	
	private GroupAdapter mAdapter;
	@Override
	protected View initView(LayoutInflater inflater) {
		// TODO Auto-generated method stub
		View view=inflater.inflate(R.layout.fragment_group, null);
		Button btnCreateGroup=(Button) view.findViewById(R.id.btn_create_group);
		btnCreateGroup.setOnClickListener(this);
		
		ListView mListView=(ListView) view.findViewById(R.id.listView_group);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
			
		mAdapter=new GroupAdapter(ct, null);
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
		case R.id.btn_create_group:
			showCreateGroupDialog();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		/**
		 * 1、 通过点击的item得到对应的group_id
		 * 2、通过group_id在关联表中得到thread_id
		 * 3、将thread_id传递到GroupDetailActivity展示
		 */	
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String group_id = cursor.getString(cursor.getColumnIndex("_id"));
		String group_name = cursor.getString(cursor.getColumnIndex("group_name"));
     	// 
		Intent intent = new Intent(ct, GroupDetailActivity.class);
		intent.putExtra("group_name", group_name);
		startActivity(intent);
		
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		String group_id = cursor.getString(cursor.getColumnIndex("_id"));
		
		showOperatorDialog(group_id);
		return false;
	}
	/**
	 * 对话框
	 * 选择对群组的操作
	 * @param group_id
	 */
	private void showOperatorDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(ct);
		builder.setItems(new String[]{"修改", "删除"}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0) {
					// 修改对话框
					showUpdateGroupDialog(group_id);
				} else {
                    //删除对话框
					showDeleteGroupDialog(group_id);
				}
			}

		});
		builder.show();
	}
	
	private void prepareData() {
		MyAsyncQuery asyncQuery = new MyAsyncQuery(ct.getContentResolver());
		asyncQuery.startQuery(0, mAdapter, Sms.GROUPS_QUERY_ALL_URI, null, null, null, null);
	}
	/**
	 * 删除对话框
	 * @param group_id
	 */
	private void showDeleteGroupDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(ct);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setTitle("删除");
		builder.setMessage("删除该群组即删除所有与该群组关联的会话?");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				deleteGroup(group_id);
			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}
	
	/**
	 * 对话框
	 * 通过group_id修改群组
	 * @param group_id
	 */
	private void showUpdateGroupDialog(final String group_id) {
		AlertDialog.Builder builder = new Builder(ct);
		builder.setTitle("修改群组");
		final AlertDialog updateGroupDialog = builder.create();
		
		View view = View.inflate(ct, R.layout.dialog_create_group, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_create_group_name);
		Button btnCreateGroup = (Button) view.findViewById(R.id.btn_create_group);
		btnCreateGroup.setText("确认修改");
		btnCreateGroup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 
				String group_name = etName.getText().toString();
				if(!TextUtils.isEmpty(group_name)) {
					updateGroup(group_id, group_name);
					updateGroupDialog.dismiss();
				}
			}
		});
		updateGroupDialog.setView(view, 0, 0, 0, 0);
		updateGroupDialog.show();
		
		LayoutParams lp = updateGroupDialog.getWindow().getAttributes();
		lp.width = (int) (getActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		updateGroupDialog.getWindow().setAttributes(lp);
	}
	
	/**
	 * 新建群组对话框
	 */
	private void showCreateGroupDialog() {
		AlertDialog.Builder builder = new Builder(ct);
		builder.setTitle("新建群组");
		final AlertDialog dialog = builder.create();
		
		View view = View.inflate(ct, R.layout.dialog_create_group, null);
		final EditText etName = (EditText) view.findViewById(R.id.et_create_group_name);
		view.findViewById(R.id.btn_create_group).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String groupName = etName.getText().toString();
				if(!TextUtils.isEmpty(groupName)) {
					createGroup(groupName);
					dialog.dismiss();
				}
			}

		});
		
		dialog.setView(view, 0, 0, 0, 0);
		dialog.show();
		
		// 对话框密度
		LayoutParams lp = dialog.getWindow().getAttributes();

		// 整个窗体密度	
		lp.width = (int)(getActivity().getWindowManager().getDefaultDisplay().getWidth() * 0.7);
		
		dialog.getWindow().setAttributes(lp);
		
	}
	/**
	 * 通过group_id修改group_name
	 * @param group_id
	 * @param group_name
	 */
	private void updateGroup(String group_id, String group_name) {
		ContentValues values = new ContentValues();
		values.put("group_name", group_name);
		String where = "_id = " + group_id;
		int count = ct.getContentResolver().update(Sms.GROUPS_UPDATE_URI, values, where, null);
		if(count > 0) {
			Toast.makeText(ct, "修改成功", 0).show();
		} else {
			Toast.makeText(ct, "修改失败", 0).show();
		}
	}
	/**
	 * 通过group_id删除group
	 * @param group_id
	 */
	private void deleteGroup(String group_id) {
		
		Uri uri = ContentUris.withAppendedId(Sms.GROUPS_SINGLE_DELETE_URI, Long.valueOf(group_id));
		
		int count = ct.getContentResolver().delete(uri, null, null);
		if(count > 0) {
			Toast.makeText(ct, "删除成功", 0).show();
		} else {
			Toast.makeText(ct, "删除失败", 0).show();
		}
	}
	
	/**
	 * 新建群组
	 * @param groupName
	 */
	private void createGroup(String groupName) {
		ContentValues values = new ContentValues();
		values.put("group_name", groupName);
		Uri uri = ct.getContentResolver().insert(Sms.GROUPS_INSERT_URI, values);
		if(ContentUris.parseId(uri) >= 0) {
			Log.i(TAG, "新建群组成功");
			Toast.makeText(ct, "新建成功", 0).show();
		}
	}
	
	class GroupAdapter extends CursorAdapter {

		public GroupAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.item_groups, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tvName = (TextView) view.findViewById(R.id.tv_group_item_name);
			
			tvName.setText(cursor.getString(cursor.getColumnIndex("group_name")));
		}
		
	}	
}

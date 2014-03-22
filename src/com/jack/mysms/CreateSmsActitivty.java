package com.jack.mysms;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.jack.mssms.utils.Utils;

public class CreateSmsActitivty extends Activity implements OnClickListener{

	protected static final String TAG = "CreateSmsActitivty";
	private AutoCompleteTextView autoTextView;
	private EditText etContent;
	
	private final int ADDRESS_COLUMN_INDEX = 1;
	private final int NAME_COLUMN_INDEX = 2;
	private ContactAdapter contactAdapter;
	
	private final String[] projection = {
			"_id", 
			"data1", 
			"display_name"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_message);
		initActionBar();
		
		initView();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ib_new_message_select_contact://选择联系人
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setData(Contacts.CONTENT_URI);
			startActivityForResult(intent, 200);		
			break;
		case R.id.btn_new_message_send://发送
		    
			String address = autoTextView.getText().toString();
			String content = etContent.getText().toString();
			
			if(TextUtils.isEmpty(address)) {
				Toast.makeText(this, "请输入联系人", 0).show();
				break;
			}
			
			if (TextUtils.isEmpty(content)) {
				Toast.makeText(this, "请输入内容", 0).show();
				break;
			}
			
			Utils.sendMessage(this, address, content);
			finish();
		    break;

		default:
			break;
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (200==requestCode&&Activity.RESULT_OK==resultCode) {
			Uri uri=data.getData();
			Log.i(TAG, uri+"");
			
			long contactID = Utils.getContactID(getContentResolver(), uri);
			if(contactID != -1) {
				String address = Utils.getContactAddress(getContentResolver(), contactID);
				if(!TextUtils.isEmpty(address)) {
					autoTextView.setText(address);
				}
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 
	 */
	private void initView() {
		autoTextView = (AutoCompleteTextView) findViewById(R.id.actv_new_message_number);
		etContent = (EditText) findViewById(R.id.et_new_message_content);
		
		findViewById(R.id.ib_new_message_select_contact).setOnClickListener(this);
		findViewById(R.id.btn_new_message_send).setOnClickListener(this);
		
		contactAdapter = new ContactAdapter(this, null);
		autoTextView.setAdapter(contactAdapter);
		
		contactAdapter.setFilterQueryProvider(new FilterQueryProvider() {
			
			@Override
			public Cursor runQuery(CharSequence constraint) {
				// constraint  输入的关键字
				Log.i(TAG, "xxxxxxxx" + constraint);
				String selection = "data1 like ?";
				String selectionArgs[] = {"%" + constraint + "%"};
				Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, 
						projection, selection, selectionArgs, null);
				return cursor;
			}
			
			
		});
		
		
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
	 */
	private void initActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = getActionBar();
		//返回箭头
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setTitle("新建信息");
		
	}
	
	private class ContactAdapter extends CursorAdapter {

		public ContactAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return View.inflate(context, R.layout.item_contact_auto, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			String name = cursor.getString(NAME_COLUMN_INDEX);
			
			TextView tvName = (TextView) view.findViewById(R.id.tv_contact_item_name);
			TextView tvAddress = (TextView) view.findViewById(R.id.tv_contact_item_address);
			
			tvName.setText(name);
			tvAddress.setText(address);
		}
		/**
		 * 将结果显示到textView
		 */
		@Override
		public CharSequence convertToString(Cursor cursor) {
			// TODO Auto-generated method stub
			return cursor.getString(ADDRESS_COLUMN_INDEX);
		}
		
	}

}

package com.jack.mysms;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.jack.mssms.utils.JsonParser;
import com.jack.mssms.utils.MyAsyncQuery;
import com.jack.mssms.utils.MyAsyncQuery.OnQueryNotifyCompleteListener;
import com.jack.mssms.utils.Sms;
import com.jack.mssms.utils.Utils;
import com.jack.mysms.view.MyTextView;

public class ConversationDetailActivity extends Activity implements OnQueryNotifyCompleteListener,OnClickListener{

	private static final String TAG = "ConversationDetailActivity";
	private int thread_id;
	private String address;
	private ConversationDetailAdapter mAdapter;
	
	private final String[] projection = {
			"_id",
			"body",
			"date",
			"type"
	};

	private final int ID_COLUMN_INDEX = 0;
	private final int BODY_COLUMN_INDEX = 1;
	private final int DATE_COLUMN_INDEX = 2;
	private final int TYPE_COLUMN_INDEX = 3;
	private ListView mListView;
	private ImageButton btnVoice;
	private Button btnSend;
	private EditText eTextContent;
	
	//语音识别对话框
	private RecognizerDialog recognizerDialog;
	
	/**
	 * 设置item项的子控件能够获得焦点（默认为false，即默认item项的子空间是不能获得焦点的）
		mListView.setItemsCanFocus(true);
		
	 * listView的item中的textView是否被长按listView的item中的textView是否被长按
	 * 当item.setonItemLongClick  item中的控件textView也设置了 setOnLongClick
	 * 
	 * 执行顺序：先执行 item中的控件textView也设置了 setOnLongClick
	 * 在执行  item的 item.setonItemLongClick。
	 */
	private boolean tvIsonLongClick=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conversation_detail);
		Intent intent = getIntent();
		thread_id = intent.getIntExtra("thread_id", -1);
		address = intent.getStringExtra("address");
		
		initActionBar();
		
		initView();
		
		prepareData();
		
		initRecognizerDialog();
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
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_conversation_detail_voice://语音识别
			recognizerDialog.show();
			break;
		case R.id.btn_conversation_detail_send://发送短信
			//得到短信内容
			//对短信内容做判断和处理
			//发送
			String content=eTextContent.getText().toString();
			if (TextUtils.isEmpty(content)) {
				Toast.makeText(this, "请输入短信内容",0).show();
				return;
			}
			Utils.sendMessage(this, address, content);
			eTextContent.setText("");
			break;
	
		default:
			break;
		}
	}
	/**
	 * 初始化语音对话框，选择默认值
	 */
	private void initRecognizerDialog() {
		/**
		 * 初始化识别Dialog
		 */
		recognizerDialog = new RecognizerDialog(this, initListener);
		/**
		 * 设置识别监听器，用于回调识别状态。
		 */
		recognizerDialog.setListener(recognizerDialogListener);
		
	}
	
	/**
	 * 初始化完成回调接口:errorCode - 错误码，0表示成功
	*/
	 InitListener initListener=new InitListener() {
			
			@Override
			public void onInit(int arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, String.valueOf(arg0));
				Toast.makeText(ConversationDetailActivity.this, "语音对话框初始化失败",Toast.LENGTH_SHORT).show();
			}
	};

	/**
	 * 语音对话框的语音识别监听器
	 */
	RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {

		@Override
		public void onResult(RecognizerResult result, boolean arg1) {
			// TODO Auto-generated method stub
			Log.i(TAG, result.getResultString());
			String string = JsonParser.parseIatResult(result.getResultString());
			eTextContent.setText(string);
		}

		@Override
		public void onError(SpeechError arg0) {
			// TODO Auto-generated method stub
			Log.i(TAG, "错误码:" +String.valueOf(arg0));
			Toast.makeText(ConversationDetailActivity.this, String.valueOf(arg0), Toast.LENGTH_SHORT).show();
			
		}
	};
	
	/**
	 * 初始化view
	 */
	private void initView() {
		btnVoice = (ImageButton) findViewById(R.id.btn_conversation_detail_voice);
		btnSend = (Button) findViewById(R.id.btn_conversation_detail_send);
		eTextContent = (EditText) findViewById(R.id.et_conversation_detail_content);
		
		btnSend.setOnClickListener(this);
		btnVoice.setOnClickListener(this);
			
		mListView = (ListView) findViewById(R.id.lv_conversation_detail_sms);
		
		mAdapter = new ConversationDetailAdapter(this, null);
		
		mListView.setAdapter(mAdapter);
		//设置item项的子控件能够获得焦点（默认为false，即默认item项的子空间是不能获得焦点的）
		mListView.setItemsCanFocus(true);

	}
	
	/**
	 * 获取数据
	 */
	private void prepareData() {
		MyAsyncQuery asyncQuery = new MyAsyncQuery(getContentResolver());
		asyncQuery.setOnQueryNotifyCompleteListener(this);
		
		String selection = "thread_id = ?";
		String[] selectionArgs = {
				String.valueOf(thread_id)
		};
		asyncQuery.startQuery(0, mAdapter, Sms.SMS_URI, projection, selection, selectionArgs, "date");
	}

    /**
     * 设置actionBar
     * 隐藏Label标签：actionBar.setDisplayShowTitleEnabled(false);
                      隐藏logo和icon：actionBar.setDisplayShowHomeEnabled(false);
                     置标题，一个主标题，一个子标题
       actionBar.setSubtitle(“Inbox”); 
       actionBar.setTitle(“Label:important”);
     */
	private void initActionBar() {
		
		ActionBar actionBar = getActionBar();
		//返回箭头
		actionBar.setDisplayHomeAsUpEnabled(true);
			
        String contactName = Utils.getContactName(getContentResolver(), address);
		
		if(TextUtils.isEmpty(contactName)) {
			actionBar.setTitle(address);
		} else {
			actionBar.setTitle(contactName);
		}
	}
	
	class ConversationDetailAdapter extends CursorAdapter {

		ConversationDetailHolderView mHolder;
		public ConversationDetailAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View newView(Context context, final Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			mHolder = new ConversationDetailHolderView();
			View view = View.inflate(context, R.layout.item_conversation_detail, null);
			mHolder.receiveView = view.findViewById(R.id.tl_conversation_detail_item_receive);
			mHolder.tvReceiveBody = (MyTextView) view.findViewById(R.id.tv_conversation_detail_item_receive_body);
			
			mHolder.tvReceiveDate = (TextView) view.findViewById(R.id.tv_conversation_detail_item_receive_date);
			mHolder.tvReceiveBody.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					showOperatorDialog(cursor.getPosition());
					return false;
				}
			});
			mHolder.sendView = view.findViewById(R.id.tl_conversation_detail_item_send);
			mHolder.tvSendBody = (MyTextView) view.findViewById(R.id.tv_conversation_detail_item_send_body);
			
			mHolder.tvSendDate = (TextView) view.findViewById(R.id.tv_conversation_detail_item_send_date);
			
			mHolder.tvSendBody.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					// TODO Auto-generated method stub
					showOperatorDialog(cursor.getPosition());
					return false;
				}
			});
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
            mHolder = (ConversationDetailHolderView) view.getTag();
			
			String body = cursor.getString(BODY_COLUMN_INDEX);
			@SuppressWarnings("unused")
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			int type = cursor.getInt(TYPE_COLUMN_INDEX);
			
			// 处理时间
			String strDate = null;
			if(DateUtils.isToday(date)) {
				strDate = DateFormat.getTimeFormat(context).format(date);
			} else {
				strDate = DateFormat.getDateFormat(context).format(date);
			}
			
			if(type == Sms.RECEVIE_TYPE) {
				// 显示的左边的起泡 receive
				mHolder.receiveView.setVisibility(View.VISIBLE);
				mHolder.sendView.setVisibility(View.GONE);
				
				mHolder.tvReceiveBody.setText(body);
				mHolder.tvReceiveDate.setText(strDate);
			} else {
				// 显示的右边的起泡 send
				mHolder.receiveView.setVisibility(View.GONE);
				mHolder.sendView.setVisibility(View.VISIBLE);
				
				mHolder.tvSendBody.setText(body);
				mHolder.tvSendDate.setText(strDate);
			}
		}
		
		@Override
		protected void onContentChanged() {
			// TODO Auto-generated method stub
			super.onContentChanged();//!
			mListView.setSelection(mListView.getCount());//默认显示在底部
		}
		
	}
	
	public class ConversationDetailHolderView {
		public View receiveView;
		public MyTextView tvReceiveBody;
		public TextView tvReceiveDate;
		
		public View sendView;
		public MyTextView tvSendBody;
		public TextView tvSendDate;
	}
    /**
     * adpter更新之前的回调
     */
	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	/**
     * adpter更新之后的回调
     */
	@Override
	public void onPostNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		mListView.setSelection(mListView.getCount());//默认显示在底部
	}
	
	/**
	 * 对话框
	 * 对单条短信的操作
	 * 
	 */
	@SuppressLint("NewApi")
	private void showOperatorDialog(int position) {
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		final String _id = cursor.getString(cursor.getColumnIndex("_id"));
		AlertDialog.Builder builder = new Builder(this);
		builder.setItems(new String[]{"重发", "删除"}, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == 0) {
					// 重发
                    Log.i(TAG, "短信id为"+_id);
				} else {
                    //删除
//					showDeleteGroupDialog(group_id);
				}
			}
		});
		
		AlertDialog dialog=builder.create();
		dialog.show();
		
		dialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				Log.i(TAG, "onDismiss");
				tvIsonLongClick=false;
			}
		});
	}

}

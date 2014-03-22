package com.jack.mysms.fragment;

import java.util.HashMap;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jack.mssms.utils.MyAsyncQuery;
import com.jack.mssms.utils.MyAsyncQuery.OnQueryNotifyCompleteListener;
import com.jack.mssms.utils.Utils;
import com.jack.mysms.FolderDetailActivity;
import com.jack.mysms.R;

/**
 * 文件夹fragment
 * 
 */
public class FolderFragment extends BaseFragment implements
		OnQueryNotifyCompleteListener ,OnItemClickListener{

	private int[] imageIDs;
	private String[] typeArrays;
	private HashMap<Integer, Integer> countMap;
	private FolderAdapter mAdapter;

	@Override
	protected View initView(LayoutInflater inflater) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_folder, null);

		imageIDs = new int[] { R.drawable.a_f_inbox, 
				R.drawable.a_f_outbox,
				R.drawable.a_f_sent,
				R.drawable.a_f_draft };

		typeArrays = new String[] { "收件箱", "发件箱", "已发送", "草稿箱" };

		ListView mListView = (ListView) view.findViewById(R.id.listView_folder);
		mAdapter = new FolderAdapter();
		mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
		return view;
	}

	@Override
	protected void initData(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		countMap = new HashMap<Integer, Integer>();

		MyAsyncQuery asyncQuery = new MyAsyncQuery(ct.getContentResolver());

		asyncQuery.setOnQueryNotifyCompleteListener(this);

		Uri uri;
		for (int i = 0; i < 4; i++) {
			countMap.put(i, 0);

			uri = Utils.getUriFromIndex(i);

			asyncQuery.startQuery(i, null, uri, new String[] { "count(*)" },
					null, null, null);
		}
	}

	class FolderAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return imageIDs.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView == null) {
				view = View.inflate(ct, R.layout.item_folder, null);
			} else {
				view = convertView;
			}

			ImageView ivIcon = (ImageView) view
					.findViewById(R.id.iv_folder_item_icon);
			TextView tvType = (TextView) view
					.findViewById(R.id.tv_folder_item_type);
			TextView tvCount = (TextView) view
					.findViewById(R.id.tv_folder_item_count);

			ivIcon.setImageResource(imageIDs[position]);
			tvType.setText(typeArrays[position]);
			tvCount.setText(String.valueOf(countMap.get(position)));
			return view;
		}

	}

	@Override
	public void onPreNotify(int token, Object cookie, Cursor cursor) {
		// TODO Auto-generated method stub
		if (cursor != null && cursor.moveToFirst()) {
			countMap.put(token, cursor.getInt(0));
			mAdapter.notifyDataSetChanged();
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
		Intent intent = new Intent(ct, FolderDetailActivity.class);
		intent.putExtra("index", position);
		startActivity(intent);
	}

}

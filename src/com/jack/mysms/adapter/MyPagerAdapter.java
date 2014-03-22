package com.jack.mysms.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jack.mysms.fragment.ConversationFragment;
import com.jack.mysms.fragment.FolderFragment;
import com.jack.mysms.fragment.GroupFragment;

public class MyPagerAdapter extends FragmentPagerAdapter {
	
	/**
	 * 会话界面的Fragment
	 */
	private ConversationFragment conversationFragment;

	/**
	 * 文件夹界面的Fragment
	 */
	private FolderFragment folderFragment;

	/**
	 * 群组界面的Fragment
	 */
	private GroupFragment groupFragment;

	
	public MyPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	private final String[] titles = { "会话", "文件夹", "群组" };

	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public int getCount() {
		return titles.length;
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			if (conversationFragment == null) {
				conversationFragment = new ConversationFragment();
			}
			return conversationFragment;
		case 1:
			if (folderFragment == null) {
				folderFragment = new FolderFragment();
			}
			return folderFragment;
		case 2:
			if (groupFragment == null) {
				groupFragment = new GroupFragment();
			}
			return groupFragment;
		default:
			return null;
		}
	}

}

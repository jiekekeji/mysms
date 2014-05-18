package com.jack.mysms;

import java.lang.reflect.Method;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.jack.mysms.adapter.MyPagerAdapter;
import com.jack.mysms.view.PagerSlidingTabStrip;
/**
 * 
 * @author Administrator
 *
 */
public class MainActivity extends FragmentActivity{

	private static final String TAG = "MainActivity";

	/**
	 * PagerSlidingTabStrip的实例
	 */
	private PagerSlidingTabStrip tabs;

	/**
	 * 当前屏幕的密度
	 */
	private DisplayMetrics dm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dm = getResources().getDisplayMetrics();
		
		initView();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {    
	    getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.action_search:
			Intent searchIntent=new Intent(this,SearchActivity.class);
			startActivity(searchIntent);
			return true;
			
        case R.id.action_settings:
        	Intent settingsIntent=new Intent(this,SettingsActivity.class);
			startActivity(settingsIntent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * 让隐藏在overflow当中的Action按钮的图标显示出来
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}
	
    /**
     * 给viewpager和tab设置初始值
     */
	private void initView() {
		// TODO Auto-generated method stub
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
		tabs.setViewPager(pager);
		setTabsValue();
	}

	/**
	 * 对PagerSlidingTabStrip的各项属性进行赋值。
	 * 
	 * pstsIndicatorColor Color of the sliding indicator
	 * 
		pstsUnderlineColor Color of the full-width line on the bottom of the view
		
		pstsDividerColor Color of the dividers between tabs
		
		pstsIndicatorHeightHeight of the sliding indicator
		
		pstsUnderlineHeight Height of the full-width line on the bottom of the view
		
		pstsDividerPadding Top and bottom padding of the dividers
		
		pstsTabPaddingLeftRight Left and right padding of each tab
		
		pstsScrollOffset Scroll offset of the selected tab
		
		pstsTabBackground Background drawable of each tab, should be a StateListDrawable
		
		pstsShouldExpand If set to true, each tab is given the same weight, default false
		
		pstsTextAllCaps If true, all tab titles will be upper case, default true
	 */
	private void setTabsValue() {
		// 设置Tab是自动填充满屏幕的
		tabs.setShouldExpand(true);
		// 设置Tab的分割线是透明的
		tabs.setDividerColor(Color.TRANSPARENT);
		// 设置Tab底部线的高度
		tabs.setUnderlineHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 1, dm));
		// 设置Tab Indicator的高度
		tabs.setIndicatorHeight((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, dm));
		// 设置Tab标题文字的大小
		tabs.setTextSize((int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, 16, dm));
		// 设置Tab Indicator的颜色
		tabs.setIndicatorColor(Color.parseColor("#45c01a"));
		// 设置选中Tab文字的颜色 
		tabs.setSelectedTextColor(Color.parseColor("#45c01a"));
		// 取消点击Tab时的背景色
		tabs.setTabBackground(0);
	}



}
package com.jack.mssms.utils;

import android.net.Uri;

public class Sms {
 
	/**
	 * 短信会话的Uri
	 */
	public static final Uri CONVERSATION_URI = Uri.parse("content://sms/conversations");
	
	/**
	 * 短信的Uri
	 */
	public static final Uri SMS_URI = Uri.parse("content://sms");
	
	/**
	 * 短信类型
	 */
	public static final int RECEVIE_TYPE = 1;	//  接收的
	public static final int SEND_TYPE = 2;		//  发送的
	
	/**
	 * 收件箱
	 */
	public static final Uri INBOX_URI = Uri.parse("content://sms/inbox");
	
	/**
	 * 发件箱
	 */
	public static final Uri OUTBOX_URI = Uri.parse("content://sms/outbox");
	
	/**
	 * 已发送
	 */
	public static final Uri SENT_URI = Uri.parse("content://sms/sent");
	
	/**
	 * 草稿
	 */
	public static final Uri DRAFT_URI = Uri.parse("content://sms/draft");
	
	/**
	 * 群组
	 */
	public static final Uri GROUPS_INSERT_URI=Uri.parse("content://com.jack.mysms.provider.GroupContentProvider/groups/insert");
	/**
	 * 查询所有
	 */
	public static final Uri GROUPS_QUERY_ALL_URI=Uri.parse("content://com.jack.mysms.provider.GroupContentProvider/groups");
	/**
	 * 查询thread_group表的所有数据
	 */
	public static final Uri THREAD_GROUP_QUERY_ALL_URI=Uri.parse("content://com.jack.mysms.provider.GroupContentProvider/thread_group");
	/**
	 * 往thread_group添加数据
	 */
	public static final Uri THREAD_GROUP_INSERT_URI=Uri.parse("content://com.jack.mysms.provider.GroupContentProvider/thread_group/insert");
	/**
	 * 修改群组
	 */
	public static final Uri GROUPS_UPDATE_URI=Uri.parse("content://com.jack.mysms.provider.GroupContentProvider/groups/update");
	/**
	 * 删除群组
	 */
	public static final Uri GROUPS_SINGLE_DELETE_URI=Uri.parse("content://com.jack.mysms.provider.GroupContentProvider/groups/delete");
	
	
	
	
}

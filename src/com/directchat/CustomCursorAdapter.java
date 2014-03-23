package com.directchat;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomCursorAdapter extends CursorAdapter {

	public CustomCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View retView = inflater.inflate(R.layout.row_item__main_activity_user_list_lv, parent,false);
		
		return retView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView userStatusCircle = (ImageView) view.findViewById(R.id.userStatus_lvRow_mainActivity);
		if(cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_IS_ONLINE))==1){ //userOnline
			userStatusCircle.setImageResource(R.drawable.user_online_circle);
		}else{
			userStatusCircle.setImageResource(R.drawable.user_offline_circle);
		}
	}

}

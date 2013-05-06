package com.nec.xplayer.ui;


import com.nec.xplayer.R;
import com.nec.xplayer.util.FileUtil;
import com.nec.xplayer.util.PlayerProvider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

@SuppressLint("NewApi")
public class OnlineFragment extends Fragment implements LoaderCallbacks<Cursor>{
	private ListView mListView;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	
	private ImageButton mAddButton; 	
	private static final String[] STORE_IMAGES={
		PlayerProvider.NAME,
		PlayerProvider.URL,
		PlayerProvider.ID
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		final View mView = inflater.inflate(R.layout.online_listview, container,
				false);
		mListView = (ListView)mView.findViewById(R.id.listView);
		mAddButton = (ImageButton)mView.findViewById(R.id.add_video);
		mSimpleCursorAdapter = new SimpleCursorAdapter(
				mView.getContext(),
				R.layout.online_item,
				null,
				STORE_IMAGES,
				new int[]{R.id.online_Name,R.id.online_url},
				0
				);
		mListView.setAdapter(mSimpleCursorAdapter);
		getLoaderManager().initLoader(0, null, this);

		
		mAddButton.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根

				LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
				View DialogView = layoutInflater.inflate(R.layout.alert_dialog_text_entry, null);
				
				final EditText name = (EditText)DialogView.findViewById(R.id.nameEditText);
				final EditText url = (EditText)DialogView.findViewById(R.id.urlEditText);

				Dialog alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle("添加地址")
				.setView(DialogView)
				.setPositiveButton("取消", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自动生成的方法存根
					}
					
				}).setNegativeButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自动生成的方法存根
						String mName = name.getText().toString();
						String mUrl = url.getText().toString();
						
						ContentResolver resolver = getActivity().getContentResolver();
						ContentValues values = new ContentValues();
						values.put("name", mName);
						values.put("url",mUrl);
						resolver.insert(PlayerProvider.CONTENT_URI, values);
					}
				}).create();
				
				Window window = alertDialog.getWindow();
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.alpha=0.6f;
				window.setAttributes(lp);

				alertDialog.show();
			
				
			}
		});
		
		mListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){

			@SuppressLint("NewApi")
			@Override
			public void onCreateContextMenu(ContextMenu menu, View arg1,
					ContextMenuInfo arg2) {
				// TODO Auto-generated method stub
				
				
				menu.setHeaderTitle("操作");
				menu.add(0,1,0,"播放");
				menu.add(0,2,0,"删除");
				menu.add(0,3,0,"编辑");
				menu.add(0,4,0,"取消");
			}
			
		});
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				
				// TODO 自动生成的方法存根
				if(!FileUtil.isNetworkConnected(getActivity())){
					Toast.makeText(getActivity(), "Network impassability", Toast.LENGTH_SHORT).show();
				}
				final TextView content = (TextView)view.findViewById(R.id.online_url);
				Log.d("path",content.getText().toString());
				Intent intent = new Intent(mView.getContext(),VideoViewPlayer.class);
				Bundle bundle = new Bundle();
				Log.d("path",content.getText().toString());
				bundle.putCharSequence("url", content.getText().toString());
				intent.putExtras(bundle);
				startActivity(intent);

			}
		});

		return mView;
	}
    @SuppressLint("NewApi")
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem actionItem = menu.add("Action Button");
    	actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    	actionItem.setIcon(android.R.drawable.ic_menu_report_image);
    	
    	return true;
    }

	
	public boolean onContextItemSelected(MenuItem item) {
		// TODO 自动生成的方法存根
		final AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		ContentResolver resolver = getActivity().getContentResolver();
		Cursor cursor = resolver.query(PlayerProvider.CONTENT_URI, STORE_IMAGES, PlayerProvider.ID+"="+menuInfo.id, null, null);

		switch(item.getItemId()){
		//播放
		case 1:
			if(cursor.moveToFirst()){
				Log.d("url",cursor.getString(cursor.getColumnIndexOrThrow(PlayerProvider.URL)));
				Intent intent = new Intent(getActivity(),VideoViewPlayer.class);
				Bundle bundle = new Bundle();
				bundle.putCharSequence("url", cursor.getString(cursor.getColumnIndexOrThrow(PlayerProvider.URL)));
				intent.putExtras(bundle);
				startActivity(intent);
			}
			break;
		//删除
		case 2:
			resolver.delete(PlayerProvider.CONTENT_URI, PlayerProvider.ID+"="+menuInfo.id, null);
			break;	
		//编辑
		case 3:
			if(cursor.moveToFirst()){
				LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
				View DialogView = layoutInflater.inflate(R.layout.alert_dialog_text_entry, null);
				
				final EditText name = (EditText)DialogView.findViewById(R.id.nameEditText);
				final EditText url = (EditText)DialogView.findViewById(R.id.urlEditText);
	
				name.setText(cursor.getString(cursor.getColumnIndexOrThrow(PlayerProvider.NAME)));
				url.setText(cursor.getString(cursor.getColumnIndexOrThrow(PlayerProvider.URL)));
				
				Dialog alertDialog = new AlertDialog.Builder(getActivity())
				.setTitle("添加地址")
				.setView(DialogView)
				.setPositiveButton("取消", new DialogInterface.OnClickListener(){
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自动生成的方法存根
					}
					
				}).setNegativeButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自动生成的方法存根
						String mName = name.getText().toString();
						String mUrl = url.getText().toString();
 
						ContentResolver resolver = getActivity().getContentResolver();
						ContentValues values = new ContentValues();
						values.put("name", mName);
						values.put("url",mUrl);
						resolver.update(PlayerProvider.CONTENT_URI, values, PlayerProvider.ID+"="+menuInfo.id, null);

					}
				}).create();
				
				Window window = alertDialog.getWindow();
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.alpha=0.6f;
				window.setAttributes(lp);

				alertDialog.show();
			}
			break;
		case 4:
			break;
			default:
				cursor.close();
				return super.onContextItemSelected(item);
		}
		return false;
	
	}
	
 
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO 自动生成的方法存根
		CursorLoader cursorLoader = new CursorLoader(
				getActivity(),
				PlayerProvider.CONTENT_URI,
				STORE_IMAGES,
				null,
				null,
				null);
		
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		// TODO 自动生成的方法存根
		mSimpleCursorAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO 自动生成的方法存根
		mSimpleCursorAdapter.swapCursor(null);
	}
	
}

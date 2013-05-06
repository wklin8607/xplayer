package com.nec.xplayer.ui;


import java.io.File;
import com.nec.xplayer.R;
import com.nec.xplayer.util.FileUtil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Thumbnails;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MovieFragment extends Fragment implements LoaderCallbacks<Cursor>{
	private GridView mGridView;
	private SimpleCursorAdapter mSimpleCursorAdapter;
	private static final String[] STORE_IMAGES={
		MediaStore.Video.Media.DISPLAY_NAME,
		MediaStore.Video.Media.SIZE,
		MediaStore.Video.Media.TITLE,
		MediaStore.Video.Media.DURATION,
		MediaStore.Video.Media.MIME_TYPE,
		MediaStore.Video.Media.DATA,
		MediaStore.Video.Media.DATE_ADDED,
		MediaStore.Video.Media.DATE_MODIFIED,
		MediaStore.Video.Media._ID
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		final View mView = inflater.inflate(R.layout.gridview, container,
				false);
		mGridView = (GridView)mView.findViewById(R.id.gridview);
		mSimpleCursorAdapter = new SimpleCursorAdapter(
				mView.getContext(),
				R.layout.grid_item,
				null,
				STORE_IMAGES,
				new int[]{R.id.ItemTitle,R.id.ItemMimeType,R.id.ItemImage,R.id.ItemTime},
				0
				);
		mSimpleCursorAdapter.setViewBinder(new ImageLocationBinder());
		mGridView.setAdapter(mSimpleCursorAdapter);
		getLoaderManager().initLoader(0, null, this);

		mGridView.setOnCreateContextMenuListener(new OnCreateContextMenuListener(){

			@SuppressLint("NewApi")
			@Override
			public void onCreateContextMenu(ContextMenu menu, View arg1,
					ContextMenuInfo arg2) {
				// TODO Auto-generated method stub
								
				menu.setHeaderTitle("操作");
				menu.add(0,1,0,"播放");
				menu.add(0,2,0,"删除");
				menu.add(0,3,0,"详细信息");
				menu.add(0,4,0,"取消");
			}
			
		});
		
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,
					long id) {
				// TODO 自动生成的方法存根
					CursorLoader cursorLoader = new CursorLoader(getActivity(),
							MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							STORE_IMAGES,MediaStore.Video.Media._ID+"="+id,
							null,null);
					Log.d("select",MediaStore.Video.Media._ID+"="+id);
					Cursor c = cursorLoader.loadInBackground();
					c.moveToFirst();
					
					Intent intent = new Intent(mView.getContext(),VideoViewPlayer.class);
					Bundle bundle = new Bundle();
					bundle.putCharSequence("url", c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA)));
					intent.putExtras(bundle);
					startActivity(intent);
					c.close();
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

	
	@SuppressLint("SimpleDateFormat")
	public boolean onContextItemSelected(MenuItem item) {
		// TODO 自动生成的方法存根
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		CursorLoader cursorLoader = new CursorLoader(getActivity(), 
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				STORE_IMAGES, MediaStore.Video.Media._ID+"="+menuInfo.id,
				null, null);
		Log.d("select",MediaStore.Video.Media._ID+"="+menuInfo.id);
		Cursor c = cursorLoader.loadInBackground();

		switch(item.getItemId()){
		case 1:
			if(c.moveToFirst()){
				Intent intent = new Intent(getActivity(),VideoViewPlayer.class);
				Bundle bundle = new Bundle();
				bundle.putCharSequence("url", c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA)));
				intent.putExtras(bundle);
				startActivity(intent);
			}
			break;
		case 2:
			ContentResolver resolver = getActivity().getContentResolver();
			resolver.delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media._ID+"="+menuInfo.id, null);
			if(c.moveToFirst()){
				File file = new File(c.getString(c.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
				if(file.exists()){
					file.delete();
				}
			}
			break;		
		case 3:
			if(c.moveToFirst()){
				//String DISPLAY_NAME = c.getString(c.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
				float SIZE = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
				
				int DURATION = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
				
				String TITLE =  c.getString(c.getColumnIndex(MediaStore.Video.Media.TITLE));
				String MIME_TYPE = c.getString(c.getColumnIndex(MediaStore.Video.Media.MIME_TYPE));
				String DATA = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
				AlertDialog alertDialog = new AlertDialog.Builder(getActivity())  
                .setTitle("详细信息")
                .setItems(new String[]{
                		"标题: "+TITLE,
                		"类型: "+MIME_TYPE,
                		"大小: "+SIZE/1000/1000+"MB",
                		"时长: "+FileUtil.toTime(DURATION),
                		//"Added time:"+dataFormat.format(DATE_ADDED),
                		//"Modified time:"+dataFormat.format(DATE_MODIFIED),
                		"路径: "+DATA

                }, null)
                .setNegativeButton("确定", null).create();
				
				//设置透明度
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
			c.close();
			return super.onContextItemSelected(item);
		}
		return false;
	}
	
 
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// TODO 自动生成的方法存根
		CursorLoader cursorLoader = new CursorLoader(
				getActivity(),
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
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
	
	public class ImageLocationBinder implements ViewBinder{

		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		@Override
		public boolean setViewValue(View view, Cursor cursor, int arg2) {
			// TODO 自动生成的方法存根
			
			Log.d("viewBinder","arg2:"+arg2);
			if(arg2==1){
				TextView text = (TextView)view;
				int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
				text.setText(size/1000/1000+" MB");
				return true;
				
			}else if(arg2==2){
				ImageView image = (ImageView)view;
				Log.d("path",cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)));
				Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA)),Thumbnails.MICRO_KIND);
				Bitmap bitmap1 = ThumbnailUtils.extractThumbnail(bitmap, 150, 150);
				
				BitmapDrawable mBitmap = new BitmapDrawable(bitmap1);
				image.setBackgroundDrawable(mBitmap);
				
				return true;
			}else if(arg2==3){
				TextView timetext = (TextView)view;
				int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
				Log.d("toTime",FileUtil.toTime(duration));

				timetext.setText(FileUtil.toTime(duration));
				return true;
			}
			else{
				return false;
			}
		}
	
		
	}
	

}

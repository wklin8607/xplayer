package com.nec.xplayer.ui;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nec.xplayer.R;
import com.nec.xplayer.util.FileUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class FileFragment extends Fragment {
	
	private ListView mListView;
	private String mDir = "/mnt/sdcard";
	private List<Map<String, Object>> mData;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		final View mView = inflater.inflate(R.layout.listview, container,
				false);

		mListView = (ListView)mView.findViewById(R.id.listView);
		//getActivity().setTitle(mDir);
		mData = getData();
		MyAdapter adapter = new MyAdapter(mView.getContext());
		
		mListView.setAdapter(adapter);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> l, View v, int position,
					long id) {
				// TODO 自动生成的方法存根
				
				if ((Integer) mData.get(position).get("img") == R.drawable.ex_folder) {
					mDir = (String) mData.get(position).get("info");
					mData = getData();
					MyAdapter adapter = new MyAdapter(mView.getContext());
					mListView.setAdapter(adapter);
				} else {
					String path=(String)mData.get(position).get("info");
					
					/**判断是否是视频文件，是的话调用videoViewPlayer播放*/
					if(FileUtil.isVideoType(path)){
						Intent intent = new Intent(mView.getContext(),VideoViewPlayer.class);
						Bundle bundle = new Bundle();
						bundle.putCharSequence("URL", path);
						intent.putExtras(bundle);
						startActivity(intent);
					}else{
						Toast.makeText(getActivity(), "Not video file", Toast.LENGTH_SHORT).show();
					}
				}
			}
		
		});

		return mView;
	}

	
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = null;
		File f = new File(mDir);
		File[] files = f.listFiles();

		if (!mDir.equals("/mnt/sdcard")) {
			map = new HashMap<String, Object>();
			map.put("title", "Back to ../");
			map.put("info", f.getParent());
			map.put("img", R.drawable.ex_folder);
			list.add(map);
		}
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				map = new HashMap<String, Object>();
				map.put("title", files[i].getName());
				map.put("info", files[i].getPath());
				if (files[i].isDirectory())
					map.put("img", R.drawable.ex_folder);
				else
					map.put("img", R.drawable.ex_doc);
				list.add(map);
			}
		}
		return list;
	}
	

	public class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public MyAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return mData.size();
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.file_item, null);
				holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.info = (TextView) convertView.findViewById(R.id.info);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.img.setBackgroundResource((Integer) mData.get(position).get("img"));
			holder.title.setText((String) mData.get(position).get("title"));
			holder.info.setText((String) mData.get(position).get("info"));
			return convertView;
		}
	}
	public final class ViewHolder {
		public ImageView img;
		public TextView title;
		public TextView info;
	}

}

package com.nec.xplayer.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.nec.xplayer.R;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
//import android.widget.ProgressBar;


public class VideoViewPlayer extends Activity {
    private static final String TAG = "ePlayer";
	private String url = null;
	private VideoView mVideoView;
    private View mVolumeBrightnessLayout;
    private View mVideoMenu;
    private ImageView mOperationBg;
    private ImageView mOperationPercent;
    private AudioManager mAudioManager;
    private ImageButton mCaptureButton;
    private ProgressDialog mPD;
    
    /** 最大声音 */
    private int mMaxVolume;
    /** 当前声音 */
    private int mVolume = -1;
    /** 当前亮度 */
    private float mBrightness = -1f;
    /** 当前缩放模式 */
    private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
    private GestureDetector mGestureDetector;
    //private MediaController mMediaController;
    
    private boolean mViewMenu = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);  

		setContentView(R.layout.videoview);
		

        mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
        mVideoMenu = (View)findViewById(R.id.video_menu);
        mVideoMenu.setVisibility(View.GONE);
        mOperationBg = (ImageView) findViewById(R.id.operation_bg);
        mOperationPercent = (ImageView) findViewById(R.id.operation_percent);
        mCaptureButton = (ImageButton)findViewById(R.id.capture);

        mPD = new ProgressDialog(this);
		mPD.setCancelable(false);
		mPD.setMessage("loading...");
		mPD.show();

        
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		url = bundle.getString("url");
		

		mVideoView = (VideoView)findViewById(R.id.videoView);
		mVideoView.setVideoPath(url);
		mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
		mVideoView.setMediaController(new MediaController(this));
	

		/*注册一个回调函数，在视频预处理完成后调用。*/
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer arg0) {
				// TODO 自动生成的方法存根
				mVideoMenu.setVisibility(View.VISIBLE);
				mViewMenu=true;
				finish();
			}
		});
			
		/*注册一个回调函数，在异步操作调用过程中发生错误时调用*/
		mVideoView.setOnErrorListener(new OnErrorListener(){

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				// TODO 自动生成的方法存根
				Toast.makeText(getApplicationContext(), "Play failure!", Toast.LENGTH_SHORT).show();
				finish();
				return false;
			}
			
		});
		
		/*注册一个回调函数，在网络视频流缓冲变化时调用。*/
		mVideoView.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			
			@Override
			public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
				// TODO 自动生成的方法存根
				
				mPD.dismiss();
			}
		});
		
		/*截图*/
		mCaptureButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				if(mVideoView.isPlaying())
				{
					Bitmap bitmap = null;
					FileOutputStream fos = null;
					try {
						bitmap = mVideoView.getCurrentFrame();
						if(bitmap!=null){
							File file = new File(Environment.getExternalStorageDirectory()+"/DCIM/"+TAG);
							if(!file.exists()){
								file.mkdir();
							}
							fos = new FileOutputStream(Environment.getExternalStorageDirectory()+"/DCIM/"+TAG+"/captrue["+System.currentTimeMillis()+"].png");
							if(null != fos){
								Log.i("path",Environment.getExternalStorageDirectory()+"/DCIM/"+TAG+"/captrue["+System.currentTimeMillis()+"].png");
								Log.i("fos","fos seccess!");
								bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
								fos.flush();
								fos.close();
							}	
							sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
							Toast.makeText(getApplicationContext(),"Capture Successfully!",Toast.LENGTH_SHORT).show();
							
						}else{
							Toast.makeText(getApplicationContext(),"Capture Failure!",Toast.LENGTH_SHORT).show();
						}
					} catch (FileNotFoundException e) {
						// TODO 自动生成的 catch 块
						Toast.makeText(getApplicationContext(),"Capture Failure!",Toast.LENGTH_SHORT).show();

						e.printStackTrace();
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						Toast.makeText(getApplicationContext(),"Capture Failure!",Toast.LENGTH_SHORT).show();

						e.printStackTrace();
					}

					}
			}


			
		});

		mGestureDetector = new GestureDetector(this, new MyGestureListener());
	
	}
	

	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;

        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_UP:
            endGesture();
            break;
        }

        return super.onTouchEvent(event);
    }

    /** 手势结束 */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        // 隐藏
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
        
        menuDissmissHandler.removeMessages(0);
        menuDissmissHandler.sendEmptyMessageDelayed(0, 3000);
    }

    private class MyGestureListener extends SimpleOnGestureListener {

        /** 双击 */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
                mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
            else
                mLayout++;
            if (mVideoView != null)
                mVideoView.setVideoLayout(mLayout, 0);
            return true;
        }

        //单击
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
        	// TODO 自动生成的方法存根
        	if(mViewMenu){
        		mVideoMenu.setVisibility(View.GONE);
        		mViewMenu=false;
        	}else{
        		mVideoMenu.setVisibility(View.VISIBLE);
        		mViewMenu=true;
        	}
        	return true;
        }
        /** 滑动 */
        @SuppressWarnings("deprecation")
		@Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();
            Display disp = getWindowManager().getDefaultDisplay();
            int windowWidth = disp.getWidth();
            int windowHeight = disp.getHeight();

            if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / windowHeight);
            else if (mOldX < windowWidth / 5.0)// 左边滑动
                onBrightnessSlide((mOldY - y) / windowHeight);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }


    /** 定时隐藏 */
	private Handler mDismissHandler = new Handler() {
    	
		@SuppressLint("HandlerLeak")
		@Override
        public void handleMessage(Message msg) {
            mVolumeBrightnessLayout.setVisibility(View.GONE);
        }
    };
    
    /**隐藏上方图标**/
	private Handler menuDissmissHandler = new Handler() {
    	
		@Override
        public void handleMessage(Message msg) {
            	mVideoMenu.setVisibility(View.GONE);
            
        }
    };
    /**
     * 滑动改变声音大小
     * 
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
            mVideoMenu.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

        // 变更进度条
        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
                * index / mMaxVolume;
        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     * 
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mOperationBg.setImageResource(R.drawable.video_brightness_bg);
            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);

        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
        lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
        mOperationPercent.setLayoutParams(lp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// TODO 自动生成的方法存根
    	if(keyCode==KeyEvent.KEYCODE_HOME){
    		finish();
    	}
    	return super.onKeyDown(keyCode, event);
    }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mVideoView != null)
			mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
		super.onConfigurationChanged(newConfig);
	}

}

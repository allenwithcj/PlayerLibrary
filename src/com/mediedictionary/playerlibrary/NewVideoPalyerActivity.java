package com.mediedictionary.playerlibrary;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.mediedictionary.playerlibrary.PlayerView.OnChangeListener;
import org.videolan.libvlc.EventHandler;

public class NewVideoPalyerActivity extends Activity implements OnChangeListener, OnClickListener,
		OnSeekBarChangeListener, Callback{

	private static final String TAG ="NewVideoPalyerActivity";
	private static final int SHOW_PROGRESS = 0;
	private static final int ON_LOADED = 1;
	private static final int HIDE_OVERLAY = 2;

	private View rlLoading;
	private PlayerView mPlayerView;
	private TextView tvBuffer, tvTime, tvLength;
	private SeekBar sbVideo;
//	private ImageButton ibLock, ibFarward, ibBackward, ibPlay;
	private ImageButton ibEnlarge;
	private View llOverlay, rlOverlayTitle;
//	private View control_layot;
	private Handler mHandler;
	private static final int MSG_SCREEN_FULL = 0x00000001;
	private static final int MSG_SCREEN_WRAP = 0x00000002;

	private ImageView image_back;
	private String mUrl,mAddress,mDeviceModel,mHostVersion;
	private TextView tvAddress,tvDeviceModel,tv_hostVersion;
//	private boolean canControl = false;

	private ImageButton up_btn,down_btn,left_btn,right_btn;
	private View rlHeader;


	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MSG_SCREEN_FULL:
					screenFullModeUI();
					break;

				case MSG_SCREEN_WRAP:
					screenWrapModeUI();
					break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_new_video);
		Log.e(TAG,"onCreate");

		mUrl = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
		mAddress = "192.168.1.1";
		mDeviceModel = "枪式摄像头";
		mHostVersion = "v1";
//      mUrl = getIntent().getStringExtra("url");
//		mAddress=getIntent().getStringExtra("address");
//		mDeviceModel=getIntent().getStringExtra("deviceModel");
//		mHostVersion = getIntent().getStringExtra("hostVersion");
		if (TextUtils.isEmpty(mUrl)) {
			Toast.makeText(this, "error:no url in intent!", Toast.LENGTH_SHORT).show();
			return;
		}

		mHandler = new Handler(this);

		tvTime = (TextView) findViewById(R.id.tv_time);
		tvLength = (TextView) findViewById(R.id.tv_length);
		sbVideo = (SeekBar) findViewById(R.id.sb_video);
		sbVideo.setOnSeekBarChangeListener(this);
//		ibLock = (ImageButton) findViewById(R.id.ib_lock);
//		ibLock.setOnClickListener(this);
//		ibBackward = (ImageButton) findViewById(R.id.ib_backward);
//		ibBackward.setOnClickListener(this);
//		ibPlay = (ImageButton) findViewById(R.id.ib_play);
//		ibPlay.setOnClickListener(this);
//		ibFarward = (ImageButton) findViewById(R.id.ib_forward);
//		ibFarward.setOnClickListener(this);
		ibEnlarge = (ImageButton) findViewById(R.id.ib_enlarge);
		ibEnlarge.setOnClickListener(this);

		llOverlay = findViewById(R.id.ll_overlay);
		rlOverlayTitle = findViewById(R.id.rl_title);
//		control_layot = findViewById(R.id.control_layot);
		rlHeader = findViewById(R.id.rlHeader);
		rlLoading = findViewById(R.id.rl_loading);
		tvBuffer = (TextView) findViewById(R.id.tv_buffer);

		image_back = (ImageView)findViewById(R.id.image_back);
		image_back.setOnClickListener(this);

		tvAddress=(TextView) findViewById(R.id.tv_address);
		tvDeviceModel=(TextView) findViewById(R.id.tv_deviceModel);
		tv_hostVersion=(TextView) findViewById(R.id.tv_hostVersion);

		up_btn = (ImageButton)findViewById(R.id.up_btn);
		down_btn = (ImageButton)findViewById(R.id.down_btn);
		left_btn = (ImageButton)findViewById(R.id.left_btn);
		right_btn = (ImageButton)findViewById(R.id.right_btn);

		up_btn.setOnClickListener(this);
		down_btn.setOnClickListener(this);
		left_btn.setOnClickListener(this);
		right_btn.setOnClickListener(this);

		if(TextUtils.isEmpty(mAddress)){
			tvAddress.setText("摄像头地址:无");
		}else{
			tvAddress.setText("摄像头地址："+mAddress);
		}
		if(TextUtils.isEmpty(mDeviceModel)){
			tvDeviceModel.setText("摄像头类型:无");
		}else{
			tvDeviceModel.setText("摄像头类型："+ mDeviceModel);
//			if(mDeviceModel.equals("枪式摄像头") || mDeviceModel.equals("半球摄像头")){
//				canControl = true;
//			}else{
//				canControl = false;
//			}
		}
		if(TextUtils.isEmpty(mHostVersion)){
			tv_hostVersion.setText("主控版本:无");
		}else{
			tv_hostVersion.setText("主控版本："+mHostVersion);
		}

		//使用步骤
		//第一步 ：通过findViewById或者new PlayerView()得到mPlayerView对象
		//mPlayerView= new PlayerView(PlayerActivity.this);
		mPlayerView = (PlayerView) findViewById(R.id.pv_video);

		//第二步：设置参数，毫秒为单位
		mPlayerView.setNetWorkCache(20000);

		//第三步:初始化播放器
		mPlayerView.initPlayer(mUrl);

		//第四步:设置事件监听，监听缓冲进度等
		mPlayerView.setOnChangeListener(this);

		//第五步：开始播放
		mPlayerView.start();

		//init view
		showLoading();
		hideOverlay();
		setVideoViewPosition();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {


		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (rlHeader.getVisibility() != View.VISIBLE) {
					showOverlay();
				} else {
					hideOverlay();
				}
			}
		}else{
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (llOverlay.getVisibility() != View.VISIBLE) {
					showOverlay();
				} else {
					hideOverlay();
				}
			}
		}
		return false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.e(TAG,"onConfigurationChanged");
		setVideoViewPosition();
		super.onConfigurationChanged(newConfig);
	}

	public void screenFullModeUI() {
		setScreenLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	}

	public void screenWrapModeUI() {
		setScreenLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(this, 300));
	}

	private void setScreenLayoutParams(int width, int height) {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
		mPlayerView.setLayoutParams(layoutParams);
		mPlayerView.requestFocus();
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private void setVideoViewPosition() {
		mPlayerView.changeSurfaceSize();
		switch (getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				llOverlay.setVisibility(View.GONE);
				rlOverlayTitle.setVisibility(View.GONE);
				ibEnlarge.setVisibility(View.GONE);
				handler.sendEmptyMessage(MSG_SCREEN_FULL);
				break;


			case Configuration.ORIENTATION_PORTRAIT:
				llOverlay.setVisibility(View.VISIBLE);
				rlOverlayTitle.setVisibility(View.VISIBLE);
				ibEnlarge.setVisibility(View.VISIBLE);
				handler.sendEmptyMessage(MSG_SCREEN_WRAP);
				break;
		}
	}

	@Override
	public void onPause() {
		Log.e(TAG,"onPause");
//		if(mPlayerView != null || mPlayerView.isPlaying()){
//		 	mPlayerView.pause();
//			 ibPlay.setBackgroundResource(R.drawable.ic_play);
//		 }
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		Log.e(TAG,"onResume");
//		 if(mPlayerView != null){
//		 	mPlayerView.play();
//			 ibPlay.setBackgroundResource(R.drawable.ic_pause);
//		 }
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Log.e(TAG,"onDestroy");
//		canControl = false;
		if(mPlayerView != null){
			mPlayerView.stop();
		}
		super.onDestroy();
	}

	@Override
	public void onBufferChanged(float buffer) {
		if (buffer >= 100) {
			hideLoading();
		} else {
			showLoading();
		}
		tvBuffer.setText("正在缓冲中..." + (int) buffer + "%");
	}

	private void showLoading() {
		rlLoading.setVisibility(View.VISIBLE);

	}

	private void hideLoading() {
		rlLoading.setVisibility(View.GONE);
	}

	@Override
	public void onLoadComplet() {
		mHandler.sendEmptyMessage(ON_LOADED);
	}

	@Override
	public void onError() {
		Toast.makeText(NewVideoPalyerActivity.this, "视频播放失败，请稍后再试...", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onEnd() {
		finish();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
//		case R.id.ib_lock:
//			break;
//		case R.id.ib_forward:
//			mPlayerView.seek(10000);
//			break;
//		case R.id.ib_play:
//			if (mPlayerView.isPlaying()) {
//				mPlayerView.pause();
//				ibPlay.setBackgroundResource(R.drawable.ic_play);
//			} else {
//				mPlayerView.play();
//				ibPlay.setBackgroundResource(R.drawable.ic_pause);
//			}
//			break;

//		case R.id.ib_backward:
//			mPlayerView.seek(-10000);
//			break;
		case R.id.ib_enlarge:
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				ibEnlarge.setVisibility(View.GONE);
				rlOverlayTitle.setVisibility(View.GONE);
				hideOverlay();
			}
			break;

		case R.id.image_back:
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				rlOverlayTitle.setVisibility(View.VISIBLE);
				ibEnlarge.setVisibility(View.VISIBLE);
				rlHeader.setVisibility(View.VISIBLE);
			}else{
				if(mPlayerView != null){
					mPlayerView.stop();
				}
				finish();
			}
			break;

			case R.id.up_btn:

				break;

			case R.id.down_btn:

				break;

			case R.id.left_btn:

				break;

			case R.id.right_btn:

				break;
		}
	}

	private void showOverlay() {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			llOverlay.setVisibility(View.VISIBLE);
		}
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			rlHeader.setVisibility(View.VISIBLE);
		}

//		if(canControl){
//			control_layot.setVisibility(View.VISIBLE);
//		}
		mHandler.sendEmptyMessage(SHOW_PROGRESS);
		mHandler.removeMessages(HIDE_OVERLAY);
		mHandler.sendEmptyMessageDelayed(HIDE_OVERLAY, 5 * 1000);
	}

	private void hideOverlay() {
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			llOverlay.setVisibility(View.GONE);
		}
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
			rlHeader.setVisibility(View.GONE);
		}
//		control_layot.setVisibility(View.GONE);
		mHandler.removeMessages(SHOW_PROGRESS);
	}

	private int setOverlayProgress() {
		if (mPlayerView == null) {
			return 0;
		}
		int time = (int) mPlayerView.getTime();
		int length = (int) mPlayerView.getLength();
//		boolean isSeekable = mPlayerView.canSeekable() && length > 0;
//		ibFarward.setClickable(isSeekable ? true : false);
//		ibBackward.setClickable(isSeekable ? true : false);
		sbVideo.setMax(length);
		sbVideo.setProgress(time);
		if (time >= 0) {
			tvTime.setText(millisToString(time, false));
		}
		if (length >= 0) {
			tvLength.setText(millisToString(length, false));
		}
		return time;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser && mPlayerView.canSeekable()) {
			mPlayerView.setTime(progress);
			setOverlayProgress();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case SHOW_PROGRESS:
			setOverlayProgress();
			mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 20);
			break;
		case ON_LOADED:
			showOverlay();
			break;
		case HIDE_OVERLAY:
			hideOverlay();
			break;
		default:
			break;
		}
		return false;
	}

	private String millisToString(long millis, boolean text) {
		boolean negative = millis < 0;
		millis = java.lang.Math.abs(millis);
		int mini_sec = (int) millis % 1000;
		millis /= 1000;
		int sec = (int) (millis % 60);
		millis /= 60;
		int min = (int) (millis % 60);
		millis /= 60;
		int hours = (int) millis;

		String time;
		DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		format.applyPattern("00");

		DecimalFormat format2 = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		format2.applyPattern("000");
		if (text) {
			if (millis > 0)
				time = (negative ? "-" : "") + hours + "h" + format.format(min) + "min";
			else if (min > 0)
				time = (negative ? "-" : "") + min + "min";
			else
				time = (negative ? "-" : "") + sec + "s";
		} else {
			if (millis > 0)
				time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec) + ":" + format2.format(mini_sec);
			else
				time = (negative ? "-" : "") + min + ":" + format.format(sec) + ":" + format2.format(mini_sec);
		}
		return time;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//				if(canControl){
//					control_layot.setVisibility(View.VISIBLE);
//				}
				llOverlay.setVisibility(View.VISIBLE);
				rlOverlayTitle.setVisibility(View.VISIBLE);
				ibEnlarge.setVisibility(View.VISIBLE);
				rlHeader.setVisibility(View.VISIBLE);
			}else{
				if(mPlayerView != null){
					mPlayerView.stop();
				}
				finish();
			}
		}
		return true;
	}

}

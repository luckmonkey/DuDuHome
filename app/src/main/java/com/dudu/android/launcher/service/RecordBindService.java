package com.dudu.android.launcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.bean.VideoEntity;
import com.dudu.android.launcher.db.DbHelper;
import com.dudu.android.launcher.ui.activity.video.VideoListActivity;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.FileUtils;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecordBindService extends Service implements SurfaceHolder.Callback {

	private static final String TAG = "RecordBindService";

	private static final int VIDEO_CACHE_MAX_SIZE = 3 * 1024;

	private static final int VIDEO_INTERVAL = 10 * 60 * 1000;

	private WindowManager windowManager;

	private SurfaceView surfaceView;

	private Camera camera = null;

	private MediaRecorder mediaRecorder = null;

	private LayoutParams layoutParams;

	private volatile boolean isRecording = false;

	private SurfaceHolder surfaceHolder;

	private MyBinder binder = new MyBinder();

	private View videoView;

	private Button backButton;

	private ImageButton localVideo;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	private String videoName = "";

	private String videoPath = "";

	private DbHelper dbHelper = null;

	private Handler handler = new Handler();

	private Timer timer;

	private TimerTask timerTask;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	return START_STICKY;
    }
	@Override
	public void onCreate() {
		super.onCreate();

		videoPath = FileUtils.getVideoStorageDir().getAbsolutePath();

		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		dbHelper = DbHelper.getDbHelper(RecordBindService.this);

		videoView = LayoutInflater.from(this).inflate(R.layout.video_main, null, false);
		surfaceView = (SurfaceView) videoView.findViewById(R.id.surfaceView);
		backButton = (Button) videoView.findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBroadcast(new Intent(Constants.VIDEO_PREVIEW_BROADCAST));
//				takePicture();
			}
		});

		localVideo = (ImageButton) videoView.findViewById(R.id.local_video);
		localVideo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecordBindService.this, VideoListActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});

		SurfaceHolder holder = surfaceView.getHolder();

		holder.addCallback(this);

		layoutParams = new LayoutParams(1, 1, LayoutParams.TYPE_PHONE,
				LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		layoutParams.gravity = Gravity.LEFT | Gravity.TOP;

		windowManager.addView(videoView, layoutParams);

		timer = new Timer();

	}

	public void updatePreviewSize(int width, int height) {
		layoutParams.width = width;
		layoutParams.height = height;
		windowManager.updateViewLayout(videoView, layoutParams);
	}

	public void startRecord() {
		if (isRecording) {
			return;
		}

		new MediaPrepareTask().execute(null, null, null);
	}

	public void stopRecord() {
		if (mediaRecorder != null) {
			try {
				mediaRecorder.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		releaseMediaRecorder();
		if (camera != null) {
			camera.lock();
		}

		isRecording = false;
		releaseCamera();

		insertVideo();
	}

	public void startRecordTimer() {
		timer = new Timer();

		timerTask = new TimerTask() {

			@Override
			public void run() {
				stopRecord();
				startRecord();
			}
		};

		timer.schedule(timerTask, VIDEO_INTERVAL, VIDEO_INTERVAL);
	}

	public void stopRecordTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopRecordTimer();

		releaseMediaRecorder();

		releaseCamera();

		windowManager.removeView(videoView);

		removeLastVideo();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class MyBinder extends Binder {
		public RecordBindService getService() {
			return RecordBindService.this;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.surfaceHolder = holder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		this.surfaceHolder = holder;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	private void removeLastVideo() {
		File file = new File(videoPath, videoName);
		if (file != null && file.exists()) {
			file.delete();
		}
	}

	private boolean prepareVideoRecorder() {
		camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
		Camera.Parameters p = camera.getParameters();
		final List<Size> listSize = p.getSupportedPreviewSizes();
		Size mPreviewSize = listSize.get(2);
		Log.v(TAG, "use: width = " + mPreviewSize.width + " height = " + mPreviewSize.height);
		p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
		p.setPictureSize(1280, 720);
		camera.setParameters(p);
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setOnErrorListener(new OnErrorListener() {

			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				try {
					if (mr != null)
						mr.reset();
				} catch (Exception e) {
					Log.e(TAG, "stopRecord: ", e);
				}
			}
		});

		camera.unlock();
		mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
		mediaRecorder.setCamera(camera);
		// mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		// mediaRecorder.setProfile(CamcorderProfile
		// .get(CamcorderProfile.QUALITY_HIGH));

		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
		mediaRecorder.setVideoSize(640, 480);

		if (profile.videoBitRate > 2 * 1024 * 1024)
			mediaRecorder.setVideoEncodingBitRate(2 * 1024 * 1024);
		else
			mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
		mediaRecorder.setVideoFrameRate(30);
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

		videoName = dateFormat.format(new Date()) + ".mp4";
		mediaRecorder.setOutputFile(videoPath + File.separator + videoName);
		try {
			mediaRecorder.prepare();
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			try {
				mediaRecorder.reset();
				mediaRecorder.release();
				mediaRecorder = null;
				if (camera != null) {
					camera.lock();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void releaseCamera() {
		if (camera != null) {
			try {
				camera.release();
				camera = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void insertVideo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				File file = new File(videoPath, videoName);
				if (file.exists() && file.length() > 0) {
					String length = FileUtils.fileByte2Mb(file.length());
					float totalSize = dbHelper.getTotalSize() + Float.parseFloat(length);
					if (totalSize >= VIDEO_CACHE_MAX_SIZE) {
						if (dbHelper.isAllVideoLocked()) {
							handler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), R.string.alert_cache_space_full,
											Toast.LENGTH_SHORT).show();
								}
							});

							file.delete();
							return;
						}

						do {
							dbHelper.deleteOldestVideo();
							totalSize = dbHelper.getTotalSize() + Float.parseFloat(length);
						} while (totalSize >= VIDEO_CACHE_MAX_SIZE);
					}

					VideoEntity video = new VideoEntity();
					video.setName(file.getName());
					video.setFile(file);
					video.setPath(videoPath);
					video.setSize(length);
					dbHelper.insertVideo(video);
				}
			}
		}).start();
	}

	class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			if (prepareVideoRecorder()) {
				mediaRecorder.start();
				isRecording = true;
			} else {
				releaseMediaRecorder();
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {

		}

	}
	
}

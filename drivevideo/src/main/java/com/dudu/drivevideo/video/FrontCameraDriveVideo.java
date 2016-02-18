package com.dudu.drivevideo.video;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import com.dudu.commonlib.CommonLib;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.drivevideo.utils.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Created by dengjun on 2016/2/13.
 * Description :
 */
public class FrontCameraDriveVideo implements MediaRecorder.OnErrorListener ,MediaRecorder.OnInfoListener{
    private FrontVideoConfigParam frontVideoConfigParam;
    private String curVideoFileAbsolutePath;

    private Camera mCamera = null;
    private CameraPreview cameraPreview;
    private MediaRecorder mMediaRecorder;


    private boolean isRecording = false;

    private Logger log;

    public FrontCameraDriveVideo() {
        log = LoggerFactory.getLogger("video.frontdrivevideo");

        init();
    }

    private void init(){
        frontVideoConfigParam = new FrontVideoConfigParam();
    }

    public boolean initCamera(){
        log.debug("开始初始化camera...");
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            cameraPreview = new CameraPreview(CommonLib.getInstance().getContext(), mCamera);
        } catch (Exception e) {
            log.error("获取相机失败", e);
            return false;
        }
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFormat(PixelFormat.YCbCr_420_SP);

//        params.setPreviewSize(CommonLib.getInstance().getContext().getResources().getDisplayMetrics().widthPixels,
//                CommonLib.getInstance().getContext().getResources().getDisplayMetrics().heightPixels);
        params.setPreviewSize(1920, 480);
        params.setPictureSize(1920, 480);
//        List<Camera.Size> sizeList =  params.getSupportedPictureSizes();
//        sizeList =params.getSupportedVideoSizes();
//        sizeList = params.getSupportedPreviewSizes();


        mCamera.setParameters(params);
        mCamera.setErrorCallback(new Camera.ErrorCallback() {
            @Override
            public void onError(int error, Camera camera) {
                log.error("相机出错了： " + error);
            }
        });
        log.debug("初始化camera成功...");
        return true;
    }

    public void releaseCamera(){
        log.debug("释放前置摄像头");
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean prepareMediaRecorder() {
        log.debug("准备录像");
        if (mMediaRecorder == null){
            mMediaRecorder = new MediaRecorder();
        }

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnInfoListener(this);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        CamcorderProfile profile = CamcorderProfile.get(frontVideoConfigParam.getQuality());
        if (profile.videoBitRate > frontVideoConfigParam.getVideoBitRate()) {
            mMediaRecorder.setVideoEncodingBitRate(frontVideoConfigParam.getVideoBitRate());
        } else {
            mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        }

        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        curVideoFileAbsolutePath = generateCurVideoName();
        log.debug("前置当前摄像文件路径：{}", curVideoFileAbsolutePath);
        mMediaRecorder.setOutputFile(curVideoFileAbsolutePath);

        mMediaRecorder.setVideoFrameRate(frontVideoConfigParam.getRate());
        mMediaRecorder.setVideoSize(1920, 480);

        mMediaRecorder.setPreviewDisplay(cameraPreview.getmHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            log.error("准备录像出错: ", e);
            releaseMediaRecorder();
            return false;
        }
        log.debug("准备录像成功");
        return true;
    }

    public void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }


    public boolean startDriveVideo() {
        log.debug("开始行车记录");
        boolean returnFlag = false;
        if (initCamera()){
            returnFlag = startRecord();
        }
        return returnFlag;
    }

    public void stopDrvieVideo(){
        log.debug("结束行车记录");
        releaseMediaRecorder();
        releaseCamera();
    }

    public boolean startRecord(){
        if (isRecording)
            return true;
        log.debug("开始录像");
        boolean returnFlag = false;
        if (prepareMediaRecorder()){
            try {
                mMediaRecorder.start();
                isRecording = true;
                returnFlag = true;
            }catch (Exception e){
                log.error("开始录像失败");
            }


        }
        return returnFlag;
    }

    public void stopRecord(){
        log.debug("结束录像");
        if (mMediaRecorder != null){
            mMediaRecorder.stop();
        }
        releaseMediaRecorder();
        isRecording = false;
    }



    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
//        this.surfaceHolder = surfaceHolder;
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            log.error("异常", e);
        }
    }

    public void startPreview() {
        log.debug("前置开启预览");
        mCamera.startPreview();
    }

    public void stopPreview(){
        log.debug("前置关闭预览");
        mCamera.stopPreview();
    }

    public String getCurVideoFileAbsolutePath() {
        return curVideoFileAbsolutePath;
    }


    public FrontVideoConfigParam getFrontVideoConfigParam() {
        return frontVideoConfigParam;
    }

    private String generateCurVideoName(){
        return FileUtil.getTFlashCardDirFile("/dudu", FrontVideoConfigParam.VIDEO_STORAGE_PATH).getAbsolutePath()
                + File.separator+ TimeUtils.format(TimeUtils.format5)+".mp4";
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        log.info("MediaRecorder error   what = {}", what);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }


    public Camera getmCamera() {
        return mCamera;
    }

    public CameraPreview getCameraPreview() {
        return cameraPreview;
    }
}

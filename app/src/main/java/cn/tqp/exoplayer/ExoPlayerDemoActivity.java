package cn.tqp.exoplayer;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.tqp.exoplayer.listener.EventLogger;
import cn.tqp.exoplayer.manager.ExoPlayerManager;
import cn.tqp.exoplayer.exoplayerui.ExoPlayerView;
import cn.tqp.exoplayer.entity.PreviewImage;
import cn.tqp.exoplayer.entity.VideoInfo;
import cn.tqp.exoplayer.utils.ScreenUtils;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by tangqipeng on 2018/1/25.
 */

public class ExoPlayerDemoActivity extends AppCompatActivity {

    private ExoPlayerView exoPlayerView;
    private ExoPlayerManager exoPlayerManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);
        requestRxPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //竖屏
        getSupportActionBar().hide();
        //横屏
//        ExoPlayerUtils.hideActionBarAndBottomUiMenu(this);

        //常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        exoPlayerView = (ExoPlayerView) findViewById(R.id.exoplayerview);

        //横屏
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                if (android.os.Build.VERSION.SDK_INT > 18) {
//                    ((ViewGroup) exoPlayerView.getParent()).setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.INVISIBLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//                } else {
//                    ((ViewGroup) exoPlayerView.getParent()).setSystemUiVisibility(View.INVISIBLE);
//                }
//            }
//        });

        //竖屏加入这个
        int playerWidth = ScreenUtils.getScreenWidth(this);
        int playerHeight = playerWidth * 9 / 16;
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, playerHeight);
        exoPlayerView.setLayoutParams(layoutParams);

        //--------------------------------------------------------------------------
        //这句绝对不能少（再有大小屏切换的时候，不需要大小屏切换可以不设置）最好是每次都设置
        exoPlayerView.setExoPlayerViewContainer((ViewGroup) exoPlayerView.getParent());

        exoPlayerManager = new ExoPlayerManager(this, exoPlayerView);

        /**
         * 这里说明 如果采用的预览窗口的资源是个播放地址，那么 videoInfo.moviePreviewUrl 就是播放地址，注入数据的方法是exoPlayerManager.addVideoDatas(videoInfos)；
         * 如果预览窗口的资源是图片地址，那么videoInfo.moviePreviewUrl就是图片的地址，注入数据的方式是exoPlayerManager.addVideoDatasAndPreviewImages(videoInfos)
         */
        List<VideoInfo> videoInfos = new ArrayList<>();

//        VideoInfo videoInfo = new VideoInfo();
//        videoInfo.movieId = "1";
//        videoInfo.movieTitle = "银河护卫队";
//        videoInfo.movieUrl = "http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8";
//        videoInfo.moviePreviewUrl = "http://cdn.ali.vcinema.com.cn/201709/xtMHgEOw/xOmtuUUGLj.m3u8";
//        videoInfo.imageCount = 42;
//        List<PreviewImage> previewImages = new ArrayList<>();
//        PreviewImage previewImage = new PreviewImage();
//        previewImage.imagePreviewUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg";
//        previewImage.imageSize = 42;
//        previewImage.lines = 7;
//        previewImage.colums = 7;
//        previewImages.add(previewImage);
//        videoInfo.previewImagesList = previewImages;
//        videoInfos.add(videoInfo);

//        VideoInfo videoInfo1 = new VideoInfo();
//        videoInfo1.movieId = "2";
//        videoInfo1.movieTitle = "TCL";
//        videoInfo1.movieUrl = getResources().getString(R.string.url_hls1);
//        videoInfo1.moviePreviewUrl = getResources().getString(R.string.url_hls1);
//        videoInfo1.imageCount = 42;
//        List<PreviewImage> previewImages1 = new ArrayList<>();
//        PreviewImage previewImage1 = new PreviewImage();
//        previewImage1.imagePreviewUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg";
//        previewImage1.imageSize = 42;
//        previewImage1.lines = 7;
//        previewImage1.colums = 7;
//        previewImages1.add(previewImage1);
//        videoInfo1.previewImagesList = previewImages1;
//        videoInfos.add(videoInfo1);

        VideoInfo videoInfo2 = new VideoInfo();
        videoInfo2.movieId = "2";
        videoInfo2.movieTitle = "怪奇物语";
        videoInfo2.movieUrl = "http://appback.csmc-cloud.com/index.php?user/public_link&fid=cc2cdNq81kx88CGZsIgR1kEMD7FPS8ZEAYnk0p1Xi_5-vzuB4uMTi1s_XjQUjfVSTQgFQgztuYkXM7PciJfiAeGWDRG08tm_ZEi0uwaq0XXj2JfaiWCsuD9KIs-1yDwxdO1eSoFL2RYJM10xQYujHmHOFLkRPvRga5shlrFjdRro40rCgQaldoeutJRgRly9PFOWvE-qmdgFyKTW-A&file_name=/UFO+model_Animation_Eyes%28VoiceOver%29%E6%A8%B1%E6%A1%83%E7%BA%A2.mp4";
//        videoInfo2.movieUrl = "http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4";
//        videoInfo2.moviePreviewUrl = "http://pumpkin-online-movie-development.oss-cn-beijing.aliyuncs.com/201801/PIQvhUGb/pqczrawfwM.m3u8";
        videoInfo2.moviePreviewUrl = "http://appback.csmc-cloud.com/index.php?user/public_link&fid=cc2cdNq81kx88CGZsIgR1kEMD7FPS8ZEAYnk0p1Xi_5-vzuB4uMTi1s_XjQUjfVSTQgFQgztuYkXM7PciJfiAeGWDRG08tm_ZEi0uwaq0XXj2JfaiWCsuD9KIs-1yDwxdO1eSoFL2RYJM10xQYujHmHOFLkRPvRga5shlrFjdRro40rCgQaldoeutJRgRly9PFOWvE-qmdgFyKTW-A&file_name=/UFO+model_Animation_Eyes%28VoiceOver%29%E6%A8%B1%E6%A1%83%E7%BA%A2.mp4";
        videoInfo2.imageCount = 42;
        List<PreviewImage> previewImages2 = new ArrayList<>();
        PreviewImage previewImage2 = new PreviewImage();
        previewImage2.imagePreviewUrl = "https://bitdash-a.akamaihd.net/content/MI201109210084_1/thumbnails/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.jpg";
        previewImage2.imageSize = 42;
        previewImage2.lines = 7;
        previewImage2.colums = 7;
        previewImages2.add(previewImage2);
        videoInfo2.previewImagesList = previewImages2;
        videoInfos.add(videoInfo2);

//        exoPlayerManager.addVideoDatas(videoInfos);
        EventLogger eventLogger = new EventLogger(exoPlayerView);
        exoPlayerManager.addVideoDatasAndPreviewImages(videoInfos, eventLogger, 0, 0);

    }

    @Override
    protected void onStart() {
        super.onStart();
        exoPlayerManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        exoPlayerManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayerManager.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        exoPlayerManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayerManager.destroy();
    }

    private void requestRxPermissions(String... permissions) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(permissions).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(@NonNull Boolean granted) throws Exception {
                if (granted) {
                    Toast.makeText(ExoPlayerDemoActivity.this, "已获取权限", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

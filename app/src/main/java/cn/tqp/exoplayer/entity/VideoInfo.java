package cn.tqp.exoplayer.entity;

import java.util.List;

import cn.tqp.exoplayer.entity.PreviewImage;

/**
 * Created by tangqipeng on 2018/1/25.
 */

public class VideoInfo {

    public String movieId;
    public String movieTitle;
    public String movieUrl;
    public String moviePreviewUrl;
    public int imageCount;
    public boolean isCache;
    public List<PreviewImage> previewImagesList;

}

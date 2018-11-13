package cn.tqp.exoplayer.exoplayerui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.tqp.exoplayer.R;
import cn.tqp.exoplayer.manager.ExoPlayerSoundManager;

/**
 * Created by tangqipeng on 2018/1/29.
 */

public class ExoPlayerLightAndSoundView extends LinearLayout {

    public static final int LIGHT_TYPE = 0;
    public static final int SOUND_TYPE = 1;

    private Context mContext;
    private ImageView imageIcon;
    private TextView txtPercentage;

    private int mType;

    public ExoPlayerLightAndSoundView(Context context) {
        this(context, null);
    }

    public ExoPlayerLightAndSoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.exoplayer_light_view, this);
        imageIcon = (ImageView) findViewById(R.id.image_icon);
        txtPercentage = (TextView) findViewById(R.id.txt_percentage);
    }

    public void setIconType(int type){
        this.mType = type;
        if (type == LIGHT_TYPE){
            imageIcon.setImageResource(R.mipmap.gestrue_brightness);
        }else{
            if (ExoPlayerSoundManager.getCurrSoundVolume(mContext) <= 0){
                imageIcon.setImageResource(R.mipmap.play_ctrl_sound_gestrue_silent);
            }else{
                imageIcon.setImageResource(R.mipmap.play_ctrl_sound_gestrue);
            }
        }
    }

    public void setLightPercentage(float percentage){
        if (this.mType == SOUND_TYPE){
            if (percentage <= 0){
                imageIcon.setImageResource(R.mipmap.play_ctrl_sound_gestrue_silent);
            }else{
                imageIcon.setImageResource(R.mipmap.play_ctrl_sound_gestrue);
            }
            int maxVolume = ExoPlayerSoundManager.getMaxSoundVolume(mContext);
            float currPercent = (float) percentage / maxVolume;
            if (currPercent > 100) {
                currPercent = 100;
            }
            txtPercentage.setText(String.format("%.0f%%", currPercent * 100));
        }else{
            if (percentage >= 100){
                percentage = 99;
            }
            txtPercentage.setText(String.format("%.0f%%", percentage * 100));
        }
    }


}

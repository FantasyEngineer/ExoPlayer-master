package cn.tqp.exoplayer.exoplayerui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.util.Util;

import java.util.Formatter;
import java.util.Locale;

import cn.tqp.exoplayer.R;

/**
 * Created by tangqipeng on 2018/2/22.
 */

public class ExoPlayerSeekView extends LinearLayout {

    private ImageView mImageView;
    private TextView mTxtTime;
    private long oldPosition;
    private StringBuilder formatBuilder;
    private Formatter formatter;

    public ExoPlayerSeekView(Context context) {
        this(context, null);
    }

    public ExoPlayerSeekView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        setBackgroundResource(R.drawable.translucence_round_bg);
        formatBuilder = new StringBuilder();

        formatter = new Formatter(formatBuilder, Locale.getDefault());

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 5, 20, 5);
        addView(linearLayout, layoutParams);

        mImageView = new ImageView(context);
        LayoutParams imageParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        imageParams.setMargins(0, 0, 15, 0);
        linearLayout.addView(mImageView, imageParams);
        mTxtTime = new TextView(context);
        linearLayout.addView(mTxtTime);
    }

    public void setIconAndPosition(long position){
        if (position >= oldPosition){
            mImageView.setImageResource(R.mipmap.icon_state_front);
        }else{
            mImageView.setImageResource(R.mipmap.icon_state_back);
        }
        oldPosition = position;
        mTxtTime.setText(Util.getStringForTime(formatBuilder, formatter, position));
    }

}

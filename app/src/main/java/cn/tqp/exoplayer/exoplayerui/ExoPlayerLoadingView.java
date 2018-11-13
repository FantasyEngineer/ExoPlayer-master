package cn.tqp.exoplayer.exoplayerui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import cn.tqp.exoplayer.R;

/**
 * Created by tangqipeng on 2018/1/29.
 */

public class ExoPlayerLoadingView extends AppCompatImageView {

    private AnimationDrawable anim;

    public ExoPlayerLoadingView(Context context) {
        this(context, null);
    }

    public ExoPlayerLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.loading_animation);
        anim = (AnimationDrawable) this.getBackground();
    }

    public void showLoading() {
        setVisibility(VISIBLE);
        anim.start();
    }

    public void hideLoading() {
        setVisibility(GONE);
        anim.stop();
    }

}

package com.example.animatedbottombar;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class DummyAnimatedContent implements AnimatedBottomBar.AnimatedContent {
    @Override
    public View getView() {
        return _view;
    }

    @Override
    public void start(int offset) {
        if (_view != null) {
            _animator.setCurrentPlayTime(offset);
            _animator.start();
        }
    }

    @Override
    public void stop() {
        if (_view != null) {
            _animator.cancel();

            // ???
            _view.setScaleX(1.0f);
            _view.setScaleY(1.0f);
            _view.setRotation(0.0f);
        }
    }

    public void setView(View view) {
        _view = view;
    }

    private View _view;
    private ValueAnimator _animator = ValueAnimator.ofFloat(0.0f, 1.0f);

    {
        _animator.setDuration(1000);
        _animator.setInterpolator(new LinearInterpolator());
        _animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float v = (Float)_animator.getAnimatedValue();
                _view.setScaleX(v);
                _view.setScaleY(v);
                _view.setRotation(v * 360.0f);
            }
        });
    }
}

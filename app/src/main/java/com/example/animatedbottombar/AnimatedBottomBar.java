package com.example.animatedbottombar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

public class AnimatedBottomBar extends ViewGroup {
    private Config _config = new Config();
    private float _width;
    private Bubble _bubble;
    private Band _band;
    private ItemButton[] _buttons;
    private int _selection0 = -1;
    private int _selection = -1;
    private ValueAnimator _baseAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
    private boolean[] _buttonHidden;
    private float _lastTx;

    private static final float FACTOR0 = 3.0f;
    private static final float FACTOR1 = 1.5f;

    {
        _baseAnimator.setInterpolator(new LinearInterpolator());
        _baseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateAnimation();

                float v = getBaseAnimationFraction();
                float m = 1.0f - (float) Math.pow(0.5f, 1.0f / FACTOR0);
                if (v > m) {
                    _bubble.setSelectionAnimated(_selection, (int) (_config.baseAnimationDuration * (v - m)));
                }
            }
        });
    }

    public AnimatedBottomBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        _bubble = new Bubble(context);
        _band = new Band(context);

        _config.apply();
    }

    public Config getConfig() {
        return _config;
    }

    public void setContents(ItemButton[] items, AnimatedContent[] animations) {
        if (items == null || animations == null || items.length != animations.length) {
            throw new IllegalArgumentException();
        }
        _buttons = items;
        _bubble.setContents(animations);
        removeAllViews();
        addView(_bubble);
        addView(_band);
        for (ItemButton i : items) {
            i.setDY(_config.itemsDY);
            i.setAnimationDuration(_config.itemsAnimationDuration);
            addView(i.getButton());
        }
        for (int i = 0; i < items.length; ++i) {
            final int id = i;
            items[i].getButton().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectonAnimated(id);
                }
            });
        }
        _buttonHidden = new boolean[_buttons.length];
        requestLayout();

        _selection = -1;
    }

    public void setSelection(int selection) {
        if (_buttons == null) {
            throw new IllegalStateException();
        }
        if (selection < 0 || selection >= _buttons.length) {
            throw new IllegalArgumentException();
        }
        if (selection != _selection) {
            _baseAnimator.cancel();
            if (_selection >= 0 && _selection < _buttons.length) {
                _buttonHidden[_selection] = false;
            }
            _buttonHidden[selection] = true;
            _selection = selection;
            _bubble.setSelection(selection);

            updateAnimation();
        }
    }

    public void setSelectonAnimated(int selection) {
        if (_buttons == null) {
            throw new IllegalStateException();
        }
        if (selection < 0 || selection >= _buttons.length) {
            throw new IllegalArgumentException();
        }
        if (selection != _selection) {
            _baseAnimator.cancel();
            _selection0 = _selection;
            _selection = selection;
            _baseAnimator.start();
        }
    }

    public void stopAnimation() {
        _baseAnimator.cancel();
        updateAnimation();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        _width = (float) (r - l);

        int bs = _config.bubbleSize;
        int bl = -bs / 2;
        int bt = _config.topLineY + _config.bubbleY - bs / 2;

        _bubble.layout(bl, bt, bl + bs, bt + bs);
        _band.layout(0, _config.topLineY, r - l, b - t);

        float width = _width - _config.itemsMarginH * 2.0f;

        for (int i = 0; i < _buttons.length; ++i) {
            _buttons[i].getButton().layout((int) (width * i / _buttons.length + _config.itemsMarginH), _config.topLineY + _config.itemsMarginTop, (int) (width * (i + 1) / _buttons.length + _config.itemsMarginH), _config.topLineY + _config.itemsMarginTop + _config.itemsHeight);
        }

        updateAnimation();
    }

    private void updateAnimation() {
        float tx;
        float ty = 0.0f;
        float vy = 0.0f;

        if (_baseAnimator.isRunning()) {
            float t = getBaseAnimationFraction();

            float x0 = getSelectionX(_selection0);
            float x1 = getSelectionX(_selection);

            {
                float v0 = (float)Math.pow(t, FACTOR1);
                float v1 = 1.0f - v0;

                tx = (1.0f - v1 * v1) * (x1 - x0) + x0;
            }

            float y0 = (float) _config.bubbleY;
            float y1 = (float) _config.bubbleY1;

            {
                float v2 = (1.0f - (float) Math.pow(1.0f - t, FACTOR0)) * 2.0f - 1.0f;

                vy = (-v2 * v2 + 1.0f);
                ty = vy * (y1 - y0);
            }

            float bw = (_width - 2.0f * _config.itemsMarginH) / _buttons.length;

            if (x1 > x0) {
                int begin = (int) ((_lastTx - (_config.itemsMarginH + bw * (0.5f - _config.itemHideDistance1))) / bw) + 1;
                int end = (int) ((tx - (_config.itemsMarginH + bw * (0.5f - _config.itemHideDistance1))) / bw);
                for (int i = begin; i <= end; ++i) {
                    _buttonHidden[i] = true;
                }
            } else {
                int begin = (int) ((_lastTx - (_config.itemsMarginH + bw * (0.5f - _config.itemHideDistance1))) / bw) - 1;
                int end = (int) ((tx - (_config.itemsMarginH + bw * (0.5f - _config.itemHideDistance1))) / bw);
                for (int i = begin; i >= end; --i) {
                    _buttonHidden[i] = true;
                }
            }

            for (int i = 0; i < _buttons.length; ++i) {
                float x = getSelectionX(i);
                float d = (tx - x) / bw;
                float ad = Math.abs(d);

                if (x1 > x0 && d >= _config.itemHideDistance1 || x1 < x0 && d <= -_config.itemHideDistance1) {
                    if (_buttonHidden[i]) {
                        float _x = x1 > x0 ? x + _config.itemHideDistance1 * bw : x - _config.itemHideDistance1 * bw;
                        float _t1 = 1.0f - (float)Math.sqrt(1.0f - (_x - x0) / (x1 - x0));
                        float _t0 = (float)Math.pow(_t1, 1.0f / FACTOR1);
                        _buttons[i].showAnimated((int)((t - _t0) * _config.baseAnimationDuration));
                        _buttonHidden[i] = false;
                    }
                }

                if (ad < _config.itemHideDistance1) {
                    _buttons[i].setAlpha(0.0f);
                } else if (ad > _config.itemHideDistance0) {
                    _buttons[i].setAlpha(1.0f);
                } else {
                    _buttons[i].setAlpha(Math.max((ad - _config.itemHideDistance1) / (_config.itemHideDistance0 - _config.itemHideDistance1), 0.0f));
                }
            }
        } else {
            tx = getSelectionX(_selection);

            if (_buttons != null) {
                for (int i = 0; i < _buttons.length; ++i) {
                    _buttons[i].setAlpha(i == _selection ? 0.0f : 1.0f);
                }
            }
        }

        _lastTx = tx;

        _bubble.setTranslationX(tx);
        _bubble.setTranslationY(ty);

        _band.setFractionAndX(vy, tx);
    }

    private float getSelectionX(int selection) {
        if (_buttons != null) {
            return (_width - 2.0f * _config.itemsMarginH) / _buttons.length * (0.5f + selection) + _config.itemsMarginH;
        } else {
            return 0.0f;
        }
    }

    private float getBaseAnimationFraction() {
        return (Float) _baseAnimator.getAnimatedValue();
    }

    public interface AnimatedContent {
        View getView();

        void start(int offset);

        void stop();
    }

    private class Band extends View {
        private float _fraction;
        private float _x;
        private Paint _paint = new Paint();
        private Path _path = new Path();

        {
            _paint.setColor(0xFFFFFFFF);
            _paint.setStyle(Paint.Style.FILL);
        }

        public Band(Context context) {
            super(context);
        }

        public void setFractionAndX(float fraction, float x) {
            _fraction = fraction;
            _x = x;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            _path.reset();

            float width = _config.curveWidth0 + _fraction * (_config.curveWidth1 - _config.curveWidth0);
            float depth = _config.curveDepth0 + _fraction * (_config.curveDepth1 - _config.curveDepth0);
            float controlA = _config.curveControlA0 + _fraction * (_config.curveControlA1 - _config.curveControlA0);
            float controlB = _config.curveControlB0 + _fraction * (_config.curveControlB1 - _config.curveControlB0);

            float cl = _x - width * 0.5f;
            float cr = _x + width * 0.5f;

            float left = Math.min(cl, 0.0f);
            float right = Math.max(cr, (float) getWidth());
            float bottom = (float) getHeight();

            _path.moveTo(cl, 0.0f);
            _path.cubicTo(cl + controlA, 0.0f, _x - controlB, depth, _x, depth);
            _path.cubicTo(_x + controlB, depth, cr - controlA, 0.0f, cr, 0.0f);
            if (right != cr) {
                _path.lineTo(right, 0.0f);
            }
            _path.lineTo(right, bottom);
            _path.lineTo(left, bottom);
            _path.lineTo(left, 0.0f);
            if (left != cl) {
                _path.lineTo(cl, 0.0f);
            }

            canvas.drawPath(_path, _paint);
        }
    }

    private class Bubble extends ViewGroup {
        private AnimatedContent[] _contents;
        private int _selection = -1;

        public Bubble(Context context) {
            super(context);

            ShapeDrawable bg = new ShapeDrawable(new OvalShape());
            bg.getPaint().setColor(0xFFFFFFFF);

            setBackground(bg);
        }

        public void setContents(AnimatedContent[] contents) {
            if (contents.length == 0) {
                throw new IllegalArgumentException();
            }
            _contents = contents;

            removeAllViews();

            for (int i = 0; i < _contents.length; ++i) {
                View v = _contents[i].getView();
                addView(v);
                v.setVisibility(INVISIBLE);
            }

            requestLayout();

            _selection = -1;
        }

        public void setSelection(int selection) {
            if (_contents == null) {
                throw new IllegalStateException();
            }
            if (selection != _selection) {
                if (selection < 0 || selection >= _contents.length) {
                    throw new IllegalArgumentException();
                }
                if (_selection >= 0 && _selection < _contents.length) {
                    _contents[_selection].getView().setVisibility(INVISIBLE);
                }
                _contents[selection].getView().setVisibility(VISIBLE);
                _selection = selection;
            }
        }

        public void setSelectionAnimated(int selection, int animationOffset) {
            if (_contents == null) {
                throw new IllegalStateException();
            }
            if (selection < 0 || selection >= _contents.length) {
                throw new IllegalArgumentException();
            }
            if (selection != _selection) {
                if (_selection >= 0 && _selection < _contents.length) {
                    _contents[_selection].stop();
                    _contents[_selection].getView().setVisibility(INVISIBLE);
                }
                _contents[selection].getView().setVisibility(VISIBLE);
                _contents[selection].start(animationOffset);
                _selection = selection;
            }
        }

        public void cancelAnimation() {
            if (_contents != null && _selection >= 0 && _selection < _contents.length) {
                _contents[_selection].stop();
            }
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (_contents != null) {
                int w = r - l;
                int h = b - t;
                for (int i = 0; i < _contents.length; ++i) {
                    View v = _contents[i].getView();
                    int vl = (w - _config.bubbleContentSize) / 2;
                    int vt = (h - _config.bubbleContentSize) / 2;

                    v.layout(vl, vt, vl + _config.bubbleContentSize, vt + _config.bubbleContentSize);
                }
            }
        }
    }

    public static class ItemButton {
        private View _button;
        private float _alpha = 1.0f;
        private float _animatedAlpha = 1.0f;
        private ValueAnimator _baseAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        private float _dy;

        {
            _baseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateButton();
                }
            });
        }

        public void setDY(float dy) {
            _dy = dy;
        }

        public void setAnimationDuration(int duration) {
            _baseAnimator.setDuration(duration);
        }

        public View getButton() {
            return _button;
        }

        public void setButton(View button) {
            _button = button;
        }

        public void setAlpha(float alpha) {
            _alpha = alpha;
            updateAlpha();
        }

        public void showAnimated(int offset) {
            if (!_baseAnimator.isRunning()) {
                _animatedAlpha = 0.0f;
                _baseAnimator.setCurrentPlayTime(offset);
                _baseAnimator.start();
            }
        }

        public void stopAnimation() {
            _baseAnimator.cancel();
            updateButton();
        }

        private void updateAlpha() {
            _button.setAlpha(_alpha < _animatedAlpha ? _alpha : _animatedAlpha);
        }

        private void updateButton() {
            if (_baseAnimator.isRunning()) {
                float t = getBaseAnimatorValue();
                float v = 1.0f - t;
                v = 1.0f - v * v;

                _animatedAlpha = v;
                _button.setTranslationY(_dy * (1.0f - v));
            } else {
                _animatedAlpha = 1.0f;
                _button.setTranslationY(0.0f);
            }
            updateAlpha();
        }

        private float getBaseAnimatorValue() {
            return (Float) _baseAnimator.getAnimatedValue();
        }
    }

    public class Config {
        public int topLineY = 100;

        public int bubbleSize = 175;
        public int bubbleContentSize = 80;
        public int bubbleY = 10;
        public int bubbleY1 = 165;

        public int itemsMarginH = 0;
        public int itemsMarginTop = 25;
        public int itemsHeight = 80;

        public float itemHideDistance0 = 1.0f;
        public float itemHideDistance1 = 0.75f;
        public int itemsAnimationDuration = 333;

        public int itemsDY = 30;

        public float curveWidth0 = 340.0f;
        public float curveWidth1 = 180.0f;
        public float curveDepth0 = 120.0f;
        public float curveDepth1 = 75.0f;
        public float curveControlA0 = 80.0f;
        public float curveControlA1 = 40.0f;
        public float curveControlB0 = 115.0f;
        public float curveControlB1 = 50.0f;

        public int baseAnimationDuration = 333;

        public void apply() {
            _baseAnimator.setDuration(baseAnimationDuration);

            if (_buttons != null) {
                for (ItemButton b : _buttons) {
                    b.setDY(itemsDY);
                    b.setAnimationDuration(itemsAnimationDuration);
                }
            }

            requestLayout();
        }
    }
}

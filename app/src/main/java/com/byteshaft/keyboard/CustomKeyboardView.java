package com.byteshaft.keyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.inputmethodservice.*;
import android.inputmethodservice.Keyboard;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.util.List;

public class CustomKeyboardView extends KeyboardView implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Context mContext;
    private Paint mPaint;
    private SharedPreferences mPreferences;
    private String mTextColor;
    private String mButtonColor;
    private String mBackgroundColor;
    private String mButtonPressedColor;

    private final String COLOR_WHITE = "#FFFFFF";
    private final String COLOR_BLACK = "#000000";
    private final String COLOR_LGREY = "#a8a8a8";
    private final String COLOR_DGREY = "#333333";

    public CustomKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
     }

    @Override
    public void onDraw(Canvas canvas) {
        mTextColor = mPreferences.getString("textColor", COLOR_WHITE);
        mButtonColor = mPreferences.getString("buttonColor", COLOR_DGREY);
        mBackgroundColor = mPreferences.getString("backgroundColor", COLOR_BLACK);
        mButtonPressedColor = mPreferences.getString("popupColor", COLOR_LGREY);

        if (!mTextColor.startsWith("#")) {
            mTextColor = "#" + mTextColor;
        }
        if (!mButtonColor.startsWith("#")) {
            mButtonColor = "#" + mButtonColor;
        }
        if (!mBackgroundColor.startsWith("#")) {
            mBackgroundColor = "#" + mBackgroundColor;
        }
        if (!mButtonPressedColor.startsWith("#")) {
            mButtonPressedColor = "#" + mButtonPressedColor;
        }

        validateSavedColorCode(mTextColor, "textColor");
        validateSavedColorCode(mButtonColor, "buttonColor");
        validateSavedColorCode(mBackgroundColor, "backgroundColor");
        validateSavedColorCode(mButtonPressedColor, "popupColor");

        drawKeyboardBackground(canvas, mBackgroundColor);

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            if (key.label != null) {
                drawKey(canvas, key, mButtonColor, mBackgroundColor, mTextColor);
            }
            if (key.pressed) {
                drawKey(canvas, key, mButtonPressedColor, mBackgroundColor, mTextColor);
            }
        }
    }

    float getDensityPixels(int pixels) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics());
    }

    private void drawKeyboardBackground(Canvas canvas, String colour) {
        ShapeDrawable background = new ShapeDrawable(new RectShape());
        background.getPaint().setColor(Color.parseColor(colour));
        background.setBounds((int) getX(), (int) getY(), (int) getX() + getWidth(), (int) getY() + getHeight());
        background.draw(canvas);
    }

    private void drawKey(Canvas canvas, Keyboard.Key key, String color, String strokeColor, String textColor) {
        int fontValue = 40;
        int maxTextSize = (int) getDensityPixels(fontValue);
        Rect keyRectangle = new Rect(key.x, key.y, key.x + key.width, key.y + key.height);

        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor(color));
        canvas.drawRect(keyRectangle, mPaint);

        mPaint.setColor(Color.parseColor(strokeColor));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(getDensityPixels(5));
        canvas.drawRect(keyRectangle, mPaint);

        mPaint.setColor(Color.parseColor(textColor));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(maxTextSize);
        mPaint.setStyle(Paint.Style.FILL);

        Rect bounds = new Rect();
        mPaint.getTextBounds(key.label.toString(), 0, key.label.length(), bounds);

        if (isButtonPortrait(key)) {
            if (key.label.toString().equals("←")) {
                mPaint.setTextSize(getDensityPixels(fontValue) * 2.0f);
                while (bounds.height() > (keyRectangle.height() / 10) * 6 || bounds.width() >= (keyRectangle.width() / 10) * 9) {
                    fontValue -= 1;
                    mPaint.setTextSize(getDensityPixels(fontValue) * 2.0f);
                    mPaint.getTextBounds(key.label.toString(), 0, key.label.length(), bounds);
                }
            } else {
                while (bounds.height() > (keyRectangle.height() / 10) * 3 || bounds.width() >= (keyRectangle.width() / 10) * 9) {
                    fontValue -= 1;
                    mPaint.setTextSize(getDensityPixels(fontValue));
                    mPaint.getTextBounds(key.label.toString(), 0, key.label.length(), bounds);
                }
            }
        } else {
            if (key.label.toString().equals("←")) {
                mPaint.setTextSize(getDensityPixels(fontValue) * 2.0f);
                while (bounds.height() > (keyRectangle.height() / 10) * 4 || bounds.width() >= (keyRectangle.width() / 10) * 4) {
                    fontValue -= 1;
                    mPaint.setTextSize(getDensityPixels(fontValue) * 2.0f);
                    mPaint.getTextBounds(key.label.toString(), 0, key.label.length(), bounds);
                }
            } else {
                while (bounds.height() > (keyRectangle.height() / 10) * 6 || bounds.width() >= (keyRectangle.width() / 10) * 9) {
                    fontValue -= 1;
                    mPaint.setTextSize(getDensityPixels(fontValue));
                    mPaint.getTextBounds(key.label.toString(), 0, key.label.length(), bounds);
                }
            }
        }

        if (key.label.toString().equals("←")) {
            canvas.drawText(key.label.toString(), keyRectangle.centerX(), keyRectangle.centerY() + (mPaint.descent() / 2 * getDensityPixels(1)), mPaint);
        } else {
            mPaint.setTextSize(getDensityPixels(fontValue));
            canvas.drawText(key.label.toString(), keyRectangle.centerX(), keyRectangle.centerY() + mPaint.descent() * 1.5f, mPaint);
        }
    }

    private void validateSavedColorCode(String code, String key) {
        try {
            ShapeDrawable shapeDrawable = new ShapeDrawable();
            shapeDrawable.getPaint().setColor(Color.parseColor(code));
        } catch (Exception e) {
            mPreferences.edit().putString(key, null).commit();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "textColor":
                mTextColor = mPreferences.getString("textColor", COLOR_WHITE);
                break;
            case "buttonColor":
                mButtonColor = mPreferences.getString("buttonColor", COLOR_DGREY);
                break;
            case "backgroundColor":
                mBackgroundColor = mPreferences.getString("backgroundColor", COLOR_BLACK);
                break;
            case "popupColor":
                mButtonPressedColor = mPreferences.getString("popupColor", COLOR_LGREY);
                break;
        }
    }

    private boolean isButtonPortrait(Keyboard.Key key) {
        return key.height > key.width;
    }
}

package com.liuh.customview2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

/**
 * Date: 2017/12/19 11:11
 * Description:自定义View画了一个包含图片和文字的控件
 * 涉及到自定义属性和控件测量,绘制的流程,,比第一个自定义控件练习稍显复杂些(内部有图片和文字)
 * canvas.drawRect(...)绘制矩形
 * canvas.drawText(...)绘制文字
 * canvas.drawBitmap(...)绘制图片
 */

public class CustomImageView extends View {

    private Bitmap mImage;

    private int mImageScale;

    private String mTitleText;

    private int mTitleTextColor;

    private int mTitleTextSize;

    private Paint mPaint;
    private Rect mTextBound;//文字区域
    private Rect mRect;//图片+文字区域

    private int mWidth, mHeight;//控件宽高

    private static final int IMAGE_SCALE_FITXY = 0;
    private static final int IMAGE_SCALE_CENTER = 1;


    public CustomImageView(Context context) {
        this(context, null);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomImageView, defStyleAttr, 0);

        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {

            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.CustomImageView_image:
                    mImage = BitmapFactory.decodeResource(getResources(), a.getResourceId(attr, 0));
                    break;
                case R.styleable.CustomImageView_imageScaleType:
                    mImageScale = a.getInt(attr, 0);
                    break;
                case R.styleable.CustomImageView_titleText:
                    mTitleText = a.getString(attr);
                    break;
                case R.styleable.CustomImageView_titleTextColor:
                    mTitleTextColor = a.getColor(attr, Color.BLACK);
                    break;
                case R.styleable.CustomImageView_titleTextSize:
                    mTitleTextSize = a.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics()));
                    break;
            }
        }
        a.recycle();

        mPaint = new Paint();
        mTextBound = new Rect();
        mRect = new Rect();

        mPaint.setTextSize(mTitleTextSize);
        //获取描绘字体的范围
        mPaint.getTextBounds(mTitleText, 0, mTitleText.length(), mTextBound);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        /**
         * 设置宽度
         */
        if (widthSpecMode == MeasureSpec.EXACTLY) {
            Log.e("xxxxx", "width...EXACTLY");
            mWidth = widthSpecSize;
        } else {
            //由图片决定的宽
            int desireByImg = getPaddingLeft() + getPaddingRight() + mImage.getWidth();
            //由文字决定的宽
            int desireByText = getPaddingLeft() + getPaddingRight() + mTextBound.width();
            if (widthSpecMode == MeasureSpec.AT_MOST) {
                //wrap_content
                Log.e("xxxxx", "AT_MOST");
                int desire = Math.max(desireByImg, desireByText);
                mWidth = Math.min(desire, widthSpecSize);
            }
        }

        if (heightSpecMode == MeasureSpec.EXACTLY) {
            Log.e("xxxxx", "height...EXACTLY");
            mHeight = heightSpecSize;
        } else {
            int desire = getPaddingTop() + getPaddingBottom() + mImage.getHeight() + mTextBound.height();
            if (heightSpecMode == MeasureSpec.AT_MOST) {
                //wrap_content
                Log.e("xxxxx", "height...AT_MOST");
                mHeight = Math.min(desire, heightSpecSize);
            }
        }
        Log.e("xxxxxxx", "mWidth:" + mWidth + "....mHeight:" + mHeight);
        setMeasuredDimension(mWidth, mHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        /**绘制边框**/
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.CYAN);
        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);

        mRect.left = getPaddingLeft();
        mRect.right = mWidth - getPaddingRight();
        mRect.top = getPaddingTop();
        mRect.bottom = mHeight - getPaddingBottom();

        mPaint.setColor(mTitleTextColor);
        mPaint.setStyle(Paint.Style.FILL);

        /**当设置的文字的宽度大于当前控件宽度时,将文字改为xxx...**/
        if (mTextBound.width() > mWidth) {
            TextPaint paint = new TextPaint(mPaint);
            String msg = TextUtils.ellipsize(mTitleText, paint, (float) mWidth - getPaddingLeft() - getPaddingRight(),
                    TextUtils.TruncateAt.END).toString();
            canvas.drawText(msg, getPaddingLeft(), mHeight - getPaddingBottom(), mPaint);
        } else {
            //文字宽度小于当前控件宽度时,将文字居中显示
            canvas.drawText(mTitleText, mWidth / 2 - mTextBound.width() * 1.0f / 2, mHeight - getPaddingBottom(), mPaint);
        }

        /**绘制图片**/
        //减去使用掉的区块
        mRect.bottom -= mTextBound.height();
        if (mImageScale == IMAGE_SCALE_FITXY) {
            canvas.drawBitmap(mImage, null, mRect, mPaint);
        } else {
            //计算居中的矩形范围
            mRect.left = mWidth / 2 - mImage.getWidth() / 2;
            mRect.right = mWidth / 2 + mImage.getWidth() / 2;
            mRect.top = (mHeight - mTextBound.height()) / 2 - mImage.getHeight() / 2;
            mRect.bottom = (mHeight - mTextBound.height()) / 2 + mImage.getHeight() / 2;

            canvas.drawBitmap(mImage, null, mRect, mPaint);
        }

    }
}

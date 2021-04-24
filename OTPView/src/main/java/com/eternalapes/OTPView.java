package com.eternalapes;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class OTPView extends AppCompatEditText {
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect textRect = new Rect();

    private String pinString;
    private float viewStrokeRadius;

    private int viewType;
    private int viewCount;
    private int viewFillColor;
    private int viewStrokeColor;

    private float viewWidth;
    private float viewSpace;
    private float viewHeight;
    private float viewStrokeWidth;

    public OTPView(@NonNull Context context) {
        this(context, null);
    }

    public OTPView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.viewStyle);
    }

    public OTPView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OTPView, defStyleAttr, 0);

        viewCount = typedArray.getInt(R.styleable.OTPView_viewCount, 4);
        viewType = typedArray.getInt(R.styleable.OTPView_viewType, ViewType.BORDER.index);

        viewWidth = typedArray.getDimension(R.styleable.OTPView_viewWidth, dpToPx(40));
        viewSpace = typedArray.getDimension(R.styleable.OTPView_viewSpace, dpToPx(10));
        viewHeight = typedArray.getDimension(R.styleable.OTPView_viewHeight, dpToPx(40));
        viewStrokeWidth = typedArray.getDimension(R.styleable.OTPView_viewStrokeWidth, dpToPx(2));
        viewStrokeRadius = typedArray.getDimension(R.styleable.OTPView_viewStrokeRadius, dpToPx(4));

        viewStrokeColor = typedArray.getColor(R.styleable.OTPView_viewStrokeColor, Color.BLACK);
        viewFillColor = typedArray.getColor(R.styleable.OTPView_viewFillColor, Color.TRANSPARENT);

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedWidth = 0;
        int calculatedHeight = 0;

        if (widthSpecMode == MeasureSpec.EXACTLY) {
            calculatedWidth = widthSpecSize;
        } else {
            calculatedWidth += (viewWidth + viewSpace) * viewCount - viewSpace;
            calculatedWidth += getPaddingStart() + getPaddingEnd() + (viewStrokeWidth / 2);
        }

        if (heightSpecMode == MeasureSpec.EXACTLY) {
            calculatedHeight = heightSpecSize;
        } else {
            calculatedHeight += viewHeight + getPaddingTop() + getPaddingBottom() + (viewStrokeWidth / 2);
        }

        setMeasuredDimension(calculatedWidth, calculatedHeight);
    }

    @Override
    public void setBackground(Drawable background) {
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        pinString = text.toString().replace(" ", "");
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
    }

    @Override
    public boolean isSuggestionsEnabled() {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initData();
        float lastPointPoint = 0;
        float strokeCorrection = viewStrokeWidth / 2;

        if(viewType == ViewType.BORDER.index) {
            for (int index = 0; index < viewCount; index++) {
                drawBorderedView(canvas, index, lastPointPoint, strokeCorrection);
                lastPointPoint += viewWidth + viewSpace;
            }
        } else if(viewType == ViewType.UNDERLINE.index) {
            for (int index = 0; index < viewCount; index++) {
                drawLineView(canvas, index, lastPointPoint, strokeCorrection);
                lastPointPoint += viewWidth + viewSpace;
            }
        }
    }

    private void drawBorderedView(Canvas canvas, int index, float lastPointPoint, float strokeCorrection) {
        float start = lastPointPoint + strokeCorrection;
        float end = start + viewWidth - strokeCorrection;

        canvas.drawRoundRect(start, strokeCorrection, end, viewHeight - strokeCorrection, viewStrokeRadius, viewStrokeRadius, innerPaint);

        if(viewStrokeWidth > 0)
            canvas.drawRoundRect(start, strokeCorrection, end, viewHeight - strokeCorrection, viewStrokeRadius, viewStrokeRadius, outerPaint);

        if(pinString.length() > index) drawText(canvas, start, index);
    }

    private void drawLineView(Canvas canvas, int index, float lastPointPoint, float strokeCorrection) {
        float start = lastPointPoint + strokeCorrection;
        float end = start + viewWidth - strokeCorrection;

        canvas.drawRoundRect(start, viewHeight - viewStrokeWidth, end, viewHeight, viewStrokeWidth / 2, viewStrokeWidth / 2, linePaint);

        if(pinString.length() > index) drawText(canvas, start, index);
    }

    private void drawText(Canvas canvas, float start, int index) {
        Paint textPaint = getPaint();
        textPaint.getTextBounds(String.valueOf(pinString.charAt(index)), 0, 1, textRect);

        int textStart = (int) (start + (viewWidth / 2) - (Math.abs(textRect.width()) / 2));
        int textTop = (int) ((viewHeight / 2) + (Math.abs(textRect.height()) / 2));

        canvas.drawText(String.valueOf(pinString.charAt(index)), textStart, textTop, textPaint);
    }

    private void initData() {
        if (viewStrokeRadius > (Math.min(viewWidth, viewHeight) / 2))
            viewStrokeRadius = (Math.min(viewWidth, viewHeight) / 2);

        if(viewType == ViewType.BORDER.index) {
            innerPaint.setStyle(Paint.Style.FILL);
            innerPaint.setColor(viewFillColor);

            outerPaint.setStyle(Paint.Style.STROKE);
            outerPaint.setStrokeWidth(viewStrokeWidth);
            outerPaint.setColor(viewStrokeColor);
        } else if(viewType == ViewType.UNDERLINE.index) {
            linePaint.setStyle(Paint.Style.FILL);
            linePaint.setColor(viewStrokeColor);
        }

        setInputType(InputType.TYPE_CLASS_NUMBER);
        setFocusableInTouchMode(true);
        setCursorVisible(false);
        setMaxLength(viewCount);
    }

    private void setMaxLength(int maxLength) {
        setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(maxLength)
        });
    }

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    public void setViewType(ViewType viewType) {
        this.viewType = viewType.index;
    }

    public int getViewType() { return this.viewType; }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getViewCount() { return this.viewCount; }

    public void setViewFillColor(int color) {
        this.viewFillColor = color;
    }

    public int getViewFillColor() { return this.viewFillColor; }

    public void setViewStrokeColor(int color) {
        this.viewStrokeColor = color;
    }

    public int getViewStrokeColor() { return this.viewStrokeColor; }

    public void setViewWidth(float value) {
        this.viewWidth = value;
    }

    public float getViewWidth() { return this.viewWidth; }

    public void setViewSpace(float value) {
        this.viewSpace = value;
    }

    public float getViewSpace() { return this.viewSpace; }

    public void setViewHeight(float value) {
        this.viewHeight = value;
    }

    public float getViewHeight() { return this.viewHeight; }

    public void setViewStrokeWidth(float value) {
        this.viewStrokeWidth = value;
    }

    public float getViewStrokeWidth() { return this.viewStrokeWidth; }

    enum ViewType {
        BORDER(1),
        UNDERLINE(2);

        private final int index;

        ViewType(int index) {
            this.index = index;
        }
    }
}

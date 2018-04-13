package com.hhz.sign.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hhz.sign.LogT;

import java.util.ArrayList;
import java.util.Random;

public class VerificationCodeView extends View {
    public static final int defaultCodeCount = 4;
    public static final int defaultTextSize = 20;
    /**
     * 控件的宽度
     */
    private int mWidth;
    /**
     * 控件的高度
     */
    private int mHeight;
    /**
     * 验证码文本画笔
     */
    private Paint mTextPaint;
    // 文本画笔 /** * 干扰点坐标的集合 */
    private ArrayList<PointF> mPoints = new ArrayList<PointF>();
    //默认干扰点数
    private int pointCount = 50;
    private Random mRandom = new Random();
    /**
     * 干扰点画笔
     */
    private Paint mPointPaint;
    /**
     * 绘制贝塞尔曲线的路径集合
     */
    private ArrayList<Path> mPaths = new ArrayList<Path>();
    private int lineCount = 1;
    /**
     * 干扰线画笔
     */
    private Paint mPathPaint;
    /**
     * 验证码字符串
     */
    private String mCodeString;
    /**
     * 验证码的位数
     */
    private int mCodeCount = defaultCodeCount;
    /**
     * 验证码字符的大小
     */
    private float mTextSize = defaultTextSize;
    /**
     * 验证码字符串的显示宽度
     */
    private float mTextWidth;

    public VerificationCodeView(Context context, AttributeSet attrs,
                                int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public VerificationCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VerificationCodeView(Context context) {
        super(context);
        init(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = measureWidth(widthMeasureSpec);
        int measureHeight = measureHeight(heightMeasureSpec);
        // 设置当前View的大小，其实这个方法最终会调用，
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 初始化数据
        initData();
        int length = mCodeString.length();
        float charLength = mTextWidth / length;
        for (int i = 1; i <= length; i++) {
            int offsetDegree = mRandom.nextInt(15);
            // 这里只会产生0和1，如果是1那么正旋转正角度，否则旋转负角度
            offsetDegree = mRandom.nextInt(2) == 1 ? offsetDegree : -offsetDegree;
            canvas.save();
            canvas.rotate(offsetDegree, mWidth / 2, mHeight / 2);
            // 给画笔设置随机颜色
            mTextPaint.setARGB(255, mRandom.nextInt(200) + 20, mRandom.nextInt(200) + 20, mRandom.nextInt(200) + 20);
            canvas.drawText(String.valueOf(mCodeString.charAt(i - 1)), (i - 1) * charLength * 1.6f + 30, mHeight * 2 / 3f, mTextPaint);
            canvas.restore();
        }
//        /*是否显示干扰点*/
//        boolean isShowpoiinter = true;
//        if (isShowpoiinter) {
            // 产生干扰效果1 -- 干扰点
            for (PointF pointF : mPoints) {
                mPointPaint.setARGB(255, mRandom.nextInt(200) + 20, mRandom.nextInt(200) + 20, mRandom.nextInt(200) + 20);
                canvas.drawPoint(pointF.x, pointF.y, mPointPaint);
            }
//        }
//        /*是否显示干扰线*/
//        boolean isShowline = true;
//        if (isShowline) {// 产生干扰效果2 -- 干扰线
            for (Path path : mPaths) {
                mPathPaint.setARGB(255, mRandom.nextInt(200) + 20, mRandom.nextInt(200) + 20, mRandom.nextInt(200) + 20);
                canvas.drawPath(path, mPathPaint);
            }
//        }
    }

    private void initData() {
        // 获取控件的宽和高，此时已经测量完成
        mHeight = getHeight();
        mWidth = getWidth();
        mPoints.clear();
        // 生成干扰点坐标
        for (int i = 0; i < pointCount; i++) {
            PointF pointF = new PointF(mRandom.nextInt(mWidth) + 10, mRandom.nextInt(mHeight) + 10);
            mPoints.add(pointF);
        }
        mPaths.clear();
        // 生成干扰线坐标
        for (int i = 0; i < lineCount; i++) {
            Path path = new Path();
            int startX = mRandom.nextInt(mWidth / 3) + 10;
            int startY = mRandom.nextInt(mHeight / 3) + 10;
            int endX = mRandom.nextInt(mWidth / 2) + mWidth / 2 - 10;
            int endY = mRandom.nextInt(mHeight / 2) + mHeight / 2 - 10;
            // 不会进行绘制，只用于移动移动画笔。
            path.moveTo(startX, startY);
            // mPath.quadTo(x1, y1, x2, y2) (x1,y1) 为控制点，(x2,y2)为结束点。
            path.quadTo(Math.abs(endX - startX) / 2, Math.abs(endY - startY) / 2, endX, endY);
            mPaths.add(path);
        }
    }

    /**
     * 初始化一些数据
     */
    private void init(Context mcontext) {
        this.mTextSize = sp2px(mcontext, mTextSize);
        LogT.i("验证码字符的大小_mTextSize:" + mTextSize);
        // 生成随机数字和字母组合—4
        mCodeString = getCharAndNumr(mCodeCount);
        // 初始化文字画笔，线宽(画笔大小)为33px
        mTextPaint = new Paint();
        mTextPaint.setStrokeWidth(3);
        // 设置文字大小
        mTextPaint.setTextSize(mTextSize);
        // 初始化干扰点画笔
        mPointPaint = new Paint();
        mPointPaint.setStrokeWidth(3);
        // 设置线冒样式(断点处为圆形)，取值有Cap.ROUND(圆形线冒)、Cap.SQUARE(方形线冒)、Paint.Cap.BUTT(无线冒)
        mPointPaint.setStrokeCap(Paint.Cap.ROUND);
        // 初始化干扰线画笔
        mPathPaint = new Paint();
        mPathPaint.setStrokeWidth(3);
        mPathPaint.setColor(Color.GRAY);
        // 设置画笔样式为空心，Paint.Style.FILL :填充内部，Paint.Style.FILL_AND_STROKE ：填充内部和描边，Paint.Style.STROKE ：仅描边
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        // 取得验证码字符串显示的宽度值
        mTextWidth = mTextPaint.measureText(mCodeString);
    }

    private int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public void updateChar(String code) {
        this.mCodeString = code;
        // postInvalidate可以直接在线程中更新界面
        postInvalidate();
    }

    /**
     * java生成随机数字和字母组合 * @param length[生成随机数的长度] * @return
     */
    public static String getCharAndNumr(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            // 输出字母还是数字
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            // 字符串
            if ("char".equalsIgnoreCase(charOrNum)) {
                // 取得大写字母还是小写字母
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (choice + random.nextInt(26)));
            } else if ("num".equalsIgnoreCase(charOrNum)) {
                // 数字
                val.append(String.valueOf(random.nextInt(10)));
            }
        }
        return val.toString().toUpperCase();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 重新生成随机数字和字母组合
				/*mCodeString = getCharAndNumr(mCodeCount);
				invalidate(); */
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 测量宽度 * @param widthMeasureSpec
     */
    private int measureWidth(int widthMeasureSpec) {
        int result = (int) (mTextWidth * 1.8f);
        // 定义测量规则
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // 3种模式，EXACTLY（尺寸的值是多少，那么这个组件的长或宽就是多少），AT_MOST（能够给出的最大的空间），UNSPECIFIED（随便使用空间，不受限制）
        if (widthMode == MeasureSpec.EXACTLY) {
            // 精确测量模式，即布局文件中layout_width或layout_height一般为精确的值或match_parent
            result = widthSize;
            // 既然是精确模式，那么直接返回测量的宽度即可
        } else {
            if (widthMode == MeasureSpec.AT_MOST) { // 最大值模式，即布局文件中layout_width或layout_height一般为wrap_content
                result = Math.min(result, widthSize);
            }
        }
        return result;
    }

    /**
     * 测量高度 * @param heightMeasureSpec
     */
    private int measureHeight(int heightMeasureSpec) {
        int result = (int) (mTextWidth / 1.2f);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            // 精确测量模式，即布局文件中layout_width或layout_height一般为精确的值或match_parent
            result = heightSize;
            // 既然是精确模式，那么直接返回测量的宽度即可
        } else {
            if (heightMode == MeasureSpec.AT_MOST) {
                // 最大值模式，即布局文件中layout_width或layout_height一般为wrap_content
                result = Math.min(result, heightSize);
            }
        }
        return result;
    }

    public String getCodeString() {
        return mCodeString;
    }

    /**
     * 验证
     *
     * @param fromcode
     * @return
     */
    public boolean validateCode(String fromcode) {
        if (!TextUtils.isEmpty(fromcode)) {
            return this.mCodeString.equalsIgnoreCase(fromcode);
        }
        return false;
    }

    public void setpointCount(int count) {
        this.pointCount = count;
        invalidate();
    }

    public int getPointCount() {
        return this.pointCount;
    }

    public void setlineCount(int count) {
        this.lineCount = count;
        invalidate();
    }

    public int getLineCount() {
        return this.lineCount;
    }

    public int getmWidth() {
        return mWidth;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public Paint getmTextPaint() {
        return mTextPaint;
    }

    public void setmTextPaint(Paint mTextPaint) {
        this.mTextPaint = mTextPaint;
    }

    public ArrayList<PointF> getmPoints() {
        return mPoints;
    }

    public void setmPoints(ArrayList<PointF> mPoints) {
        this.mPoints = mPoints;
    }

    public Random getmRandom() {
        return mRandom;
    }

    public void setmRandom(Random mRandom) {
        this.mRandom = mRandom;
    }

    public Paint getmPointPaint() {
        return mPointPaint;
    }

    public void setmPointPaint(Paint mPointPaint) {
        this.mPointPaint = mPointPaint;
    }

    public ArrayList<Path> getmPaths() {
        return mPaths;
    }

    public void setmPaths(ArrayList<Path> mPaths) {
        this.mPaths = mPaths;
    }

    public Paint getmPathPaint() {
        return mPathPaint;
    }

    public void setmPathPaint(Paint mPathPaint) {
        this.mPathPaint = mPathPaint;
    }

    public String getmCodeString() {
        return mCodeString;
    }

    public void setmCodeString(String mCodeString) {
        this.mCodeString = mCodeString;
    }

    public int getmCodeCount() {
        return mCodeCount;
    }

    public void setmCodeCount(int mCodeCount) {
        this.mCodeCount = mCodeCount;
    }

    public float getmTextSize() {
        return mTextSize;
    }

    public void setmTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
    }

    public float getmTextWidth() {
        return mTextWidth;
    }

    public void setmTextWidth(float mTextWidth) {
        this.mTextWidth = mTextWidth;
    }


}

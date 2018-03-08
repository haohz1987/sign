package com.hhz.sign;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * View来实现签名
 * 
 * @author jmshuai
 * 
 */
public class SignatureView extends View {
	private static final String LOG_TAG = "SignatureView";
	private static final int SIGNATURE_LENGTH = 850;//签名长度设置
	// 画笔
	private Paint mPaint;
	// 记录当前的笔画所经过的点
	private List<Point> mPoints;
	// 记录前面的笔画
	private List<List<Point>> mStrokes = new ArrayList<List<Point>>(50);
	// 记录第一个手指对应的pointerId
	private int mPointerId;
	// Empty监听器
	private OnEmptyListener mEmptyListener;

	public SignatureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	public SignatureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	public SignatureView(Context context) {
		super(context);
		init();
	}
	/**
	 * 两点求距离
	 */
	public double getSqrt(double x, double x2, double y, double y2) {
		return Math.sqrt(Math.abs(x - x2) * Math.abs(x - x2) + Math.abs(y - y2) * Math.abs(y - y2));
	}
	/**
	 * 初始化
	 */
	private void init() {
		//force to use cache
		//http://stackoverflow.com/questions/2339429/android-view-getdrawingcache-returns-null-only-null/4618030#4618030
		//this.setDrawingCacheBackgroundColor(0xffff0000);
		this.setDrawingCacheEnabled(true);
		this.buildDrawingCache();
		mPaint = new Paint();
		mPaint.setColor(Color.RED);
		mPoints= null;
		mPointerId = -1;
	}
	/**
	 * 设置监听画布为空不为空
	 * 
	 * @param listener
	 */
	public void setOnEmptyListener(OnEmptyListener listener) {
		mEmptyListener = listener;
	}
	/**
	 * 清除笔画 方法：清除记录的笔画和点。并重新绘制
	 */
	public void clearText() {
		sqrt = 0d;
		if(null != mPoints) {
			mPoints.clear();
		}
		mStrokes.clear();
		invalidate();
		invokeEmptyListener(true);
	}
	/**
	 * 判断用户是否绘画
	 */
	public boolean isEmpty() {
//		Toast.makeText(getContext(), "长度:sqrt="+sqrt, 1).show();
		Log.i("HPSQRT", "isEmpty点的长度:sqrt="+sqrt);
		return mStrokes.isEmpty() || sqrt < SIGNATURE_LENGTH;
	}
	/**
	 * Set the paint's text size. This value must be > 0
	 * @param textSize set the paint's text size
	 * @return
	 */
	public boolean setTextSize(float textSize) {
		// 改变笔画的大小，并重新绘制。
		if(textSize > 0) {
			//mPaint.setTextSize(textSize);
			mPaint.setStrokeWidth(textSize);
			invalidate();
			return true;
		}
		return false;
	}
	/**
	 * Set the paint's color. Note that the color is an int containing alpha as well as r,g,b. 
	 * This 32bit value is not premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b. 
	 * See the Color class for more details.
	 * @param color The new color (including alpha) to set in the paint
	 */
	public void setTextColor(int color) {
		// 改变笔画的颜色，并重新绘制
		mPaint.setColor(color);
		invalidate();
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.save(Canvas.ALL_SAVE_FLAG);
//		HPLog.i(LOG_TAG, "onDraw" + "height:" + this.getHeight() + " width: " + this.getWidth());
		boolean hasDraw = false;
		// 画记录了的每一笔画。
		for(List<Point> stroke : mStrokes) {
			if(stroke.size() > 1) {
				Point pStart = stroke.get(0);
				Point pEnd;
				for(int pos = 1; pos < stroke.size(); pos++) {
					pEnd = stroke.get(pos);
					canvas.drawLine(pStart.x, pStart.y, pEnd.x, pEnd.y, mPaint);
					pStart = pEnd;
				}
				hasDraw = true;
			}
		}
		// 画，记录中的笔画。
		if(null != mPoints) {
			if(mPoints.size() > 1) {
				Point pStart = mPoints.get(0);
				Point pEnd;
				for(int pos = 1; pos < mPoints.size(); pos++) {
					pEnd = mPoints.get(pos);
					canvas.drawLine(pStart.x, pStart.y, pEnd.x, pEnd.y, mPaint);
					pStart = pEnd;
				}
				hasDraw = true;
			}
		}
		canvas.restore();
		this.invokeEmptyListener(!hasDraw);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.i(LOG_TAG, "ActionMask:"+event.getActionMasked());
		switch(event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: {
				if(null == mPoints) {
					mPoints = new ArrayList<Point>(30);
				} else {
					Log.e(LOG_TAG, "mPoints must be empty");
				}
				// 记录第一个手指pointerId.
				mPointerId = event.getPointerId(0);
				sx = event.getX();
				sy = event.getY();
				// 开始把触摸的点，记录到当前笔画的集合里面。
				mPoints.add(new Point((int)event.getX(),(int)event.getY()));
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				int pointerIndex = event.findPointerIndex(mPointerId);
				// 找到第一个手指的index，并取得这个手指的坐标，加入到当前笔画的集合里面。
				if(-1 != pointerIndex) {
					int endX = (int)event.getX(pointerIndex);
					int endY = (int)event.getY(pointerIndex);
					mPoints.add(new Point(endX,endY));
					sqrt += getSqrt(sx, endX, sy, endY);
					sx = endX;
					sy = endY;
					// 触发绘制操作。
					this.invalidate();
				}
				break;
			}
			case MotionEvent.ACTION_POINTER_DOWN: {
				int pointerIndex = event.getActionIndex();
				if(mPointerId == event.getPointerId(pointerIndex)) {
					Log.i(LOG_TAG, "Reused the same id");
				}
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: {
				int pointerIndex = event.findPointerIndex(mPointerId);
				Log.i(LOG_TAG, "pointerIndex:"+pointerIndex);
				if(-1 != pointerIndex && pointerIndex == event.getActionIndex()) {
					int endX = (int)event.getX(pointerIndex);
					int endY = (int)event.getY(pointerIndex);
					// 找到第一个手指的位置，加到points里面。因为该笔画已经结束。所以把points加到笔画里面。
					mPoints.add(new Point(endX,endY));
					mStrokes.add(mPoints);
					sqrt += getSqrt(sx, endX, sy, endY);
					sx = endX;
					sy = endY;
					mPoints = null ;
					mPointerId = -1;
					// 触发重绘
					this.invalidate();
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
			default: {
				int pointerIndex = event.findPointerIndex(mPointerId);
				if(-1 != pointerIndex && pointerIndex == event.getActionIndex()) {
					int endX = (int)event.getX(pointerIndex);
					int endY = (int)event.getY(pointerIndex);
					// 找到第一个手指的位置，加到points里面。因为该笔画已经结束。所以把points加到笔画里面。
					mPoints.add(new Point(endX,endY));
					mStrokes.add(mPoints);
					mPoints = null ;
					mPointerId = -1;
					sqrt += getSqrt(sx, endX, sy, endY);
					sx = endX;
					sy = endY;
					// 触发重绘
					this.invalidate();
				}
				break;
			}
		}
		return true;
	}
	
	private double sqrt = 0d,sx = 0d,sy = 0d;
	
	private void invokeEmptyListener(boolean isEmpty) {
		if(null != this.mEmptyListener) {
			this.mEmptyListener.onEmpty(isEmpty);
		}
	} 
	public interface OnEmptyListener {
		void onEmpty(boolean isEmpty);
	}
}

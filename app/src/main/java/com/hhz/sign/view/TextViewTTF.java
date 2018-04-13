package com.hhz.sign.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class TextViewTTF extends TextView {
    private static final String fontName="fonts/tranfsc.ttf";
	public TextViewTTF(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFontStly(context);
	}
	public TextViewTTF(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFontStly(context);
	}
	public TextViewTTF(Context context) {
		super(context);
		setFontStly(context);
	}
	
	
	private void setFontStly(Context context){
		try{
			Typeface typeface=Typeface.createFromAsset(context.getAssets(), fontName);
			this.setTypeface(typeface);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

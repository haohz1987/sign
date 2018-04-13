package com.hhz.sign.view;

import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;


/**
 * ActionBar 标题栏
 *
 * @author dlfeng
 */
public class ActionBar {
    //标题
    public String title;
    //左边图片
    public int leftResId = -1;
    //左边图片是否显示
    public boolean isLeftShow = false;
    //右边图片
    public Object rightView = null;
    //右边图片是否显示
    public boolean isRightShow = false;
    //左边按钮的点击事件
    public OnClickListener leftOnListener;

    public int bgGroudColor = -1;
    public int titleColor = -1;

    public LayoutParams lp = null;

    public ActionBar(String title, int leftResId, boolean isLeftShow,
                     Object rightView, boolean isRightShow) {
        super();
        this.title = title;
        this.leftResId = leftResId;
        this.isLeftShow = isLeftShow;
        this.rightView = rightView;
        this.isRightShow = isRightShow;
    }

    public ActionBar(String title, int leftResId, OnClickListener leftOnListener) {
        super();
        this.title = title;

        if (leftResId > 0) {
            this.leftResId = leftResId;
            this.isLeftShow = true;
        }
        this.leftOnListener = leftOnListener;
    }

    public ActionBar(String title, Object rightView) {
        super();
        this.title = title;
        if (rightView != null) {
            isRightShow = true;
            this.rightView = rightView;
        }
    }


    public ActionBar(String title, boolean isLeftShow,
                     OnClickListener leftOnListener) {
        super();
        this.title = title;
        this.isLeftShow = isLeftShow;
        this.leftOnListener = leftOnListener;
    }

    public ActionBar(String title, boolean isLeftShow) {
        super();
        this.title = title;
        this.isLeftShow = isLeftShow;
    }

    public ActionBar(String title) {
        super();
        this.title = title;
    }

    public ActionBar(String title, boolean isLeftShow, Object rightView) {
        super();
        this.title = title;
        this.isLeftShow = isLeftShow;
        if (rightView != null) {
            isRightShow = true;
            this.rightView = rightView;
        }
    }

    public ActionBar(String title, boolean isLeftShow, OnClickListener leftOnListener, Object rightView, LayoutParams rightLP) {
        super();
        this.title = title;
        this.isLeftShow = isLeftShow;
        this.leftOnListener = leftOnListener;
        if (rightView != null) {
            isRightShow = true;
            this.rightView = rightView;
        }
        this.lp = rightLP;
    }

    public ActionBar(String title, boolean isLeftShow, Object rightView, LayoutParams rightLP) {
        super();
        this.title = title;
        this.isLeftShow = isLeftShow;
        if (rightView != null) {
            isRightShow = true;
            this.rightView = rightView;
        }
        this.lp = rightLP;
    }

    public ActionBar(String title, Object rightView, LayoutParams rightLP) {
        super();
        this.title = title;
        if (rightView != null) {
            isRightShow = true;
            this.rightView = rightView;
        }
        this.lp = rightLP;
    }


    public ActionBar(String title, int bgGroudColor) {
        super();
        this.title = title;
        this.bgGroudColor = bgGroudColor;
    }

    public ActionBar(String title, int bgGroudColor, int titleColor) {
        super();
        this.title = title;
        this.bgGroudColor = bgGroudColor;
        this.titleColor = titleColor;
    }

    public ActionBar() {
        super();
    }


}

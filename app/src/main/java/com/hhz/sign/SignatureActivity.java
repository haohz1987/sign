package com.hhz.sign;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SignatureActivity extends AppCompatActivity implements View.OnClickListener{

    private SignatureView mSigView = null;
    private View mContainer = null;
    private View mSaveBtn = null;
    private TextView mHints = null;
    private TextView tfName = null;
    private TextView tfMoney = null;
    private CheckBox cbPhone = null;
    protected static CustomDialog dialog;
    public static int screenWidth = -1;
    public static int screenHeight = -1;
    private AbTitleBar myActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        mContainer=findViewById(R.id.sig_all);
        mSaveBtn = this.findViewById(R.id.btn_save);
        this.findViewById(R.id.btn_save).setOnClickListener(this);
        this.findViewById(R.id.btn_clear).setOnClickListener(this);
        cbPhone = (CheckBox) this.findViewById(R.id.cbPhone);
        mHints = (TextView) this.findViewById(R.id.tv_hints);
        tfName = (TextView) this.findViewById(R.id.tfName);
        tfMoney = (TextView) this.findViewById(R.id.tfMoney);
        cbPhone.setVisibility(View.GONE);
        tfName.setText("店铺名称");
        tfMoney.setText("100.00");
        mSigView = (SignatureView) this.findViewById(R.id.hpSigView);
        mSigView.setTextColor(Color.BLACK);
        mSigView.setTextSize(10);
        mSigView.setOnEmptyListener(new SignatureView.OnEmptyListener() {

            @Override
            public void onEmpty(boolean isEmpty) {
                if (isEmpty) {
                    mHints.setVisibility(View.VISIBLE);
                    mSaveBtn.setEnabled(false);
                } else {
                    mHints.setVisibility(View.GONE);
                    mSaveBtn.setEnabled(true);
                }
                // BUG: xiaomi2s 会导致整个界面花掉。这里强制整个界面重绘。
                mContainer.invalidate();
            }
        });

        myActionBar = (AbTitleBar) this.findViewById(R.id.top_actionbar);
        if (isShownTopBar()) {
            if (myActionBar != null) {
                myActionBar.ininTitleBar(this);
                myActionBar.refreshActionBar(getActivityActionBar());
                initActionBarAndTabHeight();
                if (!isVisiableTopBar()) {
                    myActionBar.setVisibility(View.GONE);
                }
            } else {
                throw new RuntimeException("please use top_bar.xml");
            }
        }

    }
    protected boolean isVisiableTopBar() {
        return false;
    }
    /**
     * 初始化ActionBar 和 Tab 的高度
     */
    public void initActionBarAndTabHeight() {
        TypedValue tv = new TypedValue();
        if (this.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, this.getResources().getDisplayMetrics());
            android.view.ViewGroup.LayoutParams params = myActionBar.getLayoutParams();
            params.height = actionBarHeight;
            myActionBar.setLayoutParams(params);
        }
    }
    // 设置标题 按钮 事件
    public ActionBar getActivityActionBar() {
        return null;
    }
    @Override
    public void onClick(View v) {
        int i=v.getId();
        if(i==R.id.btn_clear){
            mSigView.clearText();
        }else if(i==R.id.btn_save){
            boolean contentEmpty = mSigView.isEmpty();
            if (contentEmpty) {
                this.showAlertDialog(this, null, getString(R.string.no_signature_msg), true, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        doSave();
                        dialog.dismiss();
                    }
                });
                return;
            } else {
                doSave();
            }
        }
    }
    private void doSave() {
        Intent intent = new Intent();
            int resultCode = composeIntent(intent);
            this.setResult(resultCode, intent);
            this.finish();
    }
    private int composeIntent(Intent intent) {
        if (null == intent) {
            // should never enter this!
            return Activity.RESULT_CANCELED;
        }
        Bitmap cache = mSigView.getDrawingCache();
        // 最大byte限定到100k
        byte[] cacheByte;
        // 如果getDrawCache不work，采用View.draw方法
        if (null == cache) {
            cache = Bitmap.createBitmap(mSigView.getWidth(),
                    mSigView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(cache);
            mSigView.draw(cv);
            cacheByte = resizeImage(cache, screenWidth);
            cache.recycle();
        } else {
            cacheByte = resizeImage(cache, screenWidth);
        }
        boolean contentEmpty = mSigView.isEmpty();
        intent.putExtra("empty", contentEmpty);
        int resultCode = Activity.RESULT_OK;
        // 只有内容不为空的时候，才将result设置为OK
        if (null != cacheByte && 0 != cacheByte.length && !contentEmpty) {
            LogT.w("Sign Size:" + cacheByte.length);
            intent.putExtra("signature", cacheByte);
        } else {
            LogT.w("getSignature failed! contentEmpty? " + contentEmpty);
            resultCode = Activity.RESULT_CANCELED;
        }
        return resultCode;
    }
    public static byte[] resizeImage(Bitmap cache, int maxWidth) {
        Bitmap zooImg = zoomImg(cache, maxWidth);
        byte[] imgByte = getBitmapBytes(zooImg, 50);
        LogT.w("imageresize  newWith:" + maxWidth);
        LogT.w("imageresize  length:" + imgByte.length);
        if (zooImg != null) {
            zooImg.recycle();
        }
        return imgByte;
    }
    /**
     * 图片缩放
     */
    public static Bitmap zoomImg(Bitmap bm, int newWidth) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scale = ((float) newWidth) / width;
        // float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        int nWidth = (int) (width * scale);
        int nHeight = (int) (width * scale);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale, 0, 0);
        // 得到新的图片
        Bitmap newbm;
        try {
            newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        } catch (Exception e) {
            newbm = bm;
            e.printStackTrace();
        }
        return newbm;
    }
    protected boolean isShownTopBar() {
        return false;
    }
    /**
     * 根据给定的Bitmap,返回byte数组。
     *
     * @param cache
     * @param max   限定byte数组大小。单位为KB
     * @return
     */
    public static byte[] getBitmapBytes(Bitmap cache, int max) {
        int maxByte = max * 1024;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        LogT.w("bitmap:" + cache + " bitmap size:" + cache.getByteCount());
        int options = 70;
        cache.compress(Bitmap.CompressFormat.JPEG, options, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        LogT.w("baos.toByteArray().length 1:" + baos.toByteArray().length + " options:" + options);
        try {
            while (baos.toByteArray().length > maxByte) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
                if (options <= 20) {
                    options -= 3;//每次都减少3
                    if (options <= 3) {
                        break;
                    }
                } else {
                    options -= 20;//每次都减少10
                }
                baos.reset();//重置baos即清空baos
                LogT.w("baos.toByteArray().length 2:" + baos.toByteArray().length + " options:" + options);
                cache.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
                LogT.w("baos.toByteArray().length 3:" + baos.toByteArray().length + " options:" + options);

            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
    public final void showAlertDialog(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener oclOK) {
        showAlertDialog(context, title, message, cancelable, oclOK, null);
    }
    public final void showAlertDialog(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener oclOK, DialogInterface.OnClickListener oclCancel) {
        showAlertDialog(context, title, message, cancelable, oclOK, oclCancel, null, null);
    }
    /**
     * @param context
     * @param title        标题
     * @param message      提示信息
     * @param cancelable   是否可取消
     * @param oclOK        确定事件
     * @param oclCancel    取消事件
     * @param middleButton 中间按钮文字
     * @param oclMiddle    中间按钮事件
     */
    public final void showAlertDialog(Context context, String title, String message, boolean cancelable, DialogInterface.OnClickListener oclOK, DialogInterface.OnClickListener oclCancel, String middleButton, DialogInterface.OnClickListener oclMiddle) {
        showAlertDialog(context, title, message, cancelable, null, oclOK, null, oclCancel, middleButton, oclMiddle);
    }
    // 对话框
    public final void showAlertDialog(Context context, String title, String message, boolean cancelable, String okString, DialogInterface.OnClickListener oclOK, String cancelString, DialogInterface.OnClickListener oclCancel, String middleButton, DialogInterface.OnClickListener oclMiddle) {
        class RunnableShowAlertDialog implements Runnable {
            private Context context;
            private String title, message, neutral, mOKString, mCancelString;
            private boolean cancelable;
            private DialogInterface.OnClickListener oclPositive, oclNeutral, oclNegative;
            private CustomDialog.Builder builder;

            public RunnableShowAlertDialog(Context context, String title, String message, boolean cancelable, String okString, DialogInterface.OnClickListener oclOK, String cancelString, DialogInterface.OnClickListener oclCancel, String middleButton, DialogInterface.OnClickListener oclMiddle) {
                this.context = context;
                this.title = title;
                this.message = message;
                this.mOKString = okString;
                this.mCancelString = cancelString;
                this.cancelable = cancelable;
                oclPositive = oclOK;
                oclNegative = oclCancel;
                neutral = middleButton;
                oclNeutral = oclMiddle;
            }

            @Override
            public void run() {
                try {
                    if (dialog != null && dialog.isShowing()) {
                        if (builder == null) {
                            builder = setAlertBuilder(new CustomDialog.Builder(context));
                        } else {
                            builder = setAlertBuilder(builder);
                        }
                    } else {
                        builder = setAlertBuilder(new CustomDialog.Builder(context));
                    }
                    dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(cancelable);
                    dialog.setCancelable(cancelable);
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            /**
             * 设置Builder
             */
            private CustomDialog.Builder setAlertBuilder(CustomDialog.Builder builder) {
                builder.setTitle(title);
                builder.setMessage(StringFilter(message));
                // 按钮文字为空时，不显示按钮
                if (TextUtils.isEmpty(mOKString)) {
                    builder.setPositiveButton(android.R.string.ok, oclPositive);
                } else {
                    builder.setPositiveButton(mOKString, oclPositive);
                }
                if (oclNeutral != null) {
                    builder.setNeutralButton(neutral, oclNeutral);
                }
                if (oclNegative != null) {
                    // 取消事件为空，认为是没有取消按钮
                    if (TextUtils.isEmpty(mCancelString)) {
                        builder.setNegativeButton(android.R.string.cancel, oclNegative);
                    } else {
                        builder.setNegativeButton(mCancelString, oclNegative);
                    }
                }
                return builder;
            }
        }
        this.runOnUiThread(new RunnableShowAlertDialog(context, title, message, cancelable, okString, oclOK, cancelString, oclCancel, middleButton, oclMiddle));
    }
    /**
     * 替换、过滤特殊字符
     */
    public static String StringFilter(String str) throws PatternSyntaxException {
        str = str.replaceAll("【", "[").replaceAll("】", "]").replaceAll("！", "!").replaceAll("，", ",").replaceAll("&", "\n");//替换中文标号
        String regEx = "[『』]"; // 清除掉特殊字符
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}
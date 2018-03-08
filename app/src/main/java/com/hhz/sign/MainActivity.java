package com.hhz.sign;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.ByteArrayInputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView mScrollView;
    protected Bitmap signatureBitmap;
    private byte[] signatureBytes;
    public static final int REQUEST_SIGNATURE = 1;
    private AbTitleBar myActionBar;
    private ImageView ivSignature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScrollView = findViewById(R.id.mScrollView);
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.activity_sign, null);
        mScrollView.addView(layout);
        ivSignature = layout.findViewById(R.id.iv_signature);
        layout.findViewById(R.id.signature_container).setOnClickListener(this);
        ivSignature.setOnClickListener(this);

        myActionBar = this.findViewById(R.id.top_actionbar);
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

    public ActionBar getActivityActionBar() {
        return new ActionBar("签购单", true);
    }

    protected boolean isShownTopBar() {
        return true;
    }

    protected boolean isVisiableTopBar() {
        return true;
    }

    /* 初始化ActionBar 和 Tab 的高度 */
    public void initActionBarAndTabHeight() {
        TypedValue tv = new TypedValue();
        if (this.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, this.getResources().getDisplayMetrics());
            android.view.ViewGroup.LayoutParams params = myActionBar.getLayoutParams();
            params.height = actionBarHeight;
            myActionBar.setLayoutParams(params);
        }
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_signature || v.getId() == R.id.signature_container) {
            if (signatureBitmap == null) {
                goToSignature();
            }

        }
    }

    private void goToSignature() {
        Intent i = new Intent(this, SignatureActivity.class);
        this.startActivityForResult(i, REQUEST_SIGNATURE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 进入以后，拖动到最下面
        mScrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.smoothScrollTo(0, mScrollView.getHeight());
            }
        }, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SIGNATURE:
                if (Activity.RESULT_OK == resultCode) {
                    if (null != signatureBitmap) {
                        signatureBitmap.recycle();
                        signatureBitmap = null;
                    }
                    byte[] imgBytes = data.getByteArrayExtra("signature");
                    signatureBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(imgBytes));
                    signatureBytes = imgBytes;
                    LogT.w("imgBytes sign length:" + imgBytes.length);
                    LogT.w("signatureBitmap size:" + signatureBitmap.getByteCount());
                    if (null != signatureBitmap) {
                        int width = ivSignature.getWidth();
                        int height = ivSignature.getHeight();
                        LogT.w("签名坐标，width=" + width + ",height=" + height);
                        Bitmap b = Bitmap.createScaledBitmap(signatureBitmap, width, height, false);
                        ivSignature.setImageBitmap(b);
                        findViewById(R.id.tv_signature_hints).setVisibility(View.GONE);
                    } else {
                        LogT.w("signatureBitmap null");
                        findViewById(R.id.tv_signature_hints).setVisibility(View.VISIBLE);
                    }
                } else {
                    if (null == signatureBitmap) {
                        findViewById(R.id.tv_signature_hints).setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }
}

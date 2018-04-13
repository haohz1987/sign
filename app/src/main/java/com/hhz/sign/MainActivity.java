package com.hhz.sign;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.hhz.sign.view.AbTitleBar;
import com.hhz.sign.view.ActionBar;
import com.hhz.sign.view.VerificationCodeView;

import java.io.ByteArrayInputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ScrollView mScrollView;
    protected Bitmap signatureBitmap;
    private byte[] signatureBytes;
    public static final int REQUEST_SIGNATURE = 1;
    private AbTitleBar myActionBar;
    private ImageView ivSignature;
    private VerificationCodeView verificationCodeView;
    private EditText mInput;
    private ProgressBar loadingcode_progress;
    private String inputStr;
    private Button btn_confirm;
    private String veriCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScrollView = findViewById(R.id.mScrollView);
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.activity_sign, null);
        mScrollView.addView(layout);
        ivSignature = layout.findViewById(R.id.iv_signature);
        verificationCodeView = layout.findViewById(R.id.verificationcodeview);
        verificationCodeView.setOnClickListener(this);
        mInput = layout.findViewById(R.id.et_input);
        loadingcode_progress = layout.findViewById(R.id.loadingcode_progress);
        btn_confirm = layout.findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(this);
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
        loadingcode_progress.setVisibility(View.GONE);
        getVerificationCode();
    }
    /**
     * 获取验证码
     **/
    private void getVerificationCode() {
        loadingcode_progress.setVisibility(View.VISIBLE);
        verificationCodeView.postDelayed(ENABLE_AGAIN,500);
        loadingcode_progress.setVisibility(View.GONE);
        veriCode = getCharAndNumr(4);
        updateLocalVerificationCode(veriCode);
        LogT.w("产生随机数："+ veriCode);
    }
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
    private static boolean enabled = true;
    private static final Runnable ENABLE_AGAIN = new Runnable() {
        @Override
        public void run() {
            enabled = true;
        }
    };
    /**
     * 刷新验证码
     *
     * @param code
     */
    private void updateLocalVerificationCode(String code) {
        if (verificationCodeView != null) {
            mInput.setText("");
            verificationCodeView.setVisibility(View.VISIBLE);
            loadingcode_progress.setVisibility(View.INVISIBLE);
            verificationCodeView.updateChar(code);
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
        switch (v.getId()){
            case R.id.iv_signature:
            case R.id.signature_container:
                if (signatureBitmap == null) {
                    goToSignature();
                }
                break;
            case R.id.verificationcodeview:
                getVerificationCode();
                break;
            case R.id.btn_confirm:
                inputStr = mInput.getText().toString().trim();
                LogT.w("输入字符："+inputStr+",生成的字符："+veriCode);
                if(TextUtils.isEmpty(inputStr)){
                    Toast.makeText(MainActivity.this, "请输入校验码", Toast.LENGTH_SHORT).show();
                    getVerificationCode();
                    return;
                }
                if(!verificationCodeView.validateCode(inputStr)){
                    Toast.makeText(MainActivity.this, "验证码校验失败", Toast.LENGTH_SHORT).show();
                    getVerificationCode();
                }else{
                    Toast.makeText(MainActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                }
                break;
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

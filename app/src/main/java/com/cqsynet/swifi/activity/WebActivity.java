/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：显示网页,打开网页先获取cmsId后才能做其它操作
 *
 *
 * 创建标识：zhaosy 20150420
 */
package com.cqsynet.swifi.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.AppManager;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.db.BlackWhiteUrlDao;
import com.cqsynet.swifi.db.CollectCacheDao;
import com.cqsynet.swifi.db.MessageDao;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.model.BaseResponseObject;
import com.cqsynet.swifi.model.BlackUrlObject;
import com.cqsynet.swifi.model.CollectRemoveInfo;
import com.cqsynet.swifi.model.CollectRemoveRequestBody;
import com.cqsynet.swifi.model.CollectRequestBody;
import com.cqsynet.swifi.model.CommentInfo;
import com.cqsynet.swifi.model.CommentRequestBody;
import com.cqsynet.swifi.model.CommentSubmitResponseObject;
import com.cqsynet.swifi.model.RequestHeader;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.model.ResponseObject;
import com.cqsynet.swifi.model.ShareObject;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.network.WebServiceIf.IResponseCallback;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.DateUtil;
import com.cqsynet.swifi.util.InputManagerUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.SoftKeyboardStateHelper;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.CommentDialog;
import com.cqsynet.swifi.view.ShareDialog;
import com.cqsynet.swifi.view.SimpleDialog;
import com.google.gson.Gson;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cqsynet.swifi.Globals.mHasFreeAuthority;
import static com.cqsynet.swifi.Globals.mIsConnectFreeWifi;
import static com.cqsynet.swifi.activity.WifiActivity.REQUEST_CODE_WEB;

@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends HkActivity {

    private WebView mWebView;
    private ImageView mIvBlankPage;
    private ImageButton mBtnBack;
    private ImageButton mBtnClose;
    private ImageButton mBtnShare;
    private ImageButton mBtnCollect;
    private LinearLayout mLlComment;
    private EditText mEtComment;
    private TextView mTvSend;
    private FrameLayout mFlCommentCount;
    private View mPlaceHolderView;
    private TextView mTvCommentCount;
    private ImageView mIvWrite;
    private TextView mTvCommentHint;
    private TextView mTvCommentDisable;
    private View mMarkView;
    private View mTitleMarkView;
    private FrameLayout mRootLayout;
    private String mType;
    private String mId;
    private String mUrl;
    private String mImage;
    private String mSource;
    private String mFrom; //从哪个页面点击进来
    private String mFlag; // 用来区别全屏广告
    private boolean mIsUpdate; // 是否刷新页面；当连上网络之后，应该刷新界面
    private String mFromInStatistics;
    private String mChannelId;
    private String mContent;
    private ProgressBar mProgress;
    private boolean mIsLoaded = false; // 是否加载完成
    private boolean mIsCollect = false;// 是否已被收藏
    private boolean mIsBlackUrl = false; // 是否在黑名单中
    private boolean mIsShowAdv = false; // 是否需要加载广告
    private int mErrorCount = 0;
    private String mTitle; //标题
    private RelativeLayout mLlPotrait;
    private FrameLayout mFlLandscape;
    private RelativeLayout mRlTip;
    private TextView mTvTip;
    private ImageButton mIbtnCloseTip;
    private ValueCallback mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 102;
    private final static int REQUEST_SELECT_FILE = 103;
    private MyHandler mHdl = new MyHandler(this);
    private Map mExtraHeaders = new HashMap();
    private SimpleDialog mHintDialog;
    private String mCommentStatus;
    private String mCommentMessage;
    private String mCommentCount;
    private Dialog mDialog;
//    private GestureDetector mGestureDetector;   //手势检测
//    private GestureDetector.OnGestureListener mOnSlideGestureListener = null;   //左右滑动手势检测监听器

    static class MyHandler extends Handler {
        WeakReference<WebActivity> mWeakRef;

        public MyHandler(WebActivity webActivity) {
            mWeakRef = new WeakReference<WebActivity>(webActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            WebActivity activity = mWeakRef.get();
            switch (msg.what) {
                case 0:
                    activity.isCollect();
                    break;
//                case 1:
//                    break;
//                case 2:
////                    CollectCacheDao.deleteData(activity, activity.mId);
//                    break;
                case 3: //支付宝回调地址
                    String url = (String) msg.obj;
                    activity.mWebView.loadUrl(url);
                    break;
                case 4:
                    Bundle bundle = msg.getData();
                    String message = bundle.getString("message");
                    String status = bundle.getString("status");
                    String count = bundle.getString("count");
                    activity.handleCommentLayout(message, status, count);
                    break;
                case 5:
                    activity.dismissProgressDialog();
                    if (activity.mDialog != null && activity.mDialog.isShowing()) {
                        activity.mDialog.dismiss();
                    }
                    break;
                case 6:
                    Bundle bundle1 = msg.getData();
                    String message1 = bundle1.getString("message");
                    String status1 = bundle1.getString("status");
                    if ("0".equals(status1)) {
                        activity.mDialog = CommentDialog.createDialog(activity, R.drawable.ic_failure, message1);
                        activity.mDialog.show();
                        activity.mHdl.sendEmptyMessageDelayed(5, 1000);
                    } else if ("1".equals(status1)) {
                        activity.mDialog = CommentDialog.createDialog(activity, R.drawable.ic_success, message1);
                        activity.mDialog.show();
                        activity.mHdl.sendEmptyMessageDelayed(5, 1000);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webactivity);
        mBtnBack = findViewById(R.id.btnBack_webactivity);
        mBtnClose = findViewById(R.id.btnClose_webactivity);
        mBtnShare = findViewById(R.id.btnShare_webactivity);
        mBtnCollect = findViewById(R.id.btnCollect_webactivity);
        mLlComment = findViewById(R.id.comment_layout);
        mEtComment = findViewById(R.id.et_comment);
        mTvSend = findViewById(R.id.tv_send);
        mFlCommentCount = findViewById(R.id.fl_comment_count);
        mPlaceHolderView = findViewById(R.id.place_holder_view);
        mTvCommentCount = findViewById(R.id.tv_comment_count);
        mTvCommentHint = findViewById(R.id.tv_comment_hint);
        mTvCommentDisable = findViewById(R.id.tv_comment_disable);
        mMarkView = findViewById(R.id.mark_view);
        mTitleMarkView = findViewById(R.id.title_mark_view);
        mRootLayout = findViewById(R.id.root_layout);
        mIvWrite = findViewById(R.id.iv_write);
        mProgress = findViewById(R.id.progress_webactivity);
        mIvBlankPage = findViewById(R.id.ivBlank_webactivity);
        mLlPotrait = findViewById(R.id.rlPotrait_webactivity);
        mFlLandscape = findViewById(R.id.flLandscape_webactivity);
        mRlTip = findViewById(R.id.rlTip_webactivity);
        mTvTip = findViewById(R.id.tvTip_webactivity);
        mIbtnCloseTip = findViewById(R.id.ibtnCloseTip_webactivity);
        mIbtnCloseTip.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRlTip.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.rlTitlebar_webactivity).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.scrollTo(0, 0);
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mType = bundle.getString("type");
            mUrl = bundle.getString("url");
            mImage = bundle.getString("image");
            mSource = bundle.getString("source");
            mFrom = bundle.getString("from");
            mChannelId = bundle.getString("channelId");
            mFlag = bundle.getString("flag");
        } else {
            finish();
        }

        //左右滑动手势监听器
//        mOnSlideGestureListener = new OnSlideGestureListener();
//        mGestureDetector = new GestureDetector(this, mOnSlideGestureListener);

        mFromInStatistics = initFrom(mFrom);

        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                onBackPressed();
            }
        });

        mBtnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                HashMap<String, Activity> activityMap = AppManager.getInstance().getActivityMap();
                if (!TextUtils.isEmpty(mFrom) && mFrom.equals("notification")) {
                    if (!activityMap.containsKey("HomeActivity")) {
                        Intent jumpIntent = new Intent();
                        jumpIntent.setClass(WebActivity.this, HomeActivity.class);
                        startActivity(jumpIntent);
                    }
                    finish();
                } else {
                    if ("fullAdv".equals(mFlag)) {
                        Intent fullAdv = new Intent(WebActivity.this, FullAdvActivity.class);
                        fullAdv.putExtra("source", "WifiActivity");
                        fullAdv.putExtra("accessType", getIntent().getIntExtra("accessType", 0));
                        startActivity(fullAdv);
                    }
                    setResult(RESULT_OK); //splash页可以继续跳转
                    finish();
                }
            }
        });
        mBtnShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getShare();
            }
        });
        mBtnCollect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mIsLoaded) {
                    if (!mIsCollect) {
                        collect();
                    } else {
                        removeCollect();
                    }
                }
            }
        });
        mEtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    mFlCommentCount.setVisibility(View.VISIBLE);
                    mTvSend.setVisibility(View.GONE);
                    mIvWrite.setVisibility(View.VISIBLE);
                    mTvCommentHint.setVisibility(View.VISIBLE);
                } else {
                    mFlCommentCount.setVisibility(View.GONE);
                    mTvSend.setVisibility(View.VISIBLE);
                    mIvWrite.setVisibility(View.GONE);
                    mTvCommentHint.setVisibility(View.GONE);
                }
            }
        });
        mFlCommentCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentActivity.launch(WebActivity.this, mId, mCommentStatus, mCommentMessage);
            }
        });
        mTvSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });
        mTitleMarkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputManagerUtil.toggleKeyboard(WebActivity.this);
            }
        });
        mMarkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputManagerUtil.toggleKeyboard(WebActivity.this);
            }
        });

        SoftKeyboardStateHelper helper = new SoftKeyboardStateHelper(mRootLayout);
        helper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                mMarkView.setVisibility(View.VISIBLE);
                mTitleMarkView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSoftKeyboardClosed() {
                mMarkView.setVisibility(View.GONE);
                mTitleMarkView.setVisibility(View.GONE);
            }
        });

        mIvBlankPage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setVisibility(View.VISIBLE);
                mIsLoaded = false;
                loadUrlAndError(mUrl);
            }
        });

        if (TextUtils.isEmpty(mUrl)) {
            ToastUtil.showToast(this, "该新闻已过期");
        }

        mWebView = findViewById(R.id.wv_webactivity);
        mWebView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.getSettings().setTextZoom(100);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setGeolocationDatabasePath(getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath());
        String ua = mWebView.getSettings().getUserAgentString();
        mWebView.getSettings().setUserAgentString(ua + " App/heikuai");
        if (Build.VERSION.SDK_INT >= 21) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
//        //测试用
//        if(Build.VERSION.SDK_INT >= 19) {
//            mWebView.setWebContentsDebuggingEnabled(true);
//        }

//		mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.toLowerCase().endsWith(".apk")) {
                    return false;
                }

                //app内部跳转
                if (isToAppPage(url)) {
                    return true;
                }

                mIsBlackUrl = isBlackUrl(url);

                //支付宝
                final PayTask task = new PayTask(WebActivity.this);
                final String ex = task.fetchOrderInfoFromH5PayUrl(url); //处理订单信息
                if (!TextUtils.isEmpty(ex)) {
                    //调用支付接口进行支付
                    new Thread(new Runnable() {
                        public void run() {
                            H5PayResultModel result = task.h5Pay(ex, true);
                            //处理返回结果
                            if (!TextUtils.isEmpty(result.getReturnUrl())) {
                                Message msg = new Message();
                                msg.what = 3;
                                msg.obj = result.getReturnUrl();
                                mHdl.sendMessage(msg);
                            }
                        }
                    }).start();
                    return true;
                }

                //非网址类的schema,例如weixin:// , bdapp://
                if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
                    if (url.startsWith("weixin://") || url.startsWith("bdapp://")) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        intent.setData(Uri.parse(url));
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                        return true;
                    } else {
                        return false;
                    }
                }

                if (url.contains("wx.tenpay.com")) {
                    mExtraHeaders.put("Referer", mUrl);
                }

                //跳转有赞
                if (url.toLowerCase().contains("?yz=1") || url.toLowerCase().contains("&yz=1")) {
                    Intent intent = new Intent(WebActivity.this, YouzanWebActivity.class);
                    intent.putExtra("url", url);
                    startActivity(intent);
                    return true;
                }
                initPage(url);

                //统计页面访问
                saveStatistics(mUrl, null, String.valueOf(System.currentTimeMillis())); //记录上个页面的结束时间
                saveStatistics(url, String.valueOf(System.currentTimeMillis()), null); //记录这个页面的开始时间

                String tempUrl = url;
                tempUrl = tempUrl.toLowerCase();
                if (!tempUrl.startsWith("http://www.heikuai.com/mobile/portal.html")) { //非portal页
                    mUrl = url;
                    //内网页面
                    if (tempUrl.contains("heikuai.com") || tempUrl.contains("cqsynet.com")) {
                        if (!url.contains("userAccount=")) {
                            if (url.contains("?")) {
                                mUrl = url + "&userAccount=" + SharedPreferencesInfo.getTagString(WebActivity.this, SharedPreferencesInfo.ACCOUNT);
                            } else {
                                mUrl = url + "?userAccount=" + SharedPreferencesInfo.getTagString(WebActivity.this, SharedPreferencesInfo.ACCOUNT);
                            }
                        }
                    }
                    loadUrlAndError(mUrl);
                    return true;
                } else {
                    //公司portal页,自动回退到portal页的前面一页
                    view.goBack();
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mIsLoaded = false;
                mProgress.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.VISIBLE);
                mLlComment.setVisibility(View.GONE);
                mEtComment.setText("");
                mId = "";
                mContent = "";
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (Globals.DEBUG) {
                    System.out.println(url);
                }
                super.onPageFinished(view, url);
                mIsLoaded = true;
                mProgress.setVisibility(View.GONE);
                mIvBlankPage.setVisibility(View.GONE);

                view.loadUrl("javascript:function cover(){" +
                        "var href=document.getElementsByTagName(\"img\");" +
                        "\t\t if(href.length>0){\n" +
                        "\t\t      window.localJS.loadCover(href[0].src)\n" +
                        "\t\t }" +
                        "}");
                view.loadUrl("javascript:function content(){" +
                        "var href=document.getElementsByTagName(\"description\");" +
                        "\t\t if(href.length>0){\n" +
                        "\t\t      window.localJS.loadContent(href[0].content)\n" +
                        "\t\t }" +
                        "}");
//                view.loadUrl("javascript:window.localJS.loadCover("
//                        + "document.getElementsByTagName('img')[0].src);"
//                        + "javascript:window.localJS.loadContent("
//                        + "document.getElementsByName('description')[0].content);");
                view.loadUrl("javascript:cover()");
                view.loadUrl("javascript:content()");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                mProgress.setVisibility(View.GONE);

                mErrorCount++;
                if (mErrorCount < 2) {
                    try {
                        mUrl = URLDecoder.decode(mUrl, "utf-8");
                        loadUrlAndError(mUrl);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    mErrorCount = 0;
                    mIvBlankPage.setVisibility(View.VISIBLE);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            View mView;
            CustomViewCallback mCallback;

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mTitle = title;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCallback != null) {
                    mCallback.onCustomViewHidden();
                    mCallback = null;
                    return;
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                mFlLandscape.addView(view);
                mLlPotrait.setVisibility(View.GONE);
                mFlLandscape.setVisibility(View.VISIBLE);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                mView = view;
                mCallback = callback;
            }

            @Override
            public void onHideCustomView() {
                if (mView != null) {
                    if (mCallback != null) {
                        mCallback.onCustomViewHidden();
                        mCallback = null;
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mFlLandscape.removeAllViews();
                    mFlLandscape.setVisibility(View.GONE);
                    mLlPotrait.setVisibility(View.VISIBLE);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    mView = null;
                }
            }

            /**
             * 通过input file标签上传文件
             * @param uploadMsg
             */
            public void openFileChooser(ValueCallback uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

            /**
             *  通过input file标签上传文件 For android 3.0
             * @param uploadMsg
             * @param acceptType
             */
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            /**
             * * 通过input file标签上传文件 For ansdroid 4.1
             * @param uploadMsg
             * @param acceptType
             * @param capture
             */
            public void openFileChooser(ValueCallback uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

            /**
             * * 通过input file标签上传文件 For Lollipop 5.0+ Devices
             * @param mWebView
             * @param filePathCallback
             * @param fileChooserParams
             * @return
             */
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                    mUploadMessage = null;
                }
                mUploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    mUploadMessage = null;
                    Toast.makeText(getBaseContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            /**
             * 定位(农商行需求增加)
             * @param origin
             * @param callback
             */
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });

        mWebView.addJavascriptInterface(new Object() {
            /**
             * 获取图片地址
             * @param index 当前图片序号
             * @param json 图片地址的json
             */
            @JavascriptInterface
            public void showPhoto(String index, String json) {
                if (!TextUtils.isEmpty(index) && !TextUtils.isEmpty(json)) {
                    Intent intent = new Intent();
                    intent.setClass(WebActivity.this, GalleryActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("from", "web");
                    bundle.putInt("index", Integer.parseInt(index));
                    bundle.putString("json", json);
                    bundle.putString("id", mId);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }

            /**
             * 向页面传递用户账号
             */
            @JavascriptInterface
            public String transferAccount() {
                return SharedPreferencesInfo.getTagString(WebActivity.this, SharedPreferencesInfo.ACCOUNT);
            }

            /**
             * 通过pk_id获取分享数据
             */
            @JavascriptInterface
            public void setPKID(String cmsId, String title) {
                mId = cmsId;
//                mTitle = title;
                if (!TextUtils.isEmpty(mId)) {
                    mHdl.sendEmptyMessage(0); //判断是否已收藏
                }
            }

            /**
             * 获取分享信息
             * @param shareUrl
             * @param shareTitle
             * @param sharePic
             * @param shareContent
             */
            @JavascriptInterface
            public void setShareInfo(String shareUrl, String shareTitle, String sharePic, String shareContent) {
//                mImage = sharePic;
//                mContent = shareContent;
            }

            /**
             * 复制到剪切板
             * @param content
             */
            @JavascriptInterface
            public void copyToClipboard(String content) {
                ClipboardManager cbm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cbm.setPrimaryClip(ClipData.newPlainText("code", content));
                ToastUtil.showToast(WebActivity.this, "已复制到剪切板");
            }

            /**
             * 向页面传递app版本
             * @return
             */
            @JavascriptInterface
            public int setVersion() {
                int version = AppUtil.getVersionCode(WebActivity.this);
                return version;
            }

            /**
             * 获取微信帐号并弹出提示框
             * @param name 微信昵称
             * @param account 微信帐号
             */
            @JavascriptInterface
            public void weixin(String name, String account) {
                if (Globals.DEBUG) {
                    System.out.println("@@@@@   " + name + "    " + account);
                }
                if (AppUtil.isWeixinAvilible(WebActivity.this)) {
                    ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData myClip;
                    myClip = ClipData.newPlainText("text", account);
                    myClipboard.setPrimaryClip(myClip);
                    final CustomDialog dialog = new CustomDialog(WebActivity.this, R.style.round_corner_dialog, R.layout.dialog_weixin_focus);
                    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                    lp.width = AppUtil.getScreenW(WebActivity.this) - 80; // 设置宽度
                    lp.height = lp.width * 2 / 3;
                    dialog.getWindow().setAttributes(lp);
                    dialog.show();
                    View view = dialog.getCustomView();
                    ((TextView) view.findViewById(R.id.tvMsg_dialog_weixin_focus)).setText(String.format(getResources().getString(R.string.weixin_focus_reminder), name));
                    view.findViewById(R.id.btnOk_dialog_weixin_focus).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            ComponentName cn = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_LAUNCHER);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setComponent(cn);
                            startActivity(intent);
                            dialog.cancel();
                        }
                    });
                    view.findViewById(R.id.btnCancel_dialog_weixin_focus).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                } else {
                    final CustomDialog dialog = new CustomDialog(WebActivity.this, R.style.round_corner_dialog, R.layout.dialog_weixin_focus);
                    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                    lp.width = AppUtil.getScreenW(WebActivity.this) - 80; // 设置宽度
                    lp.height = lp.width * 2 / 3;
                    dialog.getWindow().setAttributes(lp);
                    dialog.show();
                    View view = dialog.getCustomView();
                    ((TextView) view.findViewById(R.id.tvMsg_dialog_weixin_focus)).setText(R.string.no_weixin);
                    ((Button) view.findViewById(R.id.btnCancel_dialog_weixin_focus)).setText("知道了");
                    view.findViewById(R.id.btnOk_dialog_weixin_focus).setVisibility(View.GONE);
                    view.findViewById(R.id.btnCancel_dialog_weixin_focus).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                }
            }

            /**
             * AR扫描
             */
            @JavascriptInterface
            public void openScaner() {
//                Intent intent = new Intent();
//                intent.setClass(WebActivity.this, ScanerActivity.class);
//                startActivity(intent);
            }

            @JavascriptInterface
            public void browseAllComments() {
                CommentActivity.launch(WebActivity.this, mId, mCommentStatus, mCommentMessage);
            }

            @JavascriptInterface
            public void browseAllReplyComments(String json) {
                CommentInfo comment = new Gson().fromJson(json, CommentInfo.class);
                ReplyActivity.launch(WebActivity.this, mId, comment, mCommentStatus, mCommentMessage);
            }

            @JavascriptInterface
            public void showStatusDialog(String message, String status) {
                Message msg = Message.obtain();
                msg.what = 6;
                Bundle data = new Bundle();
                data.putString("message", message);
                data.putString("status", status);
                msg.setData(data);
                mHdl.sendMessage(msg);
            }

            @JavascriptInterface
            public void isSupportComment(String message, String status, String count) {
                Message msg = Message.obtain();
                msg.what = 4;
                Bundle data = new Bundle();
                data.putString("message", message);
                data.putString("status", status);
                data.putString("count", count);
                msg.setData(data);
                mCommentMessage = message;
                mCommentStatus = status;
                mCommentCount = count;
                mHdl.sendMessage(msg);
            }

            @JavascriptInterface
            public String getLocation() {
                return RequestHeader.mRequestHead.longitude + "," + RequestHeader.mRequestHead.latitude;
            }

        }, "heikuai");
        mWebView.addJavascriptInterface(new LocalJavaScriptInterface(), "localJS");

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                        long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        // 解决5.0以上手机WebView无法成功同步Cookie
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(mWebView, true);
        }

        //若是从通知栏打开的页面,在消息中设置为已读
        if (!TextUtils.isEmpty(mFrom) && mFrom.equals("notification")) {
            String msgId = getIntent().getStringExtra("msgId");
            MessageDao.getInstance(WebActivity.this).updateMsgStatus(msgId); // 修改消息为已读状态
            Intent action = new Intent();
            action.setAction(AppConstants.ACTION_REFRESH_RED_POINT);
            sendBroadcast(action); // 发送广播提示此消息已读，并更新未读提示
        }

//		mUrl = "http://www.heikuai.com/app/frame.html?url=http%3A%2F%2Fm.aiercq.com%2Fhtml%2Fspecial%2Fvip%2F2.html&a=a";
//		mUrl = "http://www.iqiyi.com/v_19rrkbj4hs.html?vfm=f_198_zqsy";
//		mUrl = "http://www.baidu.com";
        //增加频道id
        if (!TextUtils.isEmpty(mChannelId)) {
            if (mUrl.contains("?")) {
                mUrl = mUrl + "&cId=" + mChannelId;
            } else {
                mUrl = mUrl + "?cId=" + mChannelId;
            }
        }
        initPage(mUrl);
        mIsBlackUrl = isBlackUrl(mUrl);
        loadUrlAndError(mUrl);
    }

    private void handleCommentLayout(String message, String status, String count) {
        if ("0".equals(status)) { // 正常使用
            mLlComment.setVisibility(View.VISIBLE);
            mEtComment.setEnabled(true);
            mIvWrite.setVisibility(View.VISIBLE);
            mTvCommentHint.setVisibility(View.VISIBLE);
            mTvCommentDisable.setVisibility(View.GONE);
            setCommentCount();
        } else if ("2".equals(status)) { // 用户被冻结
            mLlComment.setVisibility(View.VISIBLE);
            mEtComment.setEnabled(false);
            mTvCommentHint.setVisibility(View.GONE);
            mIvWrite.setVisibility(View.GONE);
            mTvCommentDisable.setVisibility(View.VISIBLE);
            mTvCommentDisable.setText(message);
            setCommentCount();
        } else { // 文章禁止评论
            mLlComment.setVisibility(View.GONE);
        }
    }

    private void setCommentCount() {
        int commentCount = !TextUtils.isEmpty(mCommentCount) ? Integer.parseInt(mCommentCount) : 0;
        if (commentCount == 0) {
            mTvCommentCount.setVisibility(View.GONE);
            mPlaceHolderView.setVisibility(View.GONE);
            mTvCommentCount.setText("0");
        } else if (commentCount % 10000 == 0) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 10000 + "W"));
        } else if (commentCount % 1000 == 0) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 1000 + "K"));
        } else if (commentCount > 10000) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 10000 + "." + commentCount % 10000 / 1000 + "W"));
        } else if (commentCount > 1000) {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount / 1000 + "." + commentCount % 1000 / 100 + 'K'));
        } else {
            mPlaceHolderView.setVisibility(View.VISIBLE);
            mTvCommentCount.setVisibility(View.VISIBLE);
            mTvCommentCount.setText(String.valueOf(commentCount));
        }
    }

    private class LocalJavaScriptInterface {
        @JavascriptInterface
        public void loadCover(String src) {
            if (!src.isEmpty() && src.contains("http")) {
                mImage = src;
            } else {
                mImage = "";
            }
        }

        @JavascriptInterface
        public void loadContent(String content) {
            mContent = content;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Bundle bundle = getIntent().getExtras();
        mType = bundle.getString("type");
        mUrl = bundle.getString("url");
        mImage = bundle.getString("image");
        mSource = bundle.getString("source");
        mFrom = bundle.getString("from");
        mChannelId = bundle.getString("channelId");
        mFlag = bundle.getString("flag");

        mFromInStatistics = initFrom(mFrom);

        //若是从通知栏打开的页面,在消息中设置为已读
        if (!TextUtils.isEmpty(mFrom) && mFrom.equals("notification")) {
            String msgId = getIntent().getStringExtra("msgId");
            MessageDao.getInstance(WebActivity.this).updateMsgStatus(msgId); // 修改消息为已读状态
            Intent action = new Intent();
            action.setAction(AppConstants.ACTION_REFRESH_RED_POINT);
            sendBroadcast(action); // 发送广播提示此消息已读，并更新未读提示
        }
        initPage(mUrl);
        mIsBlackUrl = isBlackUrl(mUrl);
        loadUrlAndError(mUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        // 用户点击了联网，网络也确实连接上了
        if (mIsUpdate && Globals.mHasFreeAuthority) {
            mIsUpdate = false;
            mFlag = "";  // 全屏广告页面销毁
            loadUrlAndError(mUrl);
        }
    }

    @Override
    protected void onDestroy() {
        saveStatistics(mUrl, null, String.valueOf(System.currentTimeMillis())); //记录上个页面的结束时间
        CookieManager.getInstance().removeSessionCookie();  //淘宝页面一次登录后就无法退出
        mWebView.loadData("", "text/html", "utf-8");
        mWebView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        HashMap<String, Activity> activityMap = AppManager.getInstance().getActivityMap();
        if (mWebView.canGoBack()) {
            //初始化返回的页面
            int index = mWebView.copyBackForwardList().getCurrentIndex() - 1;
            String url = mWebView.copyBackForwardList().getItemAtIndex(index).getUrl();
            initPage(url);
            mUrl = url;
            if (mUrl.contains(AppConstants.COMMENT_PAGE)) {
                mBtnCollect.setVisibility(View.GONE);
                mBtnShare.setVisibility(View.GONE);
            } else {
                mBtnCollect.setVisibility(View.VISIBLE);
                mBtnShare.setVisibility(View.VISIBLE);
            }
            mWebView.goBack();
            isCollect();
        } else if (!TextUtils.isEmpty(mFrom) && mFrom.equals("notification")) {
            //点击通知打开的页面
            if (!activityMap.containsKey("HomeActivity")) {
                //若app未运行,则打开到标题列表
                Intent jumpIntent = new Intent();
                jumpIntent.setClass(WebActivity.this, HomeActivity.class);
                startActivity(jumpIntent);
            }
            finish();
        } else {
            if ("fullAdv".equals(mFlag)) {
                Intent fullAdv = new Intent(WebActivity.this, FullAdvActivity.class);
                fullAdv.putExtra("source", "WifiActivity");
                fullAdv.putExtra("accessType", getIntent().getIntExtra("accessType", 0));
                startActivity(fullAdv);
            }

            setResult(RESULT_OK); //splash页可以继续跳转
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_WEB) {
            if (resultCode == 100) {
                loadUrlAndError(mUrl);
            } else if (resultCode == 200) {
                if (TextUtils.isEmpty(mWebView.getUrl())) {
                    finish();
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (mUploadMessage == null)
                    return;
                mUploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                mUploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = data == null || resultCode != Activity.RESULT_OK ? null : data.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    private void loadUrlAndError(final String url) {
        if (mWebView == null) {
            return;
        }
        if (mHasFreeAuthority) {
            gotoNewPage(url);
        } else if (!mIsConnectFreeWifi) {
            gotoNewPage(url);
        } else if (mIsBlackUrl) {
            showHintDialog(mIsShowAdv);
        } else if (isWhiteUrl(url)) {
            gotoNewPage(url);
        } else {
            showHintDialog(true);
        }
    }

    private void gotoNewPage(String url) {
        saveStatistics(url, String.valueOf(System.currentTimeMillis()), null);
        if (url.toLowerCase().contains("heikuai.com") || url.toLowerCase().contains("cqsynet.com")) {
            if (!url.contains("userAccount=")) {
                if (url.contains("?")) {
                    url = url + "&userAccount=" + SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT);
                } else {
                    url = url + "?userAccount=" + SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT);
                }
            }
        }
        if (url.contains(AppConstants.COMMENT_PAGE)) {
            mBtnCollect.setVisibility(View.GONE);
            mBtnShare.setVisibility(View.GONE);
        } else {
            mBtnCollect.setVisibility(View.VISIBLE);
            mBtnShare.setVisibility(View.VISIBLE);
        }
        if (url.contains("wx.tenpay.com")) {
            mWebView.loadUrl(url, mExtraHeaders);
        } else {
            mWebView.loadUrl(url);
        }
        isCollect();
    }

    private void showHintDialog(final boolean isShowAdv) {
        mHintDialog = new SimpleDialog(this, getString(R.string.wifi_tip_not_connect),
                new SimpleDialog.MyDialogListener() {
                    @Override
                    public void onOkClick(View view) {
                        mHintDialog.dismiss();
                        mIsUpdate = true;
                        if ("fullAdv".equals(mFlag)) {
                            Intent fullAdv = new Intent(WebActivity.this, FullAdvActivity.class);
                            fullAdv.putExtra("source", "WifiActivity");
                            startActivity(fullAdv);
                        } else {
                            WifiActivity.launchForResult(WebActivity.this, isShowAdv);
                        }
                    }

                    @Override
                    public void onCloseClick(View view) {
                        mHintDialog.dismiss();
                        if (TextUtils.isEmpty(mWebView.getUrl())) {
                            finish();
                        }
                    }
                });
        mHintDialog.show();
    }

    private boolean isWhiteUrl(String url) {
        if (Globals.g_whiteList == null) {
            Globals.g_whiteList = BlackWhiteUrlDao.getWhiteList(this);
            if (Globals.g_whiteList == null) {
                Globals.g_whiteList = new ArrayList<>();
            }
        }
        if (Globals.g_whiteList.size() <= 0) {
            return false;
        }
        String[] urls = url.split("://");
        urls = urls[1].split("/");
        String newUrl = urls[0].split(":")[0];
        newUrl = newUrl.split("\\?")[0];
        for (String whiteUrl : Globals.g_whiteList) {
            if (whiteUrl.contains("*.")) {
                whiteUrl = whiteUrl.replace("*.", "");
            }
            if (newUrl.endsWith(whiteUrl)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlackUrl(String url) {
        if (Globals.g_blackList == null) {
            Globals.g_blackList = BlackWhiteUrlDao.getBlackList(this);
            if (Globals.g_blackList == null) {
                Globals.g_blackList = new ArrayList<>();
            }
        }
        if (Globals.g_blackList.size() <= 0) {
            return false;
        }
        if (url.contains("?")) {
            url = url.split("\\?")[0];
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        for (BlackUrlObject object : Globals.g_blackList) {
            if (url.equals(object.url)) {
                mIsShowAdv = Boolean.parseBoolean(object.showAdv);
                return true;
            }
        }
        return false;
    }

    /**
     * 调用收藏接口，并处理服务器返回信息
     */
    private void collect() {
        final CollectRequestBody collectRequestBody = new CollectRequestBody();
        collectRequestBody.id = "";
        if (!TextUtils.isEmpty(mType)) {
            collectRequestBody.type = mType;
        } else {
            collectRequestBody.type = "0";
        }
        if (!TextUtils.isEmpty(mTitle)) {
            collectRequestBody.title = mTitle;
        } else {
            collectRequestBody.title = mUrl;
        }
        if (!TextUtils.isEmpty(mImage)) {
            collectRequestBody.image = mImage;
        } else {
            collectRequestBody.image = "";
        }
        collectRequestBody.url = mUrl;
        if (!TextUtils.isEmpty(mSource)) {
            collectRequestBody.source = mSource;
        } else {
            collectRequestBody.source = "";
        }
        IResponseCallback collectCallbackIf = new IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    ResponseObject responseObj = gson.fromJson(response, ResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            mIsCollect = true;
                            mBtnCollect.setBackgroundResource(R.drawable.btn_collect_green_on);
                            ToastUtil.showToast(WebActivity.this, R.string.collect_success);
                            CollectCacheDao.insertData(WebActivity.this, mType, "", mTitle, mUrl, mImage, mSource,
                                    DateUtil.formatTime(System.currentTimeMillis(), "yyyy/MM/dd HH:mm"));
                        } else {
                            if (isLoaded()) {
                                ToastUtil.showToast(WebActivity.this, R.string.request_fail_warning);
                            }
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(WebActivity.this, R.string.request_fail_warning);
            }
        };
        // 调用接口发起登陆
        WebServiceIf.collect(this, collectRequestBody, collectCallbackIf);
    }

    private boolean isLoaded() {
        return mType != null
                && mTitle != null
                && mImage != null
                && mUrl != null
                && mSource != null;
    }

    private void removeCollect() {
        CollectRemoveRequestBody body = new CollectRemoveRequestBody();
        List<CollectRemoveInfo> infos = new ArrayList<>();
        CollectRemoveInfo info = new CollectRemoveInfo();
        info.type = mType;
        info.id = mId;
        info.url = mUrl;
        info.title = mTitle;
        infos.add(info);
        body.favorList = infos;
        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Gson gson = new Gson();
                    BaseResponseObject responseObj = gson.fromJson(response, BaseResponseObject.class);
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            mIsCollect = false;
                            mBtnCollect.setBackgroundResource(R.drawable.btn_collect_green_off);
                            ToastUtil.showToast(WebActivity.this, R.string.remove_collect_success);
                            CollectCacheDao.deleteDataByUrl(WebActivity.this, mUrl);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                ToastUtil.showToast(WebActivity.this, R.string.request_fail_warning);
            }
        };
        WebServiceIf.removeCollect(this, body, callback);
    }

    /**
     * 通过pk_id掉接口获取分享信息
     */
    private void getShare() {
        ShareObject shareObj = new ShareObject();
        if (mUrl.contains("heikuai.com")) {
            shareObj.setTitle(mTitle);
            shareObj.setText(mContent);
            shareObj.setTitleUrl(mUrl);
            shareObj.setImageUrl(mImage);
            shareObj.setUrl(mUrl);
            shareObj.setSite("嘿快");
            shareObj.setSiteUrl("www.heikuai.com");
            ShareDialog dialog = new ShareDialog(WebActivity.this, shareObj);
            dialog.show();
        } else {
            shareObj.setTitle(mTitle);
            shareObj.setText(mContent);
            shareObj.setTitleUrl(mUrl);
            shareObj.setImageUrl(mImage);
            shareObj.setUrl(mUrl);
            shareObj.setSite("外链");
            shareObj.setSiteUrl(mUrl);
            ShareDialog dialog = new ShareDialog(WebActivity.this, shareObj);
            dialog.show();
        }
    }

    /**
     * 判断此条资讯是否被收藏，因此显示不同的图标
     */
    private void isCollect() {
        boolean isSave = CollectCacheDao.queryByUrl(WebActivity.this, mUrl);
        if (isSave) {
            mBtnCollect.setBackgroundResource(R.drawable.btn_collect_green_on);//如果此资讯已被收藏，显示已被收藏的图标
            mIsCollect = true;
        } else {
            mIsCollect = false;
            mBtnCollect.setBackgroundResource(R.drawable.btn_collect_green_off);
        }
    }

    private void submitComment() {
        if (TextUtils.isEmpty(mEtComment.getText().toString().trim())) {
            return;
        }

        showProgressDialog(R.string.comment_submitting);

        CommentRequestBody body = new CommentRequestBody();
        body.type = "0";
        body.newsId = mId;
        body.levelOneId = "";
        body.levelTwoId = "";
        body.content = mEtComment.getText().toString();

        WebServiceIf.IResponseCallback callback = new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                dismissProgressDialog();
                if (!TextUtils.isEmpty(response)) {
                    Gson gson = new Gson();
                    CommentSubmitResponseObject object = gson.fromJson(response, CommentSubmitResponseObject.class);
                    ResponseHeader header = object.header;
                    if (AppConstants.RET_OK.equals(header.ret)) {
                        mDialog = CommentDialog.createDialog(WebActivity.this, R.drawable.ic_success, getString(R.string.comment_success));
                        mDialog.show();
                        mHdl.sendEmptyMessageDelayed(5, 1000);
                        if (!TextUtils.isEmpty(object.body.id)) {
                            CommentInfo commentInfo = new CommentInfo();
                            commentInfo.id = object.body.id;
                            commentInfo.userAccount = Globals.g_userInfo.userAccount;
                            commentInfo.nickname = Globals.g_userInfo.nickname;
                            commentInfo.headUrl = Globals.g_userInfo.headUrl;
                            commentInfo.userLevel = "";
                            commentInfo.content = mEtComment.getText().toString();
                            commentInfo.date = String.valueOf(System.currentTimeMillis());
                            commentInfo.like = "0";
                            commentInfo.likeCount = "0";
                            commentInfo.replyCount = "0";

                            String json = new Gson().toJson(commentInfo);
                            mWebView.loadUrl("javascript:window.heikuaiH5.addComment(" + json + ")");

                            if (!mCommentCount.isEmpty()) {
                                mCommentCount = String.valueOf(Integer.parseInt(mCommentCount) + 1);
                            } else {
                                mCommentCount = "1";
                            }
                            handleCommentLayout("", "0", mCommentCount);
                        }
                    }
                }

                mEtComment.setText("");
            }

            @Override
            public void onErrorResponse() {
                mDialog = CommentDialog.createDialog(WebActivity.this, R.drawable.ic_failure, getString(R.string.comment_fail));
                mDialog.show();
                mHdl.sendEmptyMessageDelayed(5, 1000);
            }
        };
        WebServiceIf.submitComment(WebActivity.this, body, callback);
    }

//    /**
//     * 检查url返回页面是否成功
//     *
//     * @param url
//     * @return
//     */
//    private boolean validStatusCode(String url) {
//        if (!url.toLowerCase().contains("http://") && !url.toLowerCase().contains("https://")) {
//            return false;
//        }
//        if (url.toLowerCase().contains("suning")) {
//            return false;
//        }
//        OkHttpClient mClient = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.MILLISECONDS).build();
//        try {
//            Request request = new Request.Builder().get().url(url).build();
//            Response response = mClient.newCall(request).execute();
//            if (!response.isSuccessful()) {
//                mErrorCount++;
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }

    /**
     * 设置页面
     *
     * @param url
     */
    private void initPage(String url) {
        mRlTip.setVisibility(View.GONE);
        if (url.toLowerCase().contains("http://www.heikuai.com/mobile/portal")) {
            //公司portal页
//            mBtnCollect.setVisibility(View.INVISIBLE);
//            mBtnShare.setVisibility(View.INVISIBLE);
            mBtnClose.setVisibility(View.INVISIBLE);
//            mRlOpenWifi.setVisibility(View.VISIBLE);
        } else if (!url.toLowerCase().contains("heikuai.com")) {
            //外网页面
//            mBtnCollect.setVisibility(View.INVISIBLE);
//            mBtnShare.setVisibility(View.INVISIBLE);
            mBtnClose.setVisibility(View.VISIBLE);
//            mRlOpenWifi.setVisibility(View.GONE);
            if (!AppUtil.isConnectWifi(WebActivity.this)) {
                mTvTip.setText("您当前正在使用移动数据网络，将耗费较多流量");
                mRlTip.setVisibility(View.VISIBLE);
            }
        }

        //没有用户信息时,不显示分享和收藏按钮
        if (TextUtils.isEmpty(SharedPreferencesInfo.getTagString(WebActivity.this, SharedPreferencesInfo.ACCOUNT))) {
            mBtnCollect.setVisibility(View.INVISIBLE);
            mBtnShare.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 判断是否为app内部界面的跳转
     *
     * @param url
     * @return
     */
    private boolean isToAppPage(String url) {
        boolean result = true;
        if (url.startsWith("heikuai://gallery")) { //图集
            if (url.split("=").length >= 2) {
                Intent intent = new Intent(WebActivity.this, GalleryActivity.class);
                intent.putExtra("id", url.split("=")[1]);
                startActivity(intent);
            }
        } else if (url.startsWith("heikuai://topic")) { //资讯专题
            if (url.split("=").length >= 2) {
                Intent intent = new Intent(WebActivity.this, TopicActivity.class);
                intent.putExtra("id", url.split("=")[1]);
                startActivity(intent);
            }
        } else if (url.startsWith("tel:")) { //拨打电话
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else if (url.startsWith("heikuai://miniProgram")) {
            IWXAPI api = WXAPIFactory.createWXAPI(this, AppConstants.WECHAT_APP_ID);
            WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
            url = url.split("\\?")[0]; //去掉参数
            String path = "";
            String[] ary = url.split("=");
            if (ary.length > 1) {
                req.userName = ary[1]; // 填小程序原始id
                if (ary.length > 2) {
                    path = ary[2];
                    if(!TextUtils.isEmpty(path)) {
                        try {
                            path = URLDecoder.decode(path, "utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                req.path = path;//拉起小程序页面的可带参路径，不填默认拉起小程序首页
//                req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
                api.sendReq(req);
            }
        } else {
            result = false;
        }
        return result;
    }

    /**
     * 统计页面访问数据
     *
     * @param url
     */
    private void saveStatistics(String url, String startTime, String endTime) {
        String tempUrl; //用于统计
        if (!url.contains("userAccount=")) {
            if (url.contains("?")) {
                tempUrl = url + "&userAccount=" + SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT);
            } else {
                tempUrl = url + "?userAccount=" + SharedPreferencesInfo.getTagString(this, SharedPreferencesInfo.ACCOUNT);
            }
        } else {
            tempUrl = url;
        }
        tempUrl = tempUrl + mFromInStatistics;
        StatisticsDao.saveWebVisitStatistics(this, tempUrl, startTime, endTime);

    }

    /**
     * 初始化页面打开的来源字段,用于统计
     *
     * @param from
     * @return
     */
    public static String initFrom(String from) {
        if (TextUtils.isEmpty(from)) {
            return "&sy001f=其它";
        }
        String f;
        if (from.equalsIgnoreCase("notification")) {
            f = "&sy001f=通知栏";
        } else if (from.equalsIgnoreCase("newsList")) {
            f = "&sy001f=资讯列表";
        } else if (from.equalsIgnoreCase("adv")) {
            f = "&sy001f=固定广告位";
        } else if (from.equalsIgnoreCase("messageCenter")) {
            f = "&sy001f=消息中心";
        } else if (from.equalsIgnoreCase("lottory")) {
            f = "&sy001f=抽奖";
        } else if (from.equalsIgnoreCase("collect")) {
            f = "&sy001f=收藏";
        } else {
            f = "&sy001f=其它";
        }
        return f;
    }


//    /**
//     * 滑动手势分发
//     * @param ev
//     * @return
//     */
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        mGestureDetector.onTouchEvent(ev);
//        return super.dispatchTouchEvent(ev);
//    }

    /**
     * 滑动手势
     */
    private class OnSlideGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // 参数解释：
            // e1：第1个ACTION_DOWN MotionEvent
            // e2：最后一个ACTION_MOVE MotionEvent
            // velocityX：X轴上的移动速度，像素/秒
            // velocityY：Y轴上的移动速度，像素/秒
            // 触发条件 ：
            // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
            if ((e1 == null) || (e2 == null)) {
                return false;
            }
            float xRatio = (e1.getX() - e2.getX()) / AppUtil.getScreenW(WebActivity.this); //横向移动的距离与屏幕宽度的比例
            float yRatio = Math.abs(e1.getY() - e2.getY()) / AppUtil.getScreenH(WebActivity.this); //纵向移动的距离与屏幕高度的比例的绝对值
            float velocityXRatio = velocityX / AppUtil.getScreenW(WebActivity.this); //每秒横向移动的距离与屏幕宽度的比例
//            System.out.println("@@@@@@@@@@@@   " + xRatio + "    " + yRatio + "    " + velocityX + "    " + velocityXRatio);
            System.out.println("@@@@@@@@@@@@@  " + yRatio + "  " + xRatio + "  " + velocityXRatio);
            if (yRatio < 0.06 && (xRatio > 0.3 || velocityXRatio < -2)) {
                // 向左滑动
                if(!TextUtils.isEmpty(mCommentStatus)) {
                    //0表示文章可以正常评论
                    CommentActivity.launch(WebActivity.this, mId, mCommentStatus, mCommentMessage);
                }
            } else if (yRatio < 0.06 && (xRatio < -0.3 || velocityXRatio > 2)) {
                // 向右滑动
                onBackPressed();
            }
            return false;
        }
    }
}

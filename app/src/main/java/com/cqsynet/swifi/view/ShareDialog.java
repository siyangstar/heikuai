package com.cqsynet.swifi.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.model.ShareObject;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.ShareUtil;

public class ShareDialog extends Dialog {

	private LinearLayout mIv_qq;// QQ好友
	private LinearLayout mIv_qzone;// QQ空间
	private LinearLayout mIv_wechat;// 微信好友
	private LinearLayout mIv_wechat_moments;// 微信朋友圈
	private LinearLayout mIv_sinaweibo;// 新浪微博
	private Button mBt_cancel;// 取消
	private ShareObject mShareObject;

	private Context mContext;

	public ShareDialog(Context context, ShareObject shareObject) {
		super(context, R.style.round_corner_dialog);
		mContext = context;
		mShareObject = shareObject;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_share);

		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.width = AppUtil.getScreenW((Activity) mContext) - AppUtil.dp2px(mContext, 48);
		getWindow().setAttributes(lp);
		mIv_qq = findViewById(R.id.LinearLayout_qqfriend);
		mIv_qzone = findViewById(R.id.linearLayout_qzone);
		mIv_wechat = findViewById(R.id.linearLayout_weixin);
		mIv_wechat_moments = findViewById(R.id.linearLayout_ciclefriend);
		mIv_sinaweibo = findViewById(R.id.LinearLayout_sinaweibo);
		mBt_cancel = findViewById(R.id.share_cancel);
		mIv_qq.setOnClickListener(new View.OnClickListener() {
			// QQ
			@Override
			public void onClick(View v) {
				mShareObject.setTag("QQ");
				ShareUtil.share(mContext.getApplicationContext(), mShareObject);
				dismiss();
			}
		});
		mIv_qzone.setOnClickListener(new View.OnClickListener() {
			// QZone
			@Override
			public void onClick(View v) {
				mShareObject.setTag("QZone");
				ShareUtil.share(mContext.getApplicationContext(), mShareObject);
				dismiss();
			}
		});
		mIv_wechat.setOnClickListener(new View.OnClickListener() {
			// 微信
			@Override
			public void onClick(View v) {
				mShareObject.setTag("Wechat");
				ShareUtil.share(mContext.getApplicationContext(), mShareObject);
				dismiss();
			}
		});
		mIv_wechat_moments.setOnClickListener(new View.OnClickListener() {
			// 微信朋友圈
			@Override
			public void onClick(View v) {
				mShareObject.setTag("CircleFriend");
				ShareUtil.share(mContext.getApplicationContext(), mShareObject);
				dismiss();
			}
		});
		mIv_sinaweibo.setOnClickListener(new View.OnClickListener() {
			// 新浪微博
			@Override
			public void onClick(View v) {
				mShareObject.setTag("SinaWeibo");
				ShareUtil.share(mContext.getApplicationContext(), mShareObject);
				dismiss();
			}
		});
		mBt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	
//	/**
//	 * 微信分享
//	 * 
//	 * @param context
//	 * @param title
//	 * @param content
//	 * @param filePath
//	 * @param backUrl
//	 * @param isFrendsCircle
//	 */
//	private static void shareWeiXinSDK(Activity context, String title,
//			String content, String filePath, String backUrl,
//			boolean isFrendsCircle) {
//		IWXAPI wxApi = WXAPIFactory.createWXAPI(context, "wxe979e60c57705d74");
//		wxApi.registerApp("wxe979e60c57705d74");
//		if (!wxApi.isWXAppInstalled()) {
//			ToastUtil.showToast(context, "未发现微信客户端");
//			return;
//		} else if (!wxApi.isWXAppSupportAPI()) {
//			ToastUtil.showToast(context, "微信客户端版本太低");
//			return;
//		}
//		WXWebpageObject webpage = new WXWebpageObject();
//		webpage.webpageUrl = "http://www.heikuai.com";
//
//		WXMediaMessage msg = new WXMediaMessage();
//		msg.title = title;
//		msg.mediaObject = webpage;
//		msg.description = content;
////		byte[] data = getBitmapBytes(filePath, THUMB_SIZE);
////		msg.thumbData = data;
//
//		SendMessageToWX.Req req = new SendMessageToWX.Req();
//		req.transaction = buildTransaction("webpage");
//		req.message = msg;
//		req.scene = isFrendsCircle ? SendMessageToWX.Req.WXSceneTimeline
//				: SendMessageToWX.Req.WXSceneSession;
//		wxApi.sendReq(req);
//	}

}

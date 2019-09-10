package com.cqsynet.swifi.util;

import android.content.Context;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.model.AdvInfoObject;
import com.cqsynet.swifi.model.AdvResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.google.gson.Gson;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class AdvDataHelper {

	private Context mContext;
	private static List<AdvInfoObject> mAdvData = new ArrayList<AdvInfoObject>();
	private OnLoadAdvCallbackListener mAdvCallbackListener;
	private boolean mIsRequesting = false; // 是否正在请求广告数据接口
	
	public AdvDataHelper(Context cxt, OnLoadAdvCallbackListener listener) {
		mContext = cxt;
		mAdvCallbackListener = listener;
	}
	
	/**
	 * 广告位数据
	 * @return
	 */
	public List<AdvInfoObject> getAdvData() {
//		if(mAdvData.isEmpty() && !mIsRequesting) {
//		if(!mIsRequesting) {
//			loadAdvData();
//		}
		return mAdvData;
	}
	
	/**
	 * 调用接口加载广告位数据
	 */
	public void loadAdvData() {
    	// 调用接口
		mIsRequesting = true;
        WebServiceIf.getAdv(mContext, new WebServiceIf.IResponseCallback() {
			@Override
			public void onResponse(String response) throws JSONException {
				if(Globals.DEBUG) {
                	System.out.println("获取广告接口返回：" + response);
                }
				mIsRequesting = false;
				if (response != null) {
                    Gson gson = new Gson();
                    AdvResponseObject responseObj = gson.fromJson(response, AdvResponseObject.class);
                                        
                    ResponseHeader header = responseObj.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            try {
                            	if(responseObj.body != null && responseObj.body != null && responseObj.body.adList != null) {
                            		mAdvData.clear();
                            		mAdvData.addAll(responseObj.body.adList);
                            		if(mAdvCallbackListener != null) {
                            			mAdvCallbackListener.onLoadAdvCallback(mAdvData);
                            		}
                            	}
                                
                            } catch (Exception e) {
                                if(Globals.DEBUG) {
                                	System.out.println("获取广告失败：" + e.getMessage());
                                }
                            }
                        } else {
                            if(Globals.DEBUG) {
                            	System.out.println("获取广告失败");
                            }
                        }
                    }
                }
			}
			
			@Override
			public void onErrorResponse() {
				if(Globals.DEBUG) {
					System.out.println("获取启广告失败");
				}
			}
		});
    }
	
	public interface OnLoadAdvCallbackListener {
		void onLoadAdvCallback(List<AdvInfoObject> data);
	}
}

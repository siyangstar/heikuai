/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：扫描图形Activity
 *
 *
 * 创建标识：zhaosiyang 20170516
 */
package com.cqsynet.swifi.scaner;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.cqsynet.swifi.AppConstants;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.HkActivity;
import com.cqsynet.swifi.model.ARLotteryResponseObject;
import com.cqsynet.swifi.model.ResponseHeader;
import com.cqsynet.swifi.network.WebServiceIf;
import com.cqsynet.swifi.util.ToastUtil;
import com.cqsynet.swifi.view.IdentifyFailDialog;
import com.google.gson.Gson;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ScanerActivity extends HkActivity implements IImageIdentify {

    static {
        System.loadLibrary("caffe");
        System.loadLibrary("caffe_jni");
    }

    private PermissionListener mListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            if(requestCode == 100) {
                openScaner();
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            if(requestCode == 100) {
                if (AndPermission.hasAlwaysDeniedPermission(ScanerActivity.this, deniedPermissions)) {
                    AndPermission.defaultSettingDialog(ScanerActivity.this, 300).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scaner);

        if (AndPermission.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            depFile(getApplicationContext(), "ocr");
            if (!AndPermission.hasPermission(this, Manifest.permission.CAMERA)) {
                AndPermission.with(this)
                        .requestCode(100)
                        .callback(mListener)
                        .permission(Manifest.permission.CAMERA)
                        .rationale(new RationaleListener() {
                            @Override
                            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                                AndPermission.rationaleDialog(ScanerActivity.this, rationale).show();
                            }
                        })
                        .send();
            } else {
                if (null == savedInstanceState) {
                    openScaner();
                }
            }
        } else {
            Toast.makeText(ScanerActivity.this, "请先打开sd卡权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 300) {
            if (!AndPermission.hasPermission(ScanerActivity.this, Manifest.permission.CAMERA)) {
                AndPermission.with(ScanerActivity.this)
                        .requestCode(100)
                        .callback(mListener)
                        .permission(Manifest.permission.CAMERA)
                        .rationale(new RationaleListener() {
                            @Override
                            public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                                AndPermission.rationaleDialog(ScanerActivity.this, rationale).show();
                            }
                        })
                        .send();
            } else {
                openScaner();
            }
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, new PermissionListener() {
//            @Override
//            public void onSucceed(int requestCode, List<String> grantPermissions) {
//                if (requestCode == 100) {
//                    openScaner();
//                }
//            }
//
//            @Override
//            public void onFailed(int requestCode, List<String> deniedPermissions) {
//                if (requestCode == 100) {
//                    if (AndPermission.hasAlwaysDeniedPermission(ScanerActivity.this, deniedPermissions)) {
//                        AndPermission.defaultSettingDialog(ScanerActivity.this, 300).show();
//                    }
//                }
//            }
//        });
//    }

    /**
     * 把assets文件夹内的指定文件或文件夹部署到sd卡
     *
     * @param con
     * @param path
     */
    public void depFile(Context con, String path) {
        try {
            String str[] = con.getAssets().list(path);
            if (str.length > 0) {
                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + AppConstants.CACHE_DIR + "/" + path);
                if (!file.exists() || !file.isDirectory()) {
                    boolean r = file.mkdirs();
                    System.out.println(r);
                }
                for (String string : str) {
                    depFile(con, path + "/" + string);
                }
            } else {
                File f = new File(Environment.getExternalStorageDirectory() + "/" + AppConstants.CACHE_DIR + "/" + path);
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                InputStream is = con.getAssets().open(path);
                FileOutputStream fos = new FileOutputStream(f);
                byte[] buffer = new byte[1024];
                while (true) {
                    int len = is.read(buffer);
                    if (len == -1) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开扫描功能
     */
    private void openScaner() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.flContainer_activity_scaner, ScanWithCamera2Fragment.newInstance())
                    .commit();
        } else {
            getFragmentManager().beginTransaction()
                    .replace(R.id.flContainer_activity_scaner, ScanWithCameraFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public void onIdentifySuccess() {
        final IScanFragment fragment = (IScanFragment) getFragmentManager().findFragmentById(R.id.flContainer_activity_scaner);
        fragment.showGiftAnimation();
        WebServiceIf.scanLottery(this, new WebServiceIf.IResponseCallback() {
            @Override
            public void onResponse(String response) throws JSONException {
                if (!TextUtils.isEmpty(response)) {
                    Log.i("ScanerActivity", "@@@response: " + response);
                    Gson gson = new Gson();
                    ARLotteryResponseObject object = gson.fromJson(response, ARLotteryResponseObject.class);
                    ResponseHeader header = object.header;
                    if (header != null) {
                        if (AppConstants.RET_OK.equals(header.ret)) {
                            ARLotteryResponseObject.ARLotteryResponseBody body = object.body;
                            fragment.updatePrize(body);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse() {
                Log.e("ScanerActivity", "onErrorResponse");
                ToastUtil.showToast(ScanerActivity.this, R.string.request_fail_warning);
            }
        });
    }

    @Override
    public void onIdentifyFail() {
        Dialog dialog = new IdentifyFailDialog(this);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                IScanFragment fragment = (IScanFragment) getFragmentManager()
                        .findFragmentById(R.id.flContainer_activity_scaner);
                fragment.scanPicture();
            }
        });
        dialog.show();
    }
}

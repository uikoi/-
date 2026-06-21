package com.aliyun.faceauthdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 阿里云金融级实人认证 Android Demo
 *
 * 使用前请：
 * 1. 把阿里云SDK的aar文件放到 app/libs/ 目录下
 * 2. 在 Config 中修改你的配置信息
 * 3. 搭建业务服务器
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FaceAuthDemo";

    // 权限请求码
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private Button btnStartAuth;
    private EditText etName;
    private EditText etIdCard;
    private TextView tvStatus;
    private TextView tvResult;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // SDK初始化状态
    private volatile boolean isSDKInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        requestPermissions();
    }

    private void initViews() {
        btnStartAuth = findViewById(R.id.btn_start_auth);
        etName = findViewById(R.id.et_name);
        etIdCard = findViewById(R.id.et_idcard);
        tvStatus = findViewById(R.id.tv_status);
        tvResult = findViewById(R.id.tv_result);

        btnStartAuth.setOnClickListener(v -> {
            if (isSDKInitialized) {
                startAuthFlow();
            } else {
                Toast.makeText(this, "SDK尚未初始化完成", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 第1步：请求必要权限
     */
    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            initSDK();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                initSDK();
            } else {
                updateStatus("权限被拒绝，无法使用认证功能");
                Toast.makeText(this, "请授予相机和录音权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 第2步：初始化SDK
     * 在当前页面初始化，保证初始化完成后再进行认证
     */
    private void initSDK() {
        updateStatus("正在初始化SDK...");

        new Thread(() -> {
            try {
                // 阿里云SDK初始化
                // 注意：需要导入阿里云SDK的aar后才能调用
                // ZIMFacade.install(MainActivity.this);

                // 模拟初始化过程
                Thread.sleep(500);

                isSDKInitialized = true;
                mainHandler.post(() -> {
                    updateStatus("SDK初始化成功 ✓");
                    btnStartAuth.setEnabled(true);
                });
            } catch (Exception e) {
                Log.e(TAG, "SDK初始化失败", e);
                mainHandler.post(() -> updateStatus("SDK初始化失败: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * 第3步：开始认证流程
     */
    private void startAuthFlow() {
        // 获取用户输入的姓名和身份证号
        String certName = etName.getText().toString().trim();
        String certNo = etIdCard.getText().toString().trim();
        
        if (certName.isEmpty()) {
            Toast.makeText(this, "请输入姓名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (certNo.isEmpty()) {
            Toast.makeText(this, "请输入身份证号", Toast.LENGTH_SHORT).show();
            return;
        }
        
        updateStatus("正在获取认证信息...");
        btnStartAuth.setEnabled(false);
        tvResult.setText("");

        // 获取MetaInfo
        String metaInfo = getMetaInfo();
        Log.d(TAG, "MetaInfo: " + metaInfo);

        // 向业务服务器发起请求获取CertifyId
        fetchCertifyId(metaInfo, certName, certNo);
    }

    /**
     * 获取MetaInfo（设备环境信息）
     */
    private String getMetaInfo() {
        // 注意：需要导入阿里云SDK的aar后才能调用
        // return ZIMFacade.getMetaInfos(this);

        // 模拟返回
        JSONObject metaInfo = new JSONObject();
        metaInfo.put("appName", "com.aliyun.faceauthdemo");
        metaInfo.put("appVersion", "1.0.0");
        metaInfo.put("deviceModel", android.os.Build.MODEL);
        metaInfo.put("deviceType", "android");
        metaInfo.put("osVersion", android.os.Build.VERSION.RELEASE);
        metaInfo.put("sdkVersion", "2.3.48");
        return metaInfo.toJSONString();
    }

    /**
     * 第4步：向业务服务器请求CertifyId
     */
    private void fetchCertifyId(String metaInfo, String certName, String certNo) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("metaInfo", metaInfo);
        requestBody.put("certName", certName);
        requestBody.put("certNo", certNo);

        RequestBody body = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(Config.SERVER_URL + "/api/getCertifyIdApp")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "获取CertifyId失败", e);
                mainHandler.post(() -> {
                    updateStatus("网络请求失败: " + e.getMessage());
                    // 网络失败时使用演示模式
                    startFaceVerify("DEMO_CERTIFY_ID_" + System.currentTimeMillis());
                    btnStartAuth.setEnabled(true);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String bodyStr = response.body().string();
                    Log.d(TAG, "获取CertifyId响应: " + bodyStr);
                    try {
                        JSONObject json = JSON.parseObject(bodyStr);
                        // 服务器返回格式: { success: true, data: { certifyId: "xxx" } }
                        JSONObject data = json.getJSONObject("data");
                        String certifyId = data != null ? data.getString("certifyId") : null;
                        if (certifyId != null && !certifyId.isEmpty()) {
                            mainHandler.post(() -> startFaceVerify(certifyId));
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "解析响应失败", e);
                    }
                }
                // 响应异常时使用演示模式
                mainHandler.post(() -> {
                    startFaceVerify("DEMO_CERTIFY_ID_" + System.currentTimeMillis());
                    btnStartAuth.setEnabled(true);
                });
            }
        });
    }

    /**
     * 第5步：调用SDK开始刷脸认证
     */
    private void startFaceVerify(String certifyId) {
        updateStatus("正在启动认证...");

        // ==========================================
        // 阿里云SDK认证调用（导入aar后生效）
        // ==========================================
        /*
        ZIMFacade.verify(certifyId, true, null, new ZIMCallback() {
            @Override
            public boolean response(ZIMResponse response) {
                mainHandler.post(() -> handleVerifyResult(response));
                return true;
            }
        });
        */

        // 演示模式：模拟认证流程
        mainHandler.postDelayed(() -> {
            // 模拟认证结果
            int mockCode = 1000; // 1000=成功, 1003=用户退出, 2002=网络错误, 2006=失败
            handleVerifyResult(mockCode, "模拟认证完成");
        }, 1500);
    }

    /**
     * 第6步：处理认证结果
     */
    private void handleVerifyResult(int code, String message) {
        String resultTitle;
        switch (code) {
            case 1000:
                resultTitle = "认证成功 ✓";
                break;
            case 1003:
                resultTitle = "用户取消认证";
                break;
            case 2002:
                resultTitle = "网络错误";
                break;
            case 2006:
                resultTitle = "认证失败";
                break;
            default:
                resultTitle = "未知错误 (code=" + code + ")";
                break;
        }

        tvResult.setText(resultTitle);
        updateStatus("认证完成: " + message);
        btnStartAuth.setEnabled(true);
    }

    /**
     * 阿里云SDK回调处理（正式版使用）
     */
    /*
    private void handleVerifyResult(ZIMResponse response) {
        String resultTitle;
        switch (response.code) {
            case 1000:
                resultTitle = "认证成功 ✓";
                // 建议进一步调用服务端接口确认最终结果
                queryVerifyResult(response.certifyId);
                break;
            case 1003:
                resultTitle = "用户取消认证";
                break;
            case 2002:
                resultTitle = "网络错误";
                break;
            case 2006:
                resultTitle = "认证失败";
                break;
            default:
                resultTitle = "未知错误";
                break;
        }

        String finalResultTitle = resultTitle;
        mainHandler.post(() -> {
            tvResult.setText(finalResultTitle);
            updateStatus("认证完成; 主码:" + response.code +
                    ", 子码:" + response.retCodeSub +
                    ", 描述:" + response.retMessageSub);
            btnStartAuth.setEnabled(true);
        });
    }
    */

    /**
     * 第7步（可选）：查询服务端认证结果
     */
    private void queryVerifyResult(String certifyId) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("certifyId", certifyId);

        RequestBody body = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(Config.SERVER_URL + "/api/verifyResultH5")
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "查询认证结果失败", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String bodyStr = response.body().string();
                    Log.d(TAG, "查询认证结果: " + bodyStr);
                    try {
                        JSONObject json = JSON.parseObject(bodyStr);
                        // 服务器返回格式: { success: true, data: { passed: true/false } }
                        JSONObject data = json.getJSONObject("data");
                        boolean passed = data != null ? data.getBooleanValue("passed", false) : false;
                        mainHandler.post(() -> {
                            if (passed) {
                                Toast.makeText(MainActivity.this, "服务端确认认证通过", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "解析结果失败", e);
                    }
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateStatus(String status) {
        tvStatus.setText("当前状态: " + status);
        Log.d(TAG, status);
    }
}
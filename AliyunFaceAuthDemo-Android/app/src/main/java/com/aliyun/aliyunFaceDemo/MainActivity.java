package com.aliyun.aliyunFaceDemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alipay.face.api.ZIMCallback;
import com.alipay.face.api.ZIMFacade;
import com.alipay.face.api.ZIMFacadeBuilder;
import com.alipay.face.api.ZIMResponse;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_URL = "https://blend-twelve-clamor.ngrok-free.dev";

    private EditText etName;
    private EditText etIdCard;
    private Button btnStart;
    private Handler handler = new Handler(Looper.getMainLooper());
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ZIMFacade.install(this);

        etName = findViewById(R.id.et_name);
        etIdCard = findViewById(R.id.et_idcard);
        btnStart = findViewById(R.id.btn_start_verify);

        if (btnStart != null) {
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = etName.getText().toString().trim();
                    String idCard = etIdCard.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(MainActivity.this, "请输入姓名", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(idCard)) {
                        Toast.makeText(MainActivity.this, "请输入身份证号", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    btnStart.setEnabled(false);
                    btnStart.setText("正在获取认证...");

                    String metaInfo = ZIMFacade.getMetaInfos(MainActivity.this);
                    fetchCertifyId(metaInfo, name, idCard);
                }
            });
        }
    }

    private void fetchCertifyId(String metaInfo, String certName, String certNo) {
        try {
            JSONObject json = new JSONObject();
            json.put("metaInfo", metaInfo);
            json.put("certName", certName);
            json.put("certNo", certNo);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(SERVER_URL + "/api/getCertifyIdApp")
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    handler.post(() -> {
                        Toast.makeText(MainActivity.this, "网络请求失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnStart.setEnabled(true);
                        btnStart.setText("开始人脸认证");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String bodyStr = response.body().string();
                            JSONObject result = new JSONObject(bodyStr);
                            JSONObject data = result.optJSONObject("data");
                            if (data != null) {
                                String certifyId = data.optString("certifyId");
                                if (!TextUtils.isEmpty(certifyId)) {
                                    handler.post(() -> startVerify(certifyId));
                                    return;
                                }
                            }
                            handler.post(() -> {
                                Toast.makeText(MainActivity.this, "获取认证ID失败", Toast.LENGTH_SHORT).show();
                                btnStart.setEnabled(true);
                                btnStart.setText("开始人脸认证");
                            });
                        } catch (Exception e) {
                            handler.post(() -> {
                                btnStart.setEnabled(true);
                                btnStart.setText("开始人脸认证");
                            });
                        }
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "请求构造失败", Toast.LENGTH_SHORT).show();
            btnStart.setEnabled(true);
            btnStart.setText("开始人脸认证");
        }
    }

    private void startVerify(String certifyId) {
        btnStart.setText("正在认证...");

        ZIMFacade zimFacade = ZIMFacadeBuilder.create(MainActivity.this);
        HashMap<String, String> params = new HashMap<>();
        zimFacade.verify(certifyId, true, params, new ZIMCallback() {
            @Override
            public boolean response(ZIMResponse response) {
                handler.post(() -> {
                    if (null != response && 1000 == response.code) {
                        Toast.makeText(MainActivity.this,
                                "认证成功\ncode: " + response.code,
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                "认证失败\ncode: " + (response != null ? response.code : "null"),
                                Toast.LENGTH_LONG).show();
                    }
                    btnStart.setEnabled(true);
                    btnStart.setText("开始人脸认证");
                });
                return true;
            }
        });
    }
}
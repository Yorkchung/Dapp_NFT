package com.example.map_project;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.config.Config;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register {
    private static String URL_Data = "";
    private Config config = new Config();
    private ContractConnection connection = new ContractConnection();
    public void register(MapsActivity mapsActivity) throws JSONException, IOException {
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(mapsActivity);
        View register = mapsActivity.getLayoutInflater().inflate(R.layout.register, null);
        Button btn_register = (Button) register.findViewById(R.id.btn_register);
        EditText account = register.findViewById(R.id.RE_account);
        EditText passwd = register.findViewById(R.id.RE_passwd);
        Spinner group = register.findViewById(R.id.RE_group);
        EditText private_key = register.findViewById(R.id.RE_private_key);
        Spinner job = register.findViewById(R.id.RE_job);
        mbuilder.setView(register);
        mbuilder.setCancelable(false);
        Dialog dialog = mbuilder.create();
        dialog.show();
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    config.setPrivateKey(private_key.getText().toString());
                    JSONObject res = registerData(account.getText().toString(),passwd.getText().toString(),config.getCredentials().getAddress(),group.getSelectedItem().toString(),job.getSelectedItem().toString());
                    if(!res.equals("")){
                        dialog.cancel();
                        connection.setProfileContract();
                        connection.getProfile().minYORKMeta(new BigInteger(String.valueOf(1)),BigInteger.valueOf(0)).sendAsync().get();
                        new Login(mapsActivity,config.getPrivateKey(),config.getCredentials().getAddress());
                        store_address(mapsActivity,config.getCredentials().getAddress(),config.getPrivateKey());
                    }else{
                        Toast.makeText(mapsActivity.getApplicationContext(), "註冊失敗~", Toast.LENGTH_SHORT).show();
                    }
                    return;
                } catch (JSONException | IOException | ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private JSONObject registerData(String account, String passwd,String address, String group, String job) throws JSONException, ExecutionException, InterruptedException {

        Credentials credentials = config.getCredentials();
        String content = String.format("userid=%s&password=%s&account=%s&group=%s&job=%s", account,passwd,address,group,job);
        String json = getAPI(config.getRegister(),content);
        JSONObject obj = new JSONObject(json);
        if(obj.getString("success").equals("true")){
            Log.e("ok",obj.toString());
            return obj;
        }else{
            return null;
        }
    }
    private void store_address(MapsActivity mapsActivity,String address,String key) throws IOException, JSONException {
        String pvk_json = mapsActivity.getSharedPreferences("pvk", mapsActivity.MODE_PRIVATE)
                .getString("pvk", "");
        JSONObject obj = new JSONObject(pvk_json);
        obj.put(address,key);
        SharedPreferences pref = mapsActivity.getSharedPreferences("pvk", mapsActivity.MODE_PRIVATE);
        pref.edit().putString("pvk", obj.toString()).commit();
   }
    //取得url連線
    private String getAPI(String url,String content) {
        try {
            // 建立 OkHttpClient
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            //傳遞JSON資料
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody.create(mediaType,
                    content
            );

            // 建立 Request，設定連線資訊
            Request request = new Request.Builder()
                    .post(body)
                    .url(url)
                    .build();

            //等待回傳再繼續執行
            CountDownLatch countDownLatch = new CountDownLatch(1);
            // 建立 Call
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                String result = "";

                @Override
                public void onFailure(Call call, IOException e) {
                    result = "server回傳錯誤~";
                    Log.e("error", e.toString());
                    countDownLatch.countDown();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // 連線成功
                    result = response.body().string();
                    Log.e("url", result);
                    URL_Data = result;
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return URL_Data;
    }
}

package com.example.map_project;

import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.config.Config;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login {
    private static String URL_Data = "";
    private Config config;
    private Admin admin;
    private Credentials credentials;
    private ContractConnection connection = new ContractConnection();
    private MapsActivity mapsActivity;
    public Login(){}
    public Login(MapsActivity mapsActivity,String key,String address){
        this.mapsActivity = mapsActivity;
        this.config = new Config();
        config.setPrivateKey(key);
        admin = config.getAdmin();
        credentials = config.getCredentials();
        Log.e("text",address);
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(mapsActivity);
        View login = mapsActivity.getLayoutInflater().inflate(R.layout.login, null);
        Button btn_login = (Button) login.findViewById(R.id.btn_login);
//        Button btn_go_register = (Button) login.findViewById(R.id.btn_go_register);
        EditText acc = login.findViewById(R.id.account);
        EditText password = login.findViewById(R.id.passwd);
        mbuilder.setView(login);
        mbuilder.setCancelable(false);
        Dialog dialog = mbuilder.create();
        dialog.show();
        btn_login.setOnClickListener(new View.OnClickListener() {
            JSONObject obj = null;
            @Override
            public void onClick(View v) {
                try {
                    obj = login(acc.getText().toString(),password.getText().toString());
                    if(obj!=null){
                        dialog.dismiss();
                        change_Profile(obj);
                    }else{
                        Toast.makeText(mapsActivity.getApplicationContext(), "登入失敗~", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public JSONObject login(String account,String pwd) throws JSONException, IOException {
        //取得憑證
        Credentials credentials = config.getCredentials();
        String content = String.format("userid=%s&password=%s&account=%s", account,pwd,credentials.getAddress());
        Log.e("ok",content);
        String json = getAPI(config.getLogin(),content);
        JSONObject obj = new JSONObject(json);
        if(obj.getString("success").equals("true")){
            Log.e("ok",obj.toString());
            return obj;
        }else{
            return null;
        }
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
            Request request = null;
            if(content.equals("")){
                // 建立 Request，設定連線資訊
                request = new Request.Builder()
                        .url(url)
                        .build();
            }else {
                // 建立 Request，設定連線資訊
                request = new Request.Builder()
                        .post(body)
                        .url(url)
                        .build();
            }

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
    public void change_Profile(JSONObject data) throws JSONException {
        //修改nav_header_profile
        NavigationView navigationView = (NavigationView) mapsActivity.findViewById(R.id.nav_view);
        TextView owner = navigationView.getHeaderView(0).findViewById(R.id.owner);
        TextView job_txt = navigationView.getHeaderView(0).findViewById(R.id.job);
        owner.setText(credentials.getAddress());
        TextView alliance = navigationView.getHeaderView(0).findViewById(R.id.alliance);
        TextView name = navigationView.getHeaderView(0).findViewById(R.id.user_name);
        try {
            connection.setProfileContract();
            String NFT_index = connection.getProfile().tokenOfOwnerByIndex(credentials.getAddress(), new BigInteger(String.valueOf(0))).sendAsync().get().toString();
            String meta = connection.getProfile().tokenURI(new BigInteger(NFT_index)).sendAsync().get().toString();
            String url = meta;
            String json = getAPI(url,"");
            JSONObject obj = new JSONObject(json);
            String image_url = obj.getString("image");
            Log.e("geturl", image_url);
            //Toast.makeText(mapsActivity.getApplicationContext(), meta, Toast.LENGTH_SHORT).show();
            new DownloadImageTask((ImageView) mapsActivity.findViewById(R.id.imageView)).execute(image_url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        connection.setProfileContract();
        JSONObject msg = data.getJSONObject("message");
        String job = msg.getString("job");
        String group = msg.getString("group");
        String userid = msg.getString("userid");
        LinearLayout profile = mapsActivity.findViewById(R.id.nav_profile);
        Toolbar navtool = (Toolbar) mapsActivity.findViewById(R.id.toolbar);
        config.setAlliance(group);
        int color = group.equals("藍色")?R.drawable.side_nav_bar2:R.drawable.side_nav_bar;
        Drawable drawable = mapsActivity.getDrawable(color);
        navtool.setBackground(drawable);
        profile.setBackgroundResource(color);
        job_txt.setText(job);
        alliance.setText(String.format("陣營：%s", group));
        name.setText(String.format("%s", userid));
        mapsActivity.leftSlider();
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}

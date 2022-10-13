package com.example.map_project;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.config.Config;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Profile{
    private Config config;
    private Admin admin;
    private Credentials credentials;
    private ContractConnection connection = new ContractConnection();
    public Profile(){
        this.config = new Config();
        admin = config.getAdmin();
        credentials = config.getCredentials();
    }

    //地圖
    public void setMap(MapsActivity mapsActivity){
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(mapsActivity);
        View map = mapsActivity.getLayoutInflater().inflate(R.layout.map, null);
        ImageView img = map.findViewById(R.id.map_image);
        img.setImageResource(R.drawable.map);
        dialog_list.setView(map);
        Dialog dialog = dialog_list.create();
        dialog.setCancelable(true);
        dialog.show();
    }
    //擁有的地標
    public void getLand(String fileName,MapsActivity mapsActivity){
        ArrayList<String> list = new ArrayList<String>();
        try {
            AssetManager assetManager = mapsActivity.getApplicationContext().getAssets();
            //通過管理器開啟檔案並讀取
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open(fileName)));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            JSONObject obj = new JSONObject(stringBuilder.toString());
            JSONArray contract = obj.getJSONArray("contract");
            config.setLandmark_address(contract.getString(0));
            connection.setContract();
            JSONArray site = obj.getJSONArray("sitehash");
            for(int i=0;i<site.length();i++){
                if (connection.getKingOfLandmark().owner(site.getString(i)).sendAsync().get().equals(credentials.getAddress()))
                    list.add(site.getString(i));
            }

        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(mapsActivity);
        dialog_list.setTitle("佔領地標");
        dialog_list.setItems(list.toArray(new String[list.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog_list.show();
    }
    //購買道具
    public void buyProps(MapsActivity mapsActivity){
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(mapsActivity);
        View store = mapsActivity.getLayoutInflater().inflate(R.layout.store, null);
        Button attack_tools = (Button) store.findViewById(R.id.btn_buy_attack);
        Button protect_tools = (Button) store.findViewById(R.id.btn_buy_protect);
        dialog_list.setView(store);
        Dialog dialog = dialog_list.create();
        dialog.show();
        attack_tools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    connection.setATTACK_NFTContract();
                    connection.getYorkMeta().minYORKMeta(BigInteger.valueOf(1),BigInteger.valueOf(0)).sendAsync().get();
                    Toast.makeText(mapsActivity.getApplicationContext(), "挖幣成功", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        protect_tools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    connection.setPROTECT_NFTContract();
                    connection.getYorkMeta2().minYORKMeta(BigInteger.valueOf(1),BigInteger.valueOf(0)).sendAsync().get();
                    Toast.makeText(mapsActivity.getApplicationContext(), "挖幣成功", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //帳戶餘額
    public BigInteger getBalance(){
        BigInteger balance = null;
        try {
            balance = config.getAdmin().ethGetBalance(config.getCredentials().getAddress(), DefaultBlockParameter.valueOf("latest")).sendAsync().get().getBalance();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return balance;
    }
    //擁有的NFT道具
    public void getTools(MapsActivity mapsActivity){
        ArrayList<String> attack_list = new ArrayList<String>();
        try {
            int number = connection.getYorkMeta().balanceOf(config.getCredentials().getAddress()).sendAsync().get().intValue();
            for(int i=0;i<number;i++)
                attack_list.add(connection.getYorkMeta().tokenOfOwnerByIndex(credentials.getAddress(),new BigInteger(String.valueOf(i))).sendAsync().get().toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> protected_list = new ArrayList<String>();
        try {
            int number = connection.getYorkMeta2().balanceOf(credentials.getAddress()).sendAsync().get().intValue();
            for(int i=0;i<number;i++)
                protected_list.add(connection.getYorkMeta2().tokenOfOwnerByIndex(credentials.getAddress(),new BigInteger(String.valueOf(i))).sendAsync().get().toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(mapsActivity);
        View tools = mapsActivity.getLayoutInflater().inflate(R.layout.tools, null);
        Button btn_tools = (Button) tools.findViewById(R.id.btn_tools);
        TextView attack_tools = tools.findViewById(R.id.attack_tools);
        try {
            for (String list : attack_list) {
                String meta = connection.getYorkMeta().tokenURI(new BigInteger(list)).sendAsync().get().toString();
                String url = meta;
                String json = getAPI(url);
                JSONObject obj = new JSONObject(json);
                String image_url = obj.getString("image");
                attack_tools.append(image_url + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView protected_tools = tools.findViewById(R.id.protected_tools);
        try {
            for (String list2 : protected_list) {
                String meta = connection.getYorkMeta2().tokenURI(new BigInteger(list2)).sendAsync().get().toString();
                String url = meta;
                String json = getAPI(url);
                JSONObject obj = new JSONObject(json);
                String image_url = obj.getString("image");
                protected_tools.append(image_url + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog_list.setView(tools);
        dialog_list.setCancelable(false);
        Dialog dialog = dialog_list.create();
        btn_tools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    //遊戲規則
    public void rules(MapsActivity mapsActivity){
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(mapsActivity);
        View rule = mapsActivity.getLayoutInflater().inflate(R.layout.rule, null);
        ImageView img = rule.findViewById(R.id.rule_graph);
        img.setImageResource(R.drawable.graph);
        dialog_list.setView(rule);
        Dialog dialog = dialog_list.create();
        dialog.setCancelable(true);
        dialog.show();

    }
    //登出
    public void logout(MapsActivity mapsActivity) throws IOException, JSONException {
        //取得私鑰
        JSONObject pvk = null;
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(mapsActivity);
        View list = mapsActivity.getLayoutInflater().inflate(R.layout.account_list, null);
        Button btn_go_register = list.findViewById(R.id.btn_go_register);
        TableLayout tableLayout = list.findViewById(R.id.account_table);
        String pvk_json = mapsActivity.getSharedPreferences("pvk", mapsActivity.MODE_PRIVATE)
                .getString("pvk", "");
        Log.e("pvk",pvk_json);
        if(pvk_json.equals("")) {
            AssetManager assetManager = mapsActivity.getApplicationContext().getAssets();
            //通過管理器開啟檔案並讀取
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("private.json")));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            //取得私鑰
            pvk = new JSONObject(stringBuilder.toString());
        }else{
            pvk = new JSONObject(pvk_json);
        }
        SharedPreferences pref = mapsActivity.getSharedPreferences("pvk", mapsActivity.MODE_PRIVATE);
        pref.edit().putString("pvk", pvk.toString()).commit();
        mbuilder.setView(list);
        Dialog dialog = mbuilder.create();
        dialog.show();
        Iterator<String> iter = pvk.keys(); //This should be the iterator you want.
        while(iter.hasNext()){
            TextView address= new TextView(mapsActivity.getApplicationContext());
            TableRow row = new TableRow(mapsActivity.getApplicationContext());
            String account = iter.next();
            String key = pvk.getString(account);
            Log.e("address",account);
            address.setText(account);
            address.setWidth(800);
            address.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    new Login(mapsActivity,key,account);
                    Toast.makeText(mapsActivity.getApplicationContext(), account, Toast.LENGTH_SHORT).show();
                }
            });
            row.addView(address);
            tableLayout.addView(row);
        }
        btn_go_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                Register register = new Register();
                try {
                    register.register(mapsActivity);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String URL_Data = "";
    //取得url連線
    public String getAPI(String url) {
        try {
            // 建立 OkHttpClient
            OkHttpClient client = new OkHttpClient().newBuilder().build();

            // 建立 Request，設定連線資訊
            Request request = new Request.Builder()
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

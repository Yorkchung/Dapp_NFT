package com.example.map_project;

import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.config.Config;
import com.contract.KingOfLandmark;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LandmarkAction {
    private static String URL_Data = "";
    ContractConnection connection = new ContractConnection();
    Config config = new Config();
    KingOfLandmark king;
    StringBuilder stringBuilder;
    private int mark_distance = 200;

    public void addMark(GoogleMap mMap,MapsActivity mapsActivity) throws IOException, JSONException, ExecutionException, InterruptedException {
        AssetManager assetManager = mapsActivity.getApplicationContext().getAssets();
        //通過管理器開啟檔案並讀取
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("landmark_contract.json")));
        String line;
        stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        //取得地標地址
        JSONObject list = new JSONObject(stringBuilder.toString());
        JSONArray mark = list.getJSONArray("mark");
        JSONArray land_address = list.getJSONArray("contract");
        JSONArray site_hash = list.getJSONArray("sitehash");
        for(int i=0;i<mark.length();i++){
            String v = mark.getJSONArray(i).getString(0);
            String v1 = mark.getJSONArray(i).getString(1);
            config.setLandmark_address(land_address.getString(0));
            connection.setContract();
            String title = connection.getKingOfLandmark().name(site_hash.getString(i)).sendAsync().get();
            config.setSite(mark.getString(i),site_hash.getString(i));
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(v), Double.parseDouble(v1))).title(title));
        }
    }
    public BottomSheetDialog show_Bottom_sheet(Marker marker,MapsActivity mapsActivity,double distance) throws JSONException, ExecutionException, InterruptedException {
        String id = marker.getId().replace("m","");
        //取得地標地址
        JSONObject list = new JSONObject(stringBuilder.toString());
        JSONArray land_address = list.getJSONArray("contract");
        JSONArray mark_list = list.getJSONArray("mark");
        String v = mark_list.getJSONArray(Integer.parseInt(id)).getString(0);
        String v1 = mark_list.getJSONArray(Integer.parseInt(id)).getString(1);
        JSONArray site_hash = list.getJSONArray("sitehash");
        String hash = site_hash.getString(Integer.parseInt(id));
        config.setLandmark_address(land_address.getString(0));
        connection.setContract();
        /************顯示bottom sheet************/
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                mapsActivity, R.style.BottomSheetDialogTheme
        );
        View bottomSheetView = LayoutInflater.from(mapsActivity.getApplicationContext())
                .inflate(
                        R.layout.layout_bottom_sheet,
                        null
                );
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
        TextView txt = bottomSheetDialog.findViewById(R.id.textView2);
        txt.setText(marker.getTitle());//修改dialog標題
        String distanceDialog = "";
        distanceDialog = distance <= this.mark_distance ? "距離地標為：" + String.valueOf(distance) : "超過可攻擊範圍";
        TextView dis = bottomSheetDialog.findViewById(R.id.distance);
        dis.setText(distanceDialog);//與地標距離
        try {
            //設定地標擁有者
            TextView owner = bottomSheetDialog.findViewById(R.id.owner);
            String user = connection.getKingOfLandmark().owner(hash).sendAsync().get();
            if (!user.equals("0x0000000000000000000000000000000000000000"))
                owner.setText(connection.getKingOfLandmark().owner(hash).sendAsync().get());
            //存取地標紀錄
            TextView record = bottomSheetDialog.findViewById(R.id.record);
            Tuple3<String, BigInteger, BigInteger> record_contract = connection.getKingOfLandmark().attackhistory(hash).sendAsync().get();
            if (!user.equals("0x0000000000000000000000000000000000000000")) {
                record.setText(record_contract.getValue1() + "\nPrice：" + record_contract.getValue2().toString() + "\nTime：" + record_contract.getValue3().toString());
            }
            //設定地標位置
            TextView site = bottomSheetDialog.findViewById(R.id.site);
            site.setText(String.format("[%s,%s]",v,v1));
            //設定地標合約地址
            TextView marker_adr = bottomSheetDialog.findViewById(R.id.marker_address);
            marker_adr.setText(connection.getKingOfLandmark().getContractAddress());
            //設定生命、價值
            TextView life = bottomSheetDialog.findViewById(R.id.life);
            life.setText("生命：" + connection.getKingOfLandmark().life(hash).sendAsync().get().toString());
            TextView price = bottomSheetDialog.findViewById(R.id.price);
            BigInteger tmp = connection.getKingOfLandmark().tokenbalance(hash).sendAsync().get();
            price.setText("地標價值：" + tmp);
            //設定陣營資料
            TextView alliance = bottomSheetDialog.findViewById(R.id.alliance);
            String content = String.format("account=%s",user);
            String json = getAPI(config.getFetch(),content);
            JSONObject obj = new JSONObject(json);
            if (obj.getString("success").equals("true")) {
                alliance.setText(String.format("陣營：%s",obj.getJSONObject("message").getString("group") ));
            } else {
                alliance.setText("陣營：無");
            }
            //設定地標NFT
            TextView attNFT = bottomSheetDialog.findViewById(R.id.mark_attack);
            TextView proNFT = bottomSheetDialog.findViewById(R.id.mark_protect);
            this.getTools(attNFT,proNFT);

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ImageView img = bottomSheetDialog.findViewById(R.id.imageView);
        String url = connection.getKingOfLandmark().getLandURI(hash).sendAsync().get().toString();
        String json = getAPI(url,"");
        JSONObject obj = new JSONObject(json);
        String image_url = obj.getString("image");
        new DownloadImageTask((ImageView) img).execute(image_url);

        Log.e("test", String.valueOf(distance));
        return bottomSheetDialog;
    }
    //使用NFT操作地標
    public void nft_action(MapsActivity mapsActivity,String action,BottomSheetDialog bottomSheetDialog) throws JSONException, ExecutionException, InterruptedException {
        AlertDialog.Builder dialog_list = new AlertDialog.Builder(mapsActivity);
        View tools = mapsActivity.getLayoutInflater().inflate(R.layout.nft, null);
        Button close = tools.findViewById(R.id.btn_nft_tools);
        TextView title = tools.findViewById(R.id.nft_title);
        dialog_list.setView(tools);
        dialog_list.setCancelable(false);
        Dialog dialog = dialog_list.create();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        if(action.equals("attack")){
            title.setText("攻擊道具NFT");
            this.getAttackTools(dialog,tools,mapsActivity,bottomSheetDialog);
        }else if(action.equals("protect")){
            title.setText("防禦道具NFT");
            this.getProtectTools(dialog,tools,mapsActivity,bottomSheetDialog);
            mapsActivity.contract_content(bottomSheetDialog,new LandmarkAction());
        }
        dialog.show();
    }
    //攻擊道具
    public void getAttackTools(Dialog dialog,View tools,MapsActivity mapsActivity,BottomSheetDialog bottomSheetDialog){
        connection.setATTACK_NFTContract();
        ArrayList<String> attack_list = new ArrayList<String>();
        try {
            int number = connection.getYorkMeta().balanceOf(config.getCredentials().getAddress()).sendAsync().get().intValue();
            for(int i=0;i<number;i++)
                attack_list.add(connection.getYorkMeta().tokenOfOwnerByIndex(config.getCredentials().getAddress(), new BigInteger(String.valueOf(i))).sendAsync().get().toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TextView attack_tools = tools.findViewById(R.id.nft_tools);
        LinearLayout images = tools.findViewById(R.id.nft_image);
        try {
            for (String list : attack_list) {
                String meta = connection.getYorkMeta().tokenURI(new BigInteger(list)).sendAsync().get().toString();
                String url = meta;
                String json = getAPI(url,"");
                JSONObject obj = new JSONObject(json);
                String image_url = obj.getString("image");
                int value = obj.getInt("value");
//                attack_tools.append(image_url + "\n");
                LinearLayout ll = new LinearLayout(mapsActivity);
                ImageView imageView = new ImageView(mapsActivity);
                TextView data = new TextView(mapsActivity);
                Button btn = new Button(mapsActivity);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                params.setMargins(0,0,0,10);
                btn.setText("使用NFT");
                data.append(String.format("Value: %d\n",value));
                data.append(image_url + "\n");
                imageView.setLayoutParams(params);
                data.setLayoutParams(params);
                btn.setLayoutParams(params);
                btn.setBackgroundColor(mapsActivity.getResources().getColor(R.color.danger));
                ll.addView(data);
                ll.addView(btn);
                images.addView(imageView);
                images.addView(ll);
                new DownloadImageTask((ImageView) imageView).execute(image_url);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connection.setATTACK_NFTContract();
                        BigInteger id = BigInteger.valueOf(Long.parseLong(list));
                        try {
                            //取得hash的地標位置，用於傳入合約
                            TextView siteview = bottomSheetDialog.findViewById(R.id.site);
                            String site = siteview.getText().toString();
                            //attack
                            int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                            if ((contract_life - value) <= 100 && (contract_life - value) >= 0) {
                                //transfer NFT
                                connection.getYorkMeta().transferFrom(config.getCredentials().getAddress(),connection.getKingOfLandmark().getContractAddress(),id).sendAsync().get();
                                //合約攻擊
                                BigInteger dec_num = Convert.toWei(new BigDecimal(value*10), Convert.Unit.FINNEY).toBigInteger();
                                connection.getKingOfLandmark().becomeking_NFT(config.getSite(site),id,config.getAlliance(),dec_num).sendAsync().get();
                            }
                            //更新地標
                            mapsActivity.contract_content(bottomSheetDialog,new LandmarkAction());
                            Toast.makeText(mapsActivity.getApplicationContext(), "攻擊成功", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Toast.makeText(mapsActivity.getApplicationContext(), "攻擊失敗", Toast.LENGTH_SHORT).show();
                        } catch (InterruptedException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(mapsActivity.getApplicationContext(), "攻擊失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //防禦道具
    public void getProtectTools(Dialog dialog,View tools,MapsActivity mapsActivity,BottomSheetDialog bottomSheetDialog){
        connection.setPROTECT_NFTContract();
        ArrayList<String> protected_list = new ArrayList<String>();
        try {
            int number = connection.getYorkMeta2().balanceOf(config.getCredentials().getAddress()).sendAsync().get().intValue();
            for(int i=0;i<number;i++)
                protected_list.add(connection.getYorkMeta2().tokenOfOwnerByIndex(config.getCredentials().getAddress(), new BigInteger(String.valueOf(i))).sendAsync().get().toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TextView protected_tools = tools.findViewById(R.id.nft_tools);
        LinearLayout images = tools.findViewById(R.id.nft_image);
        try {
            for (String list2 : protected_list) {
                String meta = connection.getYorkMeta2().tokenURI(new BigInteger(list2)).sendAsync().get().toString();
                String url = meta;
                String json = getAPI(url,"");
                JSONObject obj = new JSONObject(json);
                String image_url = obj.getString("image");
                int value = obj.getInt("value");
//                protected_tools.append(image_url + "\n");
                LinearLayout ll = new LinearLayout(mapsActivity);
                ImageView imageView = new ImageView(mapsActivity);
                TextView data = new TextView(mapsActivity);
                Button btn = new Button(mapsActivity);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 1;
                params.setMargins(0,0,0,10);
                btn.setText("使用NFT");
                data.append(String.format("Value: %d\n",value));
                data.append(image_url + "\n");
                imageView.setLayoutParams(params);
                data.setLayoutParams(params);
                btn.setLayoutParams(params);
                btn.setBackgroundColor(mapsActivity.getResources().getColor(R.color.colorAccent));
                ll.addView(data);
                ll.addView(btn);
                images.addView(imageView);
                images.addView(ll);
                new DownloadImageTask((ImageView) imageView).execute(image_url);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connection.setATTACK_NFTContract();
                        BigInteger id = BigInteger.valueOf(Long.parseLong(list2));
                        try {
                            //取得hash的地標位置，用於傳入合約
                            TextView siteview = bottomSheetDialog.findViewById(R.id.site);
                            String site = siteview.getText().toString();
                            //protect
                            int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                            if ((contract_life + value) <= 100 && (contract_life + value) >= 0) {
                                //transfer NFT
                                connection.getYorkMeta2().transferFrom(config.getCredentials().getAddress(),connection.getKingOfLandmark().getContractAddress(),id).sendAsync().get();
                                //防禦
                                BigInteger dec_num = Convert.toWei(new BigDecimal(value*10), Convert.Unit.FINNEY).toBigInteger();
                                connection.getKingOfLandmark().protect_NFT(config.getSite(site),id,dec_num).sendAsync().get();
                            }
                            //更新地標
                            mapsActivity.contract_content(bottomSheetDialog,new LandmarkAction());
                            Toast.makeText(mapsActivity.getApplicationContext(), "防禦成功", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                            Toast.makeText(mapsActivity.getApplicationContext(), "防禦失敗", Toast.LENGTH_SHORT).show();
                        } catch (InterruptedException | JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(mapsActivity.getApplicationContext(), "防禦失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //取得url連線
    public String getAPI(String url,String content) {
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
    //擁有的NFT道具
    public void getTools(TextView attack_tools,TextView protected_tools){
        ArrayList<String> attack_list = new ArrayList<String>();
        try {
            int number = connection.getYorkMeta().balanceOf(connection.getKingOfLandmark().getContractAddress()).sendAsync().get().intValue();
            if(number>0)
                for(int i=0;i<number;i++)
                    attack_list.add(connection.getYorkMeta().tokenOfOwnerByIndex(connection.getKingOfLandmark().getContractAddress(),new BigInteger(String.valueOf(i))).sendAsync().get().toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> protected_list = new ArrayList<String>();
        try {
            int number = connection.getYorkMeta2().balanceOf(connection.getKingOfLandmark().getContractAddress()).sendAsync().get().intValue();
            if(number>0)
                for(int i=0;i<number;i++)
                    protected_list.add(connection.getYorkMeta2().tokenOfOwnerByIndex(connection.getKingOfLandmark().getContractAddress(),new BigInteger(String.valueOf(i))).sendAsync().get().toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if(attack_list.size()!=0)
                for (String list : attack_list) {
                    String meta = connection.getYorkMeta().tokenURI(new BigInteger(list)).sendAsync().get().toString();
                    String url = meta;
                    String json = getAPI(url,"");
                    JSONObject obj = new JSONObject(json);
                    String image_url = obj.getString("image");
                    attack_tools.append(image_url + "\n");
                }
            else{
                attack_tools.setText("無");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(protected_list.size()!=0)
                for (String list2 : protected_list) {
                    String meta = connection.getYorkMeta2().tokenURI(new BigInteger(list2)).sendAsync().get().toString();
                    String url = meta;
                    String json = getAPI(url,"");
                    JSONObject obj = new JSONObject(json);
                    String image_url = obj.getString("image");
                    protected_tools.append(image_url + "\n");
                }
            else{
                protected_tools.setText("無");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

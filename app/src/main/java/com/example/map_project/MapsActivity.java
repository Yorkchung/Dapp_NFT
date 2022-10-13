package com.example.map_project;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.ui.AppBarConfiguration;

import com.config.Config;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Convert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LocationManager locMgr;
    private String txt = "";
    private AppBarConfiguration mAppBarConfiguration;
    public static String URL_Data = "";
    //緯度
    Double mylatitude = 0.0;
    //經度
    Double mylongitude = 0.0;

    int mark_distance = 200;

    private ContractConnection connection = new ContractConnection();
    private Config config = new Config();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //詢問位置存取權限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        } else {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            setContentView(R.layout.activity_maps);
            connection.setATTACK_NFTContract();
            connection.setPROTECT_NFTContract();
            connection.setProfileContract();
            try {
                String data = connection.getYorkMeta().symbol().sendAsync().get().toString();
//                yorkMeta.minYORKMeta(new BigInteger(String.valueOf(1))).sendAsync().get();
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
                Log.e("NFT", data);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Connected Failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
//            config.setLandmark_address(contract_address);
            connection.setContract();
            //取得私鑰
            AssetManager assetManager = getApplicationContext().getAssets();
            //通過管理器開啟檔案並讀取
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("private.json")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                try {
                    if (!((line = bufferedReader.readLine()) != null)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stringBuilder.append(line);
            }
            try {
                bufferedReader.close();
                //取得私鑰
                JSONObject pvk = null;
                pvk = new JSONObject(stringBuilder.toString());
                String address = pvk.keys().next();
                config.setPrivateKey(pvk.getString(address));
                Log.e("start", pvk.getString(address));
                new Login(MapsActivity.this, config.getPrivateKey(), address);
            } catch (Exception e) {
                e.printStackTrace();
            }
            leftSlider();

            try {
                String tmp = connection.getKingOfLandmark().LandNFT().sendAsync().get().toString();
                Log.e("test", "connect to blockchain");
                Toast.makeText(getApplicationContext(), "connection succeeded", Toast.LENGTH_SHORT).show();
            } catch (ExecutionException e) {
                Log.e("test", "wrong");
                Toast.makeText(getApplicationContext(), "connection failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.e("test", "wrong");
                Toast.makeText(getApplicationContext(), "connection failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            //確認是否支援定位
            LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //詢問定位是否開啟
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                try {
                    Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//網路定位
                    if (location == null) {
                        location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);//手機gps定位
                    }
                    //緯度
                    //mylatitude = location.getLatitude();
                    mylatitude = 25.0334;
                    //經度
                    //mylongitude = location.getLongitude();
                    mylongitude = 121.5661;
                    txt = "Latitude:" + mylatitude + "    Longitude:" + mylongitude;
                    Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT).show();
                    Log.e("location", txt);
                } catch (SecurityException e) {
                    Log.e("map error", e.toString());
                    e.printStackTrace();
                }
            } else {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));//開啟定位
            }
        }

    }

    //slider method
    public void leftSlider() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Profile p = new Profile();
        try {
//            BigInteger balance = admin.ethGetBalance(credentials.getAddress(), DefaultBlockParameter.valueOf("latest")).sendAsync().get().getBalance();
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {//帳戶餘額
                    if (item.getItemId() == R.id.nav_token) {
                        BigInteger balance = p.getBalance();
                        Toast.makeText(getApplicationContext(), Convert.fromWei(balance.toString(), Convert.Unit.ETHER).toString(), Toast.LENGTH_SHORT).show();
                    }
                    if (item.getItemId() == R.id.nav_store) {//購買道具
                        p.buyProps(MapsActivity.this);
                    }
                    if (item.getItemId() == R.id.nav_home) {//查詢擁有地標
                        p.getLand("landmark_contract.json", MapsActivity.this);
                    }
                    if (item.getItemId() == R.id.nav_tools) {//擁有的NFT道具
                        p.getTools(MapsActivity.this);
                    }
                    if (item.getItemId() == R.id.nav_rules) {//查看規則
                        p.rules(MapsActivity.this);
                    }
                    if (item.getItemId() == R.id.map) {//地圖
                        p.setMap(MapsActivity.this);
                    }
                    if (item.getItemId() == R.id.nav_logout) {//登入其他帳號
                        try {
                            p.logout(MapsActivity.this);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Add  marker
        LandmarkAction land = new LandmarkAction();
        try {
            land.addMark(mMap, MapsActivity.this);
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);//允許定位
        mMap.getUiSettings().setZoomControlsEnabled(true);// 右下角的放大縮小功能
        mMap.getUiSettings().setCompassEnabled(true);// 左上角的指南針，要兩指旋轉才會出現
        mMap.getUiSettings().setMapToolbarEnabled(true);// 右下角的導覽及開啟 Google Map功能


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mylatitude, mylongitude), 16));// 放大地圖到 16 倍大
        //點擊地標
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                double distance = 0.0;
                if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    try {
                        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location == null) {
                            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                        //算距離
                        Location tmp = new Location("place");
                        tmp.setLatitude(marker.getPosition().latitude);
                        tmp.setLongitude(marker.getPosition().longitude);
                        distance = (int)location.distanceTo(tmp);
                        Toast.makeText(getApplicationContext(), String.valueOf(distance), Toast.LENGTH_SHORT).show();
                    } catch (SecurityException e) {
                        Log.e("test", e.toString());
                        e.printStackTrace();
                    }
                } else {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                /************顯示bottom sheet************/
                BottomSheetDialog bottomSheet = null;
                try {
                    bottomSheet = land.show_Bottom_sheet(marker, MapsActivity.this,distance);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                final BottomSheetDialog bottomSheetDialog = bottomSheet;
                //取得hash的地標位置，用於傳入合約
                TextView siteview = bottomSheetDialog.findViewById(R.id.site);
                String site = siteview.getText().toString();

                if (distance <= mark_distance) {//電子圍籬
                    bottomSheetDialog.findViewById(R.id.occupy).setOnClickListener(new View.OnClickListener() {//佔領按鈕事件
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.findViewById(R.id.attack).setVisibility(View.VISIBLE);
                            bottomSheetDialog.findViewById(R.id.protect).setVisibility(View.GONE);
                            bottomSheetDialog.findViewById(R.id.mark_NFT).setVisibility(View.GONE);
                            //Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.findViewById(R.id.attack1).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        //change price
                                        int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                                        if ((contract_life - 1) <= 100 && (contract_life - 1) >= 0) {
                                            BigInteger dec_num = Convert.toWei(new BigDecimal("10"), Convert.Unit.FINNEY).toBigInteger();
                                            connection.getKingOfLandmark().becomeking_ETH(config.getAlliance(),config.getSite(site),dec_num).sendAsync().get();
                                        }

                                        leftSlider();

                                        contract_content(bottomSheetDialog,land);//更改bottom_sheet資料

                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            bottomSheetDialog.findViewById(R.id.attack2).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        //change price
                                        int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                                        if ((contract_life - 5) <= 100 && (contract_life - 5) >= 0) {
                                            BigInteger dec_num = Convert.toWei(new BigDecimal("50"), Convert.Unit.FINNEY).toBigInteger();
                                            connection.getKingOfLandmark().becomeking_ETH(config.getAlliance(),config.getSite(site),dec_num).sendAsync().get();
                                        }

                                        //攻擊
//                                        connection.getKingOfLandmark().becomeking(new BigInteger("10")).sendAsync().get();
                                        leftSlider();

                                        contract_content(bottomSheetDialog,land);//更改bottom_sheet資料

                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            bottomSheetDialog.findViewById(R.id.attack3).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        //change price
                                        int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                                        if ((contract_life - 10) <= 100 && (contract_life - 10) >= 0) {
                                            BigInteger dec_num = Convert.toWei(new BigDecimal("100"), Convert.Unit.FINNEY).toBigInteger();
                                            connection.getKingOfLandmark().becomeking_ETH(config.getAlliance(),config.getSite(site),dec_num).sendAsync().get();
                                        }

                                        leftSlider();

                                        contract_content(bottomSheetDialog,land);//更改bottom_sheet資料

                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    bottomSheetDialog.findViewById(R.id.protect_Action).setOnClickListener(new View.OnClickListener() {//防禦按鈕事件
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.findViewById(R.id.attack).setVisibility(View.GONE);
                            bottomSheetDialog.findViewById(R.id.protect).setVisibility(View.VISIBLE);
                            bottomSheetDialog.findViewById(R.id.mark_NFT).setVisibility(View.GONE);
                            //Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.findViewById(R.id.protected1).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        //change price
                                        int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                                        if ((contract_life + 1) <= 100 && (contract_life + 1) >= 0) {
                                            BigInteger dec_num = Convert.toWei(new BigDecimal("10"), Convert.Unit.FINNEY).toBigInteger();
                                            connection.getKingOfLandmark().protect_ETH(config.getSite(site),dec_num).sendAsync().get();
                                        }

                                        leftSlider();

                                        contract_content(bottomSheetDialog,land);//更改bottom_sheet資料

                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            bottomSheetDialog.findViewById(R.id.protected2).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        //change price
                                        int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                                        if ((contract_life + 5) <= 100 && (contract_life + 5) >= 0) {
                                            BigInteger dec_num = Convert.toWei(new BigDecimal("50"), Convert.Unit.FINNEY).toBigInteger();
                                            connection.getKingOfLandmark().protect_ETH(config.getSite(site),dec_num).sendAsync().get();
                                        }

                                        leftSlider();

                                        contract_content(bottomSheetDialog,land);//更改bottom_sheet資料

                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            bottomSheetDialog.findViewById(R.id.protected3).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    try {
                                        //change price
                                        int contract_life = connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().intValue();
                                        if ((contract_life + 10) <= 100 && (contract_life + 10) >= 0) {
                                            BigInteger dec_num = Convert.toWei(new BigDecimal("100"), Convert.Unit.FINNEY).toBigInteger();
                                            connection.getKingOfLandmark().protect_ETH(config.getSite(site),dec_num).sendAsync().get();
                                        }

                                        leftSlider();

                                        contract_content(bottomSheetDialog,land);//更改bottom_sheet資料

                                    } catch (ExecutionException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                }
                bottomSheetDialog.findViewById(R.id.NFT_action).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.findViewById(R.id.attack).setVisibility(View.GONE);
                        bottomSheetDialog.findViewById(R.id.protect).setVisibility(View.GONE);
                        bottomSheetDialog.findViewById(R.id.mark_NFT).setVisibility(View.VISIBLE);
                        Button attack = bottomSheetDialog.findViewById(R.id.NFT_attack);
                        Button protect = bottomSheetDialog.findViewById(R.id.NFT_protect);
                        attack.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    land.nft_action(MapsActivity.this,"attack",bottomSheetDialog);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        protect.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    land.nft_action(MapsActivity.this,"protect",bottomSheetDialog);
                                    contract_content(bottomSheetDialog,land);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                /*************************************/
                marker.showInfoWindow();
                //Toast.makeText(getApplicationContext(), marker.getPosition().toString(), Toast.LENGTH_SHORT).show();//地標位置
                return false;
            }
        });
    }

    //更改bottom_sheet資料
    public void contract_content(BottomSheetDialog bottomSheetDialog,LandmarkAction land) throws ExecutionException, InterruptedException, JSONException {
        //取得hash的地標位置，用於傳入合約
        TextView siteview = bottomSheetDialog.findViewById(R.id.site);
        String site = siteview.getText().toString();
        //設定生命、價值
        TextView life = bottomSheetDialog.findViewById(R.id.life);
        life.setText("生命：" + connection.getKingOfLandmark().life(config.getSite(site)).sendAsync().get().toString());
        TextView price = bottomSheetDialog.findViewById(R.id.price);
        BigInteger tmp = connection.getKingOfLandmark().tokenbalance(config.getSite(site)).sendAsync().get();
        price.setText("地標價值：" + tmp);
        //設定地標擁有者
        TextView owner = bottomSheetDialog.findViewById(R.id.owner);
        String user = connection.getKingOfLandmark().owner(config.getSite(site)).sendAsync().get();
        if (!user.equals("0x0000000000000000000000000000000000000000"))
            owner.setText(connection.getKingOfLandmark().owner(config.getSite(site)).sendAsync().get());
        //存取地標紀錄
        TextView record = bottomSheetDialog.findViewById(R.id.record);
        Tuple3<String, BigInteger, BigInteger> record_contract = connection.getKingOfLandmark().attackhistory(config.getSite(site)).sendAsync().get();
        if (!user.equals("0x0000000000000000000000000000000000000000")) {
            record.setText(record_contract.getValue1() + "\nPrice：" + record_contract.getValue2().toString() + "\nTime：" + record_contract.getValue3().toString());
        }
        //設定陣營資料
        TextView alliance = bottomSheetDialog.findViewById(R.id.alliance);
        String content = String.format("account=%s",user);
        String json = land.getAPI(config.getFetch(),content);
        JSONObject obj = new JSONObject(json);
        if (obj.getString("success").equals("true")) {
            alliance.setText(String.format("陣營：%s",obj.getJSONObject("message").getString("group") ));
        } else {
            alliance.setText("陣營：無");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "位置換了~", Toast.LENGTH_SHORT);
        if (location != null) {
            String msg = String.format("%f , %f ", location.getLatitude(), location.getLatitude());
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            LatLng sydney = new LatLng(location.getLatitude(), location.getLatitude());
            Log.e("test", "位置換了~");
            mMap.addMarker(new MarkerOptions().position(sydney).title("自己~"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            //mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);// 右下角的放大縮小功能
            mMap.getUiSettings().setCompassEnabled(true);// 左上角的指南針，要兩指旋轉才會出現
            mMap.getUiSettings().setMapToolbarEnabled(true);// 右下角的導覽及開啟 Google Map功能

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16));// 放大地圖到 16 倍大

        } else {
            Toast.makeText(getApplicationContext(), "Location is null", Toast.LENGTH_SHORT);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}

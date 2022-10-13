package com.example.map_project;

import android.util.Log;

import com.config.Config;
import com.contract.KingOfLandmark;
import com.contract.YORKMeta;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;

import java.math.BigInteger;

public class ContractConnection {
    private Config config;
    private static Admin admin;
    private static Credentials credentials;
    private static KingOfLandmark kingOfLandmark;
    private static YORKMeta yorkMeta;
    private static YORKMeta yorkMeta2;
    private static YORKMeta profile;

    public YORKMeta getProfile() {
        return profile;
    }

    public KingOfLandmark getKingOfLandmark() {
        return kingOfLandmark;
    }

    public YORKMeta getYorkMeta() {
        return yorkMeta;
    }

    public YORKMeta getYorkMeta2() {
        return yorkMeta2;
    }

    public ContractConnection(){
        this.config = new Config();
        admin = config.getAdmin();
        credentials = config.getCredentials();
        Log.e("123",config.getPrivateKey());
    }
    //連線合約設定
    public KingOfLandmark setContract() {
        BigInteger GAS_PRICE = BigInteger.valueOf(15_000_000_000L);
        BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000L);
        String contractAddress = config.getLandmark_address();
        kingOfLandmark = KingOfLandmark.load(
                contractAddress,
                admin,
                credentials,
                GAS_PRICE,
                GAS_LIMIT
        );
        return this.kingOfLandmark;
    }
    //連線合約設定
    public YORKMeta setProfileContract() {
        BigInteger GAS_PRICE = BigInteger.valueOf(15_000_000_000L);
        BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000L);
        String contractAddress = config.getPROFILE_NFT_address();
        profile = YORKMeta.load(
                contractAddress,
                admin,
                credentials,
                GAS_PRICE,
                GAS_LIMIT
        );
        return this.profile;
    }

    //連線合約設定
    public YORKMeta setATTACK_NFTContract() {
        BigInteger GAS_PRICE = BigInteger.valueOf(15_000L);
        BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000L);
        String contractAddress = config.getATTACK_NFT_address();
        yorkMeta = YORKMeta.load(
                contractAddress,
                admin,
                Credentials.create(config.getPrivateKey()),
                GAS_PRICE,
                GAS_LIMIT
        );
        return yorkMeta;
    }

    //連線合約設定
    public YORKMeta setPROTECT_NFTContract() {
        BigInteger GAS_PRICE = BigInteger.valueOf(15_000L);
        BigInteger GAS_LIMIT = BigInteger.valueOf(4_300_000L);
        String contractAddress = config.getPROTECT_NFT_address();
        yorkMeta2 = YORKMeta.load(
                contractAddress,
                admin,
                Credentials.create(config.getPrivateKey()),
                GAS_PRICE,
                GAS_LIMIT
        );
        return yorkMeta2;
    }
}

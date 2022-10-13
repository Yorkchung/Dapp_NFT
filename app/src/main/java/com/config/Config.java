package com.config;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;

import java.util.Hashtable;

public class Config {
    private static String privateKey = "private key";
    private static String ATTACK_NFT_address = "attack contract address";
    private static String PROTECT_NFT_address = "protect contract address";
    private static String PROFILE_NFT_address = "profile contract address";
    private static String P2P_server = "blockchain ip";
    private static Admin admin;
    private static Credentials credentials;
    private static String Landmark_address = "game contract address";
    private static String server = "api server";
    private static String login = String.format("%s%s",server,"login");
    private static String register = String.format("%s%s",server,"register");
    private static String fetch = String.format("%s%s",server,"fetch");
    private static Hashtable<String, String> sitehash = new Hashtable<String, String>();
    private static String alliance;

    public String getAlliance() {
        return alliance;
    }

    public void setAlliance(String alliance) {
        Config.alliance = alliance;
    }

    public String getSite(String site) {
        return sitehash.get(site);
    }

    public void setSite(String site,String hash) {
        Config.sitehash.put(site,hash);
    }

    public static void setPROFILE_NFT_address(String PROFILE_NFT_address) {
        Config.PROFILE_NFT_address = PROFILE_NFT_address;
    }

    public String getPROFILE_NFT_address() {
        return PROFILE_NFT_address;
    }

    public String getFetch() {
        return fetch;
    }

    public String getLogin() {
        return login;
    }

    public String getRegister() {
        return register;
    }

    public String getLandmark_address() {
        return Landmark_address;
    }

    public void setLandmark_address(String landmark_address) {
        this.Landmark_address = landmark_address;
    }

    public String getP2P_server() {
        return P2P_server;
    }

    public void setP2P_server(String p2P_server) {
        this.P2P_server = p2P_server;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getATTACK_NFT_address() {
        return ATTACK_NFT_address;
    }

    public void setATTACK_NFT_address(String ATTACK_NFT_address) {
        this.ATTACK_NFT_address = ATTACK_NFT_address;
    }

    public String getPROTECT_NFT_address() {
        return PROTECT_NFT_address;
    }

    public void setPROTECT_NFT_address(String PROTECT_NFT_address) {
        this.PROTECT_NFT_address = PROTECT_NFT_address;
    }

    public Admin getAdmin() {
        admin = Admin.build(new HttpService(getP2P_server()));
        return admin;
    }

    private void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Credentials getCredentials() {
        this.credentials = Credentials.create(this.getPrivateKey());
        return credentials;
    }

    private void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}

package com.makarand.instashop.Models;

import android.os.Looper;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
@IgnoreExtraProperties
public class Product {
    String name, price, link, sellerId, sellerName;
    String productId;
    private HashMap<String, Object> timestamp;
    double latitude, longitude;
    public Product() {
    }

    public Product(String name, String price, String link, String sellerId, String sellerName, double latitude, double longitude) {
        this.name = name;
        this.price = price;
        this.link = link;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.timestamp = new HashMap<String, Object>();
        this.timestamp.put("date", ServerValue.TIMESTAMP);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSellerName() {
        if(sellerName == null) return "";
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public HashMap<String, Object> getTimestamp() {
        if(timestamp != null) return timestamp;

        this.timestamp = new HashMap<String, Object>();
        timestamp.put("date", ServerValue.TIMESTAMP);
        return this.timestamp;
    }

    public void setTimestamp(HashMap<String, Object> timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public long getTimeStamp(){
        if(this.timestamp == null) return 0;
        return (long) this.timestamp.get("date");
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

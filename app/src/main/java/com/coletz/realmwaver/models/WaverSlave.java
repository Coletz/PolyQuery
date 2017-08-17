package com.coletz.realmwaver.models;

import com.coletz.polyquery.annotation.PolyQuery;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@PolyQuery(Waver.class)
public class WaverSlave extends RealmObject implements Waver {

    @PrimaryKey
    private String serial;
    private int pumpCount;
    private String childData;

    public WaverSlave() {}

    public WaverSlave(String serial, int pumpCount, String childData) {
        this.serial = serial;
        this.pumpCount = pumpCount;
        this.childData = childData;
    }

    @Override
    public String getSerial() {
        return serial;
    }

    @Override
    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public int getPumpCount() {
        return pumpCount;
    }

    @Override
    public void setPumpCount(int pumpCount) {
        this.pumpCount = pumpCount;
    }

    public String getChildData() {
        return childData;
    }

    public void setChildData(String childData) {
        this.childData = childData;
    }
}

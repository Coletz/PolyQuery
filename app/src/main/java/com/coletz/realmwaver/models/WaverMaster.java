package com.coletz.realmwaver.models;

import com.coletz.polyquery.annotation.PolyQuery;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@PolyQuery(Waver.class)
public class WaverMaster extends RealmObject implements Waver {

    @PrimaryKey
    private String serial;
    private int pumpCount;
    private String childId;

    public WaverMaster() {}

    public WaverMaster(String serial, int pumpCount, String childId) {
        this.serial = serial;
        this.pumpCount = pumpCount;
        this.childId = childId;
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

    public String getChildId() {
        return childId;
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }
}

package com.coletz.realmwaver.models;

import com.coletz.polyquery.annotation.PolyQuery;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@PolyQuery(BaseObject.class)
public class StrangeObject extends RealmObject implements BaseObject {

    @PrimaryKey
    private String serial;
    private int pumpCount;
    private String childData;

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

package com.coletz.realmwaver.models;

import com.coletz.polyquery.annotation.PolyQuery;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@PolyQuery(BaseObject.class)
public class ComplexObject extends RealmObject implements BaseObject {

    @PrimaryKey
    private String serial;
    private int pumpCount;
    private String childId;

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

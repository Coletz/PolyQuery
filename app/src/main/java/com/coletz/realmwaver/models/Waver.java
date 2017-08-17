package com.coletz.realmwaver.models;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import io.realm.annotations.RealmModule;

public interface Waver extends RealmModel {
    @PrimaryKey String serial = null;
    String getSerial();
    void setSerial(String serial);

    int pumpCount = 0;
    int getPumpCount();
    void setPumpCount(int pumpCount);
}
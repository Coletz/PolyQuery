package com.coletz.realmwaver;

import com.coletz.polyquery.WaverPolyQuery;
import com.coletz.realmwaver.models.Waver;

import io.realm.Realm;

class RossmontStore {

    private Realm realm

    public RossmontStore(Realm realm){
        this.realm = realm
    }

    Waver order(String serial){
        return new WaverPolyQuery(realm).equalTo("serial", serial).queryFirst();
    }
    Waver count(){
        return new WaverPolyQuery(realm).count();
    }
}
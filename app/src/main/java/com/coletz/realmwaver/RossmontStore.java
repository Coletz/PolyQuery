package com.coletz.realmwaver;

import com.coletz.polyquery.WaverPolyQuery;
import com.coletz.realmwaver.models.Waver;

import io.realm.Realm;

class RossmontStore {

    Waver order(Realm realm, String serial){
        return new WaverPolyQuery(realm).equalTo("serial", serial).queryFirst();
    }
}
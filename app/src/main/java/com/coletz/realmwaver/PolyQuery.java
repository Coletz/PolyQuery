package com.coletz.realmwaver;

import com.coletz.polyquery.core.QueryBuilder;
import com.coletz.polyquery.core.SupportedOperation;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmQuery;
import java.util.*;

abstract class PolyQuery<R extends RealmModel>{

    private Realm realm;

    public PolyQuery(Realm realm){
        this.realm = realm;
    }

    // Magic happens here
    abstract RealmList<R> query();
    abstract R queryFirst();

    // Called from the annotation processor
    private <E extends RealmModel> RealmQuery<E> addAllParameters(RealmQuery<E> caller) {
        for (QueryBuilder param : queryParameters) {
            switch(param.getOperation()){
                case EQUAL_TO: equalTo(caller, param); break;
                case NOT_EQUAL_TO: notEqualTo(caller, param); break;
            }
        }
        return caller;
    }


    private ArrayList<QueryBuilder> queryParameters = new ArrayList<>();

    /**
     * Private functions used to convert the QueryBuilder to a real realm query for EVERY annotated class
     *
     * 17/08/2017: EQUAL_TO, NOT_EQUAL_TO
     **/
    private <E extends RealmModel> void equalTo(RealmQuery<E> caller, QueryBuilder builder) {
        Object val = builder.getValue();
        if(val != null){
            if(val instanceof Date){caller.equalTo(builder.getField(), (Date)val); } else
            if(val instanceof Boolean){caller.equalTo(builder.getField(), (Boolean)val); } else
            if(val instanceof Byte){caller.equalTo(builder.getField(), (Byte)val); } else
            if(val instanceof byte[]){caller.equalTo(builder.getField(), (byte[])val); } else
            if(val instanceof Double){caller.equalTo(builder.getField(), (Double)val); } else
            if(val instanceof Float){caller.equalTo(builder.getField(), (Float)val); } else
            if(val instanceof Integer){caller.equalTo(builder.getField(), (Integer)val); } else
            if(val instanceof Long){caller.equalTo(builder.getField(), (Long)val); } else
            if(val instanceof Short){caller.equalTo(builder.getField(), (Short)val); } else
            if(val instanceof String){caller.equalTo(builder.getField(), (String)val); }
        }
    }

    private <E extends RealmModel> void notEqualTo(RealmQuery<E> caller, QueryBuilder builder) {
        Object val = builder.getValue();
        if(val != null){
            if(val instanceof Date){caller.notEqualTo(builder.getField(), (Date)val); } else
            if(val instanceof Boolean){caller.notEqualTo(builder.getField(), (Boolean)val); } else
            if(val instanceof Byte){caller.notEqualTo(builder.getField(), (Byte)val); } else
            if(val instanceof byte[]){caller.notEqualTo(builder.getField(), (byte[])val); } else
            if(val instanceof Double){caller.notEqualTo(builder.getField(), (Double)val); } else
            if(val instanceof Float){caller.notEqualTo(builder.getField(), (Float)val); } else
            if(val instanceof Integer){caller.notEqualTo(builder.getField(), (Integer)val); } else
            if(val instanceof Long){caller.notEqualTo(builder.getField(), (Long)val); } else
            if(val instanceof Short){caller.notEqualTo(builder.getField(), (Short)val); } else
            if(val instanceof String){caller.notEqualTo(builder.getField(), (String)val); }
        }
    }


    /**
     * Public functions that user will call to add query parameters.
     * These parameters will be internally converted to real RealmQuery#equalTo when
     * the PolyQuery#query() or PolyQuery#queryFirst() are called
     *
     * 17/08/2017: EQUAL_TO, NOT_EQUAL_TO
     **/
    PolyQuery<R> equalTo(String field, Object value){
        queryParameters.add(new QueryBuilder(SupportedOperation.EQUAL_TO, field, value));
        return this;
    }

    PolyQuery<R> notEqualTo(String field, Object value){
        queryParameters.add(new QueryBuilder(SupportedOperation.NOT_EQUAL_TO, field, value));
        return this;
    }
}
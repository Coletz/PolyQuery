package com.coletz.realmwaver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.coletz.realmwaver.models.Waver;
import com.coletz.realmwaver.models.WaverMaster;
import com.coletz.realmwaver.models.WaverSlave;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends Activity {

    private TextView billTextView;
    private EditText billEditText;
    private Button random;
    private RossmontStore store;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(this);
        realm = Realm.getInstance(new RealmConfiguration.Builder().build());

        store = new RossmontStore();
        billTextView = findViewById(R.id.textView);
        billEditText = findViewById(R.id.editText);
        random = findViewById(R.id.random);

        billEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    try {
                        Waver waver = store.order(realm, textView.getText().toString());
                        String txt = "";
                        if(waver instanceof WaverMaster){
                            txt = ((WaverMaster)waver).getChildId();
                        }
                        if(waver instanceof WaverSlave){
                            txt = ((WaverSlave)waver).getChildData();
                        }
                        billTextView.setText(txt);
                    } catch (IllegalArgumentException iae){
                        billTextView.setText(iae.getMessage());
                    }
                    handled = true;
                }
                return handled;
            }
        });

        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if (new Random().nextBoolean()) {
                            realm.copyToRealmOrUpdate(new WaverMaster(billEditText.getText().toString(), new Random().nextInt(), "I am a master"));
                        } else {
                            realm.copyToRealmOrUpdate(new WaverSlave(billEditText.getText().toString(), new Random().nextInt(), "I am a child"));
                        }
                    }
                }, new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        Log.e("ERROR", error.getMessage());
                    }
                });
            }
        });
    }
}

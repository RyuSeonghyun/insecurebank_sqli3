package com.android.insecurebankv2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.View;

import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class LoginActivity extends Activity {


    private final String dbName = "webnautes";
    private final String tableName = "person";

    private String[] names;
    {
        names = new String[]{"root", "admin", "1q3v"};
    }

    private final String[] phones;
    {
        phones = new String[]{"1q2w3e4r@", "djemals11", "passw0rd!!"};
    }


    ArrayList<HashMap<String, String>> personList;
    ListView list;
    private static final String TAG_NAME = "name";
    private static final String TAG_PHONE ="phone";

    SQLiteDatabase sampleDB = null;
    ListAdapter adapter;

    private final String BROADCAST_MESSAGE = "com.example.myapplication.query";
    private BroadcastReceiver mReceiver = null;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();

    }

    private void registerReceiver(){
        /** 1. intent filter를 만든다
         *  2. intent filter에 action을 추가한다.
         *  3. BroadCastReceiver를 익명클래스로 구현한다.
         *  4. intent filter와 BroadCastReceiver를 등록한다.
         * */
        if(mReceiver != null) return;

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(BROADCAST_MESSAGE);

        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String receviedData = intent.getStringExtra("query");
                if(intent.getAction().equals(BROADCAST_MESSAGE)){
                    Toast.makeText(context, "recevied Data : "+receviedData, Toast.LENGTH_SHORT).show();
                    runQuery(receviedData);
                }
            }
        };

        this.registerReceiver(this.mReceiver, theFilter);

    }

    public void clickMethod(View v){
        /** 1. 전달할 메세지를 담은 인텐트 생성
         * 2. DATA를 잘 전달받는지 확인할 수 있게 Key, value 넣기
         * 3. sendBroadcast(intent); 메서드를 이용해서 전달할 intent를 넣고, 브로드캐스트한다. */

        String query = "INSERT INTO person (name, phone) Values ('attacker', '1234');";
        Intent intent = new Intent(BROADCAST_MESSAGE);
        intent.putExtra("query", query);
        sendBroadcast(intent);

    }

    /** 동적으로(코드상으로) 브로드 캐스트를 종료한다. **/
    private void unregisterReceiver() {
        if(mReceiver != null){
            this.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_main);

        list = (ListView) findViewById(R.id.listView);
        personList = new ArrayList<HashMap<String,String>>();


        try {
            sampleDB = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);

            //테이블이 존재하지 않으면 새로 생성합니다.
            sampleDB.execSQL("CREATE TABLE IF NOT EXISTS " + tableName
                    + " (name VARCHAR(20), phone VARCHAR(20) );");

            //테이블이 존재하는 경우 기존 데이터를 지우기 위해서 사용합니다.
            sampleDB.execSQL("DELETE FROM " + tableName  );

            //새로운 데이터를 테이블에 집어넣습니다..
            for (int i=0; i<names.length; i++ ) {
                sampleDB.execSQL("INSERT INTO " + tableName
                        + " (name, phone)  Values ('" + names[i] + "', '" + phones[i]+"');");
            }
            sampleDB.close();

        } catch (SQLiteException se) {
            Toast.makeText(getApplicationContext(),  se.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("", se.getMessage());
        }

        showList();
    }

    public void showList(){
        try {
            SQLiteDatabase ReadDB = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);

            //SELECT문을 사용하여 테이블에 있는 데이터를 가져옵니다..
            Cursor c = ReadDB.rawQuery("SELECT * FROM " + tableName, null);
            personList.clear();
            if (c != null) {

                if (c.moveToFirst()) {
                    do {
                        //테이블에서 두개의 컬럼값을 가져와서
                        String Name = c.getString(c.getColumnIndex("name"));
                        String Phone = c.getString(c.getColumnIndex("phone"));

                        //HashMap에 넣습니다.
                        HashMap<String,String> persons = new HashMap<String,String>();

                        persons.put(TAG_NAME,Name);
                        persons.put(TAG_PHONE,Phone);

                        //ArrayList에 추가합니다..
                        personList.add(persons);

                    } while (c.moveToNext());
                }
            }

            ReadDB.close();

            //새로운 apapter를 생성하여 데이터를 넣은 후..
            adapter = new SimpleAdapter(
                    this, personList, R.layout.list_item,
                    new String[]{TAG_NAME,TAG_PHONE},
                    new int[]{ R.id.name, R.id.phone}
            );

            //화면에 보여주기 위해 Listview에 연결합니다.
            list.setAdapter(adapter);

        } catch (SQLiteException se) {
            Toast.makeText(getApplicationContext(),  se.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("",  se.getMessage());
        }

    }

    public void runQuery(String query){
        try {
            sampleDB = this.openOrCreateDatabase(dbName, MODE_PRIVATE, null);
            sampleDB.execSQL(query);
            sampleDB.close();
        } catch (SQLiteException se) {
            Log.e("1q3v", se.getMessage());
        }
        showList();
    }


}

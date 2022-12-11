package com.hoanglinhsama.nodejssocketio;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private Socket socket; // object socket cho Socket.IO Client
    private ListView listViewUser, listViewMessage;
    private EditText editTextContent;
    private ImageButton imageButtonAddUser, imageButtonSend;
    private ArrayList<String> arrayListUser, arrayListMessage;
    private ArrayAdapter arrayAdapterUser, arrayAdapterMessage;

    private void mapping() {
        this.listViewUser = findViewById(R.id.listViewUser);
        this.listViewMessage = findViewById(R.id.listViewMessage);
        this.editTextContent = findViewById(R.id.editTextContent);
        this.imageButtonAddUser = findViewById(R.id.imageButtonAddUser);
        this.imageButtonSend = findViewById(R.id.imageSend);
    }

    private void initialization() {
        this.arrayListUser = new ArrayList<String>();
        this.arrayAdapterUser = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, arrayListUser);
        listViewUser.setAdapter(arrayAdapterUser);
        this.arrayListMessage = new ArrayList<String>();
        this.arrayAdapterMessage = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, arrayListMessage);
        listViewMessage.setAdapter(arrayAdapterMessage);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.mapping();
        this.initialization();
        try {
            socket = IO.socket("http://192.168.1.205:3000/"); // socket() de cho biet ket noi tu client(android) den server nao (dua vao dia chi cua server), tuong tu nhu phan PHP MySQL thi cung khong the de dia chi server la localhost
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.connect(); // de ket noi socket (thuc hien ket noi tu client den server theo duong dan uri da cung cap phia tren)
//        socket.emit("client-send-data", "Hoang Nguyen Quang Linh"); // socket.emit() de phat ra mot event tac dong den duy nhat server, 2 tham so la event name va gia tri muon gui di
//        socket.on("server-send-data", onRetrieveData); // lang nghe su kien gui ve tu server, tham so Listener fn (onRetrieveData) la 1 cai ham kieu Emitter.Listener, khi nao server emit ve dung cai event "server-send-data" nhu da cung cap thi onRetrieveData moi chay
        socket.on("server-send-result", onRetrieveResult); // client nhan ket qua ve viec dang ky tu erver
        imageButtonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextContent.getText().toString().trim().isEmpty()) // neu edittext khong rong thi moi dang ky ten nguoi dung duoc
                    socket.emit("client-register-user", editTextContent.getText().toString().trim()); // tao su kien dang ky nguoi dung den user
            }
        });
        socket.on("server-send-list-user", onRetrieveListUser); // client nhan ket qua ve danh sach cac user tu server
        imageButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editTextContent.getText().toString().trim().isEmpty())
                    socket.emit("client-send-message", editTextContent.getText().toString().trim());
            }
        });
        socket.on("server-send-message", onRetrieveMessage);
    }

    //    private Emitter.Listener onRetrieveData = new Emitter.Listener() { // doan code nay de lay ra gia tri cho onRetrieveData
//        @Override
//        public void call(Object... args) { // args la mot object hoac cung co the la mot array object
//            runOnUiThread(new Runnable() { // neu de mac dinh call(){} ben trong {} rong thi se khong thuc hien duoc gi, do do phai tao runOnUiThread() la tien trinh nho can them vao ben trong UIThread de co the chay va tra ve mot cai Emitter.Listener, de tu do on() co the chay khi dung su kien
//                @Override
//                public void run() {
//                    JSONObject jsonObject = (JSONObject) args[0]; // lay ra JSONObject nhan duoc tu server emit, o day do server chi gui 1 JSONObject nen la phan tu thu 0
//                    try {
//                        String content = jsonObject.getString("content");
//                        Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//    };
    private Emitter.Listener onRetrieveResult = new Emitter.Listener() { // doan code nay de lay ra gia tri cho onRetrieveResult
        @Override
        public void call(Object... args) { // args la mot object hoac cung co the la mot array object nhan duoc tu server
            runOnUiThread(new Runnable() { // neu de mac dinh call(){} ben trong {} rong thi se khong thuc hien duoc gi, do do phai tao runOnUiThread() la tien trinh nho can them vao ben trong UIThread de co the chay va tra ve mot cai Emitter.Listener, de tu do on() co the chay khi su kien trung khop ten
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0]; // lay ra JSONObject nhan duoc tu server emit, o day do server chi gui 1 JSONObject nen la phan tu thu 0
                    try {
                        Boolean exist = jsonObject.getBoolean("result");
                        if (exist)
                            Toast.makeText(MainActivity.this, "Account Already Exist !", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(MainActivity.this, "Register Successful !", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener onRetrieveListUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        JSONArray jsonArray = jsonObject.getJSONArray("listuser");
                        arrayListUser.clear(); // clear mang de du lieu khong bi trung lap du thua
                        for (int index = 0; index < jsonArray.length(); index++) {
                            arrayListUser.add(jsonArray.getString(index));
                        }
                        arrayAdapterUser.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    private Emitter.Listener onRetrieveMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = (JSONObject) args[0];
                    try {
                        arrayListMessage.add(jsonObject.getString("messagecontent"));
                        arrayAdapterMessage.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
}
package com.example.asus.gxchat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    //右上角按钮内容
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    //按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_clear:
                SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("msgs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                msgList.clear();
                editor.clear().apply();
                adapter.notifyDataSetChanged();
                break;
            case R.id.item_headchanger:
                if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ActivityCompat.requestPermissions(ChatActivity.this,new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    openAlbum();
                }
                break;
                default:
                    break;
        }
        return true;
    }

    public static final int CHOSE_PHOTO = 2;
    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private RecyclerView msgRV;
    private MsgAdapter adapter;
    private String acceptData = "";
    private String head1,head2;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        inputText = findViewById(R.id.et_input);
        Button send = findViewById(R.id.btn_send);
        msgRV = findViewById(R.id.rv_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        msgRV.setLayoutManager(linearLayoutManager);
        head1 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1551193087698&di=eca7cf559e959086e0d8ba669639779d&imgtype=0&src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F9f833ab2c793f14fc25ea5613690d6e4911b1e405f4a00-XLMIdn_fw658";
        initMsgs();
        readHeadPath();
        adapter = new MsgAdapter(msgList, head1, head2, ChatActivity.this);
        msgRV.setAdapter(adapter);
        inputText.setMovementMethod(getMovement());
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    post(msg);
                    msgList.add(msg);
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRV.scrollToPosition(msgList.size() - 1);
                    inputText.setText("");
                    acceptData = "";
                    save(msgList);
                }
            }
        });
    }

    //读取头像所在路径
    private void readHeadPath() {
        SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("imagePath", Context.MODE_PRIVATE);
        head2 = sharedPreferences.getString("imagePath",null);
    }

    //设置头像
    private void setHead(String imagePath) {
        head2 = imagePath;
    }

    //打开相册
    private void openAlbum(){
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(albumIntent, CHOSE_PHOTO);
    }

    //判断是否得到权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else
                    Toast.makeText(ChatActivity.this,"无权限",Toast.LENGTH_SHORT).show();
                break;
                default:
        }
    }

    //回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case CHOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    if (Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
                default:
                    break;
        }
    }

    //低版本获取路径
    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);
        save(imagePath);
    }

    //高版本获取路径
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        imagePath = getImagePath(uri,null);
        save(imagePath);
    }

    //获取路径方法
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //点击editetext时消息跳至最后一行
    private MovementMethod getMovement() {
        return new MovementMethod() {
            @Override
            public void initialize(TextView widget, Spannable text) {
            }

            @Override
            public boolean onKeyDown(TextView widget, Spannable text, int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyUp(TextView widget, Spannable text, int keyCode, KeyEvent event) {
                return false;
            }

            @Override
            public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
                return false;
            }

            @Override
            public void onTakeFocus(TextView widget, Spannable text, int direction) {
            }

            @Override
            public boolean onTrackballEvent(TextView widget, Spannable text, MotionEvent event) {
                return false;
            }

            @Override
            public boolean onTouchEvent(TextView widget, Spannable text, MotionEvent event) {
                return false;
            }

            @Override
            public boolean onGenericMotionEvent(TextView widget, Spannable text, MotionEvent event) {
                return false;
            }

            @Override
            public boolean canSelectArbitrarily() {
                if (msgList.size() != 0)
                    msgRV.smoothScrollToPosition(msgList.size() - 1);
                return false;
            }
        };
    }

    //保存消息记录
    public void save(List<Msg> msgList) {
        SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("msgs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String string = gson.toJson(msgList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("msg", string);
        editor.apply();
    }

    //保存头像路径
    public void save(String imagePath){
        SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("imagePath", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imagePath", imagePath);
        editor.apply();
    }

    //读取消息记录
    public void read() {
        SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("msgs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        if (sharedPreferences.getString("msg", null) != null) {
            msgList = gson.fromJson(sharedPreferences.getString("msg", null), new TypeToken<List<Msg>>() {
            }.getType());
        }
    }

    //添加回复消息
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    try {
                        JSONObject jsonObject = new JSONObject(acceptData);
                        JSONArray results = jsonObject.getJSONArray("results");
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject value = results.getJSONObject(i);
                            JSONObject values = value.getJSONObject("values");
                            Msg msgg = new Msg(values.getString("text"), Msg.TYPE_RECEIVED);
                            msgList.add(msgg);
                            adapter.notifyItemInserted(msgList.size() - 1);
                            msgRV.scrollToPosition(msgList.size() - 1);
                            save(msgList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    //默认内容，加载储存的消息
    private void initMsgs() {
        Msg msg1 = new Msg("Hello guy.", Msg.TYPE_RECEIVED);
        msgList.add(msg1);
        Msg msg2 = new Msg("Hello. Who is that?", Msg.TYPE_SENT);
        msgList.add(msg2);
        Msg msg3 = new Msg("This is Tom. Nice to meet you.", Msg.TYPE_RECEIVED);
        msgList.add(msg3);
        read();
    }

    //判断点击区域是否为消息列表区
    public boolean isClickEt(View view, MotionEvent event) {
        if (view != null && (view instanceof EditText)) {
            int[] leftTop = new int[2];
            view.getLocationOnScreen(leftTop);
            int top = leftTop[1];
            //获取状态栏高度
            int statusBarHeight1 = -1;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
            }
            //获取标题栏高度
            int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
            if ((event.getY() > top) || event.getY() < statusBarHeight1 + viewTop) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    //关健盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
    // 获取当前获得当前焦点所在View
            View view = getCurrentFocus();
            if (isClickEt(view, event)) {
    // 如果不是edittext，则隐藏键盘
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
    // 隐藏键盘
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(event);
        }

        if (getWindow().superDispatchTouchEvent(event)) {
            return true;
        }
        return onTouchEvent(event);
    }

    //string转json
    public JSONObject strToJson(String string) {
        JSONObject msg = new JSONObject();
        JSONObject text = new JSONObject();
        JSONObject inputtext = new JSONObject();
        JSONObject userinfo = new JSONObject();
        try {
            msg.put("perception", inputtext);
            inputtext.put("inputText", text);
            text.put("text", string);
            userinfo.put("apiKey", "e2e9e09addce4603a94497fdc733847e");
            userinfo.put("userId", "10");
            msg.put("userInfo", userinfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return msg;
    }

    //上传消息
    public void post(final Msg msg) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL("http://openapi.tuling123.com/openapi/api/v2");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    OutputStream outputStream = connection.getOutputStream();
                    PrintStream printStream = new PrintStream(outputStream);
                    JSONObject submitData = strToJson(msg.getContent());
                    printStream.print(submitData);
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        acceptData += line;
                    }
                    Message msg = Message.obtain();
                    msg.obj = acceptData;
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        readHeadPath();
        adapter = new MsgAdapter(msgList, head1, head2, ChatActivity.this);
        msgRV.setAdapter(adapter);
    }


}



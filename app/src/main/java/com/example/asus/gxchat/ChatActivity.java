package com.example.asus.gxchat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

    public static final int CHOSE_PHOTO = 2;
    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private RecyclerView msgRV;
    private MsgAdapter adapter;
    private String acceptData = "";
    private String head1;
    private String bg;
    private Path mPath = new Path("", Path.TYPE_HEAD);
    private long mPressedTime = 0;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;

    //按两次返回键退出程序
    @Override
    public void onBackPressed() {
        long mNowTime = System.currentTimeMillis();//获取第一次按键时间
        if ((mNowTime - mPressedTime) > 2000) {//比较两次按键时间差
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            mPressedTime = mNowTime;
        } else {//退出程序
            this.finish();
            System.exit(0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint({"RestrictedApi", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowStatusBarColor(ChatActivity.this, R.color.colorBlue);
        setContentView(R.layout.activity_chat);
        ActionBar actionBar = getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);
        }
        if (actionBar != null) {
            actionBar.hide();
        }
        linearLayout = findViewById(R.id.ll_chat);
        final Button menu = findViewById(R.id.btn_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(ChatActivity.this,menu);
                onClick onClick = new onClick();
                @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.popup, null);
                TextView clear = view.findViewById(R.id.tv_clear);
                TextView head = view.findViewById(R.id.tv_head);
                TextView background = view.findViewById(R.id.tv_background);
                TextView cancel = view.findViewById(R.id.tv_cancel);
                cancel.setOnClickListener(onClick);
                clear.setOnClickListener(onClick);
                head.setOnClickListener(onClick);
                background.setOnClickListener(onClick);
                popupWindow = new PopupWindow(view, msgRV.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setOutsideTouchable(false);
                popupWindow.setFocusable(true);
                popupWindow.showAtLocation(msgRV, Gravity.BOTTOM, 0, 0);
            }
        });
        inputText = findViewById(R.id.et_input);
        Button send = findViewById(R.id.btn_send);
        msgRV = findViewById(R.id.rv_msg);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        msgRV.setLayoutManager(linearLayoutManager);
        head1 = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1551193087698&di=eca7cf559e959086e0d8ba669639779d&imgtype=0&src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F9f833ab2c793f14fc25ea5613690d6e4911b1e405f4a00-XLMIdn_fw658";
        initMsgs();
        adapter = new MsgAdapter(msgList, head1, readHeadPath(), ChatActivity.this);
        msgRV.setAdapter(adapter);
        if (msgList.size() > 0){
            msgRV.scrollToPosition(msgList.size() - 1);
        }
        if (bg != null) {
            setBackground(bg);
        }
        // GroupView的监听
        inputText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //让View 也就是EditText获得焦点
                inputText.requestFocus();
                showSoftInput(ChatActivity.this, inputText);
                //通过handler保证在主线程中进行滑动操作
                handler.sendEmptyMessageDelayed(0,250);
            }
        });

        //触摸recyclerView的监听
        msgRV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //隐藏键盘
                hideSoftInput(ChatActivity.this, msgRV);
                return false;
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content) && isNetworkConnected(ChatActivity.this)) {
                    Msg msg = new Msg(content, Msg.TYPE_SENT);
                    post(msg);
                    msgList.add(msg);
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRV.scrollToPosition(msgList.size() - 1);
                    inputText.setText("");
                    acceptData = "";
                    save(msgList);
                } else if (!isNetworkConnected(ChatActivity.this)) {
                    Toast.makeText(ChatActivity.this, "网络连接中断", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //设置状态栏颜色
    public static void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));

                //底部导航栏
                //window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //item点击事件
    private class onClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_clear:
                    SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("msgs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    msgList.clear();
                    editor.clear().apply();
                    adapter.notifyDataSetChanged();
                    popupWindow.dismiss();
                    break;
                case R.id.tv_head:
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            popupWindow.dismiss();
                        }
                    } else {
                        openAlbum();
                        mPath.setType(Path.TYPE_HEAD);
                        popupWindow.dismiss();
                    }
                    break;
                case R.id.tv_background:
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            popupWindow.dismiss();
                        }
                    } else {
                        openAlbum();
                        mPath.setType(Path.TYPE_BACKROUND);
                        popupWindow.dismiss();
                    }
                case R.id.tv_cancel:
                    popupWindow.dismiss();
                default:
                    break;
            }
        }
    }

    //读取头像所在路径
    private String readHeadPath() {
        SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("imagePath", Context.MODE_PRIVATE);
        String head2 = sharedPreferences.getString("head", null);
        bg = sharedPreferences.getString("background", null);
        return head2;
    }

    //打开相册
    private void openAlbum() {
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(albumIntent, CHOSE_PHOTO);
    }

    //判断是否得到权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                else
                    Toast.makeText(ChatActivity.this, "无权限", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    //回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
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
        String imagePath = getImagePath(uri);
        mPath.setPath(imagePath);
    }

    //高版本获取路径
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitKat(Intent data) {
        String imagePath;
        Uri uri = data.getData();
        imagePath = getImagePath(uri);
        mPath.setPath(imagePath);
        //save(imagePath);
    }

    //获取路径方法
    private String getImagePath(Uri uri) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //设置背景
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setBackground(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        Drawable drawable = new BitmapDrawable(bitmap);
        linearLayout.setBackground(drawable);
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
    public void save(String imagePath, String key) {
        SharedPreferences sharedPreferences = ChatActivity.this.getSharedPreferences("imagePath", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, imagePath);
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
                case 0:
                    if (msgList.size() != 0 ) {
                    msgRV.smoothScrollToPosition(msgList.size() - 1);
                }
                    break;
            }
        }
    };

    //默认内容，加载储存的消息
    private void initMsgs() {
        Msg msg = new Msg("Hello guy.", Msg.TYPE_RECEIVED);
        msgList.add(msg);
        read();
    }

//    //判断点击区域是否为消息列表区
//    public boolean isClickEt(View view, MotionEvent event) {
//        if (view != null && (view instanceof EditText)) {
//            int[] leftTop = new int[2];
//            view.getLocationOnScreen(leftTop);
//            int top = leftTop[1];
//            //获取状态栏高度
//            int statusBarHeight1 = -1;
//            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//            if (resourceId > 0) {
//                statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
//            }
//            //获取标题栏高度
//            int viewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
//            if ((event.getY() > top) || event.getY() < statusBarHeight1 + viewTop) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    //关健盘
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            // 获取当前获得当前焦点所在View
//            View view = getCurrentFocus();
//            if (isClickEt(view, event)) {
//                // 如果不是edittext，则隐藏键盘
//                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (inputMethodManager != null) {
//                    // 隐藏键盘
//                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                }
//            } else {
//                i = 1;
//            }
//            return super.dispatchTouchEvent(event);
//        }
//
//        if (getWindow().superDispatchTouchEvent(event)) {
//            return true;
//        }
//        return onTouchEvent(event);
//    }

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onRestart() {
        super.onRestart();
        if (mPath.getType() == Path.TYPE_HEAD && !mPath.getPath().equals("")) {
            adapter = new MsgAdapter(msgList, head1, mPath.getPath(), ChatActivity.this);
            msgRV.setAdapter(adapter);
            save(mPath.getPath(), "head");
        } else if (mPath.getType() == Path.TYPE_BACKROUND && !mPath.getPath().equals("")) {
            setBackground(mPath.getPath());
            save(mPath.getPath(), "background");
        }
    }


    //判断网络是否连接
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    //弹出键盘
    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    //隐藏键盘
    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}



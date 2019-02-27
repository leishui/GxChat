package com.example.asus.gxchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText mEtAccount;
    private EditText mEtPassword;
    private SharedPreferences.Editor editor;
    private CheckBox mCbAccount;
    private CheckBox mCbPssword;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCbAccount = findViewById(R.id.cb_1);
        mCbPssword = findViewById(R.id.cb_2);
        mEtAccount = findViewById(R.id.et_account);
        mEtPassword = findViewById(R.id.et_password);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        boolean rememberA = sharedPreferences.getBoolean("rememberA",true);
        boolean rememberP = sharedPreferences.getBoolean("rememberP",false);
        Button mBtnLogin = findViewById(R.id.btn_login);
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isapCrrect();
                ischecked();
            }
        });
        isCreatChecked(sharedPreferences, rememberA, rememberP);
    }

    private void isCreatChecked(SharedPreferences sharedPreferences, boolean rememberA, boolean rememberP) {
        if (rememberA){
            mEtAccount.setText(sharedPreferences.getString("account",""));
            mCbAccount.setChecked(true);
        }else if (!rememberA){
            mCbAccount.setChecked(false);
        }
        if (rememberP){
            mEtPassword.setText(sharedPreferences.getString("password",""));
            mCbPssword.setChecked(true);
        }else if (!rememberP){
            mCbPssword.setChecked(false);
        }
    }

    private void isapCrrect() {
        if (mEtAccount.getText().toString().equals("")){
            Toast.makeText(MainActivity.this,"用户名不能为空",Toast.LENGTH_SHORT).show();
        }else if (mEtPassword.getText().toString().equals("")){
            Toast.makeText(MainActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
        }else if (mEtPassword.getText().toString().equals("12345") && mEtAccount.getText().toString().equals("12345")){
            Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
            finish();
        }else {
            Toast.makeText(MainActivity.this,"用户名或密码错误",Toast.LENGTH_SHORT).show();
        }
    }

    private void ischecked() {
        if (mCbAccount.isChecked()){
            editor.putBoolean("rememberA",true);
            editor.putString("account",mEtAccount.getText().toString());
            editor.apply();
        }else if (!mCbAccount.isChecked()){
            editor.putBoolean("rememberA",false);
            editor.apply();
        }
        if (mCbPssword.isChecked()){
            editor.putBoolean("rememberP",true);
            editor.putString("password",mEtPassword.getText().toString());
            editor.apply();
        }else if (!mCbAccount.isChecked()){
            editor.putBoolean("rememberP",false);
            editor.apply();
        }
    }
}

package com.java.memodemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {
    private EditText username,password;
    private CheckBox check_reme;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //1.绑定控件
        initView();
        //2.单击登录按钮，将用户名和密码保存
        btnLoginOnClick();
        //3.下次启动，直接显示用户名和密码
        displayinfo();

    }
    //绑定控件
    private void initView(){
        username = findViewById(R.id.username);
        password = findViewById(R.id.passwprd);
        check_reme = findViewById(R.id.checkBox_reme);
        login = findViewById(R.id.Login);
    }

    //登录按钮点击
    private void btnLoginOnClick(){
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //保存用户名和密码
                SharedPreferences.Editor editor = getSharedPreferences("myinfo",0).edit();
                editor.putString("name",username.getText().toString());
                editor.putString("pwd",password.getText().toString());
                editor.putBoolean("st",check_reme.isChecked());
                editor.apply();
                //跳转到下一个Activity
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    //下次启动，直接显示用户名和密码
    private void displayinfo(){
        String name = getSharedPreferences("myinfo",0).getString("name","");
        String pwd = getSharedPreferences("myinfo",0).getString("pwd","");
        Boolean status = getSharedPreferences("myinfo",0).getBoolean("st",false);
        if(status == true){
            username.setText(name);
            password.setText(pwd);
            check_reme.setChecked(true);
        }else {
            username.setText("");
            password.setText("");
            check_reme.setChecked(false);
        }
    }
}
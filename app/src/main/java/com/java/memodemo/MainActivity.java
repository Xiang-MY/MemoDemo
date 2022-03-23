package com.java.memodemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.java.memodemo.adapter.MemoAdapter;
import com.java.memodemo.bean.MemoBean;
import com.java.memodemo.db.MyDbHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private FloatingActionButton btn_add;
    private RecyclerView recyclerView;
    private MyDbHelper myDbHelper;
    List<MemoBean> arr = new ArrayList<>();
    List<MemoBean> filterS = new ArrayList<>();
    private SearchView msearch;
    SQLiteDatabase database;
    private MemoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));
        initView();
        btnOnClicknext();
        recyDisplay();
        msearch.setOnQueryTextListener(this);
    }

    //创建菜单，加载我们之前定义的menu_main.xml布局文件
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        btn_add = findViewById(R.id.btn_add);
        recyclerView = findViewById(R.id.recycleView);
        myDbHelper = new MyDbHelper(MainActivity.this);
        database = myDbHelper.getWritableDatabase();
        msearch = findViewById(R.id.search);
        msearch.setSubmitButtonEnabled(true);
    }

    private List<MemoBean> filter(String text){
       filterS = new ArrayList<>();
        for(MemoBean memoBean:arr){
            if(memoBean.getTitle().contains(text) || memoBean.getContent().contains(text) || memoBean.getTime().contains(text)){
                filterS.add(memoBean);
            }
        }
        return filterS;
    }

    private void btnOnClicknext(){
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,AddInfoActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void recyDisplay(){
        MemoBean memoBean;
        Cursor cursor = database.rawQuery("select * from tb_memory",null);
        while (cursor.moveToNext()){
            String myid = cursor.getString(cursor.getColumnIndex("_id"));
            String mytitle = cursor.getString(cursor.getColumnIndex("titel"));
            String mycontent = cursor.getString(cursor.getColumnIndex("content"));
            String mytime = cursor.getString(cursor.getColumnIndex("mtime"));
            String myimgpath = cursor.getString(cursor.getColumnIndex("imgpath"));
            memoBean = new MemoBean(myid,mytitle,mycontent,myimgpath,mytime);
            arr.add(memoBean);
        }
        cursor.close();
        adapter = new MemoAdapter(MainActivity.this,arr);
        StaggeredGridLayoutManager st = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(st);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        filterS = filter(s);
        adapter.setFiler(filterS);
        return false;
    }
}
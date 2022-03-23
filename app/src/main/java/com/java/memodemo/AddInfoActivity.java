package com.java.memodemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.UriUtils;
import com.bumptech.glide.Glide;
import com.java.memodemo.bean.MemoBean;
import com.java.memodemo.db.MyDbHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddInfoActivity extends AppCompatActivity {
    private EditText edit_title,edit_content;
    private ImageView img_preview;
    private String disp_path;
    private MyDbHelper myDbHelper;
    private SQLiteDatabase database;
    private Uri imageUri;
    private File outputImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_info);
        setSupportActionBar(findViewById(R.id.add_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        //绑定控件
        initView();
        recyDisply();

    }

    //加载菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                recyUpdate();
                this.finish();
                Intent intent = new Intent(AddInfoActivity.this,MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.add_enter,R.anim.add_exit);
                return true;
            case R.id.add_photo:
                String[] items = {"相机","相册"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       switch (i){
                           case 0:
                               btnCameraOnClick();
                               break;
                           case 1:
                               btnPhotoOnClick();
                               break;
                       }
                   }
                });
                builder.create().show();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void initView(){
        edit_title = findViewById(R.id.editText_title);
        edit_content = findViewById(R.id.editText_content);
        img_preview = findViewById(R.id.image_preview);
        myDbHelper = new MyDbHelper(AddInfoActivity.this);
        database = myDbHelper.getWritableDatabase();

    }

    private void btnCameraOnClick(){
        Time time = new Time();
        time.setToNow();
        String randtime = time.year + (time.month + 1) + time.monthDay + time.hour + time.minute + time.second + "";
        outputImage =new  File(getExternalCacheDir(),randtime+".jpg");
        Toast.makeText(AddInfoActivity.this,"outputImage==>"+outputImage,Toast.LENGTH_LONG).show();
        if (outputImage.exists()) {
            outputImage.delete();
        }
        try {
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           imageUri =FileProvider.getUriForFile(this, "com.java.memodemo.fileProvider", outputImage);
        } else {
           imageUri =Uri.fromFile(outputImage);
        }
       Intent intent =new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, 11);
    }

    private void btnPhotoOnClick(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,22);
    }


       @Override
       protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
           super.onActivityResult(requestCode, resultCode, data);
           switch (requestCode) {
               case 11:
                   if (resultCode == RESULT_OK) {
                       disp_path = outputImage.toString();
                       Glide.with(AddInfoActivity.this).load(disp_path).into(img_preview);
                   }
                   break;
               case 22:
                   Uri imageuri = data.getData();
                   if (imageuri == null) {
                       return;
                   }
                   disp_path = UriUtils.uri2File(imageuri).getPath();
                   Glide.with(AddInfoActivity.this).load(disp_path).into(img_preview);
                   break;
               default:
                   break;
           }
       }

       //插入到数据库
    private void saveToDb(){
        Time time = new Time();
        time.setToNow();
        ContentValues contentValues = new ContentValues();
        String title = edit_title.getText().toString();
        String content = edit_content.getText().toString();
        contentValues.put("titel",title);
        contentValues.put("content",content);
        contentValues.put("imgpath",disp_path);
        contentValues.put("mtime",time.year + "/" + (time.month + 1) + "/" +time.monthDay);
        if(edit_title.getText() == null && edit_content.getText() == null && disp_path == null){
            return;
        }
        database.insert("tb_memory",null,contentValues);
    }

    //从数据库显示
    private void recyDisply(){
        String myid = getIntent().getStringExtra("_id");
        if(myid == null){
            return;
        }
        String mysql = "select * from tb_memory where _id ="+myid;
        MemoBean memoBean = null;
        Cursor cursor = database.rawQuery(mysql,null);
        while (cursor.moveToNext()){
            String mytitle = cursor.getString(cursor.getColumnIndex("titel"));
            String mycontent = cursor.getString(cursor.getColumnIndex("content"));
            String mytime = cursor.getString(cursor.getColumnIndex("mtime"));
            String myimgpath = cursor.getString(cursor.getColumnIndex("imgpath"));
            memoBean = new MemoBean(myid,mytitle,mycontent,myimgpath,mytime);
        }
        edit_title.setText(memoBean.getTitle());
        edit_content.setText(memoBean.getContent());
        if(memoBean.getImgpath() != null){
            Glide.with(AddInfoActivity.this).load(memoBean.getImgpath()).into(img_preview);
        }
    }

    //更新数据库
    private void recyUpdate(){
        String myid = getIntent().getStringExtra("_id");
        if(myid == null){
            saveToDb();
            return;
        }
        Time time = new Time();
        time.setToNow();
        ContentValues contentValues = new ContentValues();
        String title = edit_title.getText().toString();
        String content = edit_content.getText().toString();
        contentValues.put("titel",title);
        contentValues.put("content",content);
        if(disp_path == null){
            String mysql = "select * from tb_memory where _id ="+myid;
            String myimgpath = null;
            Cursor cursor = database.rawQuery(mysql,null);
            while (cursor.moveToNext()){
               myimgpath = cursor.getString(cursor.getColumnIndex("imgpath"));
            }
            contentValues.put("imgpath",myimgpath);
        }else {
            contentValues.put("imgpath",disp_path);
        }
        contentValues.put("mtime",time.year + "/" + (time.month + 1) + "/" +time.monthDay);
        if(edit_title.getText() == null && edit_content.getText() == null && disp_path == null){
            return;
        }
        database.update("tb_memory",contentValues,"_id = ?",new String[]{myid});
        Toast.makeText(AddInfoActivity.this,"修改成功",Toast.LENGTH_LONG).show();
    }
}
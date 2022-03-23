package com.java.memodemo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.java.memodemo.AddInfoActivity;
import com.java.memodemo.MainActivity;
import com.java.memodemo.R;
import com.java.memodemo.bean.MemoBean;
import com.java.memodemo.db.MyDbHelper;

import java.util.List;
import java.util.Random;

import static com.blankj.utilcode.util.ActivityUtils.isActivityExistsInStack;
import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {
    private Context mcontext; //上下文
    private List<MemoBean> arr; //用来接收传入的带有备忘录所有数据的集合
    private MyDbHelper myDbHelper; //数据库帮助类对象
    private SQLiteDatabase database; //数据库对象

    //构造器，第一个参数是上下文，第二个参数是传入的带有备忘录所有数据的集合
    public MemoAdapter(Context mcontext, List<MemoBean> arr) {
        this.mcontext = mcontext;
        this.arr = arr;
    }

    @NonNull
    @Override                        //该方法是用于创建ViewHolder的实例.
    // 在这个布局中将子项布局recycler_item加载进来，然后创建一个ViewHolder的实例，并把加载出来的布局传入构造函数当中，最后将ViewHolder的实例返回。
    public MemoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.recycler_item,parent,false);
        ViewHolder mholder = new ViewHolder(view);
        return  mholder;
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override                 //该方法用于对RecyclerView子项的数据复制，会在每个子项被滚动到屏幕内的时候执行
    public void onBindViewHolder(@NonNull MemoAdapter.ViewHolder mholder, int position) {
        final MemoBean memoBean = arr.get(position);
        mholder.item_title.setText(memoBean.getTitle());
        mholder.item_content.setText(memoBean.getContent());
        mholder.item_time.setText(memoBean.getTime());
        Glide.with(mcontext).load(memoBean.getImgpath()).into(mholder.item_img);
        if(memoBean.getImgpath() ==null){ //如果图片地址为空，则不显示图片
         //   Toast.makeText(mcontext,"memoBean.getImgpath()"+memoBean.getImgpath(),Toast.LENGTH_LONG).show();
            mholder.item_img.setVisibility(View.GONE);
        }
        Random random = new Random();
        int color = Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256));
        GradientDrawable gradientDrawable = new GradientDrawable();

        //长按删除
        mholder.item_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mcontext);
                dialog.setMessage("确定删除吗？");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myDbHelper = new MyDbHelper(mcontext);
                        database = myDbHelper.getWritableDatabase();
                        database.delete("tb_memory","_id=?",new String[]{arr.get(position).getId()});
                        arr.remove(position);
                        Toast.makeText(mcontext,"删除成功",Toast.LENGTH_LONG).show();
                        notifyItemChanged(position);
                        dialogInterface.dismiss();
                    }
                });
                dialog.setNegativeButton("取消",null);
                dialog.setCancelable(false);
                dialog.create();
                dialog.show();
                return false;
            }
        });

        //点击显示
        mholder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mcontext, AddInfoActivity.class);
                intent.putExtra("_id",arr.get(position).getId());
                Toast.makeText(mcontext,"_id"+arr.get(position).getId(),Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });

    }

    @Override  //获取RecyclerView一共有多少个子项
    public int getItemCount() {
        return arr.size();
    }

    /*该内部类继承RecyclerView.ViewHolder。然后在ViewHolder的主构造方法中传入一个View参数，
    这个参数通常就是RecyclerView子项的最外层布局。
    通过这个最外层布局的参数，那么我们就可以通过findViewById（）方法来获取布局中各组件的实例了。*/
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView item_title,item_content,item_time;
        ImageView item_img;
        LinearLayout item_layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_content = itemView.findViewById(R.id.item_content);
            item_time = itemView.findViewById(R.id.item_time);
            item_img = itemView.findViewById(R.id.item_image);
            item_layout = itemView.findViewById(R.id.item_layout);
        }
    }
    //该方法用于搜索时过滤数据。
    public void setFiler(List<MemoBean> filterList){
        arr = filterList;
        notifyDataSetChanged();
    }
}

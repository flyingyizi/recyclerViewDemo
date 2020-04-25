package com.example.recyclerviewdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.image.ImageRequester;
import com.example.image.ImageRequesterResponse;
import com.example.image.DummyImage;
import com.example.image.Photo;
import com.example.recycler.RecyclerAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //作为View与Adapter间共同可见的数据
    private ArrayList<Photo> photosList = new ArrayList<Photo>();
    private ImageRequester<ImageRequesterResponse> imageRequester;

    private LinearLayoutManager linearLayoutManager;

    private RecyclerAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        //此处设置为Linear，比如还可以设置为GriadLayoutManager
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        setRecyclerViewScrollListener();
        //连接Adapter和RecyclerView
        adapter = new RecyclerAdapter(photosList);
        recyclerView.setAdapter(adapter);

        //采用DummyImage作为第三方数据源
        imageRequester = new DummyImage(new ImageRequesterResponse() {
            @Override
            public void receivedNewPhoto(Photo newPhoto) {
                final Photo newPhoto_ = newPhoto;
                //触发UI更新
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        photosList.add(newPhoto_);
                        adapter.notifyItemInserted(photosList.size() - 1);
                    }
                });
            }
        });
    }

    private static final String TAG = "MainActivity";

    //上拉、下拉动作监听设置
    private void setRecyclerViewScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int lastVisibleItemPosition = findLastVisibleItemPosition();
                if (!imageRequester.isLoadingData() && totalItemCount == lastVisibleItemPosition + 1) {
                    //触发向第三方拉取数据;
                    requestPhoto();
                }
            }
        });
    }
   //辅助
    private void requestPhoto() {
        imageRequester.getPhoto();
    }
    //辅助
    private int findLastVisibleItemPosition() {
        if (recyclerView.getLayoutManager() == linearLayoutManager) {
            return linearLayoutManager.findLastVisibleItemPosition();
        }
//        else if(...) {
//            return gridLayoutManager.findLastVisibleItemPosition();
//        }
        return  0;
    }
}

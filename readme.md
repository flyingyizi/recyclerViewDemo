

# 准备

## 建立android app框架

### 基础框架生成

通过android studio项目向导建立程序框架

“start new android studio project” -> empty Activity"

项目相关设置为：

- name： recyclerViewDemo
- package name: com.example.recyclerviewdemo
- language: java
- minSdkVersion: API 16

### 权限与安全设置

本次recyclerView demo考虑从网络拉取数据进行显示，因此需要进行相应联网权限设置。同时由于android在高版本中默认不支持非安全的连接，因此对类似http的请求需要相应特别安全设置。相关设置如下

- 增加安全设置文件

    新增“res\xml”目录，新增“res\xml\network_security_config.xml”文件
    
   ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <network-security-config>
        <base-config cleartextTrafficPermitted="true" />
    </network-security-config>
   ```

- manifest文件修改

    修改“src\main\AndroidManifest.xml”文件, 修改内见以下两处注释

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="com.example.myapplication">
        <!--增加联网权限-->>
        <uses-permission android:name="android.permission.INTERNET"/>

        <!--新增android:networkSecurityConfig-->>
        <application
            android:networkSecurityConfig="@xml/network_security_config"
            android:allowBackup="true"
            android:icon="@mipmap/ic_launcher"
            android:label="@string/app_name"
            android:roundIcon="@mipmap/ic_launcher_round"
            android:supportsRtl="true"
            android:theme="@style/AppTheme">
            <activity android:name=".MainActivity">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
                    <category android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
        </application>
    </manifest>    
    ```

## 相关库依赖添加

在本次demo中, 将使用 http 客户端okhttp，图片加载框架Picasso，以及使用fastjson作为json解析。因此对应添加如下依赖。

添加依赖可以通过“app\build.gradle”文件修改完成，也可以通过android studio的“project structure -> dependencies --> add library denpendcy”完成。

```groovy
dependencies {
    ...

    // 图片加载框架Picasso
    implementation 'com.squareup.picasso:picasso:2.5.2'
    // recyclerView,使用最新androidx，不使用android.support
    implementation "androidx.recyclerview:recyclerview:1.2.0-alpha01"

    // http 客户端okhttp
    //For minSDK lower than 21, change your OkHttp version to 3.12.2 in gradle like this
    implementation 'com.squareup.okhttp3:okhttp:3.12.2'

    implementation 'com.google.guava:guava:28.1-android'
    //JSON解析 fastjson
    implementation 'com.alibaba:fastjson:1.1.71.android'
    ...
}
```

# 设计与开发

## 设计

### 背景

android recyclerView是采用视图与数据分离模式：

```text
recyclerView -- Adapter --- 数据源-------第三方数据源
     |                        |
     |                        |
     +------------------------+   
          数据更新触发UI更新
```

在上图中， 其中“recyclerView -- Adapter”段，建立对应View与Adapter，然后通过android提供的setAdapter method进行关联，是很明确的。对“Adapter --- 数据源”段也是很明确的，对几个Adapter强制需要实现的方法实现即可。

但对触发数据更新的“recyclerView---数据源”段，更多取决于开发人员的考虑。在本次demo中对该段采用如下level 1设计：

- 提供以下接口作为“recyclerView---数据源”段的交互通道。

```java
// 提供ImageRequester给主程序进行数据源相关操作
public abstract class ImageRequester<T extends ImageRequesterResponse>  {

    //responseListener代表主程序逻辑
    protected T responseListener;

    public ImageRequester( T responseListener){
        this.responseListener = responseListener;
    }
    //触发图片从第三方数据源拉新
    public abstract void getPhoto() ;
    //判断是否已经是在第三方数据源源拉新过程中
    public abstract boolean isLoadingData();
}
```

采用该设计的考虑主要是为了将UI与后端代码隔离，得到一个纯粹Adapter。 

## 开发：建立数据源

本次以图片示例作为数据源，


### 图片类Photo

建立下面的图片类，后续网络传输的图片将以下面结构的json流进行传递。

```java
public class Photo  {
    private String date;
    private String media_type;
    private String explanation;
    private String title;
    private String url;
    ...
```

### 数据源接口

作为数据源的接口，这些接口提供给主程序进行数据拉新驱动
```java
package com.example.image;

@FunctionalInterface
public interface ImageRequesterResponse {
    // callback when received newPhone
    void receivedNewPhoto(Photo newPhoto);
}
```

```java
package com.example.image;

// 提供ImageRequester给主程序进行数据源相关操作
public abstract class ImageRequester<T extends ImageRequesterResponse>  {
    //responseListener承载主程序逻辑
    protected T responseListener;

    public ImageRequester( T responseListener){
        this.responseListener = responseListener;
    }
    //触发图片从第三方数据源拉新
    public abstract void getPhoto() ;
    //判断是否已经是在第三方数据源源拉新过程中
    public abstract boolean isLoadingData();
}
```

```java

```

## 开发：建立recyclerView

### layout: recyclerView在activity_main.xml中占位

对“res\layout\activity_main.xml”添加，进行占位

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!--recyclerView占位，并并告知填满它的父布局-->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical" />

</LinearLayout>
```

### layout: 建立recyclerView item的布局

新建“res\layout\recyclerview_item_row.xml”， 每个item显示三个数据：图片，日期，说明

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

  <ImageView
      android:id="@+id/row_itemImage"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_gravity="center"
      android:layout_marginTop="8dp"
      android:layout_weight="3"
      android:adjustViewBounds="true" />

  <TextView
      android:id="@+id/row_itemDate"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_gravity="top|start"
      android:layout_marginTop="8dp"
      android:layout_weight="1"
      tools:text="Some date" />

  <TextView
      android:id="@+id/row_itemDescription"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_gravity="center|start"
      android:layout_weight="1"
      android:ellipsize="end"
      android:maxLines="5" />
</LinearLayout>
```


### 类：新建自定义RecyclerAdapter

通过andoid studio向导建立：

- 新建package： com.example.recycler
- 基于该package新建class: RecyclerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter

该向导建立的类代码如下：
```java
public class RecyclerAdapter extends RecyclerView.Adapter {
}
```

因为对android提供的`public abstract static class Adapter<VH extends ViewHolder>`，由于它是带泛型参数声明的类，如果我们不指定自己的MyViewHolder类型参数，显然它是满足不了我们的要求的，因为"public abstract static class ViewHolder"也是个抽象类。因此手工补充的自定义Holder框架代码如下：

```java
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PhotoHolder> {
    //自定义Holder
    class PhotoHolder extends RecyclerView.ViewHolder {        
    }
}
```

在根据界面提示补充实现抽象接口，以及加上关联数据的代码，最终生成的框架代码如下

```java
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PhotoHolder> {
    private final ArrayList<Photo> photos;
    public RecyclerAdapter(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
        throw  new UnsupportedOperationException();
    }

    @Override
    public int getItemCount() {
        throw  new UnsupportedOperationException();
    }

    class PhotoHolder extends RecyclerView.ViewHolder {
        private  View itemView;
        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            throw  new UnsupportedOperationException();
        }
    }
}

```

得到以上框架后，增加数据与视图绑定的逻辑，最终生成的Adapter如下：

```java
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PhotoHolder> {
    private final ArrayList<Photo> photos;

    public RecyclerAdapter(ArrayList<Photo> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //负责承载每个item的布局
        View inflated = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_row,parent,false);
        return new PhotoHolder(inflated);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
        //负责将每个item holder绑定数据
        Photo itemPhoto = photos.get(position);
        holder.bindPhoto(itemPhoto);
    }

    @Override
    public int getItemCount() {
        return  photos.size();
    }

    class PhotoHolder extends RecyclerView.ViewHolder {
        private  View itemView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            throw  new UnsupportedOperationException();
        }
        public void bindPhoto(Photo photo) {
            ImageView row_itemImage = (ImageView)itemView.findViewById(R.id.row_itemImage);
            TextView row_itemDate = (TextView) itemView.findViewById(R.id.row_itemDate);
            TextView row_itemDescription = (TextView) itemView.findViewById(R.id.row_itemDescription);

            Picasso.with(itemView.getContext()).load(photo.getUrl()).into(row_itemImage);
            row_itemDate.setText( photo.getDate() );
            row_itemDescription.setText( photo.getExplanation());
        }
    }
}
```

## 开发： main-activity与recyclerView配合

### 最简显示能力

以下代码将前面所有工作集合起来，实现最终UI展现。

```java
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
```

### 增强1：实现GridView效果，以及相互切换

在前面是实现的线性布局效果
```java
recyclerView.setLayoutManager(linearLayoutManager);
```

如果recyclerview实现GridView效果只需采用下面配置

```java
GridLayoutManager layoutManage = new GridLayoutManager(getContext(), 2);
recycerView.setLayoutManager(layoutManage);
```

显然，如果是需要实现线性布局与Grid布局相互切换，只需要通过下面方式在合适地方触发即可,比如提供切换的菜单。

```java
    private void changeLayoutManager() {
        if (recyclerView.getLayoutManager() == linearLayoutManager) {
            recyclerView.setLayoutManager(gridLayoutManager);
            //Grid显示的数据要更多，可能要触发拉取更多数据
            。。。
        } else {
            //3
            recyclerView.setLayoutManager(linearLayoutManager);
        }
    }
```

### 增强2：实现swipe 触划效果

要实现将某个不喜欢的item从UI中划走的效果。需要ItemTouchHelper 的支持。例如下面的配置代码。在onCreate中调用后生效，将会有触划的效果。

```java
    private void setRecyclerViewItemTouchListener() {

        ItemTouchHelper.SimpleCallback itemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0/*不关心*/, 
                                    /*swipe的方向*/ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        //不执行特别行为
                        return false;
                    }

                    //swipe ITEM时触发
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        //得到位置通知adapter删除它
                        int position = viewHolder.getAdapterPosition();
                        photosList.remove(position);
                        recyclerView.getAdapter().notifyItemRemoved(position);
                    }
                };

        //附着到recycleView上
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
```

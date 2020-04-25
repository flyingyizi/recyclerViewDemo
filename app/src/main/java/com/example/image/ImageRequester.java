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

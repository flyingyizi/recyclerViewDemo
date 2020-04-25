package com.example.image;

import java.io.Serializable;


public class Photo  {
    private String date;
    private String media_type;
    private String hdurl;
    private String service_version;
    private String explanation;
    private String title;
    private String url;

    //fastjson 需要默认构造函数，与全量构造函数
    public Photo() {}

    public Photo(String date, String media_type, String hdurl, String service_type, String explantation, String title, String url ) {
        this.date=date;
        this.media_type=media_type;
        this.hdurl=hdurl;
        this.service_version=service_type;
        this.explanation=explantation;
        this.title=title;
        this.url=url;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the media_type
     */
    public String getMedia_type() {
        return media_type;
    }

    /**
     * @param media_type the media_type to set
     */
    public void setMedia_type(String media_type) {
        this.media_type = media_type;
    }

    /**
     * @return the hdurl
     */
    public String getHdurl() {
        return hdurl;
    }

    /**
     * @param hdurl the hdurl to set
     */
    public void setHdurl(String hdurl) {
        this.hdurl = hdurl;
    }

    /**
     * @return the service_version
     */
    public String getService_version() {
        return service_version;
    }

    /**
     * @param service_version the service_version to set
     */
    public void setService_version(String service_version) {
        this.service_version = service_version;
    }

    /**
     * @return the explanation
     */
    public String getExplanation() {
        return explanation;
    }

    /**
     * @param explanation the explanation to set
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

}

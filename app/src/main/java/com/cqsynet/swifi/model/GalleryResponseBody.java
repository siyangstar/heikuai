package com.cqsynet.swifi.model;

import java.util.ArrayList;

public class GalleryResponseBody {
    public String commentStatus; //是否可以评论
    public String commentMsg; //不能评论时的提示信息
    public String commentCount; //评论数量
    public String shareUrl; //用于分享的跳转地址
    public String sharePic; //用于分享的小图片
    public String shareTitle; //用于分享的标题
    public String shareContent; //用于分享的内容
    public ArrayList<GalleryInfo> imgList; //图集图片列表
    public ArrayList<RecommendInfo> recommend; //图集推荐
}
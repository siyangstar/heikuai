/*
 * Copyright (C) 2015 重庆尚渝
 * 版权所有
 *
 * 功能描述：分享实体类。
 *
 *
 * 创建标识：xy 20150804
 */
package com.cqsynet.swifi.model;

public class ShareObject {
	private String id; //标题id
	private String title; //待分享内容的标题
	private String text; //分享内容的文本
	private String imagePath; //待分享的本地图片。如果目标平台使用客户端分享，此路径不可以在/data/data下面
	private String filePath; //待分享的文件路径。这个用在Dropbox和Wechat中
	private String imageUrl; //待分享的网络图片
	private String titleUrl; //分享内容标题的链接地址
	private String url; //分享内容的url、在微信和易信中也使用为视频文件地址
	private String site; //QQ空间的字段，标记分享应用的名称
	private String siteUrl; //QQ空间的字段，标记分享应用的网页地址
	private String tag; //分享的平台类别
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public ShareObject () {
		
	}
	
	
	public ShareObject(String title, String text, String imagePath,
			String filePath, String imageUrl, String titleUrl, String url,
			String site, String siteUrl, String tag) {
		super();
		this.title = title;
		this.text = text;
		this.imagePath = imagePath;
		this.filePath = filePath;
		this.imageUrl = imageUrl;
		this.titleUrl = titleUrl;
		this.url = url;
		this.site = site;
		this.siteUrl = siteUrl;
		this.tag = tag;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getTitleUrl() {
		return titleUrl;
	}

	public void setTitleUrl(String titleUrl) {
		this.titleUrl = titleUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	@Override
	public String toString() {
		return "ShareObject [id=" + id + ", title=" + title + ", text=" + text
				+ ", imagePath=" + imagePath + ", filePath=" + filePath
				+ ", imageUrl=" + imageUrl + ", titleUrl=" + titleUrl
				+ ", url=" + url + ", site=" + site + ", siteUrl=" + siteUrl
				+ ", tag=" + tag + "]";
	}

}

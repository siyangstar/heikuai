/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：获取图片文件夹封面
 *
 *
 * 创建标识：br 20150210
 */
package com.cqsynet.swifi.model;

public class PhotoAlbumLVItem {
    private String pathName;
    private int fileCount;
    private String firstImagePath;

    public PhotoAlbumLVItem(String pathName, int fileCount, String firstImagePath) {
        this.pathName = pathName;
        this.fileCount = fileCount;
        this.firstImagePath = firstImagePath;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

//    @Override
//    public int hashCode() {
//        return pathName.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o instanceof SelectImgGVItem){
//            SelectImgGVItem other = (SelectImgGVItem) o;
//            return this.pathName.equals(other.pathName);
//        }
//
//        return false;
//    }

    @Override
    public String toString() {
        return "SelectImgGVItem{" +
                "pathName='" + pathName + '\'' +
                ", fileCount=" + fileCount +
                ", firstImagePath='" + firstImagePath + '\'' +
                '}';
    }
}

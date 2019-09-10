/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：观察者模式中的被观察者
 *
 *
 * 创建标识：zhaosy 20150428
 */
package com.cqsynet.swifi.model;

public interface Watched {
	
	void addWatcher(int key, Watcher watcher);
	void removeWatcher(int key);
	void notifyProgress();
	void notifyOnce();
}

/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：分类新闻页面listView的公用adapter。
 *
 *
 * 创建标识：luchaowei 20141020
 */
package com.cqsynet.swifi.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.cqsynet.swifi.GlideApp;
import com.cqsynet.swifi.Globals;
import com.cqsynet.swifi.R;
import com.cqsynet.swifi.activity.GalleryActivity;
import com.cqsynet.swifi.activity.TopicActivity;
import com.cqsynet.swifi.db.StatisticsDao;
import com.cqsynet.swifi.fragment.NewsListFragment;
import com.cqsynet.swifi.fragment.NewsMainFragment;
import com.cqsynet.swifi.model.NewsItemInfo;
import com.cqsynet.swifi.util.AppUtil;
import com.cqsynet.swifi.util.NewsAdUtil;
import com.cqsynet.swifi.util.SharedPreferencesInfo;
import com.cqsynet.swifi.util.WebActivityDispatcher;

import java.util.ArrayList;

public class NewsItemsAdapter extends BaseAdapter {
    // 所在的fragment
    private NewsListFragment mFragment;
    // 模板总数
    public static final int ITEM_VIEW_TYPE_COUNT = 6;
    // 新闻模板类型
    public static final int ITEM_VIEW_TYPE_SMALL_PHOTO = 0; //左边小图模式
    public static final int ITEM_VIEW_TYPE_VERTICAL = 1; //竖直布局模式(图片可有可无)
    public static final int ITEM_VIEW_TYPE_4_1 = 2; //4:1图片模式
    public static final int ITEM_VIEW_TYPE_3PHOTO = 3; //3张图模式
    public static final int ITEM_VIEW_TYPE_3_2 = 4; //3:2图片模式
    public static final int ITEM_VIEW_TYPE_3_1 = 6; //3:1图片模式
    // 类型
    public static final int NEWS_TYPE_NORMAL = 0;
    public static final int NEWS_TYPE_TOPIC = 1;
    public static final int NEWS_TYPE_GALLERY = 2;
    public static final int NEWS_TYPE_MINI_PROGRAM = 3;
    public static final int NEWS_TYPE_AD = 9;
    // 图片url 在List中的index
    public static final int FIRST_IMAGE = 0;
    public static final int SECOND_IMAGE = 1;
    public static final int THIRD_IMAGE = 2;
    private Context mContext;
    private ArrayList<NewsItemInfo> mList;
    private NewsAdUtil mAdUtils;
    private NewsItemViewHolder mHolder;

    private String key;

    public NewsItemsAdapter(Context context, ArrayList<NewsItemInfo> list) {
        mContext = context;
        mList = list;
        if (mAdUtils == null) {
            mAdUtils = new NewsAdUtil();
        }
    }

    public NewsItemsAdapter(Context context, ArrayList<NewsItemInfo> list, NewsListFragment fragment) {
        mContext = context;
        mFragment = fragment;
        mList = list;
        if (mAdUtils == null) {
            mAdUtils = new NewsAdUtil();
        }
    }

    public ArrayList<NewsItemInfo> getData() {
        return mList;
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mList.get(position) == null) {
            // 如果新闻列表里，某一项数据为null，返回非null的实例
            return new NewsItemInfo();
        } else {
            return mList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        // 默认模板类型
        int templateType = ITEM_VIEW_TYPE_VERTICAL;
        // 如果列表中，某一个item的data为null，返回默认类型。
        if (mList.get(position) == null) {
            return templateType;
        }
        NewsItemInfo info = mList.get(position);
        String newsType = info.type;
        if (TextUtils.isEmpty(newsType)) {
            return templateType;
        }
        String template;

        //若为广告类型,计算当前应该轮播哪一个
        if (Integer.parseInt(newsType) == NEWS_TYPE_AD) {
            template = info.advTemplate.get(Globals.g_advIndexMap.get(info.id));
        } else {
            template = info.template;
        }
        if (!TextUtils.isEmpty(template)) {
            templateType = Integer.parseInt(template);
        }

        if (templateType >= ITEM_VIEW_TYPE_COUNT) {
            templateType = ITEM_VIEW_TYPE_VERTICAL;
        }
        return templateType;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int templateType;
        NewsItemInfo info = (NewsItemInfo) getItem(position);
        if (info == null || info.type == null) {
            return buildConvertView(parent, 0);
        }

        if(Integer.parseInt(info.type) == NEWS_TYPE_AD) {
            if (NewsMainFragment.mCurrentFragment == null || mFragment == null || mFragment == NewsMainFragment.mCurrentFragment) {
                //重新打开app时,mCurrentFragment为null
				mAdUtils.getCurrentIndex(info.id);
            }
        }
        templateType = getItemViewType(position);

        if (convertView == null) {
            convertView = buildConvertView(parent, templateType);
        } else {
            mHolder = (NewsItemViewHolder) convertView.getTag();
            //由于布局切换,需要检测一下布局是否一致
            if(mHolder.template != templateType) {
                convertView = buildConvertView(parent, templateType);
            }
        }
        bindView(mHolder, info, templateType);

        return convertView;
    }

    /**
     * @param parent 父viewGroup。
     * @param type   item类型。一共有5个类型。
     * @return View 构造完成的view。
     * @Description: 构造ConvertView
     */
    private View buildConvertView(ViewGroup parent, int type) {
        View itemView;
        int screenWidth = AppUtil.getScreenW((Activity) mContext);
        int imgHeight;
        mHolder = new NewsItemViewHolder();
        mHolder.template = type;
        switch (type) {
            case ITEM_VIEW_TYPE_SMALL_PHOTO:
                itemView = LayoutInflater.from(mContext).inflate(R.layout.news_item_view_small_photo, parent, false);
                LinearLayout layoutMix = itemView.findViewById(R.id.llNewsInfo_small_photo);
                mHolder.ivFirstImg = itemView.findViewById(R.id.ivNewsThumbnail_small_photo);
                mHolder.tvTitle = itemView.findViewById(R.id.tvNewsTitle_small_photo);
                mHolder.tvSource = layoutMix.findViewById(R.id.tvNewsSource_news_info);
                mHolder.tvNewsType = layoutMix.findViewById(R.id.tvNewsType_news_info);
                mHolder.llActivity = itemView.findViewById(R.id.llActivity_small_photo);
                mHolder.tvRestTime = itemView.findViewById(R.id.tvRestTime_news_item_activity);
                mHolder.tvStatus = itemView.findViewById(R.id.tvStatus_news_item_activity);
                break;
            case ITEM_VIEW_TYPE_3PHOTO:
                itemView = LayoutInflater.from(mContext).inflate(R.layout.news_item_view_3photo, parent, false);
                LinearLayout layout3Photo = itemView.findViewById(R.id.llNewsInfo_3photo);
                mHolder.ivFirstImg = itemView.findViewById(R.id.ivNewsThumbnail1_3photo);
                mHolder.ivSecondImg = itemView.findViewById(R.id.ivNewsThumbnail2_3photo);
                mHolder.ivThirdImg = itemView.findViewById(R.id.ivNewsThumbnail3_3photo);
                mHolder.tvTitle = itemView.findViewById(R.id.tvNewsTitle_3photo);
                mHolder.tvSource = layout3Photo.findViewById(R.id.tvNewsSource_news_info);
                mHolder.tvNewsType = layout3Photo.findViewById(R.id.tvNewsType_news_info);
                mHolder.llActivity = itemView.findViewById(R.id.llActivity_3photo);
                mHolder.tvRestTime = itemView.findViewById(R.id.tvRestTime_news_item_activity);
                mHolder.tvStatus = itemView.findViewById(R.id.tvStatus_news_item_activity);
                int imgWidth = (screenWidth - AppUtil.dp2px(mContext, 20)) / 3;
                imgHeight = imgWidth * 2 / 3;
                LinearLayout.LayoutParams threePhotoParams1 = new LinearLayout.LayoutParams(imgWidth, imgHeight);
                mHolder.ivFirstImg.setLayoutParams(threePhotoParams1);
                LinearLayout.LayoutParams threePhotoParams2 = new LinearLayout.LayoutParams(imgWidth, imgHeight);
                threePhotoParams2.leftMargin = AppUtil.dp2px(mContext, 2);
                mHolder.ivSecondImg.setLayoutParams(threePhotoParams2);
                LinearLayout.LayoutParams threePhotoParams3 = new LinearLayout.LayoutParams(imgWidth, imgHeight);
                threePhotoParams3.leftMargin = AppUtil.dp2px(mContext, 2);
                mHolder.ivThirdImg.setLayoutParams(threePhotoParams3);
                break;
            default: //其它模式共用模板
                itemView = LayoutInflater.from(mContext).inflate(R.layout.news_item_view_vertical, parent, false);
                LinearLayout layoutPhoto_autoFit = itemView.findViewById(R.id.llNewsInfo_vertical);
                mHolder.ivFirstImg = itemView.findViewById(R.id.ivNewsPhoto_vertical);
                mHolder.tvTitle = itemView.findViewById(R.id.tvNewsTitle_vertical);
                mHolder.tvSource = layoutPhoto_autoFit.findViewById(R.id.tvNewsSource_news_info);
                mHolder.tvNewsType = layoutPhoto_autoFit.findViewById(R.id.tvNewsType_news_info);
                mHolder.llActivity = itemView.findViewById(R.id.llActivity_vertical);
                mHolder.tvRestTime = itemView.findViewById(R.id.tvRestTime_news_item_activity);
                mHolder.tvStatus = itemView.findViewById(R.id.tvStatus_news_item_activity);
                if (type == ITEM_VIEW_TYPE_3_1) {
                    imgHeight = (screenWidth - AppUtil.dp2px(mContext, 16)) / 3;
                } else if (type == ITEM_VIEW_TYPE_3_2) {
                    imgHeight = (screenWidth - AppUtil.dp2px(mContext, 16)) * 2 / 3;
                } else if (type == ITEM_VIEW_TYPE_4_1) {
                    imgHeight = (screenWidth - AppUtil.dp2px(mContext, 16)) / 4;
                } else {
                    imgHeight = LayoutParams.WRAP_CONTENT;
                }
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, imgHeight);
                params.addRule(RelativeLayout.BELOW, R.id.tvNewsTitle_vertical);
                mHolder.ivFirstImg.setLayoutParams(params);
                break;
        }
        itemView.setTag(mHolder);
        return itemView;
    }

    /**
     * 给item的View填充数据。
     *
     * @param holder
     * @param info
     * @param template
     */
    private void bindView(final NewsItemViewHolder holder, NewsItemInfo info, int template) {
        int newsType = NEWS_TYPE_NORMAL; // 设置默认值
        String imgUrl;
        if (!TextUtils.isEmpty(info.type)) {
            newsType = Integer.parseInt(info.type);
        }

        String title;
        String label;
        String source;
        ArrayList<String> imgList;

        if (newsType != NEWS_TYPE_AD) { //非广告
            title = info.title;
            label = info.label;
            source = info.author;
            imgList = info.img;
        } else { //广告
            int advIndex = Globals.g_advIndexMap.get(info.id);
            title = info.advTitle.get(advIndex);
            label = info.advLabel.get(advIndex);
            source = info.advAuthor.get(advIndex);
            imgList = info.advImg.get(advIndex);
            StatisticsDao.saveStatistics(mContext, "advView", info.advId.get(advIndex)); // 广告显示统计
        }

        //标题
        if (!TextUtils.isEmpty(title)) {
            if (!TextUtils.isEmpty(key)) { // 给与关键字相关的所有字加红色
                SpannableStringBuilder builder = new SpannableStringBuilder(title);
                ForegroundColorSpan redSpan = null;
                char[] tempKey = key.toCharArray();
                for (int i = 0; i < tempKey.length; i++) {
                    for (int j = 0; j < title.length(); j++) {
                        if (tempKey[i] == title.charAt(j)) { // 避免字符串中的相同元素只给第一个加红色
                            redSpan = new ForegroundColorSpan(Color.RED);
                            builder.setSpan(redSpan, j, j + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                holder.tvTitle.setText(builder);
            } else {
                holder.tvTitle.setText(title);
            }
            holder.tvTitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvTitle.setVisibility(View.GONE);
        }

        //来源
        if (!TextUtils.isEmpty(source)) {
            holder.tvSource.setText(source);
            holder.tvSource.setVisibility(View.VISIBLE);
        } else {
            holder.tvSource.setVisibility(View.GONE);
        }

        //标签
        if (!TextUtils.isEmpty(label)) {
            holder.tvNewsType.setText(label);
            holder.tvNewsType.setVisibility(View.VISIBLE);
        } else {
            holder.tvNewsType.setVisibility(View.GONE);
        }

        //图片
        switch (template) {
            case ITEM_VIEW_TYPE_SMALL_PHOTO:
                if (imgList != null && imgList.size() != 0) {
                    imgUrl = imgList.get(FIRST_IMAGE);
                    if (!TextUtils.isEmpty(imgUrl)) {
                        GlideApp.with(mContext)
                                .load(imgUrl)
                                .centerCrop()
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .error(R.drawable.image_bg)
                                .into(holder.ivFirstImg);
                    }
                }
                break;
            case ITEM_VIEW_TYPE_3PHOTO:
                if (imgList != null) {
                    int imgCount = imgList.size();
                    if (imgCount > 0) {
                        imgUrl = imgList.get(FIRST_IMAGE);
                        if (imgUrl != null && !imgUrl.isEmpty()) {
                            GlideApp.with(mContext)
                                    .load(imgUrl)
                                    .centerCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.image_bg)
                                    .into(holder.ivFirstImg);
                        }
                    }
                    if (imgCount > 1) {
                        imgUrl = imgList.get(SECOND_IMAGE);
                        if (imgUrl != null && !imgUrl.isEmpty()) {
                            GlideApp.with(mContext)
                                    .load(imgUrl)
                                    .centerCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.image_bg)
                                    .into(holder.ivSecondImg);
                        }

                    }
                    if (imgCount > 2) {
                        imgUrl = imgList.get(THIRD_IMAGE);
                        if (imgUrl != null && !imgUrl.isEmpty()) {
                            GlideApp.with(mContext)
                                    .load(imgUrl)
                                    .centerCrop()
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .error(R.drawable.image_bg)
                                    .into(holder.ivThirdImg);
                        }
                    }
                }
                break;
            default:
                if (imgList != null && imgList.size() > 0) {
                    imgUrl = imgList.get(0);
                    if (!TextUtils.isEmpty(imgUrl)) {
                        GlideApp.with(mContext)
                                .load(imgUrl)
                                .error(R.drawable.image_bg)
                                .into(holder.ivFirstImg);
                        holder.ivFirstImg.setVisibility(View.VISIBLE);
                    } else {
                        holder.ivFirstImg.setVisibility(View.GONE);
                    }
                } else {
                    holder.ivFirstImg.setVisibility(View.GONE);
                }
                break;
        }

        if (!TextUtils.isEmpty(label) && (label.equals("专题") || label.equals("活动"))) {
            holder.tvNewsType.setBackgroundResource(R.drawable.news_type_bg_red);
            holder.tvNewsType.setTextColor(mContext.getResources().getColor(R.color.red));
        } else {
            holder.tvNewsType.setBackgroundResource(R.drawable.news_type_bg_green);
            holder.tvNewsType.setTextColor(mContext.getResources().getColor(R.color.green));
        }

        boolean isRead = SharedPreferencesInfo.getTagBoolean(mContext, SharedPreferencesInfo.READED + info.id, false);
        if (isRead) {
            holder.tvTitle.setTextColor(0xFF808080);
        } else {
            holder.tvTitle.setTextColor(0xFF555555);
        }

        //判断是否显示活动状态条
        if (info.status != null) {
            if (info.status.equals("0")) {
                holder.llActivity.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("未开始");
                holder.tvRestTime.setText(info.restTime);
            } else if (info.status.equals("1")) {
                holder.llActivity.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("进行中");
                holder.tvRestTime.setVisibility(View.VISIBLE);
                holder.tvRestTime.setText(info.restTime);
            } else if (info.status.equals("2")) {
                holder.llActivity.setVisibility(View.VISIBLE);
                holder.tvStatus.setText("已结束");
                holder.tvRestTime.setText(info.restTime);
            } else {
                holder.llActivity.setVisibility(View.GONE);
            }
        } else {
            holder.llActivity.setVisibility(View.GONE);
        }
    }

    /**
     * 各个新闻模板类型的跳转处理
     *
     * @param context
     * @param info
     */
    public void setNewsClickJump(Context context, NewsItemInfo info, String channelId) {
        setNewsClickJump(context, info, channelId, "newsList");
    }

    /**
     * 各个新闻模板类型的跳转处理
     * @param context
     * @param info
     * @param channelId
     * @param from
     */

    public void setNewsClickJump(Context context, NewsItemInfo info, String channelId, String from) {
        if (info == null) {
            return;
        }

        //点击后保存状态,下次进来后标题置灰
        SharedPreferencesInfo.setTagBoolean(context, SharedPreferencesInfo.READED + info.id, true);

        int newsType = 0;
        String type = info.type;
        if (type != null && !TextUtils.isEmpty(type)) {
            newsType = Integer.parseInt(type);
        }

        String url;
        Intent intent = new Intent();
        switch (newsType) {
            case NewsItemsAdapter.NEWS_TYPE_TOPIC: // 专题
                intent.setClass(context, TopicActivity.class);
                intent.putExtra("id", info.id);
                intent.putExtra("image", info.img);
                intent.putExtra("from", from);
                context.startActivity(intent);
                break;
            case NewsItemsAdapter.NEWS_TYPE_AD: // 广告
                int index = Globals.g_advIndexMap.get(info.id);
                String advId = info.advId.get(index);
                StatisticsDao.saveStatistics(context, "advClick", advId); // 广告点击统计
                if (info.url != null && info.url.size() > index) {
                    // 如果为广告类型，需要根据plan返回跳转url
                    url = info.url.get(index);
                    if (!TextUtils.isEmpty(url)) {
                        Intent advIntent = new Intent();
                        advIntent.putExtra("url", url);
                        advIntent.putExtra("type", "0");
                        advIntent.putExtra("channelId", channelId);
                        advIntent.putExtra("from", from);
                        advIntent.putExtra("source", "广告");
                        WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                        webDispatcher.dispatch(advIntent, context);
                    }
                }
                break;
            case NewsItemsAdapter.NEWS_TYPE_GALLERY: // 图集
                intent.setClass(context, GalleryActivity.class);
                intent.putExtra("id", info.id);
                intent.putExtra("channelId", channelId);
                intent.putExtra("from", from);
                context.startActivity(intent);
                break;
            default: // 其它
                if (info.url != null) {
                    // 其他类型 默认使用第一个url
                    url = info.url.get(0);
                    if (!TextUtils.isEmpty(url)) {
                        Intent newsIntent = new Intent();
                        newsIntent.putExtra("url", url);
                        newsIntent.putExtra("type", "0");
                        newsIntent.putExtra("source", "资讯");
                        newsIntent.putExtra("channelId", channelId);
                        newsIntent.putExtra("from", from);
                        WebActivityDispatcher webDispatcher = new WebActivityDispatcher();
                        webDispatcher.dispatch(newsIntent, context);
                    }
                }
                break;
        }
    }

    /**
     * 设置搜索关键字
     *
     * @param text
     */
    public void setText(String text) {
        key = text; // 搜索关键字
    }

    /**
     * 缓存menuItem的View。
     */
    private class NewsItemViewHolder {
        TextView tvTitle;
        TextView tvSource;
        ImageView ivFirstImg;
        ImageView ivSecondImg;
        ImageView ivThirdImg;
        TextView tvNewsType;
        LinearLayout llActivity;
        TextView tvRestTime;
        TextView tvStatus;
        int template;
    }
}
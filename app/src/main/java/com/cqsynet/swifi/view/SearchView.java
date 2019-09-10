/*
 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：自定义搜索框
 *
 *
 * 创建标识：xy 20160321
 */
package com.cqsynet.swifi.view;

import com.cqsynet.swifi.R;
import com.cqsynet.swifi.adapter.SearchHistoryAdapter;
import com.cqsynet.swifi.db.SearchHistoryDao;
import com.cqsynet.swifi.model.SearchHistoryInfo;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SearchView extends LinearLayout implements View.OnClickListener {

    private EditText mInput; // 输入框

    private ImageView ivDelete; // 删除键

    private Context mContext;  

    private ListView lvTips; // 弹出列表
    
    private LinearLayout lv_hint_region; // 整个历史记录区域
    
    private SearchHistoryAdapter mHintAdapter; // 搜索历史adapter

    private SearchViewListener mListener; // 搜索回调接口
    
    /**
     * 设置搜索回调接口
     *
     * @param listener 监听者
     */
    public void setSearchViewListener(SearchViewListener listener) {
        mListener = listener;
    }
    
    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.search_layout, this);
        initLayout();
    }

    /**
     * 初始化和监听
     */
	private void initLayout() {
        mInput = findViewById(R.id.search_et_input);
        mInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        ivDelete = findViewById(R.id.search_iv_delete);
        lvTips = findViewById(R.id.lv_search_tips);
        lv_hint_region = findViewById(R.id.region_search_history);
        lvTips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            	SearchHistoryInfo info = (SearchHistoryInfo) lvTips.getAdapter().getItem(i);
                mInput.setText(info.content);
                mInput.setSelection(info.content.length());
                lv_hint_region.setVisibility(View.GONE);
                notifyStartSearching();
            }
        });

        ivDelete.setOnClickListener(this);
        findViewById(R.id.search_et_input).setOnClickListener(this);
        mInput.addTextChangedListener(new EditChangedListener());
        // 设置软键盘点击搜索按钮监听
        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            	String text = mInput.getText().toString().trim();
            	if (!TextUtils.isEmpty(text)) {
            		boolean isSave = SearchHistoryDao.getInstance(mContext).isSearchSave(text);
            		if (!isSave) {
            			SearchHistoryDao.getInstance(mContext).insertSearch(text);
            		}
            		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        				if (!TextUtils.isEmpty(mInput.getText().toString())) { // 无文字输入时禁止搜索
        					lv_hint_region.setVisibility(View.GONE);
                            notifyStartSearching();
        				}
                    }
            	}
                return true;
            }
        });
    }

    /**
     * 通知监听者 进行搜索操作
     * @param text 搜索关键字
     */
    private void notifyStartSearching(){
        if (mListener != null) {
            mListener.onSearch(mInput.getText().toString());
        }
        // 隐藏软键盘
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mInput.getWindowToken(),0);
    }

    // 输入监听
    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        	if (mHintAdapter != null) {
        		mHintAdapter.notifyDataSetChanged();
        	} 
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (!TextUtils.isEmpty(charSequence)) {
                ivDelete.setVisibility(VISIBLE);
                if (mHintAdapter != null) {
                	lv_hint_region.setVisibility(VISIBLE);
                }
            } else {
                ivDelete.setVisibility(GONE);
                if (mHintAdapter != null) {
                    lvTips.setAdapter(mHintAdapter);
                }
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {
        	
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_iv_delete:
                mInput.setText("");
                ivDelete.setVisibility(GONE);
                break;
                
            case R.id.search_et_input:
    			if (mListener != null) {
    				mListener.onSearchClick();
    			}
    			break;
    			
			default: 
				break;
        }
    }

    /**
     * 搜索框回调方法
     */
    public interface SearchViewListener {
        /**
         * 开始搜索
         *
         * @param text 传入输入框的文本
         */
        void onSearch(String text);

        /**
         * 点击搜索框回调
         */
        void onSearchClick();
        
    }

    public void setHint(String hint) {
        mInput.setHint(hint);
    }

}
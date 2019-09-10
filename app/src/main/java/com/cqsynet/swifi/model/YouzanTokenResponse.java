package com.cqsynet.swifi.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: sayaki
 * Date: 2017/6/22
 */
public class YouzanTokenResponse {

    /**
     * msg : 登录成功
     * data : {"cookie_value":"e74c10db3b70d32204922f9f4e","cookie_key":"open_cookie_c2a7b9269fd095fa5b1467769433688","access_token":"cddc46b2fcf73739ad0caacfaebf4561"}
     * code : 0
     */
    private String msg;
    private Token data;
    private int code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Token getData() {
        return data;
    }

    public void setData(Token data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static class Token {
        /**
         * cookie_value : e74c10db3b70d32204922f9f4e
         * cookie_key : open_cookie_c2a7b9269fd095fa5b1467769433688
         * access_token : cddc46b2fcf73739ad0caacfaebf4561
         */
        @SerializedName("cookie_value")
        private String cookieValue;
        @SerializedName("cookie_key")
        private String cookieKey;
        @SerializedName("access_token")
        private String accessToken;

        public static Token objectFromData(String str) {

            return new Gson().fromJson(str, Token.class);
        }

        public static List<Token> arrayTokenFromData(String str) {

            Type listType = new TypeToken<ArrayList<Token>>() {
            }.getType();

            return new Gson().fromJson(str, listType);
        }

        public String getCookieValue() {
            return cookieValue;
        }

        public void setCookieValue(String cookieValue) {
            this.cookieValue = cookieValue;
        }

        public String getCookieKey() {
            return cookieKey;
        }

        public void setCookieKey(String cookieKey) {
            this.cookieKey = cookieKey;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}

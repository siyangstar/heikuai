/*

 * Copyright (C) 2014 重庆尚渝
 * 版权所有
 *
 * 功能描述：常量文件
 *
 *
 * 创建标识：zhaosy 20140823
 */
package com.cqsynet.swifi;

public class AppConstants {

    //开发环境地址
//    public static final String SERVER_URL = "http://cif-transferconfig.heikuai.com:8000/transferconfigif/interfaceCall";//转发服务器
//    public static final String SERVER_MULTIPART_URL = "http://172.16.19.58:8080/file/up";//文件服务器
//    public static final String SERVER_SOCKET_ADDRESS = "http://10.255.244.238:1994/heikuai"; //长连接socket地址

    //测试环境地址
//    public static final String SERVER_URL = "http://cif-transferconfig.heikuai.com:8000/transferconfigif/interfaceCall";//转发服务器
//    public static final String SERVER_MULTIPART_URL = "http://csrv-filerecsys.heikuai.com:8000/filerecsysif/interfaceCall.if";//文件服务器
//    public static final String SERVER_SOCKET_ADDRESS = "http://10.255.244.238:1994/heikuai"; //长连接socket地址

    // 预发布环境器地址
//    public static final String SERVER_URL = "http://yif-transferconfig.heikuai.com:9000/transferconfigif/interfaceCall";//转发服务器
//    public static final String SERVER_MULTIPART_URL = "http://ysrv-filerecsys.heikuai.com:9000/filerecsysif/interfaceCall.if";//文件服务器
//    public static final String SERVER_SOCKET_ADDRESS = "http://10.255.246.240:1994/heikuai"; //长连接socket地址
//    public static final String COMPLAIN_PAGE = "http://testweb.heikuai.com/complaint/index.html"; //投诉页面测试环境

    // 生产环境地址
    public static final String SERVER_URL = "http://sif-transferconfig.heikuai.com:8000/transferconfigif/interfaceCall";//转发服务器
    public static final String SERVER_MULTIPART_URL = "http://ssrv-filerecsys.heikuai.com:8000/filerecsysif/interfaceCall.if";//文件服务器
    public static final String SERVER_SOCKET_ADDRESS = "http://ssrv-nodejs.heikuai.com:1994/heikuai"; //长连接socket地址
    public static final String COMPLAIN_PAGE = "http://app.heikuai.com/complaint/index.html"; //投诉页面

    public static final String CACHE_DIR = "HeiKuai";
    public static final String CACHE_FILE = "debug.txt";
    public static final String CACHE_TEMP_FILE = "temp.txt"; //临时日志文件,上传后删除

    // 嘿快微信应用id, 用于小程序和分享
    public static final String WECHAT_APP_ID = "wxb1afacacdce2609e";

    // 微博地址
    public static final String WEIBO_URL = "http://weibo.com/heikuai";
    // 微信账号
    public static final String WECHAT_ID = "heikuai-wifi";
    // 嘿快官方网站地址
    public static final String OFFICIAL_WEBSITE = "http://www.heikuai.com/mobile/index.html";
    // 服务条款页面
    public static final String AGREEMENT_PAGE = "http://app.heikuai.com/agreement.html";
    // 企业介绍页面
    public static final String ABOUT_PAGE = "http://app.heikuai.com/about.html";
    // 上网必看页面
    public static final String HELP_PAGE = "http://app.heikuai.com/help.html";
    // 收不到验证码页面
    public static final String VERIFY_CODE_ERROR = "http://app.heikuai.com/message.html";
    // 评论页面
    public static final String COMMENT_PAGE = "http://app.heikuai.com/comments_lottery.html";
    // 抽奖秘籍页面
    public static final String LOTTERY_CHEATS_PAGE = "http://sftpw-img.heikuai.com:8000/zt/web/src/2017/5/cqAR/pages/tips.html";
    // 我的中奖页面
    public static final String MY_LOTERY_PAGE = "http://sftpw-img.heikuai.com:8000/zt/web/src/2017/5/cqAR/pages/list.html";
    // 兑换奖品
    public static final String REDEEM_PAGE = "http://sftpw-img.heikuai.com:8000/zt/web/src/2017/3/verification/exchange.html";

    // 有赞首页
    public static final String YOUZAN_URL = "https://h5.youzan.com/v2/showcase/homepage?alias=wsfpuvno";
    public static final String YOUZAN_LOGIN_URL = "https://uic.youzan.com/sso/open/login";
    public static final String YOUZAN_CLIENT_ID = "722af6dcc94514a62c";
    public static final String YOUZAN_CLIENT_SECRET = "b19dec60a814bd30fbc20a018dcdc13c";
    public static final String YOUZAN_KDT_ID = "16354116"; //店铺id

    // 弹出续时框广播
    public static final String ACTION_RENEW_DIALOG = "cqsynet.wifi.renew";
    // 重置计时器
    public static final String ACTION_RENEW_TIMER = "cqsynet.wifi.resettimer";
    // 统计信息广播
    public static final String ACTION_STATISTICS = "cqsynet.wifi.statistics";
    // socket发送用户登陆信息
    public static final String ACTION_SOCKET_LOGIN = "cqsynet.wifi.socket.login";
    // socket注销用户
    public static final String ACTION_SOCKET_LOGOUT = "cqsynet.wifi.socket.logout";
    // 推送消息到达广播
    public static final String ACTION_REFRESH_RED_POINT = "cqsynet.wifi.redpoint";
    // 更新头像广播
    public static final String ACTION_REFRESH_HEADER = "cqsynet.wifi.refreshheader";
    // 长连接推送广播
    public static final String ACTION_SOCKET_PUSH = "cqsynet.wifi.push";
    // 删除好友广播
    public static final String ACTION_DELETE_FRIEND = "cqsynet.wifi.deletefriend";
    // 添加好友广播
    public static final String ACTION_ADD_FRIEND = "cqsynet.wifi.addfriend";
    // socket开始连接
    public static final String ACTION_SOCKET_CONNECTING = "cqsynet.wifi.socket.connecting";
    // 更新草稿箱
    public static final String ACTION_UPDATE_DRAFT = "cqsynet.wifi.updatedraft";
    // 更新消息列表
    public static final String ACTION_UPDATE_MSG = "cqsynet.wifi.updatemsg";
    // 更改好友备注
    public static final String ACTION_MODIFY_REMARK = "cqsynet.wifi.modifyremark";

    // SharedPreferences文件名
    public static final String SHARED_PREF_FILE_NAME = "xxxxxx";
    // SharedPreferences中uuid
    public static final String SHARED_PREF_KEY_UUID = "uuid";

    // 嘿快WiFi的SSID
    public static final String WIFI_SSID = "HeiKuai";
    public static final String WIFI_SSID2 = "嘿快APP";

    // 统计接口提交频率(毫秒)
    public static final int SUBMIT_STATISTICS_INTEVAL = 1000 * 60 * 2; //2分钟

    // 免费上网邻近时间（秒）
    public static final int NEAR_CLOSE_FREE_WIFI_TIME = 60 * 5;

    // 验证码获取间隔时间(秒)
    public static final int VERIFY_INTERVAL = 120;

    // 使用app次数的间隔阀值(毫秒)
    public static final int APP_USE_INTERVAL = 1000 * 60 * 35; //35分钟

    // 请求成功返回码
    public static final String RET_OK = "0";
    // 请求失败返回码
    public static final String RET_FAIL = "1";

    // 全屏图片广告显示时长(毫秒)
    public static final long AD_IMAGE_DURATION = 6000;
    // 全屏视频广告显示时长(毫秒)
    public static final long AD_VIDEO_DURATION = 15000;

    //广告刷新最小间隔(毫秒)
    public static final long AD_REFRESH_INTEVAL = 250;

    //录音最长时长(单位:秒)
    public static final int VOICE_RECORD_OVERTIME = 60;

    //图像识别扫描间隔
    public static final long SCAN_INTERVAL = 2000;

    //RSA公钥
    public static final String RSA_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCEbevxfCfZqKHvl38YoUhlpvRsBM+iRPvlpQGgT2/B+zG8ZpfxtR964xRyPZw5RIPlG2E5a2Ufnm3XfZmKb5h0abfEtA8K/uMUPo08B4URGj63K8JxJ0RqnbAoTlYZrus7V7SAG7cw8ibjW+z+24PYckFqUJuat8nrrnil3n9fOQIDAQAB";


    // 安卓系统ID
    public static final String ANDROID_SYS = "APP0006";
    // 用户系统ID
    public static final String USER_SYS = "IFAPP0002";
    // 转发系统ID
    public static final String TRANSFER_SYS = "IFAPP0003";
    // CMS系统ID
    public static final String CMS_SYS = "IFAPP0004";
    // 广告系统ID
    public static final String AD_SYS = "IFAPP0005";
    // 升级系统ID
    public static final String UPDATE_SYS = "IFAPP0008";
    // 应用市场
    public static final String MARKET_SYS = "IFAPP0012";
    //百度云系统ID
    public static final String MSGPUSH_SYS = "IFAPP0015";
    // WiFi系统ID
    public static final String GATEWAY_SYS = "IFAPP0016";
    // 电商系统系统ID
    public static final String ECOMMERCE_SYS = "IFAPP0017";
    // 统计系统ID
    public static final String STATISTICS_SYS = "IFAPP0018";
    // 社交系统
    public static final String SNS_SYS = "IFAPP0036";
    // AR奖项系统
    public static final String LOTTERY_SYS = "IFAPP0020";
    // 基础系统
    public static final String BASE_SYS = "IFAPP0009";
    // 轨道环境Wifi信息系统
    public static final String WIFI_SYS = "IFAPP0038";
    // 评论系统
    public static final String COMMENT_SYS = "IFAPP0039";

    // 获取验证码接口ID
    public static final String IF_GET_VERIFY_CODE = "IF00021001";
    // 注册接口ID
    public static final String IF_REGIST = "IF00021002";
    // 找回密码接口ID
    public static final String IF_FORGET_PSW = "IF00021003";
    // 登陆接口ID
    public static final String IF_LOGIN = "IF00021004";
    // 修改新闻频道接口ID
    public static final String IF_SET_CHANNELS = "IF00021006";
    // 获取专题列表接口ID
    public static final String IF_GET_TOPIC_LIST = "IF00041001";
    // 获取新闻列表接口ID
    public static final String IF_GET_NEWS_LIST = "IF00041003";
    // 资讯搜索接口
    public static final String IF_GET_SEARCH_LIST = "IF00041007";
    // 热门资讯列表接口
    public static final String IF_GET_NEWS_HOT = "IF00041008";
    // 获取意见反馈列表接口ID
    public static final String IF_GET_SUGGEST_LIST = "IF00021014";
    // 获取启动图接口ID
    public static final String IF_GET_LAUNCH = "IF00051004";
    // 获取广告列表接口ID
    public static final String IF_GET_AD = "IF00051005";
    // 获取用户信息接口ID
    public static final String IF_GET_USER_INFO = "IF00021007";
    // 修改用户信息接口ID
    public static final String IF_UPDATE_USER_INFO = "IF00021008";
    // 意见反馈接口ID
    public static final String IF_SUGGEST = "IF00021011";
    // 修改手机号码
    public static final String IF_CHANGE_PHONE = "IF00021009";
    // 修改密码
    public static final String IF_UPDATE_PWD = "IF00021010";
    // 获取已使用免费WiFi信息
    public static final String IF_GET_WIFI_USE_INFO = "IF00161001";
    // 开启上网
    public static final String IF_OPEN_WIFI = "IF00161002";
    // 结束上网
    public static final String IF_CLOSE_WIFI = "IF00161003";
    // 累计WiFi使用信息接口
    public static final String IF_GET_TODAY_WIFI_USE_TIME = "IF00161004";
    // 更新用户权限分组
    public static final String IF_UPDATE_USER_GROUP = "IF00161006";
    // 版本检查
    public static final String IF_GET_VERSION = "IF00081001";
    // 客户端请求标签接口
    public static final String IF_GET_TAGS = "IF00021012";
    // 客户端上传userId和channelId接口
    public static final String IF_BINDBD = "IF00151001";
    // 客户端获取图集接口
    public static final String IF_GET_GALLERY = "IF00041002";
    // 提交收藏
    public static final String IF_COLLECT = "IF00021015";
    // 删除收藏
    public static final String IF_REMOVE_COLLECT = "IF00021021";
    // 获取收藏列表
    public static final String IF_GET_COLLECT = "IF00021016";
    // 根据pk_id获取分享信息接口
    public static final String IF_GET_SHARE = "IF00041004";
    // 我的奖品列表
    public static final String IF_GET_LOTTERY_LIST = "IF00041005";
    // 奖品详情
    public static final String IF_GET_LOTTERY_DETAIL = "IF00041006";
    // 获取新闻频道接口ID
    public static final String IF_GET_CHANNELS = "IF00041010";
    // 提交统计信息接口
    public static final String IF_SUBMIT_STATISTICS = "IF00181001";
    // 提交日志信息接口
    public static final String IF_SUBMIT_LOGS = "IF00181002";
    // 获取网址黑白名单规则
    public static final String IF_GET_URL_RULE = "IF00091001";
    // 提交附近的WIFI信息
    public static final String IF_SUBMIT_WIFI_LIST = "IF00381002";
    // 获取一级评论列表
    public static final String IF_GET_LEVEL_ONE_COMMENT_LIST = "IF00391001";
    // 获取二级评论列表
    public static final String IF_GET_LEVEL_TWO_COMMENT_LIST = "IF00391002";
    // 发表评论
    public static final String IF_SUBMIT_COMMENT = "IF00391003";
    // 点赞
    public static final String IF_LIKE = "IF00391004";
    // 评论回复
    public static final String IF_COMMENT_REPLY = "IF00391005";
    // 我的评论
    public static final String IF_MY_COMMENT = "IF00391006";

    /*******社交***********/
    // 捡瓶子
    public static final String IF_PICK_BOTTLE = "IF00361001";
    // 丢瓶子
    public static final String IF_THROW_BOTTLE = "IF00361002";
    // 发送聊天信息
    public static final String IF_SEND_MSG = "IF00361003";
    // 将瓶子扔回去
    public static final String IF_RETURN_BOTTLE = "IF00361004";
    // 删除瓶子接口
    public static final String IF_DELETE_BOTTLE = "IF00361005";
    // 获取好友用户信息
    public static final String IF_GET_FRIEND_INFO = "IF00361012";
    // 找人
    public static final String IF_FIND_PERSON = "IF00361008";
    // 获取好友列表
    public static final String IF_GET_FRIENDS = "IF00361009";
    // 添加/删除好友
    public static final String IF_ADD_OR_REMOVE_FRIEND = "IF00361010";
    // 响应好友请求
    public static final String IF_REPLY_FRIEND = "IF00361011";
    // 设置备注
    public static final String IF_MODIFY_FRIEND_REMARK = "IF00361013";
    /*******社交***********/

    // AR扫描抽奖
    public static final String IF_SCAN_LOTTERY = "IF00201101";

    //ras加密解密错误码
    public static final String ERROR_CODE_RSA = "03009";
    //des解密出错
    public static final String ERROR_CODE_DES = "03068";
    //rsa私钥获取错误
    public static final String ERROR_CODE_RSA_NULL = "03015";
    //json入参错误
    public static final String ERROR_CODE_JSON = "03066";
    //特殊rsa密钥出错
    public static final String ERROR_CODE_SPEC_RSA = "03069";
    //服务器错误
    public static final String ERROR_CODE_SERVER = "03067";
    //文件接收服务器连接转发不通错误
    public static final String ERROR_CODE_FILESYS = "014500";


    /**
     * 推送消息逻辑:
     * 1.消息分为3大类: 系统消息,营销推广,其它
     * 2.系统消息和营销推广消息需要存入个人中心里面的消息列表(图标不相同)
     */
    public static final String PUSH_NEWS = "0"; // 资讯详情
    public static final String PUSH_GALLERY = "1"; // 图集跳转
    public static final String PUSH_TOPIC = "2"; // 资讯专题
    public static final String PUSH_H5 = "3"; // H5页面(投票、抽奖、内容收集)
    public static final String PUSH_ADVICE_FEEDBACK = "8"; // 意见反馈跳转
    public static final String PUSH_SYS_MESSAGE = "9"; // 系统消息
    public static final String PUSH_CLEAR_CACHE = "10"; // 清理缓存
    public static final String PUSH_ADV = "11"; // 营销推广
    public static final String PUSH_BOTTLE = "13"; // 漂流瓶消息
    public static final String PUSH_FRIEND_APPLY = "14"; // 好友申请消息
    public static final String PUSH_CHAT = "15"; // 好友聊天消息
    public static final String PUSH_COMMENT_REPLY = "16"; // 评论回复消息
}
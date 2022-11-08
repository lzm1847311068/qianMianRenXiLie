package com.example.qianmianrenxilie;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.qianmianrenxilie.bean.BuyerNum;
import com.example.qianmianrenxilie.service.KeepAliveService;
import com.example.qianmianrenxilie.util.HttpClient;
import com.example.qianmianrenxilie.util.NotificationSetUtil;
import com.example.qianmianrenxilie.util.UpdateApk;
import com.example.qianmianrenxilie.util.WindowPermissionCheck;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 停止接单取消所有网络请求
 * 远程公告、频率等
 * try catch
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etUname,etPaw,etName,etSms;
    private TextView tvStart,tvStop,tvLog,tvBrow,tvAppDown,tvAppOpen,tvTitle,tvGetTitle;
    private LinearLayout smsLin;
    private Handler mHandler;
    private String Authorization;
    private List<String> yqList;
    private String tbId;
    /*
    接单成功音乐提示播放次数（3次）
    播放的次数是count+1次
     */
    private int count;
    private SharedPreferences userInfo;
    private int minPl;
    private int tbIndex;
    private AlertDialog dialog;
    private List<BuyerNum> buyerNumList;
    private String[] tbNameArr;
    private AlertDialog alertDialog2;
    private boolean isAuth = false;
    private static String LOGIN_URL = "";
    private static String DOWNLOAD = "";
    private static String OPENURL = "";
    private String loginType = "";
//    private Calendar startDate;
    private String status;




    /**
     * 需要更改的地方：
     *
     */
    private static String PT_URL = "qianMianRen";
    private static String APK_PACKAGE = "com.lzm.qmr";
    private static String TITLE = "千面人助手";
    private static String TI_SHI = "千面人App未安装";
    private static String CHANNELID = "qmrSuccess";
    private static String SUCCESS_TI_SHI = "千面人接单成功";
    private static int JIE_DAN_SUCCESS = R.raw.qmr_success;
    private static int JIE_DAN_FAIL = R.raw.qmr_fail;
    private static int ICON = R.mipmap.qmr;
//    private static int JIEDAN_DATE = 10;


    private static String QUAN_XIAN_LOGIN = "/api/auth/login?";
    private static String GET_TB = "/api/b-buyer-id?per_page=-1";
    private static String CHECK_ACC = "/api/consult?";
    private String CHECK_ACCOUNT = "/api/auth/info?";
    private String GET_TASK1 = "/api/b-buyer-id/";
    private String GET_TASK2 = "/status/receive?";
    private static String SMS = "/api/sms-code?";
    private static String CHECK_GET = "/api/btask?page=1&ut_status=1&bbi_id=";
    private String STOP_TASK1 = "/api/b-buyer-id/";
    private String STOP_TASK2 = "/status/stop?";
    private String TASK_DETAIL = "/api/btask/";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, KeepAliveService.class);
        //启动保活服务
        startService(intent);
        ignoreBatteryOptimization();//忽略电池优化

        if(!checkFloatPermission(this)){
            //权限请求方法
            requestSettingCanDrawOverlays();
        }
        //获取平台登录地址
        getPtAddress(this);
        initView();
    }

    private void initView(){
        //检查更新
        UpdateApk.update(MainActivity.this);
        //是否开启通知权限
        openNotification();
        //是否开启悬浮窗权限
        WindowPermissionCheck.checkPermission(this);
        tvGetTitle = findViewById(R.id.tv_getTitle);
        smsLin = findViewById(R.id.ll_sms);
        etName = findViewById(R.id.et_uname);
        etSms = findViewById(R.id.et_sms);
        mHandler = new Handler();
        tvBrow = findViewById(R.id.tv_brow);
        etUname = findViewById(R.id.et_username);
        tvAppDown = findViewById(R.id.tv_appDown);
        tvAppOpen = findViewById(R.id.tv_appOpen);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(TITLE);
        etPaw = findViewById(R.id.et_password);
        tvStart = findViewById(R.id.tv_start);
        tvStop = findViewById(R.id.tv_stop);
        tvLog = findViewById(R.id.tv_log);
        //设置textView为可滚动方式
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setTextIsSelectable(true);
        buyerNumList = new ArrayList<>();
        //读取用户信息
        getUserInfo();
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvBrow.setOnClickListener(this);
        tvAppOpen.setOnClickListener(this);
        tvAppDown.setOnClickListener(this);
        tvGetTitle.setOnClickListener(this);
        yqList = new ArrayList<>();

        tvLog.setText("找不到商品怎么办?"+"\n");
        tvLog.append("通过店铺名找到该商品，浏览1分钟左右，返回淘宝重新按照任务关键字搜索，商品就会显示在前面"+"\n");

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_start:

                /**
                 * 不设置为null的话，第一次接单停止后，第二次接单，tbID有值
                 */
                tbId = null;
                /*
                先清除掉之前的Handler中的Runnable，不然会和之前的任务一起执行多个
                 */
                mHandler.removeCallbacksAndMessages(null);

                if(smsLin.getVisibility() == View.GONE){
                    loginType = "token";
                }else {
                    loginType = "phone";
                }

                if(LOGIN_URL == ""){
                    sendLog("获取最新网址中...");
                }else {
                    login(etUname.getText().toString().trim(),
                            etPaw.getText().toString().trim(),
                            etName.getText().toString().trim(),
                            etSms.getText().toString().trim(),"login");
                }
                break;
            case R.id.tv_stop:
                stop();
                break;
            case R.id.tv_appDown:
                Uri uri = Uri.parse(DOWNLOAD);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.tv_appOpen:
                openApp(APK_PACKAGE);
                break;
            case R.id.tv_brow:
                browOpen();
                break;
            case R.id.tv_getTitle:

                if(smsLin.getVisibility() == View.GONE){
                    loginType = "token";
                }else {
                    loginType = "phone";
                }

                if(LOGIN_URL == ""){
                    tvLog.setText("获取最新网址中,请3秒后重试...");
                }else {
                    login(etUname.getText().toString().trim(),
                            etPaw.getText().toString().trim(),
                            etName.getText().toString().trim(),
                            etSms.getText().toString().trim(),"");
                }
                break;
        }

    }

    private void browOpen(){
        if(OPENURL == ""){
            sendLog("获取最新网址中...");
        }else {
            Uri uri = Uri.parse(OPENURL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    /**
     * 重写activity的onKeyDown方法，点击返回键后不销毁activity
     * 可参考：https://blog.csdn.net/qq_36713816/article/details/71511860
     * 另外一种解决办法：重写onBackPressed方法，里面不加任务内容，屏蔽返回按钮
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    /**
     * 登录平台
     * @param username
     * @param password
     */
    private void login(String username, String password,String uName,
                       String smsCode,String type){

        if(type.equals("login")){
            status = "login";
        }else {
            status = "";
        }

        /**
         * {"code":401,"data":null,"message":"用户不存在"}
         * {"code":401,"data":null,"message":"登录姓名错误。"}
         * {"code":401,"data":null,"message":"密码错误"}
         * {"code":406,"data":null,"message":"短时间内密码输入错误超过3次，必须使用手机验证码登录"}
         * {"code":0,"data":{"jwt":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE2NjA0NTY2NDQsImp0aSI6ImFVaFlNVlF6UWtkUVpVeDZNbUkxZFVOb1JuQmlXbGRQWlRKamEwVXRhbW89IiwiaXNzIjoiZnJvbnRhcGkiLCJuYmYiOjE2NjA0NTY2NDksImV4cCI6MTY2NjQ5MzQ0NCwiZGF0YSI6eyJ1c2VyX2luZm8iOnsidV9pZCI6NzYyMTIsInVfdHlwZSI6MSwidV91c2VybmFtZSI6IjEzMzcwNTIxNDA0IiwidV9hdmF0YXIiOiIiLCJ1X2VtYWlsIjoiIiwidV9tb2JpbGVfcGhvbmUiOiIxMzM3MDUyMTQwNCIsInVfaW52aXRlX2NvZGUiOiIzbjJaSDROQyIsInVfaW52aXRlX3VfaWQiOjczMTc5LCJ1X3FxIjoiNTA1MjcwMDY5IiwidV93ZWNoYXQiOiJsem05NDY1IiwidV9zdGF0dXMiOiJhY3RpdmUiLCJ1X2F1dGhfc3RhdHVzIjoiaGFkX2F1dGgiLCJ1X3JlYWxuYW1lIjoiXHU2NzRlXHU1OTI3XHU4MzYzIiwidV9jcmVhdGVkX2F0IjoxNjYwMTgxMjM0LCJ1X3VwZGF0ZWRfYXQiOjE2NjA0NDMyODAsInVfZXhhbV9zdGF0dXMiOjIsInVfbGFzdF9sb2dpbl9hdCI6MTY2MDQ0MzI4MCwidV9zaXRlX2lkIjoxLCJ1X3RyZWVfcGF0aCI6IlwvMTAwMDZcLzEwMDAzXC8xMDY1NFwvMjU3ODhcLzczMTc5XC83NjIxMiIsImNoZWNrX3N0YXR1cyI6MCwidV9sYXN0X3VubG9ja19hdCI6MCwicHVuaXNoX21vZGUiOjAsInVfcHVuaXNoX2F0IjowLCJsb2NrNF9udW0iOjAsInB1bmlzaF9tb2RlX21lc3NhZ2UiOiIifSwidG9rZW5faW5mbyI6eyJpZCI6ImFVaFlNVlF6UWtkUVpVeDZNbUkxZFVOb1JuQmlXbGRQWlRKamEwVXRhbW89IiwidHlwZSI6InBob25lIiwiZXhwaXJlIjoxNjY2NDkzNDQ0fX19.rwqnww3QrFPx05rRJVf7FTWgtOj6Rj9H6DkX5pqEyq8szGVfVO3WcXSH0GX2SWVNxwXnBddBx_F9mfFHE2DpVA","u_id":76212},"message":""}
         */

        //记录接单开始时间 并加10分钟
//        startDate = Calendar.getInstance();
//        startDate.add(Calendar.MINUTE,JIEDAN_DATE);

        tvLog.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": 正在登陆中..."+"\n");

        HttpClient.getInstance().post(QUAN_XIAN_LOGIN, LOGIN_URL)
                .params("code","1111")  //验证码没用
                .params("index",username)
                .params("password",password)
                .params("sms_code",smsCode)
                .params("type",loginType)
                .params("u_realname",uName)
                .headers("Content-Type","application/json;charset=UTF-8")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject loginJsonObj = JSONObject.parseObject(response.body());
                            if("0".equals(loginJsonObj.getString("code"))){
                                smsLin.setVisibility(View.GONE);
                                //保存账号和密码
                                saveUserInfo(username,password,uName);
                                sendLog("登录成功");
                                //获取token
                                Authorization = loginJsonObj.getJSONObject("data").getString("jwt");
                                checkAcconut();
                                return;
                            }else if(406 == loginJsonObj.getInteger("code")){
                                sendLog(loginJsonObj.getString("message"));
                                getSmsCode();
                            }else {
                                sendLog(loginJsonObj.getString("message"));
                            }
                        }catch (Exception e){
                            sendLog("登录："+e.getMessage());
                        }
                    }
                });
    }




    private void getSmsCode() {
        HttpClient.getInstance().post(SMS,LOGIN_URL)
                .params("phone",etUname.getText().toString().trim())
                .params("sms_type","login")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (0 == obj.getInteger("code")){
                                smsLin.setVisibility(View.VISIBLE);
                                sendLog("发送验证码成功，请输入验证码后再次登录");
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("短信异常："+e.getMessage());
                        }
                    }
                });
    }


    /**
     * 检查是否有强制评价
     */
    private void checkAcconut() {
        HttpClient.getInstance().post(CHECK_ACC,LOGIN_URL)
                .params("type","b_task_report")
                .headers("Content-Type","application/json;charset=UTF-8")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (0 == obj.getInteger("code")){
                                String info = obj.getJSONObject("data").getString("report_desc");
                                if (!"".equals(info)){
                                    sendLog(info);
                                    playMusic(JIE_DAN_FAIL,3000,0);
                                }else {
                                    getTbInfo();
                                }
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("checkAcconut："+e.getMessage());
                        }
                    }
                });
    }



    private void getTbInfo() {
        HttpClient.getInstance().get(GET_TB,LOGIN_URL)
                .headers("Content-Type","application/json;charset=UTF-8")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (0 == obj.getInteger("code")){
                                JSONArray tbArr = obj.getJSONObject("data").getJSONArray("items");
                                buyerNumList.clear();
                                for (int i = 0; i < tbArr.size(); i++) {
                                    JSONObject tbInfo = tbArr.getJSONObject(i);
                                    /**
                                     * 1等待审核   2通过审核
                                     * 1淘宝   4京东
                                     */
                                    if("2".equals(tbInfo.getString("bbi_status"))){
                                        if ("1".equals(tbInfo.getString("bbi_platform_id"))){
                                            String tbId = tbInfo.getString("bbi_id");
                                            String tbName = tbInfo.getString("bbi_id_index_str");
                                            buyerNumList.add(new BuyerNum(tbId,tbName));
                                        }
                                    }
                                }
                                if(buyerNumList.size() == 0){
                                    sendLog("无可用接单账号");
                                    return;
                                }
                                sendLog("获取到"+buyerNumList.size()+"个可用淘宝号");
                                tbNameArr = new String[buyerNumList.size()+1];
                                tbNameArr[0] = "自动切换买号";
                                for (int i = 0; i < buyerNumList.size(); i++){
                                    tbNameArr[i+1] = buyerNumList.get(i).getName();
                                }
                                showSingleAlertDialog();
                            }
                        }catch (Exception e){
                            sendLog("getTbInfo："+e.getMessage());
                        }
                    }
                });
    }


    public void showSingleAlertDialog(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择淘宝号");
        alertBuilder.setCancelable(false); //触摸窗口边界以外是否关闭窗口，设置 false
        alertBuilder.setSingleChoiceItems( tbNameArr, -1, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if("自动切换买号".equals(tbNameArr[index])){
                    isAuth = true;
                    sendLog("将使用 "+tbNameArr[index]+" 进行接单");
                    //默认用第一个
                    tbId = buyerNumList.get(0).getId();
                }else {
                    isAuth = false;
                    //根据选择的淘宝名获取淘宝id
                    List<BuyerNum> buyerNum = buyerNumList.stream().
                            filter(p -> p.getName().equals(tbNameArr[index])).collect(Collectors.toList());
                    tbId = buyerNum.get(0).getId();
                    sendLog("将使用 "+buyerNum.get(0).getName()+" 进行接单");
                }
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO 业务逻辑代码
                if(!isAuth && tbId == null){
                    sendLog("未选择接单淘宝号");
                    return;
                }
                start();
                // 关闭提示框
                alertDialog2.dismiss();
            }
        });
        alertDialog2 = alertBuilder.create();
        alertDialog2.show();
    }


    public void start(){
        if(isAuth){
            tbIndex = 0;
            tbId = buyerNumList.get(tbIndex).getId();
            tbIndex++;  //++的目的是，如果3个买号都是正常的，则会获取第二个买号
        }
        if("login".equals(status)){
            checkKaoShi();
        }else {
            checkTask2();
        }

    }



    private void checkKaoShi() {
        HttpClient.getInstance().post(CHECK_ACC,LOGIN_URL)
                .params("type","b_exam_report")
                .headers("Content-Type","application/json;charset=UTF-8")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            //{"code":0,"data":{"report_desc":"您已经通过考试","report_status":true,"params":[]},"message":""}
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (0 == obj.getInteger("code")){
                                boolean isStart = obj.getJSONObject("data").getBoolean("report_status");
                                if(!isStart){
                                    sendLog("请先去平台，个人中心，我的考试，完成考试在接单！");
                                    playMusic(JIE_DAN_FAIL,3000,0);
                                    return;
                                }
                                checkAccount();
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("checkKaoShi："+e.getMessage());
                        }
                    }
                });
    }




    /**
     * 领取任务前的检测
     * 银行卡没绑定会提示这个
     */
    public void checkAccount(){

        HttpClient.getInstance().get(CHECK_ACCOUNT,LOGIN_URL)
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if(0 == obj.getInteger("code")){
                                String msg = obj.getJSONObject("data").getString("u_tips");
                                if(!"".equals(msg)){
                                    playMusic(JIE_DAN_FAIL,3000,0);
                                    sendLog(msg);
                                }else {
                                    getTask();
                                }
                                return;
                            }
                            sendLog(obj.getString("message"));
                            playMusic(JIE_DAN_FAIL,3000,0);
                        }catch (Exception e){
                            sendLog("checkAccount："+e.getMessage());
                        }
                    }
                });
    }



    /**
     * 领取任务
     */
    public void getTask(){

        HttpClient.getInstance().put(GET_TASK1+tbId+GET_TASK2,LOGIN_URL)
                .params("platform", "taobao")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if(0 == obj.getInteger("code")){
                            sendLog("开始排队...");
                            jieDan();
                            return;
                        }
                        //code  为1：存在没考试的情况
                        sendLog(obj.getString("message"));
                        playMusic(JIE_DAN_FAIL,3000,0);
                    }
                });
    }



    private void checkTask() {
        HttpClient.getInstance().get(CHECK_GET+tbId,LOGIN_URL)
                .headers("Content-Type","application/json;charset=UTF-8")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        try {
                            if("0".equals(obj.getString("code"))){
                                sendLog("获取任务中...");
                                JSONArray arr = obj.getJSONObject("data").getJSONArray("items");
                                if(arr.size() != 0){
                                    for (int i = 0; i < arr.size(); i++) {
                                        JSONObject j = arr.getJSONObject(i);
                                        /**
                                         * 刚接到的是2，两日任务第一日做完这里变成了10
                                         */
                                        if("2".equals(j.getString("ut_status"))){
                                            sendLog("接单成功");
                                            playMusic(JIE_DAN_SUCCESS,3000,2);
                                            getTaskDetail(j.getString("ut_id"),j.getString("ms_store_name"));
                                        }
                                    }
                                    return;
                                }
                                jieDan();
                                return;
                            }
                            sendLog(obj.getString("message"));
                            playMusic(JIE_DAN_FAIL,3000,0);
                        }catch (Exception e){
                            sendLog("checkTask："+e.getMessage());
                        }

                    }
                });
    }



    private void checkTask2() {
        HttpClient.getInstance().get(CHECK_GET+tbId,LOGIN_URL)
                .headers("Content-Type","application/json;charset=UTF-8")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if("0".equals(obj.getString("code"))){
                                JSONArray arr = obj.getJSONObject("data").getJSONArray("items");
                                if(arr.size() != 0){
                                    for (int i = 0; i < arr.size(); i++) {
                                        JSONObject j = arr.getJSONObject(i);
                                        /**
                                         * 刚接到的是2，两日任务第一日做完这里变成了10
                                         * 第二日在可以点击继续任务后，多了个字段"tp_pay_target": "2",
                                         */
                                        if("2".equals(j.getString("ut_status"))){
                                            getTaskDetail(j.getString("ut_id"),j.getString("ms_store_name"));
                                        }else if("10".equals(j.getString("ut_status"))){
                                            if(j.containsKey("tp_pay_target")){
                                                getTaskDetail(j.getString("ut_id"),j.getString("ms_store_name"));
                                            }
                                        }
                                    }
                                    return;
                                }
                                sendLog("此接单账号暂时无可代操作任务！");
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("checkTask2："+e.getMessage());
                        }
                    }
                });
    }



    private void getTaskDetail(String orderId,String dianPuMing) {
        HttpClient.getInstance().get(TASK_DETAIL+orderId+"?",LOGIN_URL)
                .headers("Content-Type","application/json;charset=UTF-8")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                        JSONObject j = JSONObject.parseObject(response.body());
                            if("0".equals(j.getString("code"))){
                                JSONObject json = j.getJSONObject("data");
                                //3应该是多日任务，1是当天任务，5是交付任务（应该也是多天完成）
                                String taskType = json.getString("tp_type");
                                JSONArray arr = json.getJSONArray("tp_rgoods_list");  //副商品
                                JSONArray jingPinArr = json.getJSONArray("tp_cgoods_list");  //竞品
                                String zhaoCha = json.getString("tp_goods_compare_word");  //找茬答案
                                //这个有时候是脱敏的
    //                            String dianPuMing = json.getString("ms_store_name");
                                receiveSuccess(dianPuMing);
                                sendLog1("-------------------------");
                                if("1".equals(taskType)){
                                    sendLog1("关键词："+json.getString("t_keywords"));
                                }else if("3".equals(taskType)){
                                    JSONObject gjc = json.getJSONObject("t_keywords_list");
                                    sendLog1("第一天关键词："+gjc.getString("word"));
                                    sendLog1("第二天关键词："+gjc.getString("word02"));
                                }else if("5".equals(taskType)){
                                    sendLog1("关键词："+json.getString("t_keywords"));
                                    sendLog1("目标商品链接："+json.getString("tp_g_url"));
                                }
                                sendLog1("-------------------------");
                                if(zhaoCha != ""){
                                    sendLog1("找茬答案："+json.getString("tp_goods_compare_word"));
                                    sendLog1("-------------------------");
                                }
                                sendLog1("商品全标题："+json.getString("g_name"));
                                sendLog1("-------------------------");
                                sendLog1("店铺名："+dianPuMing);

                                for (int i = 0; i < arr.size(); i++) {
                                    sendLog1("-------------------------");
                                    JSONObject f = arr.getJSONObject(i);
                                    sendLog1("副商品全标题"+i+"："+f.getString("rg_name"));
                                    String zc = f.getString("rg_compare_word");
                                    if(zc != ""){
                                        sendLog1("副商品找茬"+i+"："+zc);
                                    }
                                }
                                for (int i = 0; i < jingPinArr.size(); i++) {
                                    sendLog1("-------------------------");
                                    JSONObject f = jingPinArr.getJSONObject(i);
                                    sendLog1("竞品全标题"+i+"："+f.getString("cg_name"));
                                }
                            }
                        }catch (Exception e){
                            sendLog("getTaskDetail："+e.getMessage());
                        }
                    }
                });
    }




    public void jieDan(){

        if(isAuth){  //自动接单
            if(tbIndex < buyerNumList.size()){
                tbId = buyerNumList.get(tbIndex).getId();
            }else {
                tbIndex = 0;
                tbId = buyerNumList.get(tbIndex).getId();
            }
            tbIndex++;
        }

        //判断有没有接10分钟，没有则
//        Calendar calendar = Calendar.getInstance();
//        if (calendar.after(startDate)){
//            //更新当前时间
//            startDate = calendar;
//            startDate.add(Calendar.MINUTE,JIEDAN_DATE);
//
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
////                    stopTask("1");
//                    checkTask();
//                }
//            }, minPl);
//            return;
//        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkTask();
            }
        }, minPl);


    }




    private void stopTask(String biaoZhi){

        HttpClient.getInstance().put(STOP_TASK1+tbId+STOP_TASK2,LOGIN_URL)
                .params("platform", "taobao")
                .headers("Authorization","Bearer "+Authorization)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if(0 == obj.getInteger("code")){
                                if("1".equals(biaoZhi)){
                                    mHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            getTask();
                                        }
                                    }, 5000);
                                    return;
                                }
                                sendLog("已停止接单");
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("stopTask："+e.getMessage());
                        }
                    }
                });
    }




    /**
     * 停止接单
     */
    public void stop(){
        OkGo.getInstance().cancelAll();
        //Handler中已经提供了一个removeCallbacksAndMessages去清除Message和Runnable
        mHandler.removeCallbacksAndMessages(null);
        stopTask("2");
    }


    /**
     * 接单成功后通知铃声
     * @param voiceResId 音频文件
     * @param milliseconds 需要震动的毫秒数
     */
    private void playMusic(int voiceResId, long milliseconds,int total){
        count = total;//不然会循环播放
        //播放语音
        MediaPlayer player = MediaPlayer.create(MainActivity.this, voiceResId);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //播放完成事件
                if(count != 0){
                    player.start();
                    count --;
                }
            }
        });
        //震动
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        //延迟的毫秒数
        vib.vibrate(milliseconds);
    }




    /**
     * 弹窗公告
     */
    public void announcementDialog(String[] lesson){

        // String[] lesson = new String[]{"此助手针对平台专属定制优化，接单效率比其他接单助手效率都要强，建议使用此助手接单","","接单成功后,会显示搜索关键字，商品全标题，店铺名，找茬提示","","只提供参考作用，禁止直接搜索店铺和商品全标题购买商品，客服发现会拉黑"};

        dialog = new AlertDialog
                .Builder(this)
                .setTitle("公告")
                .setCancelable(false) //触摸窗口边界以外是否关闭窗口，设置 false
                .setPositiveButton("我知道了", null)
                //.setMessage("")
                .setItems(lesson,null)
                .create();
        dialog.show();
    }



    /**
     * 日志更新
     * @param log
     */
    public void sendLog1(String log){
        scrollToTvLog();
        tvLog.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": "+log+"\n");
    }


    public void sendLog(String log){
        scrollToTvLog();
        if(tvLog.getLineCount() > 40){
            tvLog.setText("");
        }
        tvLog.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": "+log+"\n");
    }


    public void scrollToTvLog(){
        int tvHeight = tvLog.getHeight();
        int tvHeight2 = getTextViewHeight(tvLog);
        if(tvHeight2>tvHeight){
            tvLog.scrollTo(0,tvHeight2-tvLog.getHeight());
        }
    }


    private int getTextViewHeight(TextView textView) {
        Layout layout = textView.getLayout();
        int desired = layout.getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() +
                textView.getCompoundPaddingBottom();
        return desired + padding;
    }

    private void openNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //判断是否需要开启通知栏功能
            NotificationSetUtil.OpenNotificationSetting(this);
        }
    }


    /**
     * 忽略电池优化
     */
    public void ignoreBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if(!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+getPackageName()));
                startActivity(intent);
            }
        }
    }


    private void openApp(String packName){
        PackageManager packageManager = this.getPackageManager();
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
        if (apps.size() == 0) {
            Toast.makeText(this, TI_SHI, Toast.LENGTH_LONG).show();
            return;
        }
        ResolveInfo resolveInfo = apps.iterator().next();
        if (resolveInfo != null) {
            String className = resolveInfo.activityInfo.name;
            Intent intent2 = new Intent(Intent.ACTION_MAIN);
            intent2.addCategory(Intent.CATEGORY_LAUNCHER);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packName, className);
            intent2.setComponent(cn);
            this.startActivity(intent2);
        }
    }


    //判断是否开启悬浮窗权限   context可以用你的Activity.或者tiis
    public static boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }



    public void getPtAddress(Context context){

        HttpClient.getInstance().get("/ptVersion/checkUpdate","http://47.94.255.103")
                .params("ptName",PT_URL)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject ptAddrObj = JSONObject.parseObject(response.body());
                        String url = ptAddrObj.getString("ptUrl");
                        if(url == null){
                            tvLog.setText("请联系软件开发者更新平台网址信息~"+"\n");
                            return;
                        }
                        LOGIN_URL = url;

                        //公告弹窗
                        String[] gongGao = ptAddrObj.getString("ptAnnoun").split(";");
                        announcementDialog(gongGao);

                        DOWNLOAD = ptAddrObj.getString("apkDownload");
                        minPl = Integer.parseInt(ptAddrObj.getString("pinLv"));
                        OPENURL = ptAddrObj.getString("openUrl");
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("服务器出现问题啦~");
                    }
                });
    }





    /**
     * 接单成功执行逻辑
     */
    @SuppressLint("WrongConstant")
    protected void receiveSuccess(String dianPuName){
        //前台通知的id名，任意
        String channelId = CHANNELID;
        //前台通知的名称，任意
        String channelName = "接单成功状态栏通知";
        //发送通知的等级，此处为高，根据业务情况而定
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // 2. 获取系统的通知管理器
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 3. 创建NotificationChannel(这里传入的channelId要和创建的通知channelId一致，才能为指定通知建立通知渠道)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName, importance);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        //点击通知时可进入的Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        // 1. 创建一个通知(必须设置channelId)
        Notification notification = new NotificationCompat.Builder(this,channelId)
                .setContentTitle(SUCCESS_TI_SHI)
                .setContentText("店铺名:"+dianPuName)
                .setSmallIcon(ICON)
                .setContentIntent(pendingIntent)//点击通知进入Activity
                .setPriority(NotificationCompat.PRIORITY_MAX) //设置通知的优先级为最大
                .setCategory(Notification.CATEGORY_TRANSPORT) //设置通知类别
                .setVisibility(Notification.VISIBILITY_PUBLIC)  //控制锁定屏幕中通知的可见详情级别
                .build();

        // 4. 发送通知
        notificationManager.notify(2, notification);
    }


    //权限打开
    private void requestSettingCanDrawOverlays() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, 1);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
        } else {//4.4-6.0以下
            //无需处理了
        }
    }


    public void onResume() {
        super.onResume();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //移除标记为id的通知 (只是针对当前Context下的所有Notification)
        notificationManager.cancel(2);
        //移除所有通知
        //notificationManager.cancelAll();

    }



    /**
     * 保存用户信息
     */
    private void saveUserInfo(String username,String password,String uName){

        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//获取Editor
        //得到Editor后，写入需要保存的数据
        editor.putString("username",username);
        editor.putString("password", password);
        editor.putString("uName", uName);
        editor.commit();//提交修改

    }

    /**
     * 读取用户信息
     */
    private void getUserInfo(){
        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        String username = userInfo.getString("username", null);//读取username
        String passwrod = userInfo.getString("password", null);//读取password
        String yj1 = userInfo.getString("uName",null);
        if(username!=null && passwrod!=null){
            etUname.setText(username);
            etPaw.setText(passwrod);
            etName.setText(yj1);
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭弹窗，不然会 报错（虽然不影响使用）
        dialog.dismiss();
    }

}
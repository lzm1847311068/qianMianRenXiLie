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
 * ????????????????????????????????????
 * ????????????????????????
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
    ???????????????????????????????????????3??????
    ??????????????????count+1???
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
     * ????????????????????????
     *
     */
    private static String PT_URL = "qianMianRen";
    private static String APK_PACKAGE = "com.lzm.qmr";
    private static String TITLE = "???????????????";
    private static String TI_SHI = "?????????App?????????";
    private static String CHANNELID = "qmrSuccess";
    private static String SUCCESS_TI_SHI = "?????????????????????";
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
        //???????????????
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, KeepAliveService.class);
        //??????????????????
        startService(intent);
        ignoreBatteryOptimization();//??????????????????

        if(!checkFloatPermission(this)){
            //??????????????????
            requestSettingCanDrawOverlays();
        }
        //????????????????????????
        getPtAddress(this);
        initView();
    }

    private void initView(){
        //????????????
        UpdateApk.update(MainActivity.this);
        //????????????????????????
        openNotification();
        //???????????????????????????
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
        //??????textView??????????????????
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setTextIsSelectable(true);
        buyerNumList = new ArrayList<>();
        //??????????????????
        getUserInfo();
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvBrow.setOnClickListener(this);
        tvAppOpen.setOnClickListener(this);
        tvAppDown.setOnClickListener(this);
        tvGetTitle.setOnClickListener(this);
        yqList = new ArrayList<>();

        tvLog.setText("?????????????????????????"+"\n");
        tvLog.append("???????????????????????????????????????1??????????????????????????????????????????????????????????????????????????????????????????"+"\n");

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_start:

                /**
                 * ????????????null??????????????????????????????????????????????????????tbID??????
                 */
                tbId = null;
                /*
                ?????????????????????Handler??????Runnable????????????????????????????????????????????????
                 */
                mHandler.removeCallbacksAndMessages(null);

                if(smsLin.getVisibility() == View.GONE){
                    loginType = "token";
                }else {
                    loginType = "phone";
                }

                if(LOGIN_URL == ""){
                    sendLog("?????????????????????...");
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
                    tvLog.setText("?????????????????????,???3????????????...");
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
            sendLog("?????????????????????...");
        }else {
            Uri uri = Uri.parse(OPENURL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    /**
     * ??????activity???onKeyDown????????????????????????????????????activity
     * ????????????https://blog.csdn.net/qq_36713816/article/details/71511860
     * ?????????????????????????????????onBackPressed??????????????????????????????????????????????????????
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
     * ????????????
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
         * {"code":401,"data":null,"message":"???????????????"}
         * {"code":401,"data":null,"message":"?????????????????????"}
         * {"code":401,"data":null,"message":"????????????"}
         * {"code":406,"data":null,"message":"????????????????????????????????????3???????????????????????????????????????"}
         * {"code":0,"data":{"jwt":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE2NjA0NTY2NDQsImp0aSI6ImFVaFlNVlF6UWtkUVpVeDZNbUkxZFVOb1JuQmlXbGRQWlRKamEwVXRhbW89IiwiaXNzIjoiZnJvbnRhcGkiLCJuYmYiOjE2NjA0NTY2NDksImV4cCI6MTY2NjQ5MzQ0NCwiZGF0YSI6eyJ1c2VyX2luZm8iOnsidV9pZCI6NzYyMTIsInVfdHlwZSI6MSwidV91c2VybmFtZSI6IjEzMzcwNTIxNDA0IiwidV9hdmF0YXIiOiIiLCJ1X2VtYWlsIjoiIiwidV9tb2JpbGVfcGhvbmUiOiIxMzM3MDUyMTQwNCIsInVfaW52aXRlX2NvZGUiOiIzbjJaSDROQyIsInVfaW52aXRlX3VfaWQiOjczMTc5LCJ1X3FxIjoiNTA1MjcwMDY5IiwidV93ZWNoYXQiOiJsem05NDY1IiwidV9zdGF0dXMiOiJhY3RpdmUiLCJ1X2F1dGhfc3RhdHVzIjoiaGFkX2F1dGgiLCJ1X3JlYWxuYW1lIjoiXHU2NzRlXHU1OTI3XHU4MzYzIiwidV9jcmVhdGVkX2F0IjoxNjYwMTgxMjM0LCJ1X3VwZGF0ZWRfYXQiOjE2NjA0NDMyODAsInVfZXhhbV9zdGF0dXMiOjIsInVfbGFzdF9sb2dpbl9hdCI6MTY2MDQ0MzI4MCwidV9zaXRlX2lkIjoxLCJ1X3RyZWVfcGF0aCI6IlwvMTAwMDZcLzEwMDAzXC8xMDY1NFwvMjU3ODhcLzczMTc5XC83NjIxMiIsImNoZWNrX3N0YXR1cyI6MCwidV9sYXN0X3VubG9ja19hdCI6MCwicHVuaXNoX21vZGUiOjAsInVfcHVuaXNoX2F0IjowLCJsb2NrNF9udW0iOjAsInB1bmlzaF9tb2RlX21lc3NhZ2UiOiIifSwidG9rZW5faW5mbyI6eyJpZCI6ImFVaFlNVlF6UWtkUVpVeDZNbUkxZFVOb1JuQmlXbGRQWlRKamEwVXRhbW89IiwidHlwZSI6InBob25lIiwiZXhwaXJlIjoxNjY2NDkzNDQ0fX19.rwqnww3QrFPx05rRJVf7FTWgtOj6Rj9H6DkX5pqEyq8szGVfVO3WcXSH0GX2SWVNxwXnBddBx_F9mfFHE2DpVA","u_id":76212},"message":""}
         */

        //???????????????????????? ??????10??????
//        startDate = Calendar.getInstance();
//        startDate.add(Calendar.MINUTE,JIEDAN_DATE);

        tvLog.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": ???????????????..."+"\n");

        HttpClient.getInstance().post(QUAN_XIAN_LOGIN, LOGIN_URL)
                .params("code","1111")  //???????????????
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
                                //?????????????????????
                                saveUserInfo(username,password,uName);
                                sendLog("????????????");
                                //??????token
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
                            sendLog("?????????"+e.getMessage());
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
                                sendLog("?????????????????????????????????????????????????????????");
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("???????????????"+e.getMessage());
                        }
                    }
                });
    }


    /**
     * ???????????????????????????
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
                            sendLog("checkAcconut???"+e.getMessage());
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
                                     * 1????????????   2????????????
                                     * 1??????   4??????
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
                                    sendLog("?????????????????????");
                                    return;
                                }
                                sendLog("?????????"+buyerNumList.size()+"??????????????????");
                                tbNameArr = new String[buyerNumList.size()+1];
                                tbNameArr[0] = "??????????????????";
                                for (int i = 0; i < buyerNumList.size(); i++){
                                    tbNameArr[i+1] = buyerNumList.get(i).getName();
                                }
                                showSingleAlertDialog();
                            }
                        }catch (Exception e){
                            sendLog("getTbInfo???"+e.getMessage());
                        }
                    }
                });
    }


    public void showSingleAlertDialog(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("??????????????????");
        alertBuilder.setCancelable(false); //??????????????????????????????????????????????????? false
        alertBuilder.setSingleChoiceItems( tbNameArr, -1, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int index) {
                if("??????????????????".equals(tbNameArr[index])){
                    isAuth = true;
                    sendLog("????????? "+tbNameArr[index]+" ????????????");
                    //??????????????????
                    tbId = buyerNumList.get(0).getId();
                }else {
                    isAuth = false;
                    //????????????????????????????????????id
                    List<BuyerNum> buyerNum = buyerNumList.stream().
                            filter(p -> p.getName().equals(tbNameArr[index])).collect(Collectors.toList());
                    tbId = buyerNum.get(0).getId();
                    sendLog("????????? "+buyerNum.get(0).getName()+" ????????????");
                }
            }
        });
        alertBuilder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO ??????????????????
                if(!isAuth && tbId == null){
                    sendLog("????????????????????????");
                    return;
                }
                start();
                // ???????????????
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
            tbIndex++;  //++?????????????????????3??????????????????????????????????????????????????????
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
                            //{"code":0,"data":{"report_desc":"?????????????????????","report_status":true,"params":[]},"message":""}
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if (0 == obj.getInteger("code")){
                                boolean isStart = obj.getJSONObject("data").getBoolean("report_status");
                                if(!isStart){
                                    sendLog("????????????????????????????????????????????????????????????????????????");
                                    playMusic(JIE_DAN_FAIL,3000,0);
                                    return;
                                }
                                checkAccount();
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("checkKaoShi???"+e.getMessage());
                        }
                    }
                });
    }




    /**
     * ????????????????????????
     * ?????????????????????????????????
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
                            sendLog("checkAccount???"+e.getMessage());
                        }
                    }
                });
    }



    /**
     * ????????????
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
                            sendLog("????????????...");
                            jieDan();
                            return;
                        }
                        //code  ???1???????????????????????????
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
                                sendLog("???????????????...");
                                JSONArray arr = obj.getJSONObject("data").getJSONArray("items");
                                if(arr.size() != 0){
                                    for (int i = 0; i < arr.size(); i++) {
                                        JSONObject j = arr.getJSONObject(i);
                                        /**
                                         * ???????????????2?????????????????????????????????????????????10
                                         */
                                        if("2".equals(j.getString("ut_status"))){
                                            sendLog("????????????");
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
                            sendLog("checkTask???"+e.getMessage());
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
                                         * ???????????????2?????????????????????????????????????????????10
                                         * ?????????????????????????????????????????????????????????"tp_pay_target": "2",
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
                                sendLog("?????????????????????????????????????????????");
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("checkTask2???"+e.getMessage());
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
                                //3????????????????????????1??????????????????5?????????????????????????????????????????????
                                String taskType = json.getString("tp_type");
                                JSONArray arr = json.getJSONArray("tp_rgoods_list");  //?????????
                                JSONArray jingPinArr = json.getJSONArray("tp_cgoods_list");  //??????
                                String zhaoCha = json.getString("tp_goods_compare_word");  //????????????
                                //???????????????????????????
    //                            String dianPuMing = json.getString("ms_store_name");
                                receiveSuccess(dianPuMing);
                                sendLog1("-------------------------");
                                if("1".equals(taskType)){
                                    sendLog1("????????????"+json.getString("t_keywords"));
                                }else if("3".equals(taskType)){
                                    JSONObject gjc = json.getJSONObject("t_keywords_list");
                                    sendLog1("?????????????????????"+gjc.getString("word"));
                                    sendLog1("?????????????????????"+gjc.getString("word02"));
                                }else if("5".equals(taskType)){
                                    sendLog1("????????????"+json.getString("t_keywords"));
                                    sendLog1("?????????????????????"+json.getString("tp_g_url"));
                                }
                                sendLog1("-------------------------");
                                if(zhaoCha != ""){
                                    sendLog1("???????????????"+json.getString("tp_goods_compare_word"));
                                    sendLog1("-------------------------");
                                }
                                sendLog1("??????????????????"+json.getString("g_name"));
                                sendLog1("-------------------------");
                                sendLog1("????????????"+dianPuMing);

                                for (int i = 0; i < arr.size(); i++) {
                                    sendLog1("-------------------------");
                                    JSONObject f = arr.getJSONObject(i);
                                    sendLog1("??????????????????"+i+"???"+f.getString("rg_name"));
                                    String zc = f.getString("rg_compare_word");
                                    if(zc != ""){
                                        sendLog1("???????????????"+i+"???"+zc);
                                    }
                                }
                                for (int i = 0; i < jingPinArr.size(); i++) {
                                    sendLog1("-------------------------");
                                    JSONObject f = jingPinArr.getJSONObject(i);
                                    sendLog1("???????????????"+i+"???"+f.getString("cg_name"));
                                }
                            }
                        }catch (Exception e){
                            sendLog("getTaskDetail???"+e.getMessage());
                        }
                    }
                });
    }




    public void jieDan(){

        if(isAuth){  //????????????
            if(tbIndex < buyerNumList.size()){
                tbId = buyerNumList.get(tbIndex).getId();
            }else {
                tbIndex = 0;
                tbId = buyerNumList.get(tbIndex).getId();
            }
            tbIndex++;
        }

        //??????????????????10??????????????????
//        Calendar calendar = Calendar.getInstance();
//        if (calendar.after(startDate)){
//            //??????????????????
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
                                sendLog("???????????????");
                                return;
                            }
                            sendLog(obj.getString("message"));
                        }catch (Exception e){
                            sendLog("stopTask???"+e.getMessage());
                        }
                    }
                });
    }




    /**
     * ????????????
     */
    public void stop(){
        OkGo.getInstance().cancelAll();
        //Handler????????????????????????removeCallbacksAndMessages?????????Message???Runnable
        mHandler.removeCallbacksAndMessages(null);
        stopTask("2");
    }


    /**
     * ???????????????????????????
     * @param voiceResId ????????????
     * @param milliseconds ????????????????????????
     */
    private void playMusic(int voiceResId, long milliseconds,int total){
        count = total;//?????????????????????
        //????????????
        MediaPlayer player = MediaPlayer.create(MainActivity.this, voiceResId);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //??????????????????
                if(count != 0){
                    player.start();
                    count --;
                }
            }
        });
        //??????
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        //??????????????????
        vib.vibrate(milliseconds);
    }




    /**
     * ????????????
     */
    public void announcementDialog(String[] lesson){

        // String[] lesson = new String[]{"????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????","","???????????????,?????????????????????????????????????????????????????????????????????","","??????????????????????????????????????????????????????????????????????????????????????????????????????"};

        dialog = new AlertDialog
                .Builder(this)
                .setTitle("??????")
                .setCancelable(false) //??????????????????????????????????????????????????? false
                .setPositiveButton("????????????", null)
                //.setMessage("")
                .setItems(lesson,null)
                .create();
        dialog.show();
    }



    /**
     * ????????????
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
            //???????????????????????????????????????
            NotificationSetUtil.OpenNotificationSetting(this);
        }
    }


    /**
     * ??????????????????
     */
    public void ignoreBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            //  ????????????APP??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
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


    //?????????????????????????????????   context???????????????Activity.??????tiis
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
                            tvLog.setText("????????????????????????????????????????????????~"+"\n");
                            return;
                        }
                        LOGIN_URL = url;

                        //????????????
                        String[] gongGao = ptAddrObj.getString("ptAnnoun").split(";");
                        announcementDialog(gongGao);

                        DOWNLOAD = ptAddrObj.getString("apkDownload");
                        minPl = Integer.parseInt(ptAddrObj.getString("pinLv"));
                        OPENURL = ptAddrObj.getString("openUrl");
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("????????????????????????~");
                    }
                });
    }





    /**
     * ????????????????????????
     */
    @SuppressLint("WrongConstant")
    protected void receiveSuccess(String dianPuName){
        //???????????????id????????????
        String channelId = CHANNELID;
        //??????????????????????????????
        String channelName = "???????????????????????????";
        //???????????????????????????????????????????????????????????????
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // 2. ??????????????????????????????
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 3. ??????NotificationChannel(???????????????channelId?????????????????????channelId????????????????????????????????????????????????)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName, importance);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        //???????????????????????????Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        // 1. ??????????????????(????????????channelId)
        Notification notification = new NotificationCompat.Builder(this,channelId)
                .setContentTitle(SUCCESS_TI_SHI)
                .setContentText("?????????:"+dianPuName)
                .setSmallIcon(ICON)
                .setContentIntent(pendingIntent)//??????????????????Activity
                .setPriority(NotificationCompat.PRIORITY_MAX) //?????????????????????????????????
                .setCategory(Notification.CATEGORY_TRANSPORT) //??????????????????
                .setVisibility(Notification.VISIBILITY_PUBLIC)  //????????????????????????????????????????????????
                .build();

        // 4. ????????????
        notificationManager.notify(2, notification);
    }


    //????????????
    private void requestSettingCanDrawOverlays() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0??????
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, 1);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
        } else {//4.4-6.0??????
            //???????????????
        }
    }


    public void onResume() {
        super.onResume();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //???????????????id????????? (??????????????????Context????????????Notification)
        notificationManager.cancel(2);
        //??????????????????
        //notificationManager.cancelAll();

    }



    /**
     * ??????????????????
     */
    private void saveUserInfo(String username,String password,String uName){

        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//??????Editor
        //??????Editor?????????????????????????????????
        editor.putString("username",username);
        editor.putString("password", password);
        editor.putString("uName", uName);
        editor.commit();//????????????

    }

    /**
     * ??????????????????
     */
    private void getUserInfo(){
        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        String username = userInfo.getString("username", null);//??????username
        String passwrod = userInfo.getString("password", null);//??????password
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
        //???????????????????????? ?????????????????????????????????
        dialog.dismiss();
    }

}
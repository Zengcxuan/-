package studyproject.tcl.com.alarmtonotify;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import static studyproject.tcl.com.alarmtonotify.NotificationHelper.CHANNEL_ID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    protected Button notifyCurrentlyBtn, notifySettingBtn, alarmNotifyBtn, channelSettingBtn;
    protected NotificationHelper notificationHelper;
    public static final String INTENT_ALARM_LOG = "studyproject.tcl.com.RECEIVER";
    private String TAG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notifyCurrentlyBtn = findViewById(R.id.notify_currently);
        notifySettingBtn = findViewById(R.id.notify_setting);
        alarmNotifyBtn = findViewById(R.id.alarm_notify);
        channelSettingBtn = findViewById(R.id.channel_setting);
        notifySettingBtn.setOnClickListener(this);
        notifyCurrentlyBtn.setOnClickListener(this);
        alarmNotifyBtn.setOnClickListener(this);
        channelSettingBtn.setOnClickListener(this);
        notificationHelper = new NotificationHelper(this);
        TAG = this.getPackageName();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.notify_currently:
                sendNotify();
                break;
            case R.id.notify_setting:
                notificationHelper.openNotificationSetting();
                break;
            case R.id.channel_setting:
                notificationHelper.openChannelSetting(CHANNEL_ID);
            case R.id.alarm_notify:
                clockSet();
                break;
            default:
                break;
        }
    }

    /**
     * 立刻发送一条notification
     */
    private void sendNotify(){
        NotificationCompat.Builder builder = notificationHelper.getNotification("Notification",
                "即时通知");
        builder.build();
        notificationHelper.notify(1,builder);
    }

    /**
     * 用一个Dialog来设置要发送的内容
     */
    private void clockSet(){
        final EditText editText = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("定时提醒");
        builder.setMessage("请输入内容");
        builder.setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input = editText.getText().toString();
                Log.v( TAG, input);
                clockSetting(input);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "取消", Toast.LENGTH_LONG).show();
            }
        });
        builder.create().show();
    }

    /**
     * 调用AlarmManager服务来定时发送一条广播
     * 参考https://blog.csdn.net/kongqwesd12/article/details/78998151
     */
    private void clockSetting(final String string){
        final Calendar currentTime = Calendar.getInstance();
        new TimePickerDialog(this, 0, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                /*set alarm to send a Broadcast*/
                Intent intent = new Intent();
                intent.setAction(INTENT_ALARM_LOG);
                /*安卓O开始对于静态注册的广播需要设置ComponentName*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setComponent(new ComponentName("studyproject.tcl.com.alarmtonotify",
                            "studyproject.tcl.com.alarmtonotify.MyBroadcast"));
                }
                intent.putExtra("msg", string);
                PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, intent,
                        0);
                Calendar c = Calendar.getInstance();
                //set alarm time
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                //get the system's AlarmManager
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                /*非必要不使用RTC_WAKEUP*/
                if(alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
                    Log.e(TAG, c.getTime().toString());
                }else {
                    Log.e(TAG, "not found");
                }
                Log.i(TAG, String.valueOf(c.getTimeInMillis()));
            }
        }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), false).show();

    }

}

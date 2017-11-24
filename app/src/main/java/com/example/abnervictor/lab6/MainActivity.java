package com.example.abnervictor.lab6;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private CircularImageView disk_pic;
    private EditText  file_name;
    private ImageView button_play_pause;
    private ImageView button_stop;
    private ImageView button_quit;
    private TextView  play_status;
    private TextView  passed_time;
    private TextView  total_time;
    private SeekBar   seekBar;
    private SimpleDateFormat time;

    private ServiceConnection serviceConnection;
    private IBinder mBinder;
    private int status;//0 for Stop, 1 for Playing, 2 for Pause
    private int totaltime;//曲目长度
    private String RootPath;
    private FileHelper fileHelper;

    //读写权限
    private static boolean hasPermission;

    private boolean firstPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = 0;
        firstPlay = true;//首次播放，需要初始化音乐播放器

        //检测权限
        hasPermission = true;
        verifyStoragePermissions(this);
        if (hasPermission){
            //复制raw文件夹中的文件到SD卡
            RootPath = Environment.getExternalStorageDirectory().getPath();
            fileHelper = new FileHelper(RootPath);
            fileHelper.createFolder(RootPath,"Lab6MusicPlayer");//在sd卡下新建文件夹
            RootPath += "/Lab6MusicPlayer";
            fileHelper.setRootPath(RootPath);//更新根目录路径
            fileHelper.createFolder(RootPath, "music");//在根目录下新建音乐文件夹
            fileHelper.createFolder(RootPath, "picture");//在根目录下新建图片文件夹
            fileHelper.copyRawToFolder(this,R.raw.melt,"music","melt","mp3");//将音乐文件复制到文件夹
            Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.drawable.example);
            fileHelper.copyBitmapToFolder(bmp,"picture","example");
            fileHelper.copyRawToFolder(this,R.raw.melt_pic,"picture","melt","jpg");//将图片文件复制到文件夹
            fileHelper.copyRawToFolder(this,R.raw.battlefield1,"music","battlefield1","mp3");//将音乐文件复制到文件夹
            fileHelper.copyRawToFolder(this,R.raw.battlefield1_pic,"picture","battlefield1","jpg");//将图片文件复制到文件夹

//            String filename = "melt";
//            InputStream inputStream = this.getResources().openRawResource(R.raw.melt);
//            copyFilesFromStream(filename+".mp3", inputStream);
//
//            inputStream = this.getResources().openRawResource(R.raw.melt_pic);
//            copyFilesFromStream(filename+".jpg", inputStream);
//
//            filename = "battlefield1";
//            inputStream = this.getResources().openRawResource(R.raw.battlefield1);
//            copyFilesFromStream(filename+".mp3", inputStream);
//
//            inputStream = this.getResources().openRawResource(R.raw.battlefield1_pic);
//            copyFilesFromStream(filename+".jpg", inputStream);
            //复制raw文件夹中的文件到SD卡

            //绑定服务
            initService();

            //按键监听器
            findView();
            setOnClickListener();

            //启用线程与handler更新进度条
            handlerAndThread();

//          setBinder(104);
        }
        else{
            try{
                MainActivity.this.finish();
                System.exit(0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(serviceConnection != null){
            unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    public static void verifyStoragePermissions(Activity activity){
        try{
            int permission_read = ActivityCompat.checkSelfPermission(activity,"android.permission.READ_EXTERNAL_STORAGE");
            int permission_write = ActivityCompat.checkSelfPermission(activity,"android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission_read != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);//请求读权限
            }
            if (permission_write != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);//请求写权限
            }
            if (permission_read != PackageManager.PERMISSION_GRANTED && permission_write != PackageManager.PERMISSION_GRANTED){
                hasPermission = true;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
        else{
            System.exit(0);//关闭进程
        }
    }

//    private void copyFilesFromStream(String filename, InputStream inputStream){
//        File DATA_PATH = Environment.getExternalStorageDirectory();
//        String externalStorage = Environment.getExternalStorageState();
//        if (externalStorage.equals(Environment.MEDIA_MOUNTED)){
//            String filePath = DATA_PATH.getPath() + File.separator + filename;//文件路径 + 分隔符 + 文件名
//            File file = new File(filePath);
//            try{
//                if (!file.exists()){
//                    //建立通道对象
//                    FileOutputStream fileOutputStream = new FileOutputStream(file);
//                    //定义储存空间
//                    byte[] buffer = new byte[inputStream.available()];
//                    //开始读文件
//                    int length = 0;
//                    while ((length = inputStream.read(buffer)) != -1){
//                        //将buffer重的数据写到outputStream对象中
//                        fileOutputStream.write(buffer, 0, length);
//                    }//循环从输入流读取buffer字节
//                    fileOutputStream.flush();//刷新缓冲区
//                    fileOutputStream.close();//关闭流
//                    inputStream.close();
//                }//文件不存在时进行复制
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }//将文件流写入到程序文件夹

    private void initService(){
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                Log.d("service", "connected");
                mBinder = service;
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                serviceConnection = null;

            }
        };
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);//绑定服务
    }//绑定服务

    private int initPlayer(String filepath){
        try{
            int code = 104;
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeString(filepath);
            mBinder.transact(code,data,reply,0);//发送调用
            return reply.readInt();
        }catch (RemoteException e){
            e.printStackTrace();
        }
        return -1;
    }//通过binder发送信息初始化播放器

    private void findView(){
        file_name   = findViewById(R.id.file_name);
        disk_pic    = findViewById(R.id.disk_pic);
        button_play_pause = findViewById(R.id.button_play_pause);
        button_stop = findViewById(R.id.button_stop);
        button_quit = findViewById(R.id.button_quit);
        play_status = findViewById(R.id.play_status);
        passed_time = findViewById(R.id.passed_time);
        total_time  = findViewById(R.id.total_time );
        seekBar     = findViewById(R.id.progressBar);

        time = new SimpleDateFormat("mm:ss");
    }

    private Parcel setBinder(int code){
        try{
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            mBinder.transact(code,data,reply,0);//发送调用
            return reply;
        }catch (RemoteException e){
            e.printStackTrace();
        }
        return null;
    }//binder通讯

    private void setOnClickListener(){
        button_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(firstPlay){
                    //用歌曲路径初始化播放器
                    totaltime = initPlayer(file_name.getText().toString()+".mp3");
                    seekBar.setMax(totaltime);
                    total_time.setText(time.format(new Date(totaltime)));
                    //获取歌曲封面图
//                    try {
//                        bm = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(new File(picpath)));
//                        disk_pic.setImageBitmap(bm);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
                    Bitmap bm = fileHelper.getBitmapFromFolder("picture",file_name.getText().toString(),"jpg");
                    if(bm!=null){
                        Log.d("setBitmap","bitmap set!");
                        disk_pic.setImageBitmap(bm);
                    }
                    else{
                        disk_pic.setImageResource(R.drawable.example);
                    }
                    firstPlay = false;
                }//歌曲第一次播放，初始化歌曲信息
                Parcel reply = setBinder(101);
                status = reply.readInt();
                if (status == 1){
                    play_status.setText("Now Playing");
                    button_play_pause.setImageResource(R.drawable.pause);
                }
                else if(status == 2){
                    play_status.setText("Music Paused");
                    button_play_pause.setImageResource(R.drawable.play);
                }
            }
        });
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play_status.setText("Music Stop");
                Parcel reply = setBinder(102);
                status = reply.readInt();
                firstPlay = true;
                button_play_pause.setImageResource(R.drawable.play);
                status = reply.readInt();
            }
        });
        button_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbindService(serviceConnection);
                serviceConnection = null;
                try{
                    MainActivity.this.finish();
                    System.exit(0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }//点击退出按钮时，解除服务绑定并结束activity
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float deg = 1080*(float)i/(float)totaltime;
                disk_pic.setPivotX(disk_pic.getWidth()/2);
                disk_pic.setPivotX(disk_pic.getHeight()/2);
                disk_pic.setRotation(deg);
                passed_time.setText(time.format(new Date(i)));
                if (b){
                    try{
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        data.writeInt(i);
                        mBinder.transact(106,data,reply,0);//发送调用
                    }catch (RemoteException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (status == 1){
                    setBinder(101);
                    status = 2;
                    play_status.setText("Music Paused");
                    button_play_pause.setImageResource(R.drawable.play);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (status == 2){
                    setBinder(101);
                    status = 1;
                    play_status.setText("Now Playing");
                    button_play_pause.setImageResource(R.drawable.pause);
                }
            }
        });//进度条监听器

    }//按钮监听器
    private int passedtime;
    private void handlerAndThread(){
        @SuppressLint("HandlerLeak") final Handler mHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                super.handleMessage(message);
                switch (message.what){
                    case 1:
                        Parcel reply = setBinder(105);//获取当前时间
                        passedtime = reply.readInt();
                        seekBar.setProgress(passedtime);
                }
            }
        };

        Thread mThread = new Thread(){
            @Override
            public void run(){
                while (true){
                    try{
                        Thread.sleep(100);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    if (serviceConnection != null && hasPermission){
                        if(status == 1)mHandler.obtainMessage(1).sendToTarget();
                    }
                }
            }
        };//不断的让Handler更新UI
        mThread.start();
    }//Handler与线程

}

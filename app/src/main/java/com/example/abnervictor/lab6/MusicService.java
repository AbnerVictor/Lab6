package com.example.abnervictor.lab6;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;

/**
 * Created by abnervictor on 2017/11/23.
 */

public class MusicService extends Service {

    private IBinder mBinder = new MyBinder();
    private static MediaPlayer mediaPlayer = new MediaPlayer();

    private String path;//音乐文件的路径

    public MusicService(){
        path = Environment.getExternalStorageDirectory().getPath() + File.separator;//文件路径
    }

    public class MyBinder extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code){
                case 101:
                    //play
                    play_pause_music();//切换暂停和播放
                    if (mediaPlayer.isPlaying())
                        reply.writeInt(1);
                    else
                        reply.writeInt(2);
                    break;
                case 102:
                    //stop
                    stop_music();
                    reply.writeInt(0);
                    break;
                case 103:
                    stop_music();
                    reply.writeInt(0);
                    //quit
                    break;
                case 104:
                    //初始化播放器,并返回音乐长度
                    mediaPlayer.reset();
                    String filename = data.readString();
                    Log.v("The Music Path is",path+filename);
                    initMediaPlayer(path+filename);
                    reply.writeInt(mediaPlayer.getDuration());
                    break;
                case 105:
                    //currentTime
                    reply.writeInt(mediaPlayer.getCurrentPosition());
                    break;
                case 106:
                    //setTime
                    mediaPlayer.seekTo(data.readInt());
            }
            return super.onTransact(code, data, reply, flags);
        }
    }//响应Binder调用

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return mBinder;
    }

    private void initMediaPlayer(String Path){
        try{
            mediaPlayer.setDataSource(Path);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(false);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void play_pause_music(){
        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        else {
            mediaPlayer.start();
        }
    }

    private void stop_music(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}

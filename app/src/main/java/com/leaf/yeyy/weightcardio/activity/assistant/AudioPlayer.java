package com.leaf.yeyy.weightcardio.activity.assistant;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;

import com.leaf.yeyy.weightcardio.R;
import com.leaf.yeyy.weightcardio.activity.callback.IActionCallback;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayer implements OnBufferingUpdateListener, OnCompletionListener,
        OnPreparedListener {
    private static final String TAG = AudioPlayer.class.getSimpleName();
    public MediaPlayer mediaPlayer; // 媒体播放器
    List<String> mAudioUrlList;
    Iterator<String> mAudioUrlIterator;
    private SeekBar seekBar; // 拖动条
    private IActionCallback mCallback;
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int position = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            if (duration > 0 && seekBar != null) {
                // 计算进度（获取进度条最大刻度*当前音乐播放位置 / 当前音乐时长）
                long pos = seekBar.getMax() * position / duration;
                seekBar.setProgress((int) pos);
            }
        }

        ;
    };
    // 计时器
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (mediaPlayer == null)
                return;
            if (mediaPlayer.isPlaying() && seekBar != null && !seekBar.isPressed()) {
                handler.sendEmptyMessage(0); // 发送消息
            }
        }
    };
    private Timer mTimer = new Timer(); // 计时器

    // 初始化播放器
    public AudioPlayer(SeekBar seekBar) {
        super();
        this.seekBar = seekBar;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 每一秒触发一次
        mTimer.schedule(timerTask, 0, 1000);
    }

    public void play(Context context) {
        Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.shua);
        Log.d("YYYYYYYYYYYY", uri.toString());
        try {
            mediaPlayer.setDataSource(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    /**
     * @param url url地址
     */
    public void playUrl(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url); // 设置数据源
            mediaPlayer.prepare(); // prepare自动播放
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playUrlList(List<String> urlList, IActionCallback callback) {
        mCallback = callback;
        if (urlList != null && urlList.size() > 0) {
            mAudioUrlList = urlList;
            mAudioUrlIterator = urlList.iterator();
            playUrl(mAudioUrlIterator.next());
        }
    }

    /**
     * 是否在播放
     */
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    // 暂停
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    // 停止
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Log.e(TAG, "onPrepared");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mAudioUrlIterator.hasNext()) {
            playUrl(mAudioUrlIterator.next());
            Log.e(TAG, "play next");
        } else {
            Log.e(TAG, "onCompletion");
            if (mCallback != null) {
                mCallback.onComplete();
            }
        }
    }

    /**
     * 缓冲更新
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (seekBar != null) {
            seekBar.setSecondaryProgress(percent);
            int currentProgress = seekBar.getMax()
                    * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
            Log.e(TAG, percent + "% buffer");
        } else {
            Log.e(TAG, percent + "% buffer");
        }
    }

}

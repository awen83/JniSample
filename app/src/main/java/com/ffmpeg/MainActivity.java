package com.ffmpeg;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

public class MainActivity extends Activity implements Callback {
    private String TAG = "MainActivity";

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;
    
    private Bitmap mBitmap = null;
    private int mSec = 0;
    private String mFileName = null;
    private float mTimeBase = 0f;
    private int mTargetWidth = 0;
    private int mTargetHeight = 0;
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;
    private Rect mRect = null;
    private final int FIX_SCREEN = 3;
    private final int FIX_WIDTH = 2;
    private final int FIX_HEIGHT = 1;
    private final int FIX_VIDEO = 0;

    private final int MENU_FORWARD = Menu.FIRST;
    private final int MENU_BACK = Menu.FIRST+1;
    private final int MENU_PLAY = Menu.FIRST+2;
    private final int MENU_PAUSE = Menu.FIRST+3;
    private final int MENU_SELECT_FILE = Menu.FIRST+4;
    private final int MENU_EXIT = Menu.FIRST+5;
    
    private final int REQUEST_SELECT_FILE = 2000;
    
    private final int MSG_DRAW_FRAME = 1000;
    
    private DecodeThread mDecodeThread = null;
    private RenderThread mRenderThread = null;
    
    private Object mLockObject = new Object();
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case MSG_DRAW_FRAME:

                    break;

                default:
                    break;
            }
        }        
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mSurfaceView.getHolder().addCallback(this);
        mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        //mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        mScreenWidth = this.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = this.getResources().getDisplayMetrics().heightPixels;
        
        mDecodeThread = new DecodeThread();
        mDecodeThread.start();
        
        mRect = new Rect();

        Log.i(TAG, ">>>>>>>>>>>>>>>>>version="+ FFmpegJni._getVersion()+
                ", metrics"+this.getResources().getDisplayMetrics().toString()
                +" , screenDensity = "+this.getResources().getDisplayMetrics().densityDpi);
        
        Toast.makeText(this, "请按menu键显示控制菜单", Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>onResume");
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>onPause");
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mDecodeThread != null && mDecodeThread.isAlive()) {
            mDecodeThread.interrupt();
            boolean retry = true;
            while (retry) {
                try {
                    mDecodeThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // TODO: handle exception
                }
            }
        }
        if (FFmpegJni._isFileOpen()) {
            FFmpegJni._exit();
        }
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>onDestroy");
        super.onDestroy();
    }

    private void openFile() {
        synchronized (mLockObject) {
            if (FFmpegJni._isFileOpen()) {
                FFmpegJni._exit();
            }
            int ret = FFmpegJni._openFile(mFileName, mScreenWidth, mScreenHeight, FIX_WIDTH);
            if (ret < 0) {
                Toast.makeText(this, "打开文件错误!", Toast.LENGTH_LONG);
                return;
            }
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
            mTargetWidth = FFmpegJni._getTargetWidth();
            mTargetHeight = FFmpegJni._getTargetHeight();
            mRect.set((mScreenWidth-mTargetWidth)/2, (mScreenHeight-mTargetHeight)/2, 
                    (mScreenWidth+mTargetWidth)/2, (mScreenHeight+mTargetHeight)/2);
            mBitmap = Bitmap.createBitmap(mTargetWidth, mTargetHeight, Bitmap.Config.ARGB_8888);
            mTimeBase = FFmpegJni._getTimeBase();
            mRenderThread.setDirty();
            mRenderThread.setDirty();
            Log.i(TAG, ">>>>>>>>>>>>mTimeBase="+mTimeBase+","+mTargetWidth+","+mTargetHeight
                    +" , mRect ="+mRect.toString());
        }
    }
    
    private class RenderThread extends Thread {
        private boolean mAllowRendering = false;

        public RenderThread() {
            // TODO Auto-generated constructor stub
            
        }
        
        private void setDirty() {
            Rect rect = new Rect(mRect);
            Log.i(TAG, "before >>>>>>>>>>>>>>>>"+rect.toString());
            Canvas canvas = mSurfaceHolder.lockCanvas(rect);
            if (canvas != null) {
                canvas.drawColor(Color.WHITE);
            }
            mSurfaceHolder.unlockCanvasAndPost(canvas);
            Log.i(TAG, "after >>>>>>>>>>>>>>>>"+rect.toString());
        }
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>render thread start");
            while (!Thread.currentThread().isInterrupted()) {
                if (!FFmpegJni._isFileOpen()) continue;
                synchronized (mLockObject) {
                    Canvas canvas = mSurfaceHolder.lockCanvas(mRect);
                    if (canvas == null) {
                        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>canvas == null");
                    } else {
                        canvas.save();
                        canvas.drawBitmap(mBitmap, null, mRect, null);
                        canvas.restore();
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>canvas !!== null, canvas density = "+canvas.getDensity()
                                +" , mBitmap density = "+mBitmap.getDensity()
                                +" , mRect ="+mRect.toString()
                                +" , "+mScreenHeight+" , "+mScreenWidth
                                +" , "+mTargetHeight+" , "+mTargetWidth);
                    }
                    
                }
            }
            Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>render thread exit");
        }

    }
    
    private class DecodeThread extends Thread {
        private boolean mAllowDecode = false; 
        private int ret = 0;
        public DecodeThread() {
            // TODO Auto-generated constructor stub
        }
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>decode thread start");
            while (!Thread.currentThread().isInterrupted()) {
                if (!FFmpegJni._isFileOpen()) continue;
                synchronized (mLockObject) {
                    ret = FFmpegJni._drawFrame(mBitmap);
                    Log.i(TAG, ">>>>>>>>>>>>>>>decode ret = "+ret);
                    if (ret == 0) {
                        FFmpegJni._seekTo(0);
                    }
                }
            }
            Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>decode thread exit");
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>surfaceChanged, format="+format+" , width="+width+" ; height="+height);
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>surfaceCreated");
        mSurfaceHolder = holder;
        mRenderThread = new RenderThread();
        mRenderThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.i(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>surfaceDestroyed");
        mRenderThread.interrupt();
        boolean retry = true;
        while (retry) {
            try {
                mRenderThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }
    }
    

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        showAlertDialog();
    }
    
    
    protected void showAlertDialog() {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(this)
        .setTitle(R.string.exit)
        .setPositiveButton(android.R.string.ok, 
            new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    finish();
                }
            })
        .setNegativeButton(android.R.string.cancel, 
            new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    
                }
            })
        .show();
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_FORWARD, 0, getString(R.string.forward));
        menu.add(0, MENU_BACK, 0, getString(R.string.back));
        menu.add(0, MENU_PLAY, 0, getString(R.string.play));
        menu.add(0, MENU_PAUSE, 0, getString(R.string.pause));
        menu.add(0, MENU_SELECT_FILE, 0, getString(R.string.select_file));
        menu.add(0, MENU_EXIT, 0, getString(R.string.exit));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case MENU_FORWARD:
//                if (!FFmpegJni._isFileOpen()) {
//                    break;
//                }
//                mSec += 5;
//                Log.i(TAG, "mSec = "+mSec);
//                if (mSec < 0) break;
//                FFmpegJni._drawFrameAt(mBitmap, mSec);
//                mImageView.setImageBitmap(mBitmap);
                return true;

            case MENU_BACK:
//                if (!FFmpegJni._isFileOpen()) {
//                    break;
//                }
//                mSec -= 5;
//                Log.i(TAG, "mSec = "+mSec);
//                if (mSec < 0) break;
//                FFmpegJni._drawFrameAt(mBitmap, mSec);
//                mImageView.setImageBitmap(mBitmap);
                return true;
                
            case MENU_PLAY:

                return true;
                
            case MENU_PAUSE:

                return true;
                
            case MENU_SELECT_FILE:
                if ("/sdcard/clock.avi".equals(mFileName)) {
                    mFileName = "/sdcard/pili_clip.rmvb";
                } else if ("/sdcard/pili_clip.rmvb".equals(mFileName)) {
                    mFileName = "/sdcard/clock.avi";
                } else {
                    mFileName = "/sdcard/pili_clip.rmvb";
                }
                openFile();
                //startActivityForResult(new Intent(Defined.ACTION_ACTIVITY_SELECT_FILE), REQUEST_SELECT_FILE);
                return true;
                
            case MENU_EXIT:
                showAlertDialog();
                return true;
            
        }
        return super.onOptionsItemSelected(item);
    }
    
//  @Override
//  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//      // TODO Auto-generated method stub
//      switch (requestCode) {
//          case REQUEST_SELECT_FILE:
//              if (resultCode == RESULT_OK) {
//                  mFileName = data.getExtras().getString(Defined.INTENT_EXTRA_KEY_FILENAME);
//                  openFile();
//              } else if (resultCode == RESULT_CANCELED) {
//                  
//              }
//              break;
//
//          default:
//              break;
//      }
//  }
} 
//视频流信息
//+编码格式: FMP4
//+视频码率: 61 kbps
//+视频帧率：1 fps
//+分 辨 率: 320 x 240
//+显示比率: 1.333
//
//音频流信息
//+编码格式: MPA2L2
//+音频码率: 64 kbps
//+声 道 数: 1 channels
//+采 样 数: 16000 Hz
//+音频位数: 0 bits
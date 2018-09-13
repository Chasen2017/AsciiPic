package com.chasen.asciipic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // ---------------------- constant ---------------------- //

    private static final String TAG = "AsciiActivity";
    /**
     * 从相册中选择图片的返回码
     */
    private static final int ACTION_SELECT_FORM_ALBUM = 0X001;
    /**
     * 相机拍摄后的返回码
     */
    private static final int ACTION_TAKE_PHOTO = 0X002;
    /**
     * 从相册选择视频的返回码
     */
    private static final int ACTION_PICK_VIDEO = 0X003;
    /**
     * 多少ms取一帧
     */
    private static final int GET_FRAME_PER_MS = 250;

    private static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/AsciiPic/";


    // ---------------------- field ---------------------- //

    private ArrayList<Bitmap> mBitmaps = new ArrayList<>();
    /**
     * 显示的bitmap的下标
     */
    private int mIndex = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mIndex < mBitmaps.size()) {
                Bitmap bitmap = mBitmaps.get(mIndex++);
                Log.e(TAG, "bitmap:" + bitmap);
                if (bitmap != null) {
                    mIm.setImageBitmap(bitmap);
                    mHandler.postDelayed(this, 250);
                }
            }
        }
    };
    private GetFramesTask mTask;
    private Bitmap mCurBitmap;

    // ---------------------- widget ---------------------- //

    Button mSelFromAlbumBtn;
    Button mTakePhotoBtn;
    Button mSelVideoBtn;
    Button mPlayBtn;
    Button mSaveBtn;
    ImageView mIm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ascii);
        mSelFromAlbumBtn = findViewById(R.id.btn_select_album);
        mTakePhotoBtn = findViewById(R.id.btn_take_photo);
        mSelVideoBtn = findViewById(R.id.btn_select_video);
        mPlayBtn = findViewById(R.id.btn_play);
        mSaveBtn = findViewById(R.id.btn_save);
        mIm = findViewById(R.id.iv);
        mSelFromAlbumBtn.setOnClickListener(this);
        mTakePhotoBtn.setOnClickListener(this);
        mSelVideoBtn.setOnClickListener(this);
        mPlayBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_album:
                jumpToAlbum();
                break;
            case R.id.btn_take_photo:
                jumpToCamera();
                break;
            case R.id.btn_select_video:
                jumpToVideo();
                break;
            case R.id.btn_play:
                mHandler.removeCallbacks(mRunnable);
                mIndex = 0;
                mHandler.post(mRunnable);
                break;
            case R.id.btn_save:
                saveAsPic();
                break;
            default:
                break;
        }
    }

    /**
     * 跳转到系统相册
     */
    private void jumpToAlbum() {
        Log.d(TAG, "jumpToAlbum");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ACTION_SELECT_FORM_ALBUM);
    }

    /**
     * 跳转到相机
     */
    private void jumpToCamera() {
        Log.d(TAG, "jumpToCamera");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, ACTION_TAKE_PHOTO);
    }

    /**
     * 跳转到选择视频
     */
    private void jumpToVideo() {
        Log.d(TAG, "jumpToVideo");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, ACTION_PICK_VIDEO);
    }

    /**
     * 保存图片
     */
    private void saveAsPic() {
        if (mCurBitmap == null) {
            Toast.makeText(this, "图片为空，保存失败", Toast.LENGTH_SHORT).show();
            return;
        }
        // 创建目录
        File dir = new File(SAVE_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String path = SAVE_PATH + System.currentTimeMillis() + ".jpg";
        File file = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            mCurBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Toast.makeText(this, "保存成功，保存路径："+path, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // 从相册返回来
            if (requestCode == ACTION_SELECT_FORM_ALBUM) {
                Uri uri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                String path = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                cursor.close();
                Log.d(TAG, "path:" + path);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    bitmap = PicToAsciiUitl.zipBitmap(bitmap);
                    mCurBitmap = PicToAsciiUitl.createAsciiPic(bitmap, MainActivity.this);
                    mIm.setImageBitmap(mCurBitmap);
                }
            }
            // 从拍照返回来
            else if (requestCode == ACTION_TAKE_PHOTO) {
                Bitmap bitmap = data.getParcelableExtra("data");
                if (bitmap != null) {
                    mCurBitmap = PicToAsciiUitl.createAsciiPic(bitmap, MainActivity.this);
                    mIm.setImageBitmap(mCurBitmap);
                } else {
                    Log.e(TAG, "bitmap is null");
                }
            }
            // 选择视频
            else if (requestCode == ACTION_PICK_VIDEO) {
                mBitmaps.clear();
                Log.d(TAG, "pick video");
                Uri uri = data.getData();
                mTask = new GetFramesTask(this);
                mTask.execute(uri);
            }
        } else {
            Log.e(TAG, "select pic fail");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTask.cancel(true);
        mBitmaps.clear();
        mBitmaps = null;
    }

    /**
     * 将视频转化为一帧一帧的Bitmap，然后用Handler播放出来
     */
    private class GetFramesTask extends AsyncTask<Uri, Integer, List<Bitmap>> {

        private Context context;
        private AlertDialog dialog;
        private TextView mProgressTv;
        // 一共取了多少帧
        private int size;

        public GetFramesTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
            mProgressTv = view.findViewById(R.id.txt_progress);
            dialog = new AlertDialog.Builder(context)
                    .setView(view)
                    .setCancelable(false)
                    .create();
            dialog.show();
        }

        @Override
        protected List<Bitmap> doInBackground(Uri... uris) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, uris[0]);
            size = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / GET_FRAME_PER_MS;
            int index = 0;
            Log.e(TAG, "size:" + size);
            long startTime = System.currentTimeMillis();
            while (index < size + 1) {
                Bitmap bitmap = mmr.getFrameAtTime(index * 1000 * GET_FRAME_PER_MS, MediaMetadataRetriever.OPTION_CLOSEST);
                if (bitmap != null) {
                    mBitmaps.add(PicToAsciiUitl.createAsciiPic(bitmap, MainActivity.this));
                }
                Log.d(TAG, "index:" + index);
                publishProgress(index);
                index++;
            }
            Log.d(TAG, "spent time:" + (System.currentTimeMillis() - startTime));
            return mBitmaps;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressTv.setText("(" + values[0] + "/" + size + ")");
        }

        @Override
        protected void onPostExecute(List<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mBitmaps.clear();
            mBitmaps = null;
            context = null;
        }
    }
}

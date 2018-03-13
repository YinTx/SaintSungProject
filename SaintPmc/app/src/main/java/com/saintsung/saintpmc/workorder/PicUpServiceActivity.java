package com.saintsung.saintpmc.workorder;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.qr_codescan.MipcaActivityCapture;
import com.google.gson.Gson;
import com.saintsung.saintpmc.MyApplication;
import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.asynctask.RetrofitRxAndroidHttp;
import com.saintsung.saintpmc.bean.ServiceUpPhotoBean;
import com.saintsung.saintpmc.configuration.MD5;
import com.saintsung.saintpmc.tool.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by XLzY on 2017/7/26.
 */

public class PicUpServiceActivity extends AppCompatActivity {
    private String workNumber;
    private static final int TAKE_PHOTO_REQUEST_CODE = 1;
    private static final int REQUEST_ORIGINAL = 0;
    private String filePath;
    private final int SCANNIN_GREQUEST_CODE = 2;//局号
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImageView picPhoto;
    private Button submitPhoto;
    private AutoCompleteTextView bureauNo;
    private ImageButton scanBureauNo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_up_service);
        workNumber = getIntent().getStringExtra("workOrder");
        takePhoto(this);
        verifyStoragePermissions(this);
        picPhoto = (ImageView) findViewById(R.id.img);
        bureauNo = (AutoCompleteTextView) findViewById(R.id.bureauNo);
        submitPhoto = (Button) findViewById(R.id.sum_pic);
        scanBureauNo = (ImageButton) findViewById(R.id.scanBureauNo);
        scanBureauNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(PicUpServiceActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });
        submitPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bureauNo.getText().toString().equals("")) {
                    if(ToastUtil.isNetworkAvailable(PicUpServiceActivity.this)){InternetRxWork(filePath);
                        submitPhoto.setEnabled(false);}else {
                        Toast.makeText(PicUpServiceActivity.this,"请检查网络后再提交！",Toast.LENGTH_LONG).show();
                    }

                } else {
                    bureauNo.setError("请扫描或输入局号！");
                }
            }
        });
        findViewById(R.id.pic_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String state = Environment.getExternalStorageState(); // 判断是否存在sd卡
                filePath = getFileName();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    // 直接调用系统的照相机
                    //判断是否是AndroidN以及更高的版本
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(MediaStore.Images.Media.DATA,filePath);
                        Uri uri =PicUpServiceActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent, REQUEST_ORIGINAL);
                    } else {
                    Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = Uri.fromFile(new File(filePath));
                    //为拍摄的图片指定一个存储的路径
                    intent2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent2, REQUEST_ORIGINAL);}
                } else {
                    showToast("请检查手机是否有SD卡");
                }
            }
        });
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * 生成文件路径和文件名
     *
     * @return
     */
    private String getFileName() {
        String saveDir = Environment.getExternalStorageDirectory() + "/SaintSung/MyPhoto";
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建文件夹
        }
        //用日期作为文件名，确保唯一性
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = saveDir + "/" + formatter.format(date) + ".PNG";
        return fileName;
    }

    /**
     * 加载本地图片
     * http://bbs.3gstdy.com
     *
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    //拍摄完成后执行
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //返回值==-1表示拍摄成功
        if (resultCode == -1) {
            switch (requestCode) {
                case SCANNIN_GREQUEST_CODE:
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    bureauNo.setText(result);
                    break;
                case REQUEST_ORIGINAL:
                    picPhoto.setImageBitmap(getLoacalBitmap(filePath));
                    break;
            }
        }
    }

    private void InternetRxWork(String filePath) {
        Gson gson = new Gson();
        ServiceUpPhotoBean serviceUpPhotoBean = new ServiceUpPhotoBean();
        serviceUpPhotoBean.setOptUserNumber(MyApplication.getUserId());
        serviceUpPhotoBean.setUserNumber(bureauNo.getText().toString());
        serviceUpPhotoBean.setWorkOrderNumber(workNumber);
        serviceUpPhotoBean.setSign(MD5.toMD5(serviceUpPhotoBean.getOptCode() + serviceUpPhotoBean.getOptUserNumber() + serviceUpPhotoBean.getUserNumber() + serviceUpPhotoBean.getWorkOrderNumber()));
        RetrofitRxAndroidHttp retrofitRxAndroidHttp = new RetrofitRxAndroidHttp();
        String str = gson.toJson(serviceUpPhotoBean);
        retrofitRxAndroidHttp.serviceFileConnect(MyApplication.getUrl(), filePath, str, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    dataProcessing(response.body().string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "上传失败");
            }
        });
    }

    private void dataProcessing(String result) {
        Log.e("TAG", "===" + result);
        Gson gson = new Gson();
        ServiceUpPhotoBean serviceUpPhotoBean = gson.fromJson(result, ServiceUpPhotoBean.class);
        if (serviceUpPhotoBean.getResult().equals("0000")) {
            showToast("提交成功！");
            this.finish();
        } else {
            submitPhoto.setEnabled(true);
            showToast("提交失败"+serviceUpPhotoBean.getResultMessage());
        }
    }

    /**
     * 显示Toast
     *
     * @param
     */
    protected void showToast(String showText) {
        Toast.makeText(this, showText, Toast.LENGTH_LONG).show();
    }

    public static void takePhoto(Context context) {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA},
                    TAKE_PHOTO_REQUEST_CODE);
        }

    }
}

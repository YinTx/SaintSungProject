package com.example.qr_codescan;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.configuration.BaseActivity;

public class MainActivity extends BaseActivity {
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private TextView mTextView;
    private TextView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mTextView = (TextView) findViewById(R.id.result);
        mImageView = (TextView) findViewById(R.id.qrcode_bitmap);
        Button mButton = (Button) findViewById(R.id.button1);
        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MipcaActivityCapture.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "1215=" + requestCode);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    //显示扫描到的内容  
                    mTextView.setText(bundle.getString("result"));
                    //显示  
                    mImageView.setText("1、把开锁器插入井盖中间的孔内，扳动开锁器把手至水平位置\n" +
                            "2、扳动把手顺时针转动开锁器4圈，继续顺时针转动开锁器，当开锁器上的数字8对准中间孔边缘处刻度停止。\n" +
                            "3、然后逆时针转动开锁器一圈后，继续逆时针转动开锁器，当开锁器上的数字5对准中间孔边缘处刻度时停止。\n" +
                            "4、再顺时针转动开锁器，当开锁器上的数字9对准中间孔边缘处刻度时停止。\n" +
                            "5、最后逆时针转动开锁器至无法转动时为止。井盖开锁完成\n" +
                            "6、关锁步骤：待井盖放置到井圈内后，插入开锁器，扳动开锁器把手至水平位置，然后顺时针转动开锁器2圈以上即可锁住井盖。");
//                    mImageView.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
                }
                break;
        }
    }

}

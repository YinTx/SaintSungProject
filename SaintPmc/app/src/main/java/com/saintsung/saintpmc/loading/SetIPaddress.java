package com.saintsung.saintpmc.loading;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.saintsung.saintpmc.R;
import com.saintsung.saintpmc.lock.FileStream;
import com.saintsung.saintpmc.tool.DataProcess;
import com.saintsung.saintpmc.tool.ToastUtil;

import static com.saintsung.saintpmc.lock.SocketActivity.string;

/**
 * Created by Administrator on 2016/4/8.
 */
public class SetIPaddress extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private EditText ipAddress;
    private EditText portNumber;
    private Button portBtn;
    private ToastUtil toast=new ToastUtil();
    private String addressIP,port;
    private SharedPreferences myPortSharedPreferences;
    FileStream fileStream=new FileStream();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_ip_address);
        myPortSharedPreferences=this.getSharedPreferences("port", Context.MODE_PRIVATE);
        ipAddress= (EditText) findViewById(R.id.ip_address);
        portNumber= (EditText) findViewById(R.id.port_number);
        portBtn= (Button) findViewById(R.id.port_btn);
        portBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressIP =ipAddress.getText().toString();
                port=portNumber.getText().toString();
                if (addressIP.equals("")){
                    toast.getToast(SetIPaddress.this,"请输入IP地址!");
                }else if (port.equals("")){
                    toast.getToast(SetIPaddress.this,"请输入端口号!");
                }else{
                    SharedPreferences.Editor editor = myPortSharedPreferences.edit();
                    editor.putString("addressIP",addressIP);
                    editor.putString("port",port);
                    editor.commit();
                    string=addressIP+";"+port;
                    //write socketAddress
                    fileStream.fileStream(FileStream.socket, FileStream.write, string.getBytes());
                    SetIPaddress.this.finish();
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}

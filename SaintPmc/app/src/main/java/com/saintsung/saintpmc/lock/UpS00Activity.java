package com.saintsung.saintpmc.lock;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.saintsung.saintpmc.MainActivity;
import com.saintsung.saintpmc.R;


public class UpS00Activity extends Activity{
    public static final String prefixUpS00="prefixUpS00";
    public static final String dataUpS00="dataUpS00";
    public static final byte VS_HW_GOT = 2;
    //    public static final byte VS_LOFFSET_GOT = 3;
//    public static final byte VS_ALL_GOT = 4;
    public static final byte VS_LOFFSET_GOT = (byte) 0xd5;
    public static final byte VS_ALL_GOT = (byte) 0xd6;
    public static final byte OPCODE_SET_PARAM = (byte)'D';//set param
    public static final byte OPCODE_GET_PARAM = (byte)'G';//get param
    private byte[] byteArray;
    private PackageInfo packageInfo;
    private TextView textView,dateValue,timeValue;
    private Spinner spinner;
    private EditText offsetEditText,setEditText;
    private Button startIAPButton,timeButton,saveButton,cancelButton;
    public final static String S00SoftwareVersion="S00SoftwareVersion";
    public final static String S00HardwareVersion="S00HardwareVersion";
    public final static String direction="direction";
    public static String directionValue;
    public final static String positive="+";
    public final static String negative="-";
    private static final String[] stringArray={positive,negative};
    private ArrayAdapter<String> arrayAdapter;
    public final static String offset="offset";
    public final static String set="set";
    public final static String error="error";
    private String string,offsetValue,setValue,directionChange;
    private int i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //display head
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.up_s00);
        //get startIAPButton
        startIAPButton=(Button) this.findViewById(R.id.startIAPButton);
        //startIAPButton setOnClickListener
        startIAPButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                CommandPacker packet = new CommandPacker();
                string="iap_down\r\n";
                byteArray=string.getBytes();
                if(packet.setPacketParam((byte)0, byteArray, prefixUpS00)){
                    byteArray = packet.encodePacket(prefixUpS00);
                    MainActivity.bluetoothLeService.writeLlsAlertLevel(null,byteArray);
                }
                //shut UpS00Activity
                finish();
            }
        });
    }





}

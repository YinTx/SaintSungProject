package com.saintsung.saintpmc.lock;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.saintsung.saintpmc.R;


public class LockSetActivity extends Activity {
	public static final String unlockLogin= "unlockLogin";
	public static final String unlockUser = "unlockUser";
	public static final String readLockNumber="readLockNumber";
	public static final String unlockOriginal="unlockOriginal";
	public static final String unlockTwo="unlockTwo";
	public static final String unlockThree="unlockThree";
	public static final String unlockFour="unlockFour";
	public static final String unlockContinue="unlockContinue";
	public static final String unlockAutoReconnectTwo="unlockAutoReconnectTwo";
	public static final String unlockAutoReconnect="unlockAutoReconnect";
	public static final String unlock_screw = "unlock_screw";
	public static final String unlock_valve = "unlock_valve";
	public static final String unlock_well = "unlock_well";
	public static final String unlockSheet="unlockSheet";		//工单开锁
	public static String unlockType;
	private RadioButton radioButton;
	public String string,state;
	private byte[] byteArray;
	FileStream fileStream=new FileStream();

	@Override
	protected void onResume()
	{
		super.onResume();

		Log.d("onResume","onResume "+unlockType);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//去除标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lock_set);

		//从文件中读取数据
		if (fun_ReadUnlockType().length() > 0) {
			unlockType = fun_ReadUnlockType();
		}


		//初始化选中项
		if (unlockType.equals(unlockLogin)) {							//在线
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockLogin);
			radioButton.setChecked(true);
		}else if (unlockType.equals(unlockUser)) {						//离线
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockUser);
			radioButton.setChecked(true);
		}else if(unlockType.equals(unlockSheet))
		{
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockSheet);
			radioButton.setChecked(true);
		}
		else if (unlockType.equals(unlock_screw)) {					//开螺丝
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlock_screw);
			radioButton.setChecked(true);
		}else if (unlockType.equals(readLockNumber)) {					///读锁号
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.readLockNumber);
			radioButton.setChecked(true);
		} else if (unlockType.equals(unlockOriginal)) {					//原值
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockOriginal);
			radioButton.setChecked(true);
		} else if (unlockType.equals(unlockTwo)) {						//正负2
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockTwo);
			radioButton.setChecked(true);
		} else if (unlockType.equals(unlockThree)) {					//正负3
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockThree);
			radioButton.setChecked(true);
		} else if (unlockType.equals(unlockFour)) {						//正负4
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockFour);
			radioButton.setChecked(true);
		} else if (unlockType.equals(unlockContinue)) {					//连续
			//根据ID获取RadioButton的实例
			radioButton=(RadioButton) LockSetActivity.this.findViewById(R.id.unlockContinue);
			radioButton.setChecked(true);
		}
		//获得实例对象
//        SharedPreferences sharedPreferences = this.getSharedPreferences("userInfo", Context.MODE_WORLD_READABLE);
		//根据ID找到该文本控件
		final TextView textView =(TextView) this.findViewById(R.id.TextView0);
		//根据ID找到RadioGroup实例
		RadioGroup radioGroup=(RadioGroup) this.findViewById(R.id.radioGroup);
		//绑定一个匿名监听器
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int checkedId) {
				// TODO Auto-generated method stub
				//设置开锁方式
				if (checkedId==R.id.unlockLogin) {
					unlockType=unlockLogin;
				}else if (checkedId==R.id.unlockUser) {
					unlockType=unlockUser;
				}else if (checkedId==R.id.unlockSheet) {
					unlockType=unlockSheet;
				}else if (checkedId==R.id.unlock_screw) {
					unlockType=unlock_screw;
				}else if (checkedId==R.id.readLockNumber) {
					unlockType=readLockNumber;
				} else if (checkedId==R.id.unlockOriginal) {
					unlockType=unlockOriginal;
				} else if (checkedId==R.id.unlockTwo) {
					unlockType=unlockTwo;
				} else if (checkedId==R.id.unlockThree) {
					unlockType=unlockThree;
				} else if (checkedId==R.id.unlockFour) {
					unlockType=unlockFour;
				} else if (checkedId==R.id.unlockContinue) {
					unlockType=unlockContinue;
				}
				radioButton=(RadioButton) LockSetActivity.this.findViewById(checkedId);
				textView.setText("选中开锁方式:"+radioButton.getText());
				//开锁方式记录本机lockType.txt文件里
				fileStream.fileStream(FileStream.unlockType,FileStream.write,unlockType.getBytes());
			}
		});
	}

	//从文件中读取数据
	private String fun_ReadUnlockType()
	{
		byte[] by_UnlockType = fileStream.fileStream(FileStream.unlockType, FileStream.read, null);
		Log.d("unlocktype", "unlocktype"+by_UnlockType);
		if (by_UnlockType != null) {
			return new String(by_UnlockType);
		}
		return null;

	}


	//[[egood
	public static String getActionText(Context c){

		if (unlockType.equals(LockSetActivity.unlockLogin)) {			//在线
			return c.getString(R.string.unlockLogin);
		}else if (unlockType.equals(LockSetActivity.unlockUser)) {		//离线
			return c.getString(R.string.unlockUser);
		}else if(unlockType.equals(LockSetActivity.unlockSheet)){
			return c.getString(R.string.unlockSheet);					//工单
		}else if (unlockType.equals(LockSetActivity.unlock_screw)) {	//开螺丝
			return c.getString(R.string.unlock_screw);
		}else if (unlockType.equals(unlock_valve)) {
			return c.getString(R.string.unlock_valve);
		}else if (unlockType.equals(readLockNumber)) {
			return c.getString(R.string.readLockNumber);
		}else if (unlockType.equals(unlockOriginal)) {
			return c.getString(R.string.unlockOriginal);
		}else if (unlockType.equals(LockSetActivity.unlockTwo)) {
			return c.getString(R.string.unlockTwo);
		}else if (unlockType.equals(LockSetActivity.unlockThree)) {
			return c.getString(R.string.unlockThree);
		}else if (unlockType.equals(LockSetActivity.unlockFour)) {
			return c.getString((R.string.unlockFour));
		}else if (unlockType.equals(LockSetActivity.unlockContinue)) {
			return c.getString((R.string.unlockContinue));
		}else if (unlockType.equals(LockSetActivity.unlockAutoReconnectTwo)) {
			return c.getString((R.string.unlockAutoReconnectTwo));
		}else if (unlockType.equals(LockSetActivity.unlockAutoReconnect)) {
			return c.getString((R.string.unlockAutoReconnect));
		}else{
			return ("未定义开锁方式!");
		}
	}
	//]]


}

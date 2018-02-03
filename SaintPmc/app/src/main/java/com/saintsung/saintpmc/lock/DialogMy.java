package com.saintsung.saintpmc.lock;

import android.app.Activity;
import android.content.DialogInterface;

import java.lang.reflect.Field;

public class DialogMy extends Activity {
	public final static String close="close";
	public final static String close_activity="close_activity";
	public final static String unclose="unclose";


	//dialogCloseReflect
	public static void dialogCloseReflect(DialogInterface dialogInterface,String state){
		try {
			//try NoSuchFieldException
			Field field=dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
			//将mShowing变量设为false,表示对话框已关闭;
			field.setAccessible(true);
			if (state.equals(close)) {
				//try IllegalAccessException
				field.set(dialogInterface, true);
			} else {
				//try IllegalAccessException
				field.set(dialogInterface, false);
			}
			dialogInterface.dismiss();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//dialogCloseReflect0
	public static void dialogCloseReflect0(DialogInterface dialogInterface,String state){
		try {
			//try NoSuchFieldException
			Field field=dialogInterface.getClass().getSuperclass().getDeclaredField("mShowing");
			//将mShowing变量设为false,表示对话框已关闭;
			field.setAccessible(true);
			switch (state) {
				case close:
					field.set(dialogInterface, true);
					break;
				case close_activity:
					field.set(dialogInterface, true);
					break;
				case unclose:
					field.set(dialogInterface, false);
					break;

				default:
					break;
			}
			dialogInterface.dismiss();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void dialog(){
	}
}

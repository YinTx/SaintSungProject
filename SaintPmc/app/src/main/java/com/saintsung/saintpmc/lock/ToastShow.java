package com.saintsung.saintpmc.lock;

import android.content.Context;
import android.widget.Toast;

public class ToastShow {
	  private Context context;  
	    private Toast toast = null;  
	    public ToastShow(Context context) {  
	         this.context = context;  
	    }  
	    //��ʾtoast
	    public void toastShow(String text) {  
	        if(toast == null)  
	        {  
	            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);  
	        }  
	        else {  
	            toast.setText(text);  
	        }  
	        toast.show();  
	    }
	    //�ر�toast
	    public void toastShut(){
	    	if (toast != null) {
	    		toast.cancel();
	    	}
	    	
	    }

}

package com.saintsung.saintpmc.lock;

import java.util.concurrent.Semaphore;

import android.os.Handler;
import android.os.Looper;

public class WThread extends Thread {

	WRunnable mRunnbale = null;
	WThread(WRunnable r){
		mRunnbale = r;
	}
	Semaphore semp = new Semaphore(1);
	Handler mHandler = null;
	public void start(){
		try {
			semp.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			semp.acquire();
			semp.release();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.start();
	}
	public void ready(){
		semp.release();
	}
	@Override
	public void run() {
		if(this.mRunnbale != null){
			this.mRunnbale.run(this);
		}else{
			Looper.prepare();
			synchronized(this){
				this.mHandler = new Handler();
			}
			this.ready();

			Looper.loop();
			synchronized(this){
				this.mHandler = null;
			}
		}
		super.run();
	}
	public static WThread Start(WRunnable wt){
		WThread t = new WThread(wt);
		t.start();
		return t;
	}
	public interface WRunnable{
		void run(WThread wt);
	}
	public void Post(Runnable r,long ms){
		synchronized(this){
			if(this.mHandler != null){
				this.mHandler.postDelayed(r,ms);
			}
		}
	}
	public void Cancel(Runnable r){
		synchronized(this){
			if(this.mHandler != null){
				this.mHandler.removeCallbacks(r);
			}
		}
	}
}

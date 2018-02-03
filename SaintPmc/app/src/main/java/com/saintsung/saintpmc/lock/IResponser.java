package com.saintsung.saintpmc.lock;

public interface IResponser {
	public void notify(byte cmdCode, byte errCode, byte[] bData);
}

package com.saintsung.saintpmc.lock;

public interface IConnection {
	public void	send(byte[] bData);
	public void	send(byte[] bData, int off, int len);
	public int	read(byte[] bData);
	public int	read(byte[] bData, int off, int len);

	public void open();
	public void start();
	public void stop();
	public void pause();
	public void resume();
	public void close();
}

package com.wudayu.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ListenerService extends AbstractService {

	private static String TAG = "ListenerService";


	// Local Protocol - use in application itself
	public final static int MSG_SHOW_DIALOG = 0x0001;

	// Remote Protocol - use between applications
	public final static int REMOTE_MSG_SHOW_DIALOG = 0x1001;


	private Timer timer = new Timer();
	private String[] targetIpArray;
	private String currentIp;

	@Override
	public void onStartService() {
		Toast.makeText(this, "onStartService", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "ListenerService onStartService() called!");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		targetIpArray = intent.getStringArrayExtra("targetIp");
		currentIp = intent.getStringExtra("currentIp");

		Log.i(TAG, "CurrentIp : " + currentIp);

		timer.schedule(new TimerTask() {
			public void run() {
				try {
					new Server();
				} catch (Throwable t) {
				}
			}
		}, 1000);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onStopService() {
		Toast.makeText(this, "onStopService", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "ListenerService onStopService() called!");
		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public void onReceiveMessage(Message msg) {
		Log.i(TAG, "ListenerService onReceiveMessage() called!");
		switch (msg.what) {
			case ListenerService.MSG_SHOW_DIALOG:
				new Timer().schedule(new TimerTask() {
					public void run() {
						try {
							for (int i = 0; i < targetIpArray.length; ++i) {
								Log.i(TAG, "targetIpArray[i] = " + targetIpArray[i]);							
								new Client(targetIpArray[i], ListenerService.REMOTE_MSG_SHOW_DIALOG);
							}
						} catch (Throwable t) {
						}
					}
				}, 0);
				break;

			default:
				Log.i(TAG, "onReceiveMessage()::::Nothing matched.onReceiveMessage");
				break;
		}
	}

	/**
	 * class Server is used for listening messages from cloud
	 * 
	 * @author david
	 *
	 */
	class Server {
		private ServerSocket ss;
		private Socket socket;
		private BufferedReader in;
		//private PrintWriter out;

		public Server() {
			try {
				ss = new ServerSocket(6930);
				while (true) {
					socket = ss.accept();
					// the 'in' is used for receiving messages from client
					in = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					// the 'out' is used for sending messages to client
					//out = new PrintWriter(socket.getOutputStream(), true);
					String protocolCodeStr = in.readLine();
					Log.i(TAG, "The line is : " + protocolCodeStr);

					Message mes = new Message();
					mes.what = Integer.valueOf(protocolCodeStr);
					send(mes);

					//out.close();
					in.close();
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (ss != null)
					try {
						ss.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	/**
	 * class Client is used for sending messages to cloud
	 * 
	 * @author david
	 *
	 */
	class Client {
		Socket socket;
		//BufferedReader in;
		PrintWriter out;

		public Client(String targetIp, int protocolCode) {
			try {
				socket = new Socket(targetIp, 6930);
				// the 'in' is used for receiving messages from server
				//in = new BufferedReader(new InputStreamReader(
				//		socket.getInputStream()));
				// the 'out' is used for sending message to server
				out = new PrintWriter(socket.getOutputStream(), true);
				// write something useful like protocol
				out.println(protocolCode);
				out.close();
				//in.close();
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}

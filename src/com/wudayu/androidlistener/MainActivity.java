package com.wudayu.androidlistener;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wudayu.services.ListenerService;
import com.wudayu.services.ServiceManager;

public class MainActivity extends Activity {

	EditText edtTargetIp;
	Button btnStartListenerService;
	Button btnCloseListenerService;
	Button btnSendInfo;
	TextView txtCurrentIp;
	SharedPreferences preferences;
	ImageView ivDialogPic;

	private ServiceManager serviceManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		edtTargetIp = (EditText) findViewById(R.id.edt_target_ip);
		btnStartListenerService = (Button) findViewById(R.id.btn_start_listener_service);
		btnCloseListenerService = (Button) findViewById(R.id.btn_close_listener_service);
		btnSendInfo = (Button) findViewById(R.id.btn_send_info);
		txtCurrentIp = (TextView) findViewById(R.id.txt_current_ip);
		ivDialogPic = (ImageView) findViewById(R.id.iv_dialog_pic);

		btnStartListenerService
				.setOnClickListener(new btnStartListenerServiceOnClickListener());
		btnCloseListenerService
				.setOnClickListener(new btnCloseListenerServiceOnClickListener());
		btnSendInfo.setOnClickListener(new btnSendInfoOnClickListener());
		ivDialogPic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ivDialogPic.setVisibility(View.INVISIBLE);
			}
		});

		setEdtTargetIpText();
		getCurrentIpAndSetTxtCurrentIpText();
		serviceManagerInit();
	}

	@SuppressLint("HandlerLeak")
	private void serviceManagerInit() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("targetIp", new String[]{ edtTargetIp.getText().toString(), txtCurrentIp.getText().toString()});
		data.put("currentIp", txtCurrentIp.getText().toString());

		this.serviceManager = new ServiceManager(this, ListenerService.class,
				new Handler() {
					@Override
					public void handleMessage(Message msg) {
						switch (msg.what) {
						case ListenerService.REMOTE_MSG_SHOW_DIALOG:
							ivDialogPic.setVisibility(View.VISIBLE);
							/*
							AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setNegativeButton("Cancel", null);

							dialog.show();
							*/
							break;
						default:
							super.handleMessage(msg);
						}
					}
				}, data);
	}

	@SuppressWarnings("deprecation")
	private void getCurrentIpAndSetTxtCurrentIpText() {
		WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();
		String ipAddress = Formatter.formatIpAddress(ip);

		txtCurrentIp.setText(ipAddress);
	}

	private void setEdtTargetIpText() {
		preferences = getPreferences(MODE_PRIVATE);
		edtTargetIp.setText(preferences.getString("targetIp", ""));
	}
	
	private void setEdtTargetIpTextIntoSharedPreferences() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("targetIp", edtTargetIp.getText().toString());
		editor.commit();
	}

	private class btnStartListenerServiceOnClickListener implements
			View.OnClickListener {
		@Override
		public void onClick(View v) {
			setEdtTargetIpTextIntoSharedPreferences();

			serviceManager.start();
		}

	}

	private class btnCloseListenerServiceOnClickListener implements
			View.OnClickListener {
		@Override
		public void onClick(View v) {
			serviceManager.stop();
		}
	}

	private class btnSendInfoOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			try {
				serviceManager.send(Message.obtain(null, ListenerService.MSG_SHOW_DIALOG, 12345, 0));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		try {
			serviceManager.unbind();
		} catch (Throwable t) {
		}
	}

}

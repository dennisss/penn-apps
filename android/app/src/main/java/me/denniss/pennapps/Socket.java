package me.denniss.pennapps;

import java.net.MalformedURLException;

import org.json.*;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import io.socket.*;

public class Socket {

	public SocketIO socket;
	private static String TAG = "penn-apps";
    private static MainActivity parent;

	public Socket(MainActivity context){
        this.parent = context;
		try {
			socket = new SocketIO("http://192.168.1.4:3000/"); //"http://192.168.16.29:3000/");

			socket.connect(new IOCallback() {
				@Override
				public void onMessage(final JSONObject json, IOAcknowledge ack) {
					try {
						Log.i(TAG, "Server said:" + json.toString(2));

                        parent.runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    Toast.makeText(parent.getBaseContext(),"Server said: " + json.toString(2),
                                            Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });




					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onMessage(final String data, IOAcknowledge ack) {
					System.out.println("Server said: " + data);
                    parent.runOnUiThread(new Runnable() {
                        public void run() {
                                Toast.makeText(parent.getBaseContext(),"Server said: " + data,
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
				}

				@Override
				public void onError(final  SocketIOException socketIOException) {
					Log.e(TAG, "an Error occured");

					socketIOException.printStackTrace();
                    parent.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(parent.getBaseContext(), "ERROR",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
				}

				@Override
				public void onDisconnect() {
					Log.i(TAG, "Connection terminated.");
                    parent.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(parent.getBaseContext(), "Disconnected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
				}

				@Override
				public void onConnect() {
					Log.i(TAG, "Connection established");
                    parent.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(parent.getBaseContext(), "Connected",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
				}

				@Override
				public void on(String event, IOAcknowledge ack, Object... args) {
					System.out.println("Server triggered event '" + event + "'");
				}
			});

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

    public void start()
    {
        socket.emit("start");
    }

    public void stop()
    {
        socket.emit("stop");
    }

    public void update(float progress){

        JSONObject obj = new JSONObject();

        try {
            obj.put("progress", progress);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("progress", obj);

    }

    public void deflect(float angle){

        Log.i(TAG, "Deflect");

        JSONObject obj = new JSONObject();

        try {
            obj.put("angle", angle);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("deflect", obj);

    }


}

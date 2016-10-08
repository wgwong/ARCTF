package arctf.arctfnetworkclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void connectToServer(View view) {
        Log.d("debug", "connect to server called");

        Main m = (Main) getSupportFragmentManager().findFragmentById(R.id.main);

        if (m != null) {
            m.establishSession();
        } else {
            Log.d("debug", "m is null!");
        }

        Log.d("debug", "succeed?");
    }
}




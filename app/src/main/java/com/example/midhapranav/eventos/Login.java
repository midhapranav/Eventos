package com.example.midhapranav.eventos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setOnClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeHttpLoginRequest(final String username, final String password)throws Exception {
        new Thread (new Runnable(){
            @Override
            public void run(){
                try {
                    String url = "http://eventosdataapi-env.elasticbeanstalk.com/?email="+username+"&password="+password+"&selector=2";
                    URL urlObj = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setConnectTimeout(15000);
                    Log.d("Login debug->","Connecting");
                    conn.connect();
                    Log.d("Login debug->", Integer.toString(conn.getResponseCode()));
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    Log.d("Login debug->",json.toString());
                    if(json.getString("success").equals("True")) {
                        Log.d("Login debug -> ", "Starting MapView");
                        Intent intent = new Intent(Login.this, MapsActivity.class);
                        intent.putExtra("userid",json.getString("userid"));
                        Log.d("USERID Debug->","I am in login and my id is "+json.getString("userid"));
                        startActivity(intent);
                    } else {
                        Login.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), ("Invalid Credentials!"),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            }).start();
    }
    public void setOnClickListeners() {
        Button mLogin = (Button) findViewById(R.id.login_button);
        Button mCreateAccount = (Button) findViewById(R.id.create_account_button);

        final EditText mUsername = (EditText) findViewById(R.id.username_text_field);
        final EditText mPassword = (EditText) findViewById(R.id.password_text_field);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    makeHttpLoginRequest(mUsername.getEditableText().toString(), mPassword.getEditableText().toString());
                } catch (Exception e){e.printStackTrace();}
            }
        });

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Login debug ->","Starting create activity");
                Intent intent = new Intent(Login.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });
    }
}

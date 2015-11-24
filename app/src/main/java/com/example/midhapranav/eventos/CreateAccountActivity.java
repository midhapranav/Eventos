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

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Button mCreateAccountButton = (Button) findViewById(R.id.create_account_button);
        mCreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateInputs()) {
                    Intent intent = new Intent(CreateAccountActivity.this, Login.class);
                    Toast.makeText(getApplicationContext(),"Account Created", Toast.LENGTH_SHORT).show();
                    Log.d("Create accound debug ->","Starting login");
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_account, menu);
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

    public boolean validateInputs() {
        EditText mCreateAccountName = (EditText)findViewById(R.id.create_account_name_text_field);
        EditText mCreateAccountEmail = (EditText) findViewById(R.id.create_account_email_text_field);
        EditText mCreateAccountPassword = (EditText) findViewById(R.id.create_account_password_text_field);

        if(mCreateAccountName.getText().toString().equals("") || mCreateAccountEmail.getText().toString().equals("") || mCreateAccountPassword.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(),"Please Enter Valid Details", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}

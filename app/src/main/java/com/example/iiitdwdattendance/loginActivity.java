package com.example.iiitdwdattendance;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class loginActivity extends AppCompatActivity {

    TextToSpeech t1;
    EditText sid, spass;
    Button slogin;
    ProgressBar slProgress;
    TextView errText;
    String facId="", facPass="";
    public static String errorMsg="";
    public static String loginUrl = "https://files.000webhost.com/facultySignin.php";
    RequestQueue requestQueue;
    SharedPreferences sharedPreferences;
    public static final String facultyDetails = "facultyPreferences";
    public static final String facultyLoginId = "facultyId";
    public static final String facultyLoginPassword = "facultyPassword";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setContentView(R.layout.activity_login);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        sid = (EditText) findViewById(R.id.signin_fid);
        spass = (EditText) findViewById(R.id.signin_fpassword);
        slogin = (Button) findViewById(R.id.btn_flogin);
        slProgress = (ProgressBar) findViewById(R.id.flProgressbar);
        errText = (TextView) findViewById(R.id.login_errorText);

        sharedPreferences = getSharedPreferences( facultyDetails, Context.MODE_PRIVATE);

        String savedFacultyId = sharedPreferences.getString( facultyLoginId, null);
        String savedFacultyPassword = sharedPreferences.getString( facultyLoginPassword, null);

        if(savedFacultyId!=null && savedFacultyPassword!=null){

            facId = savedFacultyId;
            facPass = savedFacultyPassword;
            sid.setText(facId);
            spass.setText(facPass);
            loginFaculty(facId, facPass);
        }

        if(!errorMsg.equals("")){

            errText.setText(errorMsg);
            errText.setVisibility(View.VISIBLE);
        }

        slogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = sid.getText().toString().trim();
                String pass = spass.getText().toString().trim();

                if(!id.isEmpty() || !pass.isEmpty()){

                    if( Pattern.matches("[0-9][0-9][A-Z][A-Z][A-Z][A-Z][0-9][0-9][0-9]", id)){

                        if( pass.length()>=8){

                            loginFaculty( id, pass);
                        }
                        else{

                            errorMsg = "Password size should be > = 8 characters ";
                            finish();
                            startActivity(getIntent());
                        }
                    }
                    else{

                        errorMsg = "Invalid id !";
                        finish();
                        startActivity(getIntent());
                    }

                }
                else {

                    errorMsg = "All fields are mandatory";
                    finish();
                    startActivity(getIntent());
                }
            }
        });
    }

    public void loginFaculty(final String id, final String pass){

        slProgress.setVisibility(View.VISIBLE);
        slogin.setVisibility(View.GONE);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        final StringRequest request = new StringRequest(Request.Method.POST, loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                System.out.println(response);
                try {

                    JSONObject jsonObject = new JSONObject(response);
                    String success = jsonObject.getString("success");
                    System.out.println("1");
                    errorMsg = jsonObject.getString("error");
                    JSONArray jsonArray = jsonObject.getJSONArray("login");

                    if(success.equals("1")){

                        for(int i=0; i<jsonArray.length(); i++) {

                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String name = jsonObject1.getString("name").trim();

                            Toast.makeText(loginActivity.this, "Success Login.\nWelcome " + name, Toast.LENGTH_LONG).show();
                            t1.speak("Welcome "+name, TextToSpeech.QUEUE_FLUSH, null);
                            facId=sid.getText().toString().trim();
                            welcomeFaculty();

                        }
                    }
                    else {

                        Toast.makeText(loginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(getIntent());
                    }
                }catch ( JSONException e){

                    e.printStackTrace();
                    Toast.makeText( loginActivity.this, "Login Error! "+e.toString(), Toast.LENGTH_SHORT).show();
                    slProgress.setVisibility(View.GONE);
                    slogin.setVisibility(View.VISIBLE);
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String message="Login Error! ";
                        slProgress.setVisibility(View.GONE);
                        slogin.setVisibility(View.VISIBLE);
                        if (error instanceof TimeoutError) {
                            message = message + "Timeout Error!";

                        }else if (error instanceof NoConnectionError) {
                            message = message + "NoConnection Error!";

                        }else if (error instanceof AuthFailureError) {
                            message = message + "Authentication Error!";
                        } else if (error instanceof ServerError) {
                            message = message + "Server Side Error!";
                        } else if (error instanceof NetworkError) {
                            message = message + "Network Error!";
                        } else if (error instanceof ParseError) {
                            message = message + "Parse error";
                        }
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        requestQueue.stop();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("fac_id", id);
                parameters.put("fac_password", pass);
                return parameters;
            }
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError{

                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        requestQueue.add(request);
    }

    public void welcomeFaculty(){

        facPass = spass.getText().toString().trim();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString( facultyLoginId, facId);
        editor.putString( facultyLoginPassword, facPass);
        editor.commit();

        errorMsg="";
        Intent intent = new Intent(this, facultyWelcomePage.class);
        Intent intentThis = getIntent();
        finish();
        startActivity(intentThis);
        intent.putExtra("faculty_id", facId);
        startActivity(intent);
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this, FullscreenActivity.class));
        finish();
    }

    public void returnBack(View view) {

        Intent intent = new Intent(this, FullscreenActivity.class);
        finish();
        startActivity(intent);
    }

    public void registerFac(View view) {

        Intent intent = new Intent(this, registration.class);
        finish();
        startActivity(intent);
    }

}

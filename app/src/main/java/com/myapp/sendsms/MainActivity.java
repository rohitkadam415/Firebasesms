package com.myapp.sendsms;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

  private final String TAG = MainActivity.this.getClass().getSimpleName();
  private EditText editTextMobile;
  private FirebaseAuth mAuth;
  private String mVerificationId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mAuth = FirebaseAuth.getInstance();
    editTextMobile = findViewById(R.id.editTextMobile);

    findViewById(R.id.buttonContinue).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String mobile = editTextMobile.getText().toString().trim();

        if (mobile.isEmpty() || mobile.length() < 10) {
          editTextMobile.setError("Enter a valid mobile");
          editTextMobile.requestFocus();
          return;
        }
        sendVerificationCode(mobile);
      }
    });

    findViewById(R.id.buttonVerify).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        verifyCode(editTextMobile.getText().toString());
      }
    });
  }

  private void sendVerificationCode(String mobile) {
    PhoneAuthProvider.getInstance().verifyPhoneNumber(
        "+91" + mobile,
        60,
        TimeUnit.SECONDS,
        TaskExecutors.MAIN_THREAD,
        mCallbacks);
  }


  //the callback to detect the verification status
  private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
      new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

          //Getting the code sent by SMS
          String code = phoneAuthCredential.getSmsCode();

          //sometime the code is not detected automatically
          //in this case the code will be null
          //so user has to manually enter the code
          if (code != null) {
            Log.i(TAG, "onVerificationCompleted: " + code);
          }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
          Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s,
                               PhoneAuthProvider.ForceResendingToken forceResendingToken) {
          super.onCodeSent(s, forceResendingToken);

          //storing the verification id that is sent to the user
          mVerificationId = s;
          Log.i(TAG, "onCodeSent: " + mVerificationId);
        }
      };


  private void verifyCode(String code) {
    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
    signInWithCredentials(credential);
  }

  private void signInWithCredentials(PhoneAuthCredential credential) {
    mAuth.signInWithCredential(credential).addOnCompleteListener(
        new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
              Log.i(TAG, "onComplete: Success");
            } else {
              Log.i(TAG, "onComplete: "+task.getException().getMessage());
            }
          }
        });
  }


}

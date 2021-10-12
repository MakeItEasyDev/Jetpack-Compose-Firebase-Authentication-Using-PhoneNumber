package com.jetpack.customotp

import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.jetpack.customotp.ui.theme.CustomOTPTheme
import com.jetpack.customotp.ui.theme.Purple500
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val mAuth = FirebaseAuth.getInstance()
    var verificationOtp = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CustomOTPTheme {
                Surface(color = MaterialTheme.colors.background) {
                    OTPScreen { mobileNum, otp ->
                        if (mobileNum.isNotEmpty()) {
                            send(mobileNum)
                        }
                        if (otp.isNotEmpty()) {
                            otpVerification(otp)
                        }
                    }
                }
            }
        }
    }

    val turnOffPhoneVerify = FirebaseAuth.getInstance().firebaseAuthSettings
        .setAppVerificationDisabledForTesting(false)

    private fun send(mobileNum: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$mobileNum")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object :
            PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Toast.makeText(applicationContext, "Verification Completed", Toast.LENGTH_SHORT).show()
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(applicationContext, "Verification Failed", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(otp: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(otp, p1)
                    verificationOtp = otp
                    Toast.makeText(applicationContext, "Otp Send Successfully", Toast.LENGTH_SHORT).show()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun otpVerification(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationOtp, otp)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Verification Successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Wrong Otp", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun OTPScreen(
    onClick: (mobileNum: String, otp: String) -> Unit
) {
    val context = LocalContext.current
    var otpVal: String? = null
    val phoneNumber = remember { mutableStateOf("") }
    val customView = remember { LottieAnimationView(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Purple500),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Firebase Authentication",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Add Lottie file
            AndroidView({
                customView
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
            ) { view ->
                with(view) {
                    setAnimation(R.raw.phone_number_verify)
                    playAnimation()
                    repeatCount = LottieDrawable.INFINITE
                    foregroundGravity = Gravity.CENTER
                }
            }

            /*Image(
                painter = painterResource(id = R.drawable.otp),
                contentDescription = "Otp Image",
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )*/

            Spacer(modifier = Modifier.height(50.dp))

            OutlinedTextField(
                value = phoneNumber.value,
                onValueChange = { phoneNumber.value = it },
                label = { Text(text = "Phone Number") },
                placeholder = { Text(text = "Phone Number") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = "Phone Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onClick(phoneNumber.value, "") // Here pass the phone number value only
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(45.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Purple500)
            ) {
                Text(text = "Send Otp", fontSize = 15.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Enter the OTP",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            OTPTextFields(
                length = 6
            ) { getOpt ->
                otpVal = getOpt
            }

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (otpVal != null) {
                        onClick("", otpVal!!) // Here pass the Otp value only
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(45.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Purple500)
            ) {
                Text(
                    text = "Otp Verify",
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
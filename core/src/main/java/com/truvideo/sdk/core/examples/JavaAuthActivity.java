package com.truvideo.sdk.core.examples;

import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.truvideo.sdk.core.TruvideoSdk;
import com.truvideo.sdk.core.interfaces.TruvideoSdkCallback;

import kotlin.Unit;
import truvideo.sdk.common.exceptions.TruvideoSdkException;

class JavaAuthActivity extends ComponentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authenticate();
    }

    public void authenticate() {
        boolean isAuthenticated = TruvideoSdk.getInstance().isAuthenticated();
        boolean isAuthenticationExpired = TruvideoSdk.getInstance().isAuthenticationExpired();

        if (!isAuthenticated || isAuthenticationExpired) {
            String apiKey = "your-api-key";
            String payload = TruvideoSdk.getInstance().generatePayload();
            String signature = "your-signature";
            TruvideoSdk.getInstance().authenticate(apiKey, payload, signature, "", new TruvideoSdkCallback<Unit>() {
                @Override
                public void onComplete(Unit unit) {
                    initialize();
                }

                @Override
                public void onError(@NonNull TruvideoSdkException exception) {
                    // Handle auth error
                }
            });
        } else {
            initialize();
        }
    }

    public void initialize() {
        TruvideoSdk.getInstance().initAuthentication(new TruvideoSdkCallback<Unit>() {
            @Override
            public void onComplete(Unit unit) {
                // Authentication ready
            }

            @Override
            public void onError(@NonNull TruvideoSdkException exception) {
                // Handle init error
            }
        });
    }
}

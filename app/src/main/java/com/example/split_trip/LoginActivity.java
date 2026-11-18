
package com.example.split_trip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String DEMO_EMAIL = "developer@splittrip.local";
    private static final String DEMO_PASSWORD = "SplitTripDemo123!";

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    private Button loginButton;
    private TextView signupText;
    private ScrollView loginScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_login);

        loginScrollView = findViewById(R.id.loginScrollView);

        emailInput = findViewById(R.id.loginEmail);
        passwordInput = findViewById(R.id.loginPassword);

        loginButton = findViewById(R.id.loginButton);
        signupText = findViewById(R.id.signupText);

        ViewCompat.setOnApplyWindowInsetsListener(
                loginScrollView, (view, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    Insets keyboard = insets.getInsets(
                            WindowInsetsCompat.Type.ime());

                    int bottomPadding = Math.max(
                            systemBars.bottom,
                            keyboard.bottom
                    );

                    view.setPadding(
                            view.getPaddingLeft(),
                            systemBars.top,
                            view.getPaddingRight(),
                            bottomPadding
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(loginScrollView);

        View.OnFocusChangeListener focusListener = (view, hasFocus) -> {
            if (hasFocus) {
                view.postDelayed(() -> {

                    int[] fieldPosition = new int[2];
                    int[] scrollPosition = new int[2];

                    view.getLocationOnScreen(fieldPosition);
                    loginScrollView.getLocationOnScreen(scrollPosition);

                    int fieldBottom = fieldPosition[1] + view.getHeight();

                    int visibleBottom = scrollPosition[1]
                            + loginScrollView.getHeight()
                            - loginScrollView.getPaddingBottom();

                    int difference = fieldBottom - visibleBottom;

                    if (difference > 0) {
                        loginScrollView.smoothScrollBy(
                                0, difference + 32);
                    }

                }, 350);
            }
        };

        emailInput.setOnFocusChangeListener(focusListener);
        passwordInput.setOnFocusChangeListener(focusListener);

        loginButton.setOnClickListener(v -> validateLogin());

        signupText.setOnClickListener(v ->
                Toast.makeText(
                        this,
                        "Registration will be available when the backend is connected.",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void validateLogin() {

        String email = emailInput.getText() == null
                ? ""
                : emailInput.getText().toString().trim();

        String password = passwordInput.getText() == null
                ? ""
                : passwordInput.getText().toString();

        emailInput.setError(null);
        passwordInput.setError(null);

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (!email.equalsIgnoreCase(DEMO_EMAIL)
                || !password.equals(DEMO_PASSWORD)) {

            passwordInput.setError("Incorrect developer credentials");
            passwordInput.requestFocus();
            return;
        }

        // Save the temporary demo user's details.
        SharedPreferences preferences =
                getSharedPreferences("splittrip_session", MODE_PRIVATE);

        preferences.edit()
                .putString("username", "SplitTrip Developer")
                .putString("email", DEMO_EMAIL)
                .putBoolean("isLoggedIn", true)
                .apply();

        Toast.makeText(
                this,
                "Welcome to SplitTrip!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                LoginActivity.this,
                HomeActivity.class
        );

        startActivity(intent);
        finish();
    }
}

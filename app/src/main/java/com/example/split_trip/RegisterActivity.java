
package com.example.split_trip;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    private Button registerButton;
    private TextView signinText;
    private ScrollView registerScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_register);

        registerScrollView = findViewById(R.id.registerScrollView);

        usernameInput = findViewById(R.id.registerUsername);
        emailInput = findViewById(R.id.registerEmail);
        passwordInput = findViewById(R.id.registerPassword);
        confirmPasswordInput = findViewById(R.id.registerConfirmPassword);

        registerButton = findViewById(R.id.registerButton);
        signinText = findViewById(R.id.signinText);

        ViewCompat.setOnApplyWindowInsetsListener(
                registerScrollView, (view, insets) -> {

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

        ViewCompat.requestApplyInsets(registerScrollView);

        View.OnFocusChangeListener focusListener = (view, hasFocus) -> {
            if (hasFocus) {
                view.postDelayed(() -> {

                    int[] fieldPosition = new int[2];
                    int[] scrollPosition = new int[2];

                    view.getLocationOnScreen(fieldPosition);
                    registerScrollView.getLocationOnScreen(scrollPosition);

                    int fieldBottom = fieldPosition[1] + view.getHeight();

                    int visibleBottom = scrollPosition[1]
                            + registerScrollView.getHeight()
                            - registerScrollView.getPaddingBottom();

                    int difference = fieldBottom - visibleBottom;

                    if (difference > 0) {
                        registerScrollView.smoothScrollBy(
                                0, difference + 32);
                    }

                }, 350);
            }
        };

        usernameInput.setOnFocusChangeListener(focusListener);
        emailInput.setOnFocusChangeListener(focusListener);
        passwordInput.setOnFocusChangeListener(focusListener);
        confirmPasswordInput.setOnFocusChangeListener(focusListener);

        registerButton.setOnClickListener(v -> validateRegistration());

        signinText.setOnClickListener(v -> finish());
    }

    private void validateRegistration() {

        String username = usernameInput.getText() == null
                ? ""
                : usernameInput.getText().toString().trim();

        String email = emailInput.getText() == null
                ? ""
                : emailInput.getText().toString().trim();

        String password = passwordInput.getText() == null
                ? ""
                : passwordInput.getText().toString();

        String confirmPassword = confirmPasswordInput.getText() == null
                ? ""
                : confirmPasswordInput.getText().toString();

        usernameInput.setError(null);
        emailInput.setError(null);
        passwordInput.setError(null);
        confirmPasswordInput.setError(null);

        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

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

        if (password.length() < 8) {
            passwordInput.setError("Password must be at least 8 characters");
            passwordInput.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm your password");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // API registration will be connected here once the backend is ready.
    }
}

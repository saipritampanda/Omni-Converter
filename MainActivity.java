package com.nightxstudio.omniconverter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonObject;
import com.nightxstudio.omniconverter.Retrofit.RetrofitBuilder;
import com.nightxstudio.omniconverter.Retrofit.RetrofitInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    ConstraintLayout rootLayout;

    AutoCompleteTextView typeDropDownText;
    public AutoCompleteTextView fromDropDownText;
    public AutoCompleteTextView toDropDownText;
    EditText valueEditText;
    ImageView clearText;
    Button calculate;
    TextView result;
    TextView resultHint;
    TextView textView;
    TextView exchangeRate;
    double amount = 0;

    ArrayAdapter<String> typeAdapter;
    ArrayAdapter<String> childAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);

        typeDropDownText = findViewById(R.id.typeDropDownText);
        fromDropDownText = findViewById(R.id.fromDropDownText);
        toDropDownText = findViewById(R.id.toDropDownText);
        valueEditText = findViewById(R.id.valueEditText);
        clearText = findViewById(R.id.clearText);
        calculate = findViewById(R.id.calculate);
        result = findViewById(R.id.result);
        resultHint = findViewById(R.id.resultHint);
        textView = findViewById(R.id.textView);
        exchangeRate = findViewById(R.id.exchangeRate);

        String[] typeItems = getResources().getStringArray(R.array.typeItemsStringArray);
        String[] currencyItems = getResources().getStringArray(R.array.currencyItemsStringArray);
        String[] weightItems = getResources().getStringArray(R.array.weightItemsStringArray);
        String[] heightItems = getResources().getStringArray(R.array.heightItemsStringArray);
        String[] temperatureItems = getResources().getStringArray(R.array.temperatureItemsStringArray);

//  Snackbar for same type of From & To :

        Snackbar sameType = Snackbar.make(rootLayout, "", Snackbar.LENGTH_LONG);
        View custom = getLayoutInflater().inflate(R.layout.snackbar_layout, null);
        sameType.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) sameType.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarLayout.addView(custom, 0);

//  Snackbar for OnFailure API Request:

        Snackbar OnFailureText = Snackbar.make(rootLayout, "", Snackbar.LENGTH_LONG);
        View OnFailurecustom = getLayoutInflater().inflate(R.layout.on_failure_snackbar_layout, null);
        OnFailureText.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout OnFailureTextsnackbarLayout = (Snackbar.SnackbarLayout) OnFailureText.getView();
        OnFailureTextsnackbarLayout.setPadding(0, 0, 0, 0);
        OnFailureTextsnackbarLayout.addView(OnFailurecustom, 0);

//  Snackbar for On Empty EditBox:

        Snackbar OnEmptyEditBox = Snackbar.make(rootLayout, "", Snackbar.LENGTH_LONG);
        View OnEmptyEditBoxcustom = getLayoutInflater().inflate(R.layout.on_empty_editbox_snacbar_layout, null);
        OnEmptyEditBox.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout OnEmptyEditBoxsnackbarLayout = (Snackbar.SnackbarLayout) OnEmptyEditBox.getView();
        OnEmptyEditBoxsnackbarLayout.setPadding(0, 0, 0, 0);
        OnEmptyEditBoxsnackbarLayout.addView(OnEmptyEditBoxcustom, 0);

        typeAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.type_dropdown, typeItems);
        typeDropDownText.setAdapter(typeAdapter);

        valueEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        typeDropDownText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    childAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.type_dropdown, currencyItems);
                    textView.setVisibility(View.VISIBLE);
                } else {
                    textView.setVisibility(View.GONE);

                    if (position == 1) {
                        childAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.type_dropdown, weightItems);
                    } else if (position == 2) {
                        childAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.type_dropdown, heightItems);
                    } else if (position == 3) {
                        childAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.type_dropdown, temperatureItems);
                    }
                }

                fromDropDownText.setAdapter(childAdapter);
                toDropDownText.setAdapter(childAdapter);
            }

        });


        calculate.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                resultHint.setVisibility(View.GONE);
                valueEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);

                double input;

                try {
                    input = Double.parseDouble(valueEditText.getText().toString());
                } catch (NumberFormatException ex) {
                    OnEmptyEditBox.show();
                    return;
                }

                /*
                //Instead of try catch, TextUtils can also be used as follows (3 lines):

                if(TextUtils.isEmpty(String.valueOf(input))){
                    OnEmptyEditBox.show();
                }
                
                 */

                //  Currency Conversions:-

                if (typeDropDownText.getText().toString().equals("Currency")) {

                    RetrofitInterface retrofitInterface;
                    retrofitInterface = RetrofitBuilder.getRetrofitInstance().create(RetrofitInterface.class);
                    Call<JsonObject> call = retrofitInterface.getExchangeCurrency(fromDropDownText.getText().toString());
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            JsonObject res = response.body();
                            assert res != null;
                            JsonObject rates = res.getAsJsonObject("conversion_rates");
                            double multiplier = Double.parseDouble(String.valueOf(rates.get(toDropDownText.getText().toString())));

                            amount = input * multiplier;
                            String amountString = String.valueOf(amount);

                            exchangeRate.setVisibility(View.VISIBLE);
                            exchangeRate.setText("Rate:" + "\n1 " + fromDropDownText.getText().toString() + " = " + multiplier + " " + toDropDownText.getText().toString());
                            result.setText(amountString);
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, Throwable t) {
                            OnFailureText.show();
                        }
                    });


                }
                else {

                    exchangeRate.setVisibility(View.GONE);

                    //  Weight Conversions:-

                    //Kilo-Gram:
                    if (fromDropDownText.getText().toString().equals("Kilo-gram") && toDropDownText.getText().toString().equals("Kilo-gram")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("Kilo-gram") && toDropDownText.getText().toString().equals("Pound")) {
                        amount = input * 2.20462;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " lb");
                    } else if (fromDropDownText.getText().toString().equals("Kilo-gram") && toDropDownText.getText().toString().equals("Gram")) {
                        amount = input * 1000;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " g");
                    } else if (fromDropDownText.getText().toString().equals("Kilo-gram") && toDropDownText.getText().toString().equals("Ounce")) {
                        amount = input * 35.274;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " oz");
                    }

                    //Pound:
                    else if (fromDropDownText.getText().toString().equals("Pound") && toDropDownText.getText().toString().equals("Pound")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("Pound") && toDropDownText.getText().toString().equals("Kilo-gram")) {
                        amount = input * 0.454;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " kg");
                    } else if (fromDropDownText.getText().toString().equals("Pound") && toDropDownText.getText().toString().equals("Gram")) {
                        amount = input * 453.592;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " g");
                    } else if (fromDropDownText.getText().toString().equals("Pound") && toDropDownText.getText().toString().equals("Ounce")) {
                        amount = input * 16;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " oz");
                    }

                    //Gram:
                    else if (fromDropDownText.getText().toString().equals("Gram") && toDropDownText.getText().toString().equals("Gram")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("Gram") && toDropDownText.getText().toString().equals("Kilo-gram")) {
                        amount = input * 0.001;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " kg");
                    } else if (fromDropDownText.getText().toString().equals("Gram") && toDropDownText.getText().toString().equals("Pound")) {
                        amount = input * 0.00220462;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " lb");
                    } else if (fromDropDownText.getText().toString().equals("Gram") && toDropDownText.getText().toString().equals("Ounce")) {
                        amount = input * 0.035274;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " oz");
                    }

                    //Ounce:
                    else if (fromDropDownText.getText().toString().equals("Ounce") && toDropDownText.getText().toString().equals("Ounce")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("Ounce") && toDropDownText.getText().toString().equals("Kilo-gram")) {
                        amount = input * 0.0283495;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " kg");
                    } else if (fromDropDownText.getText().toString().equals("Ounce") && toDropDownText.getText().toString().equals("Pound")) {
                        amount = input * 0.0625;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " lb");
                    } else if (fromDropDownText.getText().toString().equals("Ounce") && toDropDownText.getText().toString().equals("Gram")) {
                        amount = input * 28.3495;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " g");
                    }

                    //  Height Conversions:-

                    //Kilo-meter:
                    else if (fromDropDownText.getText().toString().equals("Kilo-meter") && toDropDownText.getText().toString().equals("Kilo-meter")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("Kilo-meter") && toDropDownText.getText().toString().equals("Meter")) {
                        amount = input * 1000;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " m");
                    } else if (fromDropDownText.getText().toString().equals("Kilo-meter") && toDropDownText.getText().toString().equals("Foot")) {
                        amount = input * 3280.84;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " foot");
                    } else if (fromDropDownText.getText().toString().equals("Kilo-meter") && toDropDownText.getText().toString().equals("Mile")) {
                        amount = input * 0.621371;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " mile");
                    }

                    //Meter:
                    else if (fromDropDownText.getText().toString().equals("Meter") && toDropDownText.getText().toString().equals("Meter")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("Meter") && toDropDownText.getText().toString().equals("Kilo-meter")) {
                        amount = input * 0.001;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " km");
                    } else if (fromDropDownText.getText().toString().equals("Meter") && toDropDownText.getText().toString().equals("Foot")) {
                        amount = input * 3.28084;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " foot");
                    } else if (fromDropDownText.getText().toString().equals("Meter") && toDropDownText.getText().toString().equals("Mile")) {
                        amount = input * 0.000621371;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " mile");
                    }

                    //Foot:
                    else if (fromDropDownText.getText().toString().equals("Foot") && toDropDownText.getText().toString().equals("Foot")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("Foot") && toDropDownText.getText().toString().equals("Kilo-meter")) {
                        amount = input * 0.0003048;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " km");
                    } else if (fromDropDownText.getText().toString().equals("Foot") && toDropDownText.getText().toString().equals("Meter")) {
                        amount = input * 0.3048;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " m");
                    } else if (fromDropDownText.getText().toString().equals("Foot") && toDropDownText.getText().toString().equals("Mile")) {
                        amount = input * 0.000189394;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " mile");
                    }

                    //Mile:
                    else if (fromDropDownText.getText().toString().equals("Mile") && toDropDownText.getText().toString().equals("Mile")) {
                        amount = input * 1.60934;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " mile");
                    } else if (fromDropDownText.getText().toString().equals("Mile") && toDropDownText.getText().toString().equals("Kilo-meter")) {
                        amount = input * 1.60934;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " km");
                    } else if (fromDropDownText.getText().toString().equals("Mile") && toDropDownText.getText().toString().equals("Meter")) {
                        amount = input * 1609.34;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " m");
                    } else if (fromDropDownText.getText().toString().equals("Mile") && toDropDownText.getText().toString().equals("Foot")) {
                        amount = input * 5280;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " foot");
                    }

                    //  Temperature Conversions:-

                    //°C:
                    else if (fromDropDownText.getText().toString().equals("°C") && toDropDownText.getText().toString().equals("°C")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("°C") && toDropDownText.getText().toString().equals("°F")) {
                        amount = (9.0 / 5.0) * input + 32;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °F");
                    } else if (fromDropDownText.getText().toString().equals("°C") && toDropDownText.getText().toString().equals("K")) {
                        amount = input + 273.15;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " K");
                    } else if (fromDropDownText.getText().toString().equals("°C") && toDropDownText.getText().toString().equals("°R")) {
                        amount = (9.0 / 5.0) * input + 491.67;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °R");
                    }

                    //°F:
                    else if (fromDropDownText.getText().toString().equals("°F") && toDropDownText.getText().toString().equals("°F")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("°F") && toDropDownText.getText().toString().equals("°C")) {
                        amount = (input - 32) * (5.0 / 9.0);
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °C");
                    } else if (fromDropDownText.getText().toString().equals("°F") && toDropDownText.getText().toString().equals("K")) {
                        amount = (input - 32) * (5.0 / 9.0) + 273.15;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " K");
                    } else if (fromDropDownText.getText().toString().equals("°F") && toDropDownText.getText().toString().equals("°R")) {
                        amount = input + 459.67;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °R");
                    }

                    //K:
                    else if (fromDropDownText.getText().toString().equals("K") && toDropDownText.getText().toString().equals("K")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("K") && toDropDownText.getText().toString().equals("°C")) {
                        amount = input - 273.15;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °C");
                    } else if (fromDropDownText.getText().toString().equals("K") && toDropDownText.getText().toString().equals("°F")) {
                        amount = (input - 273.15) * (9.0 / 5.0) + 32;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °F");
                    } else if (fromDropDownText.getText().toString().equals("K") && toDropDownText.getText().toString().equals("°R")) {
                        amount = input * (9.0 / 5.0);
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °R");
                    }

                    //°R:
                    else if (fromDropDownText.getText().toString().equals("°R") && toDropDownText.getText().toString().equals("°R")) {
                        result.setText("No Action Performed!");
                        sameType.show();
                    } else if (fromDropDownText.getText().toString().equals("°R") && toDropDownText.getText().toString().equals("°C")) {
                        amount = (input - 491.67) * (5.0 / 9.0);
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °C");
                    } else if (fromDropDownText.getText().toString().equals("°R") && toDropDownText.getText().toString().equals("°F")) {
                        amount = input - 459.67;
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " °F");
                    } else if (fromDropDownText.getText().toString().equals("°R") && toDropDownText.getText().toString().equals("K")) {
                        amount = input * (5.0 / 9.0);
                        String amountString = String.valueOf(amount);
                        result.setText(amountString + " K");
                    }
                }

            }

        });

        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valueEditText.setText("");
            }
        });

        ActionBar actionBar;
        actionBar = getSupportActionBar();

//  Set Black color for Action Bar:
        ColorDrawable actionBackground = new ColorDrawable(Color.parseColor("#FF000000"));
        assert actionBar != null;
        actionBar.setBackgroundDrawable(actionBackground);

//  Set Black color for Status Bar:
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.black));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.about) {
            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
        } else if (item.getItemId() == R.id.connectWithUs) {
            Intent connectWithUsIntent = new Intent(MainActivity.this, ConnectWithUs.class);
            startActivity(connectWithUsIntent);
        } else if (item.getItemId() == R.id.share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "Your body here";
            String shareSub = "Your subject here";
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share using"));
        } else if (item.getItemId() == R.id.exit) {
            MainActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder backPressedBuilder = new AlertDialog.Builder(this);
        backPressedBuilder.setTitle("Exit");
        backPressedBuilder.setMessage("Are you sure you want to exit ?");
        backPressedBuilder.setIcon(R.mipmap.ic_launcher);
        backPressedBuilder.setCancelable(false);
        backPressedBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        backPressedBuilder.setNegativeButton("No", null);
        final AlertDialog backPressed = backPressedBuilder.create();
        backPressed.show();
    }

}

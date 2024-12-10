package com.example.expensetracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncomeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    //Variables
    List<TransactionModel> categoryItemList = new ArrayList<>();
    Button btnChooseCategory;
    EditText editTxtAmount, editTxtNotes;
    GridView gridViewCategory;
    TextView txtViewDate;
    Calendar calendar;
    Toolbar toolBar;
    ImageView imgViewCalendar;
    CalculatorKeyboard calKeyboard;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        //Add categories to gridView
        addData();

        //customized toolbar
        toolBar = findViewById(R.id.toolBar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        //Select date
        txtViewDate = findViewById(R.id.txtViewDate);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        txtViewDate.setText(currentDate);
        txtViewDate.setOnClickListener((View view) -> {
            showCalendar();
            editTxtAmount.clearFocus();
            editTxtNotes.clearFocus();
            calKeyboard.hide();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( view.getWindowToken(), 0);
        });
        imgViewCalendar = findViewById(R.id.imgViewCalendar);
        imgViewCalendar.setOnClickListener((View view) -> {
            showCalendar();
            editTxtAmount.clearFocus();
            editTxtNotes.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( view.getWindowToken(), 0);
        });

        //custom calculator keyboard
        editTxtAmount = findViewById(R.id.editTxtAmount);
        calKeyboard = new CalculatorKeyboard(this, findViewById(R.id.relLayKeyboard), true);
        editTxtAmount = findViewById(R.id.editTxtAmount);
        editTxtAmount.setShowSoftInputOnFocus(false);
        editTxtAmount.setOnFocusChangeListener((View view, boolean hasFocus) -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( view.getWindowToken(), 0);
            if (hasFocus) {
                calKeyboard.show(editTxtAmount);
            } else {
                calKeyboard.hide();
            }
        });

        editTxtNotes = findViewById(R.id.editTxtNotes);
        editTxtNotes.setOnClickListener((View view) -> {
            calKeyboard.hide();
        });

        //button to choose category
        btnChooseCategory = findViewById(R.id.btnChooseCategory);
        btnChooseCategory.setOnClickListener((View view) -> {
            editTxtAmount.clearFocus();
            editTxtNotes.clearFocus();
            calKeyboard.hide();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( view.getWindowToken(), 0);
            //display gridView
            gridViewCategory = findViewById(R.id.gridViewCategories);
            GridViewAdapter cAdapter = new GridViewAdapter(this, R.layout.layout_categoryitem, categoryItemList);
            gridViewCategory.setAdapter(cAdapter);
            //when a category is chosen
            gridViewCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //get database
                    DatabaseHelper DB = new DatabaseHelper(getApplicationContext());
                    //create a object
                    TransactionModel transactionModel = new TransactionModel();
                    //try and catch exception for inputs
                    try{
                        //store inputs into object
                        transactionModel.setAmount(Double.parseDouble(editTxtAmount.getText().toString()));
                        transactionModel.setNote(editTxtNotes.getText().toString());
                        transactionModel.setCategory(categoryItemList.get(i).getCategory());
                        transactionModel.setGroup("income");
                        transactionModel.setDate(dateFormat.parse(currentDate));
                        if (calendar.getTime() != null){
                            transactionModel.setDate(calendar.getTime());
                        }
                        //catch exception for when amount is empty
                    } catch (NumberFormatException ex) {
                        //Display when amount if empty
                        Toast.makeText(IncomeActivity.this, "Amount cannot be empty", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    //check if inputs are stored successfully
                    Boolean success = DB.addData(transactionModel);
                    if (success == true) {
                        //display entry stored successfully and move back to home page
                        Toast.makeText(IncomeActivity.this, "Entry Inserted", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(IncomeActivity.this, MainActivity.class);
                        intent.putExtra("date", transactionModel.getDate());
                        startActivity(intent);
                    } else {
                        //display entry stored unsuccessfully and stay in expense entry page
                        Toast.makeText(IncomeActivity.this, "Entry Not Inserted", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    //To add category details into a list
    private void addData() {
        categoryItemList.add(new TransactionModel("Deposits", R.drawable.deposit1));
        categoryItemList.add(new TransactionModel("Salary", R.drawable.salary));
    }

    //get calendar
    public void showCalendar() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    //display chosen date in textView
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDate = dateFormat.format(calendar.getTime());
        txtViewDate = (TextView) findViewById(R.id.txtViewDate);
        txtViewDate.setText(currentDate);
    }

    @Override
    public void onBackPressed() {
        if (calKeyboard.isVisible()) {
            calKeyboard.hide();
        } else {
            super.onBackPressed();
        }
    }
}
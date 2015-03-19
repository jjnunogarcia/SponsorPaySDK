package com.sponsorpay.sdk.android.testapp;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import com.sponsorpay.sdk.android.user.*;
import com.sponsorpay.sdk.android.utils.SponsorPayLogger;
import com.sponsorpay.sdk.android.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SPUserActivity extends FragmentActivity {

  private static final String TAG = "SPUserActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sp_user);

    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
                                 .add(R.id.container, new PlaceholderFragment()).commit();
    }
  }


  public static class PlaceholderFragment extends Fragment {

    private EditText     ageEditText;
    private EditText     birthdayEditText;
    private Spinner      genderSpinner;
    private Spinner      sexualOrientationSpinner;
    private Spinner      ethnicitySpinner;
    private EditText     locationEditText;
    private Spinner      maritalStatusSpinner;
    private EditText     numberOfChildrensEditText;
    private EditText     annualHouseholdIncomeEditText;
    private Spinner      educationSpinner;
    private EditText     zipcodeEditText;
    private EditText     interestsEditText;
    private CheckBox     iapCheckbox;
    private EditText     iapAmountEditText;
    private EditText     numberOfSessionsEditText;
    private EditText     psTimeEditText;
    private EditText     lastSessionEditText;
    private Spinner      connectionSpinner;
    private EditText     deviceEditText;
    private EditText     appVersionEditText;
    private Button       createCustomValue;
    private Button       submitButton;
    private LinearLayout mainLayout;

    private static int countOfCustomValues;
    private        int width;

    private String key;
    private String value;


    public PlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_spuser,
                                       container, false);

      DisplayMetrics dm = new DisplayMetrics();
      getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);

      width = dm.widthPixels;

      countOfCustomValues = 0;

      mainLayout = (LinearLayout) rootView.findViewById(R.id.main_layout_user_segmentation);

      ageEditText = (EditText) rootView.findViewById(R.id.age_edit_text);
      birthdayEditText = (EditText) rootView.findViewById(R.id.birthdate_edit_text);
      birthdayEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          if (hasFocus) {
            showDatePicker();
          }
        }
      });
      ;
      birthdayEditText.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          showDatePicker();
        }
      });
      birthdayEditText.setOnKeyListener(null);
      genderSpinner = (Spinner) rootView.findViewById(R.id.gender_spinner);
      sexualOrientationSpinner = (Spinner) rootView.findViewById(R.id.sexual_orientation_spinner);
      ethnicitySpinner = (Spinner) rootView.findViewById(R.id.ethnicity_spinner);
      locationEditText = (EditText) rootView.findViewById(R.id.location_edit_text);
      maritalStatusSpinner = (Spinner) rootView.findViewById(R.id.marital_status_spinner);
      numberOfChildrensEditText = (EditText) rootView.findViewById(R.id.number_of_childrens_edit_text);
      annualHouseholdIncomeEditText = (EditText) rootView.findViewById(R.id.annual_household_income_edit_text);
      educationSpinner = (Spinner) rootView.findViewById(R.id.education_spinner);
      zipcodeEditText = (EditText) rootView.findViewById(R.id.zipcode_edit_text);
      interestsEditText = (EditText) rootView.findViewById(R.id.interests_edit_text);
      iapCheckbox = (CheckBox) rootView.findViewById(R.id.iap_checkbox);
      iapAmountEditText = (EditText) rootView.findViewById(R.id.iap_amount_edit_text);
      numberOfSessionsEditText = (EditText) rootView.findViewById(R.id.number_of_sessions_edit_text);
      psTimeEditText = (EditText) rootView.findViewById(R.id.ps_time_edit_text);
      lastSessionEditText = (EditText) rootView.findViewById(R.id.last_session_edit_text);
      connectionSpinner = (Spinner) rootView.findViewById(R.id.connection_spinner);
      deviceEditText = (EditText) rootView.findViewById(R.id.device_edit_text);
      appVersionEditText = (EditText) rootView.findViewById(R.id.app_version_edit_text);
      createCustomValue = (Button) rootView.findViewById(R.id.add_custom_data_button);
      submitButton = (Button) rootView.findViewById(R.id.submit_sp_user_data_button);

      //we are calling these methods prior the click
      //of the submit button because we have to set
      //the callbacks
      getGenderAndSetSPUser();
      getSexualOrientationAndSetSPUser();
      getEthnicityAndSetSPUser();
      getMaritalStatusAndSetSPUser();
      getEducationAndSetSPUser();
      hasIapAndSetSPUser();
      getConnectionAndSetSPUser();

      createCustomValue.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          addEditText(view);
        }
      });

      submitButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          getAgeAndSetSPUser();
          getBirthdayAndSetSPUser();
          getLocationAndSetSPUser();
          getNumberOfChildrensAndSetSPUser();
          getAnnualHouseholdIncomeAndSetSPUser();
          getZipcodeAndSetSPUser();
          getInterestsAndSetSPUser();
          getIapAmountAndSetSPUser();
          getNumberOfSessionsAndSetSPUser();
          getPsTimeAndSetSPUser();
          getLastSessionAndSetSPUser();
          getDeviceAndSetSPUser();
          getAppVersionAndSetSPUser();

          if (StringUtils.notNullNorEmpty(key)
              && StringUtils.notNullNorEmpty(value)) {
            SPUser.addCustomValue(key, value);
          }

          SPUser.mapToString();
        }
      });

      return rootView;
    }

    private void addEditText(View view) {
      key = "";
      value = "";
      LinearLayout wrapperLayout = new LinearLayout(view.getContext());
      wrapperLayout.setOrientation(LinearLayout.HORIZONTAL);

      LayoutParams lparams = new LayoutParams((width / 2), LayoutParams.WRAP_CONTENT);

      EditText editTextKey = new EditText(view.getContext());
      editTextKey.setLayoutParams(lparams);
      editTextKey.setId(countOfCustomValues);
      editTextKey.setHint("key");

      editTextKey.addTextChangedListener(new TextWatcher() {

        @Override
        public void afterTextChanged(Editable text) {
          key = text.toString();
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1,
                                      int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence chars, int arg1,
                                  int arg2, int arg3) {
        }

      });
      countOfCustomValues++;

      EditText editTextValue = new EditText(view.getContext());
      editTextValue.setLayoutParams(lparams);
      editTextValue.setId(countOfCustomValues);
      editTextValue.setHint("value");
      editTextValue.addTextChangedListener(new TextWatcher() {

        @Override
        public void afterTextChanged(Editable text) {
          value = text.toString();
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1,
                                      int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence chars, int arg1,
                                  int arg2, int arg3) {
        }

      });
      countOfCustomValues++;

      wrapperLayout.addView(editTextKey);
      wrapperLayout.addView(editTextValue);

      mainLayout.addView(wrapperLayout);
    }


    @Override
    public void onResume() {
      super.onResume();

      if (checkIfNumberHasBeenSet(SPUser.getAge())) {
        ageEditText.setText(SPUser.getAge().toString());
      }

      if (checkIfNumberHasBeenSet(SPUser.getNumberOfChildrens())) {
        numberOfChildrensEditText.setText(SPUser.getAge().toString());
      }

      if (checkIfNumberHasBeenSet(SPUser.getAnnualHouseholdIncome())) {
        annualHouseholdIncomeEditText.setText(SPUser.getAnnualHouseholdIncome().toString());
      }

      if (checkIfValueHasBeenSet(SPUser.getZipcode())) {
        zipcodeEditText.setText(SPUser.getZipcode());
      }

      if (SPUser.getInterests() != null) {
        StringBuilder builder = new StringBuilder();
        for (String s : SPUser.getInterests()) {
          builder.append(s);
        }

        interestsEditText.setText(builder.toString());
      }

      if (checkIfNumberHasBeenSet(SPUser.getIapAmount())) {
        iapAmountEditText.setText(SPUser.getIapAmount().toString());
      }

      if (checkIfNumberHasBeenSet(SPUser.getNumberOfSessions())) {
        numberOfSessionsEditText.setText(SPUser.getNumberOfSessions().toString());
      }

      if (checkIfNumberHasBeenSet(SPUser.getPsTime())) {
        psTimeEditText.setText(SPUser.getPsTime().toString());
      }

      if (checkIfNumberHasBeenSet(SPUser.getLastSession())) {
        lastSessionEditText.setText(SPUser.getLastSession().toString());
      }

      if (checkIfValueHasBeenSet(SPUser.getDevice())) {
        deviceEditText.setText(SPUser.getDevice());
      }

      if (checkIfValueHasBeenSet(SPUser.getAppVersion())) {
        appVersionEditText.setText(SPUser.getAppVersion());
      }

    }

    private void showDatePicker() {
      //To show current date in the datepicker
      Calendar mcurrentDate = Calendar.getInstance();
      int year = mcurrentDate.get(Calendar.YEAR);
      int month = mcurrentDate.get(Calendar.MONTH);
      int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);

      DatePickerDialog mDatePicker = new DatePickerDialog(getActivity(), new OnDateSetListener() {
        public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
          //YYYY/MM/DD
          birthdayEditText.setText(String.format("%04d/%02d/%02d", selectedyear, selectedmonth, selectedday));
        }
      }, year, month, day);
      mDatePicker.setTitle("Select date");
      mDatePicker.show();
    }

    private void getAgeAndSetSPUser() {
      String age = ageEditText.getText().toString();
      if (checkIfValueHasBeenSet(age)) {
        SPUser.setAge(Integer.valueOf(age));
      }
    }

    private void getBirthdayAndSetSPUser() {
      String birthday = birthdayEditText.getText().toString();
      if (checkIfValueHasBeenSet(birthday)) {
        Date date = null;
        try {
          date = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(birthday);
        } catch (ParseException e) {
          e.printStackTrace();
        }
        SPUser.setBirthdate(date);
      }
    }

    private void getGenderAndSetSPUser() {
      genderSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          SPUser.setGender(SPUserGender.values()[pos]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
      });
    }

    private void getSexualOrientationAndSetSPUser() {
      sexualOrientationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          SPUser.setSexualOrientation(SPUserSexualOrientation.values()[pos]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
      });
    }

    private void getEthnicityAndSetSPUser() {
      ethnicitySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          SPUser.setEthnicity(SPUserEthnicity.values()[pos]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
      });
    }

    private void getLocationAndSetSPUser() {
      String provider = locationEditText.getText().toString();
      if (checkIfValueHasBeenSet(provider)) {

        String[] latAndLongt = provider.split(",");
        try {

          String latitude = spliLatAndLongt(latAndLongt[0]);
          String longtitude = spliLatAndLongt(latAndLongt[1]);

          Location location = new Location(provider);
          location.setLatitude(Double.parseDouble(latitude));
          location.setLongitude(Double.parseDouble(longtitude));

          SPUser.setLocation(location);

        } catch (IndexOutOfBoundsException ex) {
          SponsorPayLogger.e(TAG, "Not valid parameters for Location");
        }
      }
    }

    private String spliLatAndLongt(String latOrLongt) throws IndexOutOfBoundsException {
      String latOrLongtFullValue = latOrLongt;
      String[] latOrLongtArray = latOrLongtFullValue.split(":");

      return latOrLongtArray[1].replace(" ", "");
    }

    private void getMaritalStatusAndSetSPUser() {
      maritalStatusSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          SPUser.setMaritalStatus(SPUserMaritalStatus.values()[pos]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
      });
    }

    private void getNumberOfChildrensAndSetSPUser() {
      String numberOfChildrens = numberOfChildrensEditText.getText().toString();
      if (checkIfValueHasBeenSet(numberOfChildrens)) {
        SPUser.setNumberOfChildrens(Integer.valueOf(numberOfChildrens));
      }
    }

    private void getAnnualHouseholdIncomeAndSetSPUser() {
      String annualHouseholdIncome = annualHouseholdIncomeEditText.getText().toString();
      if (checkIfValueHasBeenSet(annualHouseholdIncome)) {
        SPUser.setAnnualHouseholdIncome(Integer.valueOf(annualHouseholdIncome));
      }
    }

    private void getEducationAndSetSPUser() {
      educationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          SPUser.setEducation(SPUserEducation.values()[pos]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
      });
    }


    private void getZipcodeAndSetSPUser() {
      String zipcode = zipcodeEditText.getText().toString();
      if (checkIfValueHasBeenSet(zipcode)) {
        SPUser.setZipcode(zipcode);
      }
    }


    private void getInterestsAndSetSPUser() {
      String interests = interestsEditText.getText().toString();
      if (checkIfValueHasBeenSet(interests)) {
        SPUser.setInterests(interests.split(","));
      }
    }

    private void hasIapAndSetSPUser() {
      iapCheckbox.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          SPUser.setIap(((CheckBox) v).isChecked());
        }
      });
    }

    private void getIapAmountAndSetSPUser() {
      String iapAmount = iapAmountEditText.getText().toString();
      if (checkIfValueHasBeenSet(iapAmount)) {
        SPUser.setIapAmount(Float.valueOf(iapAmount));
      }
    }

    private void getNumberOfSessionsAndSetSPUser() {
      String numberOfSessions = numberOfSessionsEditText.getText().toString();
      if (checkIfValueHasBeenSet(numberOfSessions)) {
        SPUser.setNumberOfSessions(Integer.valueOf(numberOfSessions));
      }
    }

    private void getPsTimeAndSetSPUser() {
      String psTime = psTimeEditText.getText().toString();
      if (checkIfValueHasBeenSet(psTime)) {
        SPUser.setPsTime(Long.valueOf(psTime));
      }
    }

    private void getLastSessionAndSetSPUser() {
      String lastSession = lastSessionEditText.getText().toString();
      if (checkIfValueHasBeenSet(lastSession)) {
        SPUser.setLastSession(Long.valueOf(lastSession));
      }
    }

    private void getConnectionAndSetSPUser() {
      connectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
          SPUser.setConnection(SPUserConnection.values()[pos]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
      });
    }


    private void getDeviceAndSetSPUser() {
      String device = deviceEditText.getText().toString();
      if (checkIfValueHasBeenSet(device)) {
        SPUser.setDevice(device);
      }
    }

    private void getAppVersionAndSetSPUser() {
      String appVersion = appVersionEditText.getText().toString();
      if (checkIfValueHasBeenSet(appVersion)) {
        SPUser.setAppVersion(appVersion);
      }
    }

    private boolean checkIfValueHasBeenSet(String value) {
      return StringUtils.notNullNorEmpty(value);
    }

    //Check for Integers, Floats and Doubles
    private boolean checkIfNumberHasBeenSet(Number value) {
      if (value != null) {
        return true;
      }

      return false;
    }

  }

}
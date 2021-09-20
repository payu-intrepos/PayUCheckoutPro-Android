package com.payu.sampleapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.payu.base.models.CustomNote;
import com.payu.base.models.ErrorResponse;
import com.payu.base.models.PayUBillingCycle;
import com.payu.base.models.PayUOfferDetails;
import com.payu.base.models.PayUPaymentParams;
import com.payu.base.models.PayUSIParams;
import com.payu.base.models.PaymentMode;
import com.payu.base.models.PaymentType;
import com.payu.base.models.PayuBillingLimit;
import com.payu.base.models.PayuBillingRule;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.models.PayUCheckoutProConfig;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payu.sampleapp.databinding.ActivityMainBinding;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;

import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private final String email = "snooze@payu.in";
    private final String phone = "9999999999";
    private final String merchantName = "RH Group";
    private final String surl = "https://payuresponse.firebaseapp.com/success";
    private final String furl = "https://payuresponse.firebaseapp.com/failure";
    private final String amount = "1.0";
    private final String testKey = "IUIaFM";
    private final String testSalt = "<Please_add_salt_here>";
    private final String testMerchantAccessKey = "E5ABOXOWAAZNXB6JEF5Z";
    private final String testMerchantSecretKey = "e425e539233044146a2d185a346978794afd7c66";
    private final String prodKey = "0MQaQP";
    private final String prodSalt = "<Please_add_salt_here>";
    private ActivityMainBinding binding;
    private long mLastClickTime;
    private ReviewOrderRecyclerViewAdapter reviewOrderAdapter;
    private final String[] billingCycle = {  "DAILY", "WEEKLY", "MONTHLY", "YEARLY", "ONCE", "ADHOC"};
    private final String[] billingRule = {"MAX", "EXACT"};
    private final String[] billingLimit = {"ON", "BEFORE", "AFTER"};
    private final String[] noteCategory = {"CARD", "NB", "WALLET", "UPI", "EMI", "COMMON", "NULL"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initializeSIView();
        setCustomeNote();
        setInitalData();
        initListeners();
    }

    private void initializeSIView() {
        binding.switchSiOnOff.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.layoutSiDetails.llSiDetails.setVisibility(View.VISIBLE);
            } else {
                binding.layoutSiDetails.llSiDetails.setVisibility(View.GONE);
            }
        });

        ArrayAdapter<Object> adapter = new ArrayAdapter<Object>(
                this,
                android.R.layout.simple_spinner_item,
                billingCycle
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((AppCompatSpinner)findViewById(R.id.et_billingCycle_value)).setAdapter(adapter);

        ArrayAdapter<String> billingRuleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, billingRule);
        billingRuleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.layoutSiDetails.etBillingRuleValue.setAdapter(billingRuleAdapter);

        ArrayAdapter<String> billingLimitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, billingLimit);
        billingLimitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.layoutSiDetails.etBillingLimitValue.setAdapter(billingLimitAdapter);
    }

    private void setCustomeNote(){
        ArrayAdapter<String> noteCategoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,noteCategory);
        noteCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((AppCompatSpinner)findViewById(R.id.et_custom_note_category_value)).setAdapter(noteCategoryAdapter);
    }

    private void setInitalData() {
        updateProdEnvDetails();
        binding.etSurl.setText(surl);
        binding.etFurl.setText(furl);
        binding.etMerchantName.setText(merchantName);
        binding.etPhone.setText(phone);
        binding.etAmount.setText(amount);
        binding.etUserCredential.setText(binding.etKey.getText().toString() + ":" + email);
        binding.etSurePayCount.setText("0");
    }

    private void initListeners() {
        binding.radioGrpEnv.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radioBtnTest:
                        updateTestEnvDetails();
                        break;
                    case R.id.radioBtnProduction:
                        updateProdEnvDetails();
                        break;
                    default:
                        updateTestEnvDetails();
                }
            }
        });

        binding.switchEnableReviewOrder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    showReviewOrderView();
                else
                    hideReviewOrderView();
            }
        });

        binding.btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reviewOrderAdapter.addRow();
            }
        });
    }

    private void hideReviewOrderView() {
        binding.rlReviewOrder.setVisibility(View.GONE);
        reviewOrderAdapter = null;
    }

    private void showReviewOrderView() {
        binding.rlReviewOrder.setVisibility(View.VISIBLE);
        reviewOrderAdapter = new ReviewOrderRecyclerViewAdapter();
        binding.rvReviewOrder.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviewOrder.setAdapter(reviewOrderAdapter);
    }

    private void updateTestEnvDetails() {
        //For testing
        binding.etKey.setText(testKey);
        binding.etSalt.setText(testSalt);
    }

    private void updateProdEnvDetails() {
        //For Production
        binding.etKey.setText(prodKey);
        binding.etSalt.setText(prodSalt);
    }

    public void startPayment(View view) {
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000)
            return;
        mLastClickTime = SystemClock.elapsedRealtime();
        initUiSdk(preparePayUBizParams());

    }

    private void initUiSdk(PayUPaymentParams payUPaymentParams) {
        PayUCheckoutPro.open(
                this,
                payUPaymentParams,
                getCheckoutProConfig(),
                new PayUCheckoutProListener() {

                    @Override
                    public void onPaymentSuccess(Object response) {
                        showAlertDialog(response);
                    }

                    @Override
                    public void onPaymentFailure(Object response) {
                        showAlertDialog(response);
                    }

                    @Override
                    public void onPaymentCancel(boolean isTxnInitiated) {
                        showSnackBar(getResources().getString(R.string.transaction_cancelled_by_user));
                    }

                    @Override
                    public void onError(ErrorResponse errorResponse) {
                        String errorMessage = errorResponse.getErrorMessage();
                        if (TextUtils.isEmpty(errorMessage))
                            errorMessage = getResources().getString(R.string.some_error_occurred);
                        showSnackBar(errorMessage);
                    }

                    @Override
                    public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {
                        //For setting webview properties, if any. Check Customized Integration section for more details on this
                    }

                    @Override
                    public void generateHash(HashMap<String, String> valueMap, PayUHashGenerationListener hashGenerationListener) {
                        String hashName = valueMap.get(PayUCheckoutProConstants.CP_HASH_NAME);
                        String hashData = valueMap.get(PayUCheckoutProConstants.CP_HASH_STRING);
                        if (!TextUtils.isEmpty(hashName) && !TextUtils.isEmpty(hashData)) {
                            //Generate Hash from your backend here
                            String hash = null;
                            if (hashName.equalsIgnoreCase(PayUCheckoutProConstants.CP_LOOKUP_API_HASH)){
                                //Calculate HmacSHA1 HASH for calculating Lookup API Hash
                                ///Do not generate hash from local, it needs to be calculated from server side only. Here, hashString contains hash created from your server side.

                                hash = calculateHmacSHA1Hash(hashData, testMerchantSecretKey);
                            } else {

                                //Calculate SHA-512 Hash here
                                hash = calculateHash(hashData + binding.etSalt.getText().toString());
                            }

                            HashMap<String, String> dataMap = new HashMap<>();
                            dataMap.put(hashName, hash);
                            hashGenerationListener.onHashGenerated(dataMap);
                        }
                    }
                }
        );
    }

    private void showSnackBar(String message) {
        Snackbar.make(binding.clMain, message, Snackbar.LENGTH_LONG).show();
    }

    private PayUCheckoutProConfig getCheckoutProConfig() {
        PayUCheckoutProConfig checkoutProConfig = new PayUCheckoutProConfig();
        checkoutProConfig.setPaymentModesOrder(getCheckoutOrderList());
        checkoutProConfig.setOfferDetails(getOfferDetailsList());
        checkoutProConfig.setShowCbToolbar(!binding.switchHideCbToolBar.isChecked());
        checkoutProConfig.setAutoSelectOtp(binding.switchAutoSelectOtp.isChecked());
        checkoutProConfig.setAutoApprove(binding.switchAutoApprove.isChecked());
        checkoutProConfig.setSurePayCount(Integer.parseInt(binding.etSurePayCount.getText().toString()));
        checkoutProConfig.setShowExitConfirmationOnPaymentScreen(!binding.switchDiableCBDialog.isChecked());
        checkoutProConfig.setShowExitConfirmationOnCheckoutScreen(!binding.switchDiableUiDialog.isChecked());
        checkoutProConfig.setMerchantName(binding.etMerchantName.getText().toString());
        checkoutProConfig.setMerchantLogo(R.drawable.merchant_logo);
        checkoutProConfig.setWaitingTime(30000);
        checkoutProConfig.setMerchantResponseTimeout(30000);
        checkoutProConfig.setCustomNoteDetails(getCustomeNoteList());
        if (reviewOrderAdapter != null)
            checkoutProConfig.setCartDetails(reviewOrderAdapter.getOrderDetailsList());
        return checkoutProConfig;
    }

    private ArrayList<PayUOfferDetails> getOfferDetailsList() {
        ArrayList<PayUOfferDetails> offerDetails = new ArrayList<>();
        PayUOfferDetails payUOfferDetails1 = new PayUOfferDetails();
        payUOfferDetails1.setOfferTitle("Instant discount of Rs.2");
        payUOfferDetails1.setOfferDescription("Get Instant dicount of Rs.2 on all Credit and Debit card transactions");
        payUOfferDetails1.setOfferKey("OfferKey@9227");

        ArrayList<PaymentType> offerPaymentTypes1 = new ArrayList<>();
        offerPaymentTypes1.add(PaymentType.CARD);
        payUOfferDetails1.setOfferPaymentTypes(offerPaymentTypes1);

        PayUOfferDetails payUOfferDetails2 = new PayUOfferDetails();
        payUOfferDetails2.setOfferTitle("Instant discount of Rs.2");
        payUOfferDetails2.setOfferDescription("Get Instant dicount of Rs.2 on all NetBanking transactions");
        payUOfferDetails2.setOfferKey("TestOffer100@9229");

        ArrayList<PaymentType> offerPaymentTypes2 = new ArrayList<>();
        offerPaymentTypes2.add(PaymentType.NB);
        payUOfferDetails2.setOfferPaymentTypes(offerPaymentTypes2);

        offerDetails.add(payUOfferDetails1);
        offerDetails.add(payUOfferDetails2);
        return offerDetails;
    }

    private ArrayList<PaymentMode> getCheckoutOrderList() {
        ArrayList<PaymentMode> checkoutOrderList = new ArrayList();
        if (binding.switchShowGooglePay.isChecked())
            checkoutOrderList.add(new PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_GOOGLE_PAY));
        if (binding.switchShowPhonePe.isChecked())
            checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PHONEPE));
        if (binding.switchShowPaytm.isChecked())
            checkoutOrderList.add(new PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PAYTM));

        return checkoutOrderList;
    }

    private void showAlertDialog(Object response){
        HashMap<String,Object> result = (HashMap<String, Object>) response;
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(
                        "Payu's Data : " + result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE) + "\n\n\n Merchant's Data: " + result.get(
                                PayUCheckoutProConstants.CP_MERCHANT_RESPONSE
                        )
                )
                .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    private PayUPaymentParams preparePayUBizParams() {
        HashMap<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(PayUCheckoutProConstants.CP_UDF1, "udf1");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF2, "udf2");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF3, "udf3");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF4, "udf4");
        additionalParams.put(PayUCheckoutProConstants.CP_UDF5, "udf5");

        //Below params should be passed only when integrating Multi-currency support
        //TODO Please pass your own Merchant Access Key below as provided by your Key Account Manager at PayU.
        // Merchant Access Key used here is only for testing purpose.
//        additionalParams.put(PayUCheckoutProConstants.CP_MERCHANT_ACCESS_KEY, testMerchantAccessKey);

        PayUSIParams siDetails = null;
        if(binding.switchSiOnOff.isChecked()) {
            siDetails  = new PayUSIParams.Builder().setIsFreeTrial(binding.layoutSiDetails.spFreeTrial.isChecked())
                    .setBillingAmount(binding.layoutSiDetails.etBillingAmountValue.getText().toString())
                    .setBillingCycle(PayUBillingCycle.valueOf(binding.layoutSiDetails.etBillingCycleValue.getSelectedItem().toString()))
                    .setBillingInterval(Integer.parseInt(binding.layoutSiDetails.etBillingIntervalValue.getText().toString()))
                    .setPaymentStartDate(binding.layoutSiDetails.etPaymentStartDateValue.getText().toString())
                    .setPaymentEndDate(binding.layoutSiDetails.etPaymentEndDateValue.getText().toString())
                    .setRemarks(binding.layoutSiDetails.etRemarksValue.getText().toString())
                    .setBillingLimit(PayuBillingLimit.valueOf(binding.layoutSiDetails.etBillingLimitValue.getSelectedItem().toString()))
                    .setBillingRule(PayuBillingRule.valueOf(binding.layoutSiDetails.etBillingRuleValue.getSelectedItem().toString()))
                    .build();

        }

        PayUPaymentParams.Builder builder = new PayUPaymentParams.Builder();
        builder.setAmount(binding.etAmount.getText().toString())
                .setIsProduction(binding.radioBtnProduction.isChecked())
                .setProductInfo("Macbook Pro")
                .setKey(binding.etKey.getText().toString())
                .setPhone(phone)
                .setTransactionId(String.valueOf(System.currentTimeMillis()))
                .setFirstName("John")
                .setEmail(email)
                .setSurl(binding.etSurl.getText().toString())
                .setFurl(binding.etFurl.getText().toString())
                .setUserCredential(binding.etKey.getText().toString() + ":john@yopmail.com")
                .setAdditionalParams(additionalParams)
                .setPayUSIParams(siDetails);
        PayUPaymentParams payUPaymentParams = builder.build();
        return payUPaymentParams;
    }

    /**
     * Hash Should be generated from your sever side only.
     *
     * Do not use this, you may use this only for testing.
     * This should be done from server side..
     * Do not keep salt anywhere in app.
     * */
    private String calculateHash(String hashString) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(hashString.getBytes());
            byte[] mdbytes = messageDigest.digest();
            return getHexString(mdbytes);
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private String getHexString(byte[] array){
        StringBuilder hash = new StringBuilder();
        for (byte hashByte : array) {
            hash.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return hash.toString();
    }

    /**
     * Hash Should be generated from your sever side only.
     *
     * Do not use this, you may use this only for testing.
     * This should be done from server side..
     * Do not keep salt anywhere in app.
     * */
    private String calculateHmacSHA1Hash(String data, String key) {
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        String result = null;

        try {
            Key signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            result = getHexString(rawHmac);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<CustomNote> getCustomeNoteList(){

        ArrayList<CustomNote> customNote = new ArrayList<>();
       if(!(((AppCompatSpinner)findViewById(R.id.et_custom_note_category_value)).getSelectedItem().toString().equalsIgnoreCase("NULL") && ((AppCompatSpinner)findViewById(R.id.et_custom_note_category_value)).getSelectedItem().toString().equalsIgnoreCase("COMMON"))){
            ArrayList<PaymentType> noteCategory = new ArrayList<>();
            noteCategory.add( PaymentType.valueOf(((AppCompatSpinner)findViewById(R.id.et_custom_note_category_value)).getSelectedItem().toString()));
            CustomNote customNote1 = new CustomNote(((EditText)findViewById(R.id.et_custom_note_value)).getText().toString(), noteCategory);
            customNote1.setCustom_note(((EditText)findViewById(R.id.et_custom_note_value)).getText().toString());
            customNote1.setCustom_note_category(noteCategory);
            customNote.add(customNote1);
        }
        else if(((AppCompatSpinner)findViewById(R.id.et_custom_note_category_value)).getSelectedItem().toString().equalsIgnoreCase("NULL")){
            CustomNote customNote1 = new CustomNote(((EditText)findViewById(R.id.et_custom_note_value)).getText().toString(), null);
            customNote1.setCustom_note(((EditText)findViewById(R.id.et_custom_note_value)).getText().toString());
            customNote1.setCustom_note_category(null);
            customNote.add(customNote1);
        }else {
            ArrayList<PaymentType> noteCategory = new ArrayList<>();
            noteCategory.add( PaymentType.CARD);
            noteCategory.add(PaymentType.NB);
            noteCategory.add(PaymentType.UPI);
            noteCategory.add(PaymentType.WALLET);
            noteCategory.add(PaymentType.EMI);
            CustomNote customNote1 = new CustomNote(((EditText)findViewById(R.id.et_custom_note_value)).getText().toString(), noteCategory);
            customNote1.setCustom_note(((EditText)findViewById(R.id.et_custom_note_value)).getText().toString());
            customNote1.setCustom_note_category(noteCategory);
            customNote.add(customNote1);
        }

        return customNote;
    }

}

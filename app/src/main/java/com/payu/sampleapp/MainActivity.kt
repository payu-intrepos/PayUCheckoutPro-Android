package com.payu.sampleapp

import android.app.AlertDialog
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.payu.base.models.*
import com.payu.checkoutpro.PayUCheckoutPro
import com.payu.checkoutpro.models.PayUCheckoutProConfig
import com.payu.checkoutpro.utils.PayUCheckoutProConstants
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_NAME
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_STRING
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_TYPE
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_V2_HASH
import com.payu.paymentparamhelper.PayuConstants
import com.payu.sampleapp.databinding.ActivityMainBinding
import com.payu.ui.model.listeners.PayUCheckoutProListener
import com.payu.ui.model.listeners.PayUHashGenerationListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custome_note.*
import kotlinx.android.synthetic.main.layout_si_details.*
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val email: String = "snooze@payu.in"
    private val phone = "9999999999"
    private val merchantName = "RH Group"
    private val surl = "https://cbjs.payu.in/sdk/success"
    private val furl = "https://cbjs.payu.in/sdk/failure"
    private val amount = "1.0"

    //Test Key and Salt
    private val testKey = "IUIaFM"
    private val testSalt = "<Please_add_salt_here>"
    /**
     * Enter below keys when integrating Multi Currency Payments.
     * To get these credentials, please reach out to your Key Account Manager at PayU
     * */
    private val merchantAccessKey = "<Please_add_your_merchant_access_key>"
    private val merchantSecretKey = "<Please_add_your_merchant_secret_key>"


    //Prod Key and Salt
    private val prodKey = "0MQaQP"
    private val prodSalt = "<Please_add_salt_here>"

    private lateinit var binding: ActivityMainBinding

    // variable to track event time
    private var mLastClickTime: Long = 0
    private var reviewOrderAdapter: ReviewOrderRecyclerViewAdapter? = null
    private var billingCycle = arrayOf(
        "DAILY",
        "WEEKLY",
        "MONTHLY",
        "YEARLY",
        "ONCE",
        "ADHOC"
    )
    private var billingRule = arrayOf(
        "MAX",
        "EXACT"
    )

    private var billingLimit = arrayOf(
        "ON",
        "BEFORE",
        "AFTER"
    )
    private var noteCategory = arrayOf(
        "CARD",
        "NB",
        "WALLET",
        "UPI",
        "EMI",
        "COMMON",
        "NULL"
    )

    private var splitPaymentType = arrayOf(
        "absolute",
        "percentage"
    )

    private var switchSplitPayment: SwitchCompat? = null
    private var spSplitPaymentType: AppCompatSpinner? = null
    private var llSplitPaymentDetails: LinearLayout? = null
    private var btnSplitMore: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initializeSIView()
        initializeSplitPaymentViews()
        setCustomeNote()
        setInitalData()
        initListeners()
    }

    private fun addSplitPaymentDetailedView() {
        val inflate: View =
            LayoutInflater.from(this).inflate(R.layout.layout_split_payment_details, null)
        llSplitPaymentDetails?.addView(inflate, llSplitPaymentDetails!!.childCount)
    }

    private fun initializeSplitPaymentViews() {
        switchSplitPayment = findViewById(R.id.switch_split_payment)
        llSplitPaymentDetails = findViewById(R.id.ll_split_payment_details)
        btnSplitMore = findViewById(R.id.btn_split_more)

        switchSplitPayment?.setOnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            addSplitPaymentDetailedView()
            if (isChecked) {
                findViewById<View>(R.id.ll_split_type).visibility = View.VISIBLE
                findViewById<View>(R.id.ll_split_payment_details).visibility = View.VISIBLE
                btnSplitMore?.visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.ll_split_type).visibility = View.GONE
                findViewById<View>(R.id.ll_split_payment_details).visibility = View.GONE
                llSplitPaymentDetails?.removeAllViews()
                btnSplitMore?.visibility = View.GONE
            }
        }

        btnSplitMore?.setOnClickListener { addSplitPaymentDetailedView() }
        spSplitPaymentType = findViewById(R.id.et_split_payment_value)

        val adapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            splitPaymentType
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spSplitPaymentType?.adapter = adapter
    }

    private fun initializeSIView() {
        switch_si_on_off.setOnCheckedChangeListener { buttonView, isChecked -> if(isChecked)
        { layout_si_details.visibility = View.VISIBLE }
        else { layout_si_details.visibility = View.GONE }
        }

        val adapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            billingCycle
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        et_billingCycle_value.adapter = adapter
        val billingRuleAdapter : ArrayAdapter<*> = ArrayAdapter<Any?>(
            this, android.R.layout.simple_spinner_item, billingRule
        )
        billingRuleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        et_billingRule_value.adapter = billingRuleAdapter

        val billingLimitAdapter : ArrayAdapter<*> = ArrayAdapter<Any?>(
            this, android.R.layout.simple_spinner_item, billingLimit
        )
        billingLimitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        et_billingLimit_value.adapter = billingLimitAdapter
    }

    private fun setCustomeNote(){
        val noteCategoryAdapter : ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,android.R.layout.simple_spinner_item,noteCategory
        )
        noteCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        et_custom_note_category_value.adapter = noteCategoryAdapter
    }

    private fun setInitalData() {
        updateProdEnvDetails()
        binding.etSurl.setText(surl)
        binding.etFurl.setText(furl)
        binding.etMerchantName.setText(merchantName)
        binding.etPhone.setText(phone)
        binding.etAmount.setText(amount)
        binding.etUserCredential.setText("${binding.etKey.text}:$email")
        binding.etSurePayCount.setText("0")
    }

    private fun initListeners() {
        binding.radioGrpEnv.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            when (i) {
                R.id.radioBtnTest -> updateTestEnvDetails()
                R.id.radioBtnProduction -> updateProdEnvDetails()
                else -> updateTestEnvDetails()
            }
        }

        binding.switchEnableReviewOrder.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
            if (b) showReviewOrderView() else hideReviewOrderView()
        }

        binding.btnAddItem.setOnClickListener { reviewOrderAdapter?.addRow() }
    }

    private fun hideReviewOrderView() {
        binding.rlReviewOrder.visibility = View.GONE
        reviewOrderAdapter = null
    }

    private fun showReviewOrderView() {
        binding.rlReviewOrder.visibility = View.VISIBLE
        reviewOrderAdapter = ReviewOrderRecyclerViewAdapter()
        binding.rvReviewOrder.layoutManager = LinearLayoutManager(this)
        binding.rvReviewOrder.adapter = reviewOrderAdapter
    }

    private fun updateTestEnvDetails() {
        //For testing
        binding.etKey.setText(testKey)
        binding.etSalt.setText(testSalt)
    }

    private fun updateProdEnvDetails() {
        //For Production
        binding.etKey.setText(prodKey)
        binding.etSalt.setText(prodSalt)
    }

    fun startPayment(view: View) {
        // Preventing multiple clicks, using threshold of 1 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        val paymentParams = preparePayUBizParams()
        initUiSdk(paymentParams)
    }

    fun preparePayUBizParams(): PayUPaymentParams {
        val vasForMobileSdkHash = HashGenerationUtils.generateHashFromSDK(
            "${binding.etKey.text}|${PayUCheckoutProConstants.CP_VAS_FOR_MOBILE_SDK}|${PayUCheckoutProConstants.CP_DEFAULT}|",
            binding.etSalt.text.toString()
        )
        val paymenRelatedDetailsHash = HashGenerationUtils.generateHashFromSDK(
            "${binding.etKey.text}|${PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK}|${binding.etUserCredential.text}|",
            binding.etSalt.text.toString()
        )

        val additionalParamsMap: HashMap<String, Any?> = HashMap()
        additionalParamsMap[PayUCheckoutProConstants.CP_UDF1] = "udf1"
        additionalParamsMap[PayUCheckoutProConstants.CP_UDF2] = "udf2"
        additionalParamsMap[PayUCheckoutProConstants.CP_UDF3] = "udf3"
        additionalParamsMap[PayUCheckoutProConstants.CP_UDF4] = "udf4"
        additionalParamsMap[PayUCheckoutProConstants.CP_UDF5] = "udf5"

        //Below params should be passed only when integrating Multi-currency support
        //TODO Please pass your own Merchant Access Key below as provided by your Key Account Manager at PayU.
//        additionalParamsMap[PayUCheckoutProConstants.CP_MERCHANT_ACCESS_KEY] = merchantAccessKey

        //Below param should be passed only when sodexo payment option is enabled and to show saved sodexo card
//        additionalParamsMap[PayUCheckoutProConstants.SODEXO_SOURCE_ID] = sodesosrcid  // merchant has to pass this

        //Below hashes are static hashes and can be calculated and passed in additional params
        additionalParamsMap[PayUCheckoutProConstants.CP_VAS_FOR_MOBILE_SDK] = vasForMobileSdkHash
        additionalParamsMap[PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK] =
            paymenRelatedDetailsHash

        var siDetails: PayUSIParams? =null
        if(switch_si_on_off.isChecked) {
            siDetails  = PayUSIParams.Builder()
                .setIsFreeTrial(sp_free_trial.isChecked)
                .setBillingAmount(et_billingAmount_value.text.toString())
                .setBillingCycle(PayUBillingCycle.valueOf(et_billingCycle_value.selectedItem.toString()))
                .setBillingInterval(et_billingInterval_value.text.toString().toInt())
                .setPaymentStartDate(et_paymentStartDate_value.text.toString())
                .setPaymentEndDate(et_paymentEndDate_value.text.toString())
                .setRemarks(et_remarks_value.text.toString())
                .setBillingLimit(PayuBillingLimit.valueOf(et_billingLimit_value.selectedItem.toString()))
                .setBillingRule(PayuBillingRule.valueOf(et_billingRule_value.selectedItem.toString()))
                .build()
        }

        var splitPaymentDetails : JSONObject? = null
        if (switchSplitPayment!!.isChecked) {
            val requestMap: HashMap<String, JSONObject> = HashMap()
            splitPaymentDetails = JSONObject()
            try {
                splitPaymentDetails.put(
                    PayuConstants.SPLIT_PAYMENT_TYPE,
                    spSplitPaymentType!!.selectedItem.toString()
                )
                val childCount = llSplitPaymentDetails!!.childCount
                for (i in 0 until childCount) {
                    val childAt = llSplitPaymentDetails!!.getChildAt(i)
                    val subAmt = childAt.findViewById<EditText>(R.id.et_sub_amt_value)
                    val commissionAmt =
                        childAt.findViewById<EditText>(R.id.et_aggregator_charges_value)
                    val childKey = childAt.findViewById<EditText>(R.id.et_child_key_value)
                    if (!subAmt.text.toString().isNullOrEmpty() && !commissionAmt.text.toString().isNullOrEmpty() ) {
                        val obj = JSONObject()
                        obj.put(PayuConstants.SPLIT_PAYMENT_AGGREGATOR_SUB_TXN_ID, "ab"+ (1 until 10000).random())
                        obj.put(
                            PayuConstants.SPLIT_PAYMENT_AGGREGATOR_SUB_AMOUNT,
                            subAmt.text.toString()
                        )
                        obj.put(
                            PayuConstants.SPLIT_PAYMENT_AGGREGATOR_CHARGES,
                            commissionAmt.text.toString()
                        )
                        requestMap[childKey.text.toString()] = obj
                    }
                }
                splitPaymentDetails.put(PayuConstants.SPLIT_PAYMENT_INFO, JSONObject(requestMap as Map<String, JSONObject>))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }


        return PayUPaymentParams.Builder().setAmount(binding.etAmount.text.toString())
            .setIsProduction(binding.radioBtnProduction.isChecked)
            .setKey(binding.etKey.text.toString())
            .setProductInfo("Macbook Pro")
            .setPhone(binding.etPhone.text.toString())
            .setTransactionId(System.currentTimeMillis().toString())
            .setFirstName("Abc")
            .setEmail(email)
            .setSurl(binding.etSurl.text.toString())
            .setFurl(binding.etFurl.text.toString())
            .setUserCredential(binding.etUserCredential.text.toString())
            .setAdditionalParams(additionalParamsMap)
            .setPayUSIParams(siDetails)
            .setSplitPaymentDetails(if(switchSplitPayment!!.isChecked) splitPaymentDetails.toString() else null)
                // pass here unique usertoken for user
            .setUserToken("anshul123")
            .build()
    }

    private fun initUiSdk(payUPaymentParams: PayUPaymentParams) {
        PayUCheckoutPro.open(
            this,
            payUPaymentParams,
            getCheckoutProConfig(),
            object : PayUCheckoutProListener {

                override fun onPaymentSuccess(response: Any) {
                    processResponse(response)
                }

                override fun onPaymentFailure(response: Any) {
                    processResponse(response)
                }

                override fun onPaymentCancel(isTxnInitiated: Boolean) {
                    showSnackBar(resources.getString(R.string.transaction_cancelled_by_user))
                }

                override fun onError(errorResponse: ErrorResponse) {

                    val errorMessage: String
                    if (errorResponse != null && errorResponse.errorMessage != null && errorResponse.errorMessage!!.isNotEmpty())
                        errorMessage = errorResponse.errorMessage!!
                    else
                        errorMessage = resources.getString(R.string.some_error_occurred)
                    showSnackBar(errorMessage)
                }

                override fun generateHash(
                    map: HashMap<String, String?>,
                    hashGenerationListener: PayUHashGenerationListener
                ) {
                    if (map.containsKey(CP_HASH_STRING)
                        && map.containsKey(CP_HASH_STRING) != null
                        && map.containsKey(CP_HASH_NAME)
                        && map.containsKey(CP_HASH_NAME) != null
                    ) {

                        val hashData = map[CP_HASH_STRING]
                        val hashName = map[CP_HASH_NAME]
                        val hashType = map[CP_HASH_TYPE]

                        var salt = binding.etSalt.text.toString()
                        if (map.containsKey(PayUCheckoutProConstants.CP_POST_SALT))
                            salt = salt.plus(map[PayUCheckoutProConstants.CP_POST_SALT])

                        var hash: String? = null


                        //Below hash should be calculated only when integrating Multi-currency support. If not integrating MCP
                        // then no need to have this if check.
                        if (hashName.equals(PayUCheckoutProConstants.CP_LOOKUP_API_HASH, ignoreCase = true)){

                            //Calculate HmacSHA1 hash using the hashData and merchant secret key
                            hash = HashGenerationUtils.generateHashFromSDK(
                                hashData!!,
                                salt,
                                merchantSecretKey
                            )
                        }else if (hashType.equals(CP_V2_HASH)){
                            hash = HashGenerationUtils.generateV2HashFromSDK(hashData!!,binding.etSalt.text.toString())
                        } else {
                            //calculate SDH-512 hash using hashData and salt
                            hash = HashGenerationUtils.generateHashFromSDK(
                                hashData!!,
                                salt
                            )
                        }

                        if (!TextUtils.isEmpty(hash)) {
                            val hashMap: HashMap<String, String?> = HashMap()
                            hashMap[hashName!!] = hash!!
                            hashGenerationListener.onHashGenerated(hashMap)
                        }
                    }
                }

                override fun setWebViewProperties(webView: WebView?, bank: Any?) {
                }
            })
    }

    private fun getCheckoutProConfig(): PayUCheckoutProConfig {
        val checkoutProConfig = PayUCheckoutProConfig()
        checkoutProConfig.paymentModesOrder = getCheckoutOrderList()
        checkoutProConfig.showCbToolbar = !binding.switchHideCbToolBar.isChecked
        checkoutProConfig.autoSelectOtp = binding.switchAutoSelectOtp.isChecked
        checkoutProConfig.autoApprove = binding.switchAutoApprove.isChecked
        checkoutProConfig.surePayCount = binding.etSurePayCount.text.toString().toInt()
        checkoutProConfig.cartDetails = reviewOrderAdapter?.getOrderDetailsList()
        checkoutProConfig.showExitConfirmationOnPaymentScreen =
            !binding.switchDiableCBDialog.isChecked
        checkoutProConfig.showExitConfirmationOnCheckoutScreen =
            !binding.switchDiableUiDialog.isChecked
        checkoutProConfig.merchantName = binding.etMerchantName.text.toString()
        checkoutProConfig.merchantLogo = R.drawable.merchant_logo
        checkoutProConfig.waitingTime = 3000
        checkoutProConfig.merchantResponseTimeout = 3000
        checkoutProConfig.customNoteDetails = getCustomeNoteDetails()
        // uncomment below code to perform enforcement
//        checkoutProConfig.enforcePaymentList = getEnforcePaymentList()
        return checkoutProConfig
    }

    private fun getEnforcePaymentList(): ArrayList<HashMap<String, String>> {
        val enforceList = ArrayList<HashMap<String,String>>()
        enforceList.add(HashMap<String,String>().apply {
            put(PayUCheckoutProConstants.CP_PAYMENT_TYPE, PaymentType.NB.name)
             put(PayUCheckoutProConstants.ENFORCED_IBIBOCODE,"AXIB")

        })
        enforceList.add(HashMap<String,String>().apply {
            put(PayUCheckoutProConstants.CP_PAYMENT_TYPE, PaymentType.CARD.name)
            put(PayUCheckoutProConstants.CP_CARD_TYPE, CardType.CC.name)
            put(PayUCheckoutProConstants.CP_CARD_SCHEME, CardScheme.MAST.name)
        })
        return enforceList
    }


    private fun getCheckoutOrderList(): ArrayList<PaymentMode> {
        val checkoutOrderList = ArrayList<PaymentMode>()
        if (binding.switchShowGooglePay.isChecked) checkoutOrderList.add(
            PaymentMode(
                PaymentType.UPI,
                PayUCheckoutProConstants.CP_GOOGLE_PAY
            )
        )
        if (binding.switchShowPhonePe.isChecked) checkoutOrderList.add(
            PaymentMode(
                PaymentType.WALLET,
                PayUCheckoutProConstants.CP_PHONEPE
            )
        )
        if (binding.switchShowPaytm.isChecked) checkoutOrderList.add(
            PaymentMode(
                PaymentType.WALLET,
                PayUCheckoutProConstants.CP_PAYTM
            )
        )
        return checkoutOrderList
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.clMain, message, Snackbar.LENGTH_LONG).show()
    }

    private fun processResponse(response: Any) {
        response as HashMap<*, *>
        Log.d(
            BaseApiLayerConstants.SDK_TAG,
            "payuResponse ; > " + response[PayUCheckoutProConstants.CP_PAYU_RESPONSE]
                    + ", merchantResponse : > " + response[PayUCheckoutProConstants.CP_MERCHANT_RESPONSE]
        )

        AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert)
            .setCancelable(false)
            .setMessage(
                "Payu's Data : " + response.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE) + "\n\n\n Merchant's Data: " + response.get(
                    PayUCheckoutProConstants.CP_MERCHANT_RESPONSE
                )
            )
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, cancelButton -> dialog.dismiss() }.show()
    }

    private fun getCustomeNoteDetails(): ArrayList<CustomNote>{
        val customNote = ArrayList<CustomNote>()

        if (!(et_custom_note_category_value.selectedItem.toString().equals("NULL") ||et_custom_note_category_value.selectedItem.toString().equals("COMMON")) ) {
            val noteCategory = ArrayList<PaymentType>().also {
                it.add(PaymentType.valueOf(et_custom_note_category_value.selectedItem.toString()))
            }
            customNote.add(CustomNote(et_custom_note_value.text.toString(),noteCategory))
//                .also {
//                it.custom_note = et_custom_note_value.text.toString()
//                it.custom_note_category = ArrayList<PaymentType>().also {
//                    it.add(PaymentType.valueOf(et_custom_note_category_value.selectedItem.toString()))
//                it.add(PaymentType.NB)
//                it.add(PaymentType.WALLET)
//                it.add(PaymentType.UPI)
//                it.add(PaymentType.EMI)
//                }
//            })
        }else if (et_custom_note_category_value.selectedItem.toString().equals("NULL")){
            customNote.add(CustomNote(et_custom_note_value.text.toString(),null))
        }else{
            val noteCategory = ArrayList<PaymentType>().also {
                it.add(PaymentType.CARD)
                it.add(PaymentType.NB)
                it.add(PaymentType.UPI)
                it.add(PaymentType.WALLET)
                it.add(PaymentType.EMI)
            }
            customNote.add(CustomNote(et_custom_note_value.text.toString(),noteCategory))
        }

        return customNote;
    }

}

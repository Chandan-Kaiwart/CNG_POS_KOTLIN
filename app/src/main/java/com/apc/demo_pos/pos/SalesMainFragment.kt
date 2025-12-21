package com.apc.demo_pos.pos

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apc.demo_pos.R
import com.apc.demo_pos.pos.adapter.SalesTransactionAdapter
import com.apc.demo_pos.pos.model.SaleTransaction
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.text.DecimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.isNotEmpty
import androidx.navigation.fragment.findNavController


class SalesMainFragment : Fragment() {

    // Views
    private lateinit var tvSubtitle: TextView
    private lateinit var tabAutoSales: TextView
    private lateinit var tabManualSales: TextView
    private lateinit var autoSalesContent: LinearLayout
    private lateinit var manualSalesContent: ScrollView
    private lateinit var bottomButtonsAuto: LinearLayout

    // Auto Sales Views
    private lateinit var progressBar: ProgressBar
    private lateinit var rvTransactions: RecyclerView
    private lateinit var btnBackToHome: Button
    private lateinit var btnConfirmSelection: Button

    // Manual Sales Views
    private lateinit var etSalesKg: EditText
    private lateinit var spinnerStation: Spinner
    private lateinit var spinnerDispenser: Spinner
    private lateinit var spinnerNozzle: Spinner
    private lateinit var btnPayPhonePe: Button
    private lateinit var tvGasRate: TextView
    private lateinit var tvQuantity: TextView
    private lateinit var tvTotalAmount: TextView

    private var isAutoSalesSelected = true
    private var transactionAdapter: SalesTransactionAdapter? = null
    private var selectedTransaction: SaleTransaction? = null

    // Gas rate constant
    private val GAS_RATE = 93.50
    private val decimalFormat = DecimalFormat("#,##0.00")

    // Default values
    private val dispenserId = "Dispenser1"
    private val nozzleId = "Side A"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sales_main_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupTabs()
        setupSpinners()
        setupButtons()
        setupQuantityCalculation()
        updateTabUI()
    }

    private fun initViews(root: View) {
        tvSubtitle = root.findViewById(R.id.tvSubtitle)
        tabAutoSales = root.findViewById(R.id.tabAutoSales)
        tabManualSales = root.findViewById(R.id.tabManualSales)
        autoSalesContent = root.findViewById(R.id.autoSalesContent)
        manualSalesContent = root.findViewById(R.id.manualSalesContent)
        bottomButtonsAuto = root.findViewById(R.id.bottomButtonsAuto)

        // Auto Sales
        progressBar = root.findViewById(R.id.progressBar)
        rvTransactions = root.findViewById(R.id.rvTransactions)
        btnBackToHome = root.findViewById(R.id.btnBackToHome)
        btnConfirmSelection = root.findViewById(R.id.btnConfirmSelection)

        // Manual Sales
        etSalesKg = root.findViewById(R.id.etSalesKg)
        spinnerStation = root.findViewById(R.id.spinnerStation)
        spinnerDispenser = root.findViewById(R.id.spinnerDispenser)
        spinnerNozzle = root.findViewById(R.id.spinnerNozzle)
        btnPayPhonePe = root.findViewById(R.id.btnPayPhonePe)
        tvGasRate = root.findViewById(R.id.tvGasRate)
        tvQuantity = root.findViewById(R.id.tvQuantity)
        tvTotalAmount = root.findViewById(R.id.tvTotalAmount)

        // Set initial gas rate
        tvGasRate.text = "₹${decimalFormat.format(GAS_RATE)}/kg"
    }

    private fun setupQuantityCalculation() {
        etSalesKg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateTotalAmount()
            }
        })
    }

    private fun calculateTotalAmount() {
        val quantityText = etSalesKg.text.toString().trim()

        if (quantityText.isNotEmpty()) {
            try {
                val quantity = quantityText.toDouble()
                val totalAmount = quantity * GAS_RATE

                tvQuantity.text = "${decimalFormat.format(quantity)} kg"
                tvTotalAmount.text = "₹${decimalFormat.format(totalAmount)}"

                validateManualForm()
            } catch (e: NumberFormatException) {
                tvQuantity.text = "0.00 kg"
                tvTotalAmount.text = "₹0.00"
            }
        } else {
            tvQuantity.text = "0.00 kg"
            tvTotalAmount.text = "₹0.00"
        }
    }

    private fun setupRecyclerView() {
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupTabs() {
        tabAutoSales.setOnClickListener {
            if (!isAutoSalesSelected) {
                isAutoSalesSelected = true
                updateTabUI()
            }
        }

        tabManualSales.setOnClickListener {
            if (isAutoSalesSelected) {
                isAutoSalesSelected = false
                updateTabUI()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateTabUI() {
        if (isAutoSalesSelected) {
            tabAutoSales.setBackgroundResource(R.drawable.tab_selected_background)
            tabAutoSales.setTextColor(resources.getColor(R.color.primary_blue, requireActivity().theme))
            tabAutoSales.setTypeface(null, Typeface.BOLD)

            tabManualSales.setBackgroundResource(R.drawable.tab_unselected_background)
            tabManualSales.setTextColor(resources.getColor(R.color.text_gray, requireActivity().theme))
            tabManualSales.setTypeface(null, Typeface.NORMAL)

            tvSubtitle.text = "Recent Transactions (Last 50)"
            autoSalesContent.visibility = View.VISIBLE
            manualSalesContent.visibility = View.GONE
            bottomButtonsAuto.visibility = View.VISIBLE

            fetchLatestSales()
        } else {
            tabManualSales.setBackgroundResource(R.drawable.tab_selected_background)
            tabManualSales.setTextColor(resources.getColor(R.color.primary_blue, requireActivity().theme))
            tabManualSales.setTypeface(null, Typeface.BOLD)

            tabAutoSales.setBackgroundResource(R.drawable.tab_unselected_background)
            tabAutoSales.setTextColor(resources.getColor(R.color.text_gray, requireActivity().theme))
            tabAutoSales.setTypeface(null, Typeface.NORMAL)

            tvSubtitle.text = getString(R.string.manual_sales_entry)
            autoSalesContent.visibility = View.GONE
            manualSalesContent.visibility = View.VISIBLE
            bottomButtonsAuto.visibility = View.GONE
        }
    }

    private fun fetchLatestSales() {
        progressBar.visibility = View.VISIBLE
        rvTransactions.visibility = View.GONE
        btnConfirmSelection.isEnabled = false
        btnConfirmSelection.setBackgroundResource(R.drawable.button_disabled_background)

        lifecycleScope.launch {
            try {
                val url = "https://www.cng-suvidha.in/CNGPortal/pos/fetchLatestSale.php?dispenser_id=$dispenserId&nozzle_id=$nozzleId&limit=50"
                Log.d("SalesMainFragment", "Fetching from URL: $url")

                val result = withContext(Dispatchers.IO) {
                    val response = URL(url).readText()
                    Log.d("SalesMainFragment", "API Response: $response")
                    parseJsonResponse(response)
                }

                withContext(Dispatchers.Main) {
                    Log.d("SalesMainFragment", "Parsed ${result.size} transactions")
                    if (result.isNotEmpty()) {
                        onTransactionsLoaded(result)
                    } else {
                        showNoDataState()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SalesMainFragment", "Error fetching data", e)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseJsonResponse(jsonString: String): List<SaleTransaction> {
        val transactions = mutableListOf<SaleTransaction>()

        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val transaction = SaleTransaction(
                    dispenserId = jsonObject.optString("Dispenser_Id", "N/A"),
                    nozzleId = jsonObject.optString("Nozzle_Id", "N/A"),
                    quantity = jsonObject.optString("Sale_Quantity", "0.0"),
                    amount = jsonObject.optString("Total_Amount", "0.0"),
                    date = jsonObject.optString("SaleDate", "N/A"),
                    time = "",
                    pricePerKg = jsonObject.optString("Gas_Rate", "93.50"),
                    orderId = jsonObject.optString("Manual_SaleID", null)
                )

                transactions.add(transaction)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return transactions
    }

    private fun onTransactionsLoaded(transactions: List<SaleTransaction>) {
        progressBar.visibility = View.GONE

        if (transactions.isNotEmpty()) {
            rvTransactions.visibility = View.VISIBLE

            transactionAdapter = SalesTransactionAdapter(transactions) { transaction ->
                selectedTransaction = transaction
                btnConfirmSelection.isEnabled = true
                btnConfirmSelection.setBackgroundResource(R.drawable.button_primary_background)
            }

            rvTransactions.adapter = transactionAdapter
            Toast.makeText(requireContext(), "Loaded ${transactions.size} transactions", Toast.LENGTH_SHORT).show()
        } else {
            showNoDataState()
        }
    }

    private fun showNoDataState() {
        progressBar.visibility = View.GONE
        rvTransactions.visibility = View.GONE
        Toast.makeText(requireContext(), "No recent transactions found", Toast.LENGTH_SHORT).show()
    }

    private fun setupSpinners() {
        val stations = arrayOf("Select Station", "RIICO")
        val stationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, stations)
        spinnerStation.adapter = stationAdapter
        spinnerStation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                validateManualForm()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val dispensers = arrayOf("Select Dispenser", "Dispenser1", "Dispenser2")
        val dispenserAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dispensers)
        spinnerDispenser.adapter = dispenserAdapter
        spinnerDispenser.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                validateManualForm()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val nozzles = arrayOf("Select Nozzle", "Side A", "Side B")
        val nozzleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, nozzles)
        spinnerNozzle.adapter = nozzleAdapter
        spinnerNozzle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.M)
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                validateManualForm()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun validateManualForm() {
        val salesKg = etSalesKg.text.toString().trim()
        val stationSelected = spinnerStation.selectedItemPosition > 0
        val dispenserSelected = spinnerDispenser.selectedItemPosition > 0
        val nozzleSelected = spinnerNozzle.selectedItemPosition > 0

        val isValid = salesKg.isNotEmpty() &&
                salesKg.toDoubleOrNull() != null &&
                salesKg.toDouble() > 0 &&
                stationSelected && dispenserSelected && nozzleSelected

        btnPayPhonePe.isEnabled = isValid
        if (isValid) {
            btnPayPhonePe.setBackgroundResource(R.drawable.button_primary_background)
            btnPayPhonePe.setTextColor(resources.getColor(android.R.color.white, requireActivity().theme))
        } else {
            btnPayPhonePe.setBackgroundResource(R.drawable.button_disabled_background)
            btnPayPhonePe.setTextColor(resources.getColor(R.color.text_gray, requireActivity().theme))
        }
    }

    private fun setupButtons() {
        btnBackToHome.setOnClickListener {
            requireActivity().finish()
        }

        // Auto Sales - Confirm Selection
        btnConfirmSelection.setOnClickListener {
            selectedTransaction?.let { transaction ->
                navigateToTransactionPreview(
                    transactionType = "Automatic",
                    dispenser = transaction.dispenserId,
                    nozzle = transaction.nozzleId,
                    quantity = transaction.quantity,
                    pricePerKg = transaction.pricePerKg,
                    isManual = false,
                    saleId = transaction.orderId ?: ""
                )
            }
        }

        // Manual Sales - Pay with PhonePe
        btnPayPhonePe.setOnClickListener {
            sendManualSaleAndNavigate()
        }
    }

    private fun sendManualSaleAndNavigate() {
        val quantity = etSalesKg.text.toString().toDoubleOrNull() ?: return
        val stationId = spinnerStation.selectedItem.toString()
        val dispenserSelected = spinnerDispenser.selectedItem.toString()
        val nozzleSelected = spinnerNozzle.selectedItem.toString()
        val totalAmount = quantity * GAS_RATE

        // Show loading
        btnPayPhonePe.isEnabled = false
        Toast.makeText(requireContext(), "Creating manual sale...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                val url = buildString {
                    append("https://www.cng-suvidha.in/CNGPortal/pos/sendSales.php?")
                    append("station_id=${URLEncoder.encode(stationId, "UTF-8")}")
                    append("&sales_in_kg=$quantity")
                    append("&dispenser_id=${URLEncoder.encode(dispenserSelected, "UTF-8")}")
                    append("&nozzle_id=${URLEncoder.encode(nozzleSelected, "UTF-8")}")
                    append("&price_per_quantity=$GAS_RATE")
                    append("&total_amount=$totalAmount")
                }

                Log.d("SalesMainFragment", "Sending manual sale: $url")

                val result = withContext(Dispatchers.IO) {
                    val response = URL(url).readText()
                    Log.d("SalesMainFragment", "Manual sale response: $response")
                    JSONObject(response)
                }

                withContext(Dispatchers.Main) {
                    val success = result.optBoolean("success", false)

                    if (success) {
                        val data = result.optJSONObject("data")
                        val saleId = data?.optString("id") ?: ""

                        Toast.makeText(requireContext(), "Sale created successfully!", Toast.LENGTH_SHORT).show()

                        navigateToTransactionPreview(
                            transactionType = "Manual",
                            dispenser = dispenserSelected,
                            nozzle = nozzleSelected,
                            quantity = quantity.toString(),
                            pricePerKg = GAS_RATE.toString(),
                            isManual = true,
                            saleId = saleId
                        )
                    } else {
                        val message = result.optString("message", "Failed to create sale")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        btnPayPhonePe.isEnabled = true
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SalesMainFragment", "Error sending manual sale", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnPayPhonePe.isEnabled = true
                }
            }
        }
    }

    private fun navigateToTransactionPreview(
        transactionType: String,
        dispenser: String,
        nozzle: String,
        quantity: String,
        pricePerKg: String,
        isManual: Boolean,
        saleId: String
    ) {
        val action = SalesMainFragmentDirections.actionSalesMainFragmentToTransactionPreviewFragment(
            transactionType,
            dispenser,
            nozzle,
            quantity,
            pricePerKg
        )
        findNavController().navigate(action)
    }
}



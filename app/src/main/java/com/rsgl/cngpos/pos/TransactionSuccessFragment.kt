package com.rsgl.cngpos.pos

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rsgl.cngpos.R
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.rsgl.cngpos.databinding.FragmentTransactionSuccessBinding

class TransactionSuccessFragment : Fragment() {

    private var _binding: FragmentTransactionSuccessBinding? = null
    private val binding get() = _binding!!

    private var orderId: String = ""
    private var transactionId: String = ""
    private var paymentMode: String = "UPI (PhonePe)"
    private var dateTime: String = ""
    private var dispenser: String = ""
    private var nozzle: String = ""
    private var fuelType: String = "CNG"
    private var quantity: Double = 0.0
    private var pricePerKg: Double = 0.0
    private var totalPaid: Double = 0.0

    companion object {
        private const val TAG = "TransactionSuccess"
        private const val ARG_ORDER_ID = "orderId"
        private const val ARG_TRANSACTION_ID = "transactionId"
        private const val ARG_PAYMENT_MODE = "paymentMode"
        private const val ARG_DATE_TIME = "dateTime"
        private const val ARG_DISPENSER = "dispenser"
        private const val ARG_NOZZLE = "nozzle"
        private const val ARG_FUEL_TYPE = "fuelType"
        private const val ARG_QUANTITY = "quantity"
        private const val ARG_PRICE_PER_KG = "pricePerKg"
        private const val ARG_TOTAL_PAID = "totalPaid"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDataFromArguments()
        displayTransactionDetails()
        setupButtons()
    }

    private fun loadDataFromArguments() {
        arguments?.let {
            orderId = it.getString(ARG_ORDER_ID, "")
            transactionId = it.getString(ARG_TRANSACTION_ID, "")
            paymentMode = it.getString(ARG_PAYMENT_MODE, "UPI (PhonePe)")
            dateTime = it.getString(ARG_DATE_TIME, "")
            dispenser = it.getString(ARG_DISPENSER, "")
            nozzle = it.getString(ARG_NOZZLE, "")
            fuelType = it.getString(ARG_FUEL_TYPE, "CNG")

            // ✅ FIX: Use getFloat() instead of getDouble()
            quantity = it.getFloat(ARG_QUANTITY, 0f).toDouble()
            pricePerKg = it.getFloat(ARG_PRICE_PER_KG, 0f).toDouble()
            totalPaid = it.getFloat(ARG_TOTAL_PAID, 0f).toDouble()
        }

        // If dateTime is empty, use current time
        if (dateTime.isEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
            dateTime = sdf.format(Date())
        }
    }

    private fun displayTransactionDetails() {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        binding.tvOrderId.text = orderId
        binding.tvTransactionId.text = transactionId
        binding.tvPaymentMode.text = paymentMode
        binding.tvDateTime.text = dateTime
        binding.tvDispenser.text = dispenser
        binding.tvNozzle.text = nozzle
        binding.tvFuelType.text = fuelType
        binding.tvQuantity.text = String.format("%.2f kg", quantity)
        binding.tvPricePerKg.text = currencyFormat.format(pricePerKg)
        binding.tvTotalPaid.text = currencyFormat.format(totalPaid)
    }

    private fun setupButtons() {
        binding.btnGeneratePdf.setOnClickListener {
            generatePdf()
        }

        binding.btnSkip.setOnClickListener {
            navigateToHome()
        }
    }

    private fun generatePdf() {
        Log.d(TAG, "========================================")
        Log.d(TAG, "📄 GENERATING PDF")
        Log.d(TAG, "========================================")

        binding.progressBar.visibility = View.VISIBLE
        binding.btnGeneratePdf.isEnabled = false

        try {
            // Create PDF document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas
            val paint = Paint()

            // Draw content on PDF
            var yPosition = 50f

            // Header - Station Name
            paint.textSize = 24f
            paint.color = Color.parseColor("#2E7D32") // Green
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("RIICO Station", pageInfo.pageWidth / 2f, yPosition, paint)

            yPosition += 40f
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Transaction Complete", pageInfo.pageWidth / 2f, yPosition, paint)

            yPosition += 60f

            // Transaction Details
            paint.textSize = 12f
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT

            drawLabelValue(canvas, paint, "Order ID:", orderId, 50f, yPosition)
            yPosition += 30f

            drawLabelValue(canvas, paint, "PhonePe Transaction ID:", transactionId, 50f, yPosition)
            yPosition += 30f

            drawLabelValue(canvas, paint, "Payment Mode:", paymentMode, 50f, yPosition)
            yPosition += 30f

            drawLabelValue(canvas, paint, "Date & Time:", dateTime, 50f, yPosition)
            yPosition += 30f

            drawLabelValue(canvas, paint, "Dispenser:", dispenser, 50f, yPosition)
            yPosition += 30f

            drawLabelValue(canvas, paint, "Nozzle:", nozzle, 50f, yPosition)
            yPosition += 30f

            drawLabelValue(canvas, paint, "Fuel Type:", fuelType, 50f, yPosition)
            yPosition += 30f

            drawLabelValue(canvas, paint, "Quantity (kg):", String.format("%.2f kg", quantity), 50f, yPosition)
            yPosition += 30f

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            drawLabelValue(canvas, paint, "Price per kg:", currencyFormat.format(pricePerKg), 50f, yPosition)
            yPosition += 50f

            // Total Paid - Highlighted
            paint.textSize = 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#2E7D32")
            drawLabelValue(canvas, paint, "Total Paid:", currencyFormat.format(totalPaid), 50f, yPosition)

            yPosition += 60f

            // Footer
            paint.textSize = 10f
            paint.color = Color.GRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Thank you for your business!", pageInfo.pageWidth / 2f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Powered by RSGL CNG POS System", pageInfo.pageWidth / 2f, yPosition, paint)

            pdfDocument.finishPage(page)

            // Save PDF to file
            val fileName = "Transaction_${orderId}_${System.currentTimeMillis()}.pdf"
            val file = savePdfToFile(pdfDocument, fileName)

            pdfDocument.close()

            if (file != null) {
                Log.d(TAG, "✅ PDF generated successfully: ${file.absolutePath}")
                binding.progressBar.visibility = View.GONE
                binding.btnGeneratePdf.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "PDF generated successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Open PDF
                openPdf(file)

                // Navigate to home after showing PDF
                navigateToHome()
            } else {
                throw Exception("Failed to save PDF file")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating PDF: ${e.message}", e)
            binding.progressBar.visibility = View.GONE
            binding.btnGeneratePdf.isEnabled = true

            Toast.makeText(
                requireContext(),
                "Error generating PDF: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun drawLabelValue(canvas: Canvas, paint: Paint, label: String, value: String, x: Float, y: Float) {
        val originalColor = paint.color
        val originalTypeface = paint.typeface

        // Draw label
        paint.color = Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(label, x, y, paint)

        // Draw value
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, 545f, y, paint) // Right aligned

        // Reset
        paint.color = originalColor
        paint.typeface = originalTypeface
        paint.textAlign = Paint.Align.LEFT
    }

    private fun savePdfToFile(pdfDocument: PdfDocument, fileName: String): File? {
        return try {
            // Save to Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val pdfDir = File(downloadsDir, "CNGPOS_Receipts")

            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }

            val file = File(pdfDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()

            Log.d(TAG, "PDF saved to: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving PDF: ${e.message}", e)
            null
        }
    }

    private fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
            }

            val chooser = Intent.createChooser(intent, "Open PDF with")
            startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening PDF: ${e.message}", e)
            Toast.makeText(
                requireContext(),
                "Please check Downloads/CNGPOS_Receipts folder",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun navigateToHome() {
        Log.d(TAG, "Navigating to home screen")
        // Navigate back to sales list or home
        findNavController().navigate(R.id.action_transactionSuccess_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
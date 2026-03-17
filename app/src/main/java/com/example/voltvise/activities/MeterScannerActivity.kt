package com.example.voltvise.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.voltvise.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MeterScannerActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var detectedNumber: TextView
    private lateinit var confirmBtn: Button
    private lateinit var cancelBtn: Button
    private lateinit var statusText: TextView

    // ── Scanning state ─────────────────────────────────────────
    private var lastDetectedReading = ""
    private var confirmedReading    = ""
    private var isScanning          = true

    private var lastAnalyzedTime    = 0L
    private val ANALYSIS_INTERVAL_MS = 1200L

    // Rolling window — last 6 detections only
    private val recentDetections = mutableListOf<String>()
    private val WINDOW_SIZE      = 6
    private val STABLE_THRESHOLD = 3   // must appear 3 times in last 6

    // Reset every 8 seconds to avoid stale locks
    private val resetHandler   = Handler(Looper.getMainLooper())
    private val RESET_INTERVAL = 8000L
    private val resetRunnable  = object : Runnable {
        override fun run() {
            if (confirmedReading.isEmpty()) {
                recentDetections.clear()
                lastDetectedReading = ""
                runOnUiThread {
                    detectedNumber.text = "Scanning..."
                    detectedNumber.setTextColor(0xFFFFFFFF.toInt())
                    statusText.text = "Searching for meter display..."
                }
                resetHandler.postDelayed(this, RESET_INTERVAL)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meter_scanner)

        previewView    = findViewById(R.id.cameraPreview)
        detectedNumber = findViewById(R.id.detectedNumber)
        confirmBtn     = findViewById(R.id.confirmBtn)
        cancelBtn      = findViewById(R.id.cancelScanBtn)
        statusText     = findViewById(R.id.scanInstruction)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE
            )
        }

        // Start periodic reset
        resetHandler.postDelayed(resetRunnable, RESET_INTERVAL)

        confirmBtn.setOnClickListener {
            val reading = confirmedReading.ifEmpty { lastDetectedReading }
            if (reading.isEmpty()) {
                Toast.makeText(this, "No reading detected yet — keep camera steady", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendResult(reading)
        }

        cancelBtn.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun sendResult(reading: String) {
        isScanning = false
        resetHandler.removeCallbacks(resetRunnable)
        val intent = Intent()
        intent.putExtra("meter_value", reading)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also {
                    it.setAnalyzer(cameraExecutor, MeterTextAnalyzer())
                }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("MeterScanner", "Camera binding failed: ${e.message}")
                Toast.makeText(this, "Camera failed to start", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // ── Analyzer ───────────────────────────────────────────────
    @androidx.camera.core.ExperimentalGetImage
    inner class MeterTextAnalyzer : ImageAnalysis.Analyzer {
        private val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        override fun analyze(imageProxy: ImageProxy) {
            if (!isScanning) { imageProxy.close(); return }

            val now = System.currentTimeMillis()
            if (now - lastAnalyzedTime < ANALYSIS_INTERVAL_MS) {
                imageProxy.close(); return
            }
            lastAnalyzedTime = now

            val mediaImage = imageProxy.image
            if (mediaImage == null) { imageProxy.close(); return }

            val image = InputImage.fromMediaImage(
                mediaImage, imageProxy.imageInfo.rotationDegrees
            )

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val best = extractBestCandidate(visionText)

                    if (best != null) {
                        // Add to rolling window
                        recentDetections.add(best)
                        if (recentDetections.size > WINDOW_SIZE) {
                            recentDetections.removeAt(0) // drop oldest
                        }

                        // Count frequency in current window only
                        val freq = recentDetections.count { it == best }
                        lastDetectedReading = best

                        runOnUiThread {
                            when {
                                freq >= STABLE_THRESHOLD -> {
                                    // Locked on — auto confirm
                                    confirmedReading = best
                                    detectedNumber.text = "✅ $best"
                                    detectedNumber.setTextColor(0xFF00FF88.toInt())
                                    statusText.text = "Reading locked! Tap confirm or keep scanning."
                                }
                                freq == 2 -> {
                                    detectedNumber.text = "🔍 $best (verifying...)"
                                    detectedNumber.setTextColor(0xFFFFAA00.toInt())
                                    statusText.text = "Almost there — hold steady..."
                                }
                                else -> {
                                    detectedNumber.text = "🔍 $best"
                                    detectedNumber.setTextColor(0xFFFFFFFF.toInt())
                                    statusText.text = "Point at the kWh number on your meter"
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            if (confirmedReading.isEmpty()) {
                                detectedNumber.text = "Scanning..."
                                detectedNumber.setTextColor(0xFFFFFFFF.toInt())
                                statusText.text = "Point at the kWh number on your meter"
                            }
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }

    // ── Smart extraction ───────────────────────────────────────
    private fun extractBestCandidate(
        visionText: com.google.mlkit.vision.text.Text
    ): String? {

        data class Candidate(val digits: String, var score: Int)
        val candidates = mutableListOf<Candidate>()

        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                // Fix common OCR errors on 7-segment displays
                val cleaned = line.text.trim()
                    .replace('O', '0').replace('o', '0')
                    .replace('I', '1').replace('l', '1')
                    .replace('S', '5').replace('B', '8')
                    .replace('Z', '2').replace('G', '6')

                val matches = Regex("\\d{4,8}").findAll(cleaned)

                for (match in matches) {
                    val digits = match.value
                    var score  = 0

                    // ── Prefer 5-6 digit meter readings ──
                    score += when (digits.length) {
                        5, 6 -> 12
                        4, 7 -> 8
                        8    -> 4
                        else -> 0
                    }

                    // ── Bonus: line is mostly digits ──
                    val letterCount = cleaned.count { it.isLetter() }
                    val digitCount  = cleaned.count { it.isDigit() }
                    if (digitCount > letterCount) score += 6

                    // ── Penalty: contains kW / V / A labels (not the meter total) ──
                    val upper = cleaned.uppercase()
                    if (upper.contains("KW") || upper.contains(" W"))  score -= 10
                    if (upper.contains("VOLT") || upper.contains(" V")) score -= 10
                    if (upper.contains("AMP")  || upper.contains(" A")) score -= 10
                    if (upper.contains("HZ"))                           score -= 10
                    if (upper.contains("VAR"))                          score -= 10

                    // ── Penalty: numbers that are clearly NOT meter readings ──
                    val numVal = digits.toIntOrNull() ?: 0
                    // Voltages (200-250), frequency (50,60), short codes
                    if (numVal in 200..250 && digits.length <= 3)  score -= 15
                    if (numVal == 50 || numVal == 60)               score -= 15
                    // Serial numbers are usually 8+ digits
                    if (digits.length >= 8)                         score -= 5

                    // ── Bonus: starts with 0 (typical meter rollover like 00234) ──
                    if (digits.startsWith("0"))                     score += 3

                    if (score > 0) candidates.add(Candidate(digits, score))
                }
            }
        }

        if (candidates.isEmpty()) return null

        // Return highest scored candidate
        return candidates.maxByOrNull { it.score }?.digits
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resetHandler.removeCallbacks(resetRunnable)
        cameraExecutor.shutdown()
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 200
    }
}
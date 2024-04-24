package com.aarafrao.pdflib

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 100
    private lateinit var bitmap: Bitmap
    private val fileName = "my_pdf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn: Button = findViewById(R.id.btn)
        val image: ImageView = findViewById(R.id.image)

        btn.setOnClickListener {
            val constraintLayout: ConstraintLayout = findViewById(R.id.container)
            bitmap = captureScreenShot(constraintLayout)
            image.setImageBitmap(bitmap)

            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE_WRITE_EXTERNAL_STORAGE
            )
        } else {
            // Permission already granted, proceed with file creation
            convertBitmapToPdf()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file creation
                convertBitmapToPdf()
            } else {
                // Permission denied, show a message or take appropriate action
                Toast.makeText(
                    this,
                    "Permission denied to write to external storage",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun convertBitmapToPdf() {
        // Create a new PdfDocument instance
        val pdfDocument = PdfDocument()

        // Create a PageInfo object specifying the page attributes
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()

        // Start a new page
        val page = pdfDocument.startPage(pageInfo)

        // Draw the bitmap onto the page canvas
        val canvas = page.canvas
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // Finish the page
        pdfDocument.finishPage(page)

        // Save the PDF document
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "$fileName.pdf")
        try {
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()
            Toast.makeText(this, "Exported!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Not Exported!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureScreenShot(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }
}

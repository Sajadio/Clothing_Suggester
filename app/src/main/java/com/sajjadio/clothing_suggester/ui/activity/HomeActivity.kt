package com.sajjadio.clothing_suggester.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sajjadio.clothing_suggester.data.local.SharedPref
import com.sajjadio.clothing_suggester.data.local.SharedPrefImpl
import com.sajjadio.clothing_suggester.data.model.Daily
import com.sajjadio.clothing_suggester.data.model.WeatherResponse
import com.sajjadio.clothing_suggester.data.remote.ApiServiceImpl
import com.sajjadio.clothing_suggester.data.remote.ApiService
import com.sajjadio.clothing_suggester.ui.presenter.WeatherPresenter
import com.sajjadio.clothing_suggester.ui.presenter.WeatherView
import com.sajjadio.clothing_suggester.databinding.ActivityHomeBinding
import com.sajjadio.clothing_suggester.databinding.WeatherStatusBottomSheetBinding
import com.sajjadio.clothing_suggester.ui.adapter.OnItemClickListener
import com.sajjadio.clothing_suggester.ui.adapter.ParentAdapter
import com.sajjadio.clothing_suggester.utils.ParentItem
import com.vmadalin.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class HomeActivity : AppCompatActivity(), WeatherView, OnItemClickListener {

    companion object {
        private const val GALLERY_REQUEST_CODE = 1001
        private const val TAG = "JustMe"
        private const val COLD = "Cold"
        private const val HOT = "Hot"
        private const val MODERATE = "Moderate"
    }

    private val rangeColdTemp = 0..20
    private val rangeModerateTemp = 20..30
    private val rangeHotTemp = 30..60

    private lateinit var binding: ActivityHomeBinding
    private lateinit var apiService: ApiService
    private lateinit var presenter: WeatherPresenter
    private lateinit var sharedPref: SharedPref
    private lateinit var dialog: BottomSheetDialog
    private var selectedWeatherStatus = ""
    private var weatherStatus = ""
    private lateinit var adapter: ParentAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialInjection()
        sharedPref = SharedPrefImpl(applicationContext)
        requestReadExternalStoragePermission()
        setupParentAdapter()
    }

    private fun setupParentAdapter() {
        adapter = ParentAdapter(this)
        binding.recyclerViewParent.adapter = adapter
    }


    private fun showFilterSheet() {
        val bottomSheetBinding = WeatherStatusBottomSheetBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this)

        bottomSheetBinding.apply {
            checkStatusRadioButton(this)
        }

        dialog.apply {
            setContentView(bottomSheetBinding.root)
            show()
        }
    }

    private fun checkStatusRadioButton(imageBottomSheetBinding: WeatherStatusBottomSheetBinding) {
        with(imageBottomSheetBinding) {
            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                val radioButton: RadioButton = group.findViewById(checkedId)
                if (radioButton.isChecked) {
                    openGallery()
                    selectedWeatherStatus = radioButton.text.toString()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun openGallery() {
        if (requestReadExternalStoragePermission()) {
            pickPhoto()
        }
    }

    private fun initialInjection() {
        apiService = ApiServiceImpl()
        presenter = WeatherPresenter(this, apiService)
        presenter.getCurrentWeatherResponse()
    }

    override fun getCurrentWeatherResponse(weatherResponse: WeatherResponse) {
        runOnUiThread {
            adapter.addNestedItem(ParentItem.CurrentWeather(weatherResponse))
        }
    }

    override fun getDailyWeatherResponse(daysWeather: List<Daily>) {
        runOnUiThread {
            adapter.addNestedItem(ParentItem.DailyWeather(daysWeather))
        }
    }

    private fun checkWeatherStatus(temp: Int) {
        when (temp) {
            in rangeColdTemp -> weatherStatus = COLD
            in rangeModerateTemp -> weatherStatus = MODERATE
            in rangeHotTemp -> weatherStatus = HOT
        }
    }

    private val photoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data!!.clipData?.let {
                    val count = it.itemCount
                    for (i in 0 until count) {
                        saveImageToSharedPref(it.getItemAt(i).uri)
                    }
                }
            }
        }

    @SuppressLint("Recycle")
    private fun saveImageToSharedPref(imageUri: Uri) {
        val images = Pair(selectedWeatherStatus, encodeImageAsBase64(imageUri).toString())
        sharedPref.saveImage(images)
    }

    @SuppressLint("Recycle")
    fun encodeImageAsBase64(uri: Uri): String? {
        var inputStream: InputStream? = null
        var encodedImageString: String? = null
        try {
            inputStream = contentResolver.openInputStream(uri)
            val imageBytes = inputStream?.readBytes()
            encodedImageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: IOException) {
            Log.d(TAG, "readImageBytes: ${e.message}")
        } finally {
            inputStream?.close()
        }
        return encodedImageString
    }

    private fun decodeBase64ToBitmap(encodedImage: String): Bitmap? {
        val imageBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }


    private fun requestReadExternalStoragePermission(): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        return if (EasyPermissions.hasPermissions(this, permission)) {
            true
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Please grant the Read External Storage permission to access photos",
                GALLERY_REQUEST_CODE,
                permission
            )
            false
        }
    }

    private fun pickPhoto() {
        Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            it.type = "image/*"
            photoResultLauncher.launch(it)
        }
    }

    override fun getCurrentDayTemp(temp: Int) {
        checkWeatherStatus(temp)
    }

    override fun addImage() {
        showFilterSheet()
    }

    private var listBitmap = mutableListOf<Bitmap>()
    override fun refreshSuggesterImage(): Bitmap? {
        val images = sharedPref.getImage(weatherStatus)
        if (images.isNotEmpty()) {
            sharedPref.getImage(weatherStatus).map { decodeBase64ToBitmap(it) }.forEach {
                it?.let {
                    listBitmap.add(it)
                }
            }
            val selectedImage = sharedPref.getSelectedImage(bitmapToString(listBitmap.random()))
            listBitmap.remove(decodeBase64ToBitmap(selectedImage))
            return listBitmap.random()
        }
        return null
    }

    override fun addSelectedImage(bitmap: Bitmap) {
        sharedPref.saveSelectedImage(bitmapToString(bitmap))
        Toast.makeText(this, "Selected", Toast.LENGTH_SHORT).show()
    }

    private fun bitmapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}
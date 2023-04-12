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
                result.data!!.data?.let { getPhotoResultIntent(it) }
            }
        }

    @SuppressLint("Recycle")
    private fun getPhotoResultIntent(imageUri: Uri) {
        val inputStream = contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()
        val encodedImageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        val images = Pair(selectedWeatherStatus, encodedImageString.toString())
        sharedPref.addImage(images)
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
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            it.type = "image/*"
            photoResultLauncher.launch(it)
        }
    }

    override fun getCurrentDayTemp(temp: Int) {
        checkWeatherStatus(temp)
    }

    override fun onAddImage() {
        showFilterSheet()
    }

    override fun onRefreshSuggesterImage(): Bitmap? {
        val encodedImageString = sharedPref.getImage(weatherStatus)?.random()
        val imageBytes = Base64.decode(encodedImageString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

}
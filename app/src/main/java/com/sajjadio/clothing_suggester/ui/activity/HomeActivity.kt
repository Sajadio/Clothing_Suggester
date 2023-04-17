package com.sajjadio.clothing_suggester.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sajjadio.clothing_suggester.R
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
import com.sajjadio.clothing_suggester.utils.mapStringToBitmap
import com.sajjadio.clothing_suggester.utils.mapUriToString
import com.vmadalin.easypermissions.BuildConfig
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
    private lateinit var dialog: BottomSheetDialog
    private lateinit var adapter: ParentAdapter
    private val images = mutableListOf<String>()
    private var selectedWeatherStatus = ""
    private var weatherStatus = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSwipeRefresh()
        setup()

    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            isRefreshing = true
            setOnRefreshListener {
                setup()
            }
        }
    }

    private fun setup() {
        initialInjection()
        requestReadExternalStoragePermission()
        setupParentAdapter()
    }

    private fun initialInjection() {
        apiService = ApiServiceImpl()
        presenter = WeatherPresenter(this, apiService, SharedPrefImpl(applicationContext))
        presenter.loadData()
    }

    private fun requestReadExternalStoragePermission(): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        return if (EasyPermissions.hasPermissions(this, permission)) {
            true
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.request_permission),
                GALLERY_REQUEST_CODE,
                permission
            )
            false
        }
    }

    private fun setupParentAdapter() {
        adapter = ParentAdapter(this)
        binding.recyclerViewParent.adapter = adapter
    }

    override fun addImage() {
        showFilterSheet()
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

    private fun pickPhoto() {
        Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            it.type = "image/*"
            photoResultLauncher.launch(it)
        }
    }

    private val photoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data!!.clipData?.let {
                    val count = it.itemCount
                    for (i in 0 until count) {
                        val uri = it.getItemAt(i).uri
                        saveImageToSharedPref(Pair(selectedWeatherStatus, mapUriToString(uri)))
                    }
                }
            }
        }

    private fun saveImageToSharedPref(images: Pair<String, String>) {
        presenter.saveImage(images)
    }

    override fun showImage(images: List<String>) {
        this.images.addAll(images)
    }

    override fun loadDataCurrentWeatherResponse(weatherResponse: WeatherResponse) {
        runOnUiThread {
            adapter.addNestedItem(ParentItem.CurrentWeather(weatherResponse))
        }
    }

    override fun loadDataDailyWeatherResponse(daysWeather: List<Daily>) {
        runOnUiThread {
            adapter.addNestedItem(ParentItem.DailyWeather(daysWeather))
        }
    }

    override fun checkResponseCurrentWeather(isSuccess: Boolean) {
        runOnUiThread {
            binding.apply {
                swipeRefresh.isRefreshing = !isSuccess
                recyclerViewParent.isVisible = isSuccess
            }
        }
    }

    override fun checkResponseDailyWeather(isSuccess: Boolean) {
        runOnUiThread {
            binding.swipeRefresh.isRefreshing = !isSuccess
        }

    }

    override fun getCurrentDayTemp(temp: Int) {
        checkWeatherStatus(temp)
    }

    private fun checkWeatherStatus(temp: Int) {
        when (temp) {
            in rangeColdTemp -> weatherStatus = COLD
            in rangeModerateTemp -> weatherStatus = MODERATE
            in rangeHotTemp -> weatherStatus = HOT
        }
    }

    override fun getRandomSuggestionImage(): Bitmap? {
        presenter.showImage(key = weatherStatus)
        return images.filter { it.isNotEmpty() }
            .randomOrNull()?.mapStringToBitmap()
    }
}
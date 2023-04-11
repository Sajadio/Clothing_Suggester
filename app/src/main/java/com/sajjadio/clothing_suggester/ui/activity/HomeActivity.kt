package com.sajjadio.clothing_suggester.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
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
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sajjadio.clothing_suggester.data.RepositoryImpl
import com.sajjadio.clothing_suggester.data.local.SharedPref
import com.sajjadio.clothing_suggester.data.local.SharedPrefImpl
import com.sajjadio.clothing_suggester.data.remote.ApiServiceImpl
import com.sajjadio.clothingsuggester.domain.Repository
import com.sajjadio.clothing_suggester.data.remote.ApiService
import com.sajjadio.clothing_suggester.domain.model.WeatherResponse
import com.sajjadio.clothing_suggester.ui.presenter.WeatherPresenter
import com.sajjadio.clothing_suggester.ui.presenter.WeatherView
import com.sajjadio.clothing_suggester.databinding.ActivityHomeBinding
import com.sajjadio.clothing_suggester.databinding.ImageBottomSheetBinding
import com.vmadalin.easypermissions.EasyPermissions

class HomeActivity : AppCompatActivity(), WeatherView {

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
    private lateinit var repository: Repository
    private lateinit var presenter: WeatherPresenter
    private lateinit var sharedPref: SharedPref
    private lateinit var dialog: BottomSheetDialog
    private var selectedWeatherStatus = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialInjection()
        sharedPref = SharedPrefImpl(applicationContext)
        requestReadExternalStoragePermission()

        binding.buttonAddImage.setOnClickListener {
            showFilterSheet()
        }
    }


    private fun showFilterSheet() {
        val bottomSheetBinding = ImageBottomSheetBinding.inflate(layoutInflater)
        dialog = BottomSheetDialog(this)

        bottomSheetBinding.apply {
            checkStatusRadioButton(this)
        }

        dialog.apply {
            setContentView(bottomSheetBinding.root)
            show()
        }
    }

    private fun checkStatusRadioButton(imageBottomSheetBinding: ImageBottomSheetBinding) {
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
        repository = RepositoryImpl(apiService)
        presenter = WeatherPresenter(this, repository)
        presenter.getWeatherResponse()
    }

    override fun getWeatherResponse(weatherResponse: WeatherResponse) {
        val temp = weatherResponse.main.temp.minus(273).toInt()
        runOnUiThread {
            binding.textViewTemp.text = "$temp°"
        }
        checkWeatherStatus(temp)
    }

    private fun checkWeatherStatus(temp: Int) {
        when (temp) {
            in rangeColdTemp -> displayImageDependOnWeatherStatus(COLD)
            in rangeModerateTemp -> displayImageDependOnWeatherStatus(MODERATE)
            in rangeHotTemp -> displayImageDependOnWeatherStatus(HOT)
        }
    }

    private fun displayImageDependOnWeatherStatus(weatherStatus: String) {
        val encodedImageString = sharedPref.getImage(weatherStatus)?.random()
        val imageBytes = Base64.decode(encodedImageString, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        runOnUiThread {
            binding.imageViewCloths.setImageBitmap(bitmap)
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

}
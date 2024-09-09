package com.example.licenseplaterec
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class MainActivity : ComponentActivity() {

    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var textRecognizer: TextRecognizer
    lateinit var imageReader: ImageReader
    lateinit var licensePlate: TextView
    lateinit var fuelType : TextView
    lateinit var imageView : ImageView

    private var torchOn = false
    private var jsonString = ""
    private val requestPermission =  registerForActivityResult(ActivityResultContracts.RequestPermission()){
        isGranted ->
            if(isGranted){
                Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "not ok", Toast.LENGTH_SHORT).show()
            }
    }

    private val ORIENTATIONS = SparseIntArray()
    private val RequestUrl = "https://opendata.rdw.nl/resource/8ys7-d773.json?kenteken="

    var p = HistoryInfo("","")
    var arrayofP = ArrayList<HistoryInfo>()
    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_blank)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        textureView = findViewById(R.id.textureView)
        licensePlate = findViewById(R.id.textView)
        fuelType = findViewById(R.id.textView2)
        imageView = findViewById(R.id.imageView2)
        mainApp()

    }

    override fun onResume() {
        super.onResume()
        enableEdgeToEdge()
        setContentView(R.layout.fragment_blank)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        textureView = findViewById(R.id.textureView)
        licensePlate = findViewById(R.id.textView)
        fuelType = findViewById(R.id.textView2)
        imageView = findViewById(R.id.imageView2)
        mainApp()
    }

    private fun processImage(img : Image){
        textRecognizer.process(img!!,0)
            .addOnSuccessListener { text ->
                val Num = findPlateNum(text)
                Log.i("INFO",Num)
                Toast.makeText(this@MainActivity, Num, Toast.LENGTH_SHORT).show()
                if(Num == "not found") return@addOnSuccessListener
                RequestPlateInfo(Num)
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }
    }
    private fun RequestPlateInfo(PlateNum : String){
        val PlateUrl = RequestUrl.plus(PlateNum)

        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(PlateUrl, { response ->
            if (response == "[]\n") return@StringRequest
            jsonString = response

            val typeToken = object : TypeToken<List<CarInfo>>() {}.type
            val carData = Gson().fromJson<List<CarInfo>>(jsonString, typeToken)
            licensePlate.text = "Kenteken: " + carData[0].kenteken
            var fuelString = "Brandstof Type: "
            for(data in carData){
                fuelString += data.brandstofOmschrijving + " / "
            }
            var canpark = false;
            fuelString = fuelString.removeRange(fuelString.length - 3, fuelString.length - 1)
            if(fuelString == "Brandstof Type: Benzine / Elektriciteit ") {
                imageView.setImageResource(R.drawable.checkmark)
                canpark = true
            } else if(fuelString == "Brandstof Type: Benzine ") {
                imageView.setImageResource(R.drawable.cross)
                canpark = false
            }else if(fuelString == "Brandstof Type: Elektriciteit / Benzine ") {
                imageView.setImageResource(R.drawable.checkmark)
                canpark = false
            } else if(fuelString == "Brandstof Type: Elektriciteit ") {
                imageView.setImageResource(R.drawable.checkmark)
                canpark = true
            } else {
                imageView.setImageResource(R.drawable.cross)
                canpark = false
            }

            fuelType.text = fuelString

            p = HistoryInfo(licensePlate.text.toString(), fuelType.text.toString(), canpark)
            arrayofP += p
                                                    },
            { licensePlate.text = "Kenteken: Not Found" })
        queue.add(stringRequest)
    }

    private fun findPlateNum(text: Text): String {
        for (block in text.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    var plateNum = element.text
                    if(plateNum.contains('-')){
                        plateNum = plateNum.replace("-","")
                        if(plateNum.length == 6){
                            return plateNum
                        }
                    }
                }
            }
        }
        return "not found"
    }

    @SuppressLint("MissingPermission")
    private fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                val surface = Surface(textureView.surfaceTexture)
                capReq.addTarget(surface)
                capReq.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)

                cameraDevice.createCaptureSession(listOf(surface, imageReader.surface), object: CameraCaptureSession.StateCallback(){
                    override fun onConfigured(p0: CameraCaptureSession) {
                        cameraCaptureSession = p0
                        cameraCaptureSession.setRepeatingRequest(capReq.build(),null,null)

                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        TODO("Not yet implemented")
                    }
                },handler )
            }

            override fun onDisconnected(p0: CameraDevice) {
                p0.close()
            }

            override fun onError(p0: CameraDevice, p1: Int) {
                p0.close()
            }
        },handler)
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            return false
        }
        return true
    }

    private fun requestPermission() {
        requestPermission.launch(android.Manifest.permission.CAMERA)
    }

    private fun mainApp(){
        if (checkPermission()) {
            textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            handlerThread = HandlerThread("videoThread")
            handlerThread.start()
            handler = Handler((handlerThread).looper)

            textureView.surfaceTextureListener = object: TextureView.SurfaceTextureListener{
                override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                    open_camera()
                }

                override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

                }

                override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                }

            }


        } else {
            requestPermission()
        }

        imageReader = ImageReader.newInstance(1080,1920,ImageFormat.JPEG,1)
        imageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(p0: ImageReader?) {
                val image = p0?.acquireLatestImage()
                processImage(image!!)
                image.close()
            }

        },handler)

        findViewById<Button>(R.id.capture).apply {
            setOnClickListener() {
                capReq = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                capReq.addTarget(imageReader.surface)
                cameraCaptureSession.capture(capReq.build(),null,null)
            }
        }
        findViewById<Button>(R.id.button).setOnClickListener{
            if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
                if (torchOn == false) {
                    capReq.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                    torchOn = true
                    cameraCaptureSession.setRepeatingRequest(capReq.build(),null,null)
                } else {
                    capReq.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                    torchOn = false
                    cameraCaptureSession.setRepeatingRequest(capReq.build(),null,null)
                }
            }
        }
        findViewById<Button>(R.id.history).setOnClickListener {
            val res = Gson().toJson(arrayofP)
            if(res == "[]") {
                Toast.makeText(this@MainActivity, "No History", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val i = Intent(this@MainActivity, history::class.java)
            i.putExtra("ResString",res)
            startActivity(i)
        }
    }
}


package kr.co.uxn.agms_p_a2rt.ui.components.ready

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.uxn.agms_p_a2rt.R
import kr.co.uxn.agms_p_a2rt.api.RetrofitClient.tokenRetrofit
import kr.co.uxn.agms_p_a2rt.api.token.DataStoreManager
import kr.co.uxn.agms_p_a2rt.ble.BleUtils.STATUS_BLE_ENABLED
import kr.co.uxn.agms_p_a2rt.ble.BleUtils.getBleStatus
import androidx.compose.runtime.rememberCoroutineScope
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import java.util.concurrent.Executors

private val ScannerOrange = Color(0xFFFF9F1A)
private val GuideGray = Color(0xFF9CA3AF)

@Composable
fun RegisterDeviceScreenQR(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isQrProcessing by remember { mutableStateOf(false) }
    var temporarilyBlockedQr by remember { mutableStateOf<String?>(null) }
    val statusTransition = rememberInfiniteTransition(label = "scan_status_transition")
    val scanStatusAlpha by statusTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_status_alpha"
    )
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasCameraPermission = it }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.background_white)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(40f))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.register_device_qr_title),
                color = Color(0xFF111111),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.weight(12f))

            Image(
                modifier = Modifier.size(200.dp),
                painter = painterResource(R.drawable.qr_screen),
                contentDescription = "QR 코드가 부착된 제품",
                contentScale = ContentScale.Fit
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.register_device_qr_sub_title),
                color = GuideGray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(50f))

            CameraScannerPreview(
                hasCameraPermission = hasCameraPermission,
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onQrCodeDetected = { qrValue ->
                    val serialNumber = qrValue.trim()
                    if (
                        serialNumber.isNotEmpty() &&
                        !isQrProcessing &&
                        temporarilyBlockedQr != serialNumber
                    ) {
                        isQrProcessing = true
                        temporarilyBlockedQr = serialNumber

                        if (getBleStatus(context) != STATUS_BLE_ENABLED) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.toast_turn_on_bluetooth),
                                Toast.LENGTH_SHORT
                            ).show()
                            isQrProcessing = false
                            coroutineScope.launch {
                                delay(2_000)
                                temporarilyBlockedQr = null
                            }
                        } else {
                            coroutineScope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        tokenRetrofit.getDeviceMac(serialNumber)
                                    }
                                    val responseBody = result.body()

                                    if (result.isSuccessful && responseBody?.isExists == true) {
                                        Log.e("TEST", "deviceType : ${responseBody.deviceType}")
                                        DataStoreManager.replaceDeviceType(responseBody.deviceType)
                                        navController.navigate(
                                            "ScanDeviceScreen/${responseBody.deviceMac}/$serialNumber"
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_check_serial_number),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        isQrProcessing = false
                                        delay(2_000)
                                        temporarilyBlockedQr = null
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "기기 정보를 확인하지 못했습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    isQrProcessing = false
                                    delay(2_000)
                                    temporarilyBlockedQr = null
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.weight(8f))

            Row(
                modifier = Modifier.graphicsLayer {
                    alpha = if (hasCameraPermission) scanStatusAlpha else 1f
                },
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(ScannerOrange, RoundedCornerShape(50))
                )
                Text(
                    text = when {
                        !hasCameraPermission -> stringResource(R.string.register_device_qr_scan_status_1)
                        isQrProcessing -> stringResource(R.string.register_device_qr_scan_status_2)
                        else -> stringResource(R.string.register_device_qr_scan_status_3)
                    },
                    color = ScannerOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.weight(36f))

            Text(
                modifier = Modifier
                    .clickable { navController.navigate("RegisterDeviceScreen") }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                text = stringResource(R.string.register_device_qr_plan_b),
                color = GuideGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

@Composable
private fun RegisterDeviceTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBackClick),
            painter = painterResource(R.drawable.back_icon),
            contentDescription = "뒤로가기"
        )
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = "기기 연결",
            color = GuideGray,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CameraScannerPreview(
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit,
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF171717), RoundedCornerShape(24.dp))
        ) {
            if (hasCameraPermission) {
                CameraPreview(
                    onQrCodeDetected = onQrCodeDetected,
                    modifier = Modifier.fillMaxSize()
                )
                ScannerWaveEffect(modifier = Modifier.fillMaxSize())
            } else {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable(onClick = onRequestPermission)
                        .padding(24.dp),
                    text = "카메라 권한 허용",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        ScannerCornerOverlay(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CameraPreview(
    onQrCodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnQrCodeDetected by rememberUpdatedState(onQrCodeDetected)
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage == null) {
                    imageProxy.close()
                    return@setAnalyzer
                }

                val inputImage = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        barcodes.firstNotNullOfOrNull { it.rawValue }
                            ?.takeIf(String::isNotBlank)
                            ?.let(currentOnQrCodeDetected)
                    }
                    .addOnCompleteListener { imageProxy.close() }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        }
        cameraProviderFuture.addListener(listener, executor)

        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
            barcodeScanner.close()
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}

@Composable
private fun ScannerCornerOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val inset = 4.dp.toPx()
        val cornerLength = 72.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val right = size.width - inset
        val bottom = size.height - inset

        fun corner(start: Offset, middle: Offset, end: Offset) {
            drawLine(ScannerOrange, start, middle, strokeWidth, StrokeCap.Round)
            drawLine(ScannerOrange, middle, end, strokeWidth, StrokeCap.Round)
        }

        corner(Offset(inset + cornerLength, inset), Offset(inset, inset), Offset(inset, inset + cornerLength))
        corner(Offset(right - cornerLength, inset), Offset(right, inset), Offset(right, inset + cornerLength))
        corner(Offset(inset, bottom - cornerLength), Offset(inset, bottom), Offset(inset + cornerLength, bottom))
        corner(Offset(right, bottom - cornerLength), Offset(right, bottom), Offset(right - cornerLength, bottom))
    }
}

@Composable
fun ScannerWaveEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_transition")
    val animatedPosition by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_position"
    )

    Canvas(modifier = modifier) {
        val waveHeight = 42.dp.toPx()
        val centerY = size.height * animatedPosition
        val top = centerY - waveHeight / 2
        val gradient = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                ScannerOrange.copy(alpha = 0.16f),
                ScannerOrange.copy(alpha = 0.7f),
                ScannerOrange.copy(alpha = 0.16f),
                Color.Transparent
            ),
            startY = top,
            endY = top + waveHeight
        )

        drawRect(
            brush = gradient,
            topLeft = Offset(0f, top),
            size = Size(size.width, waveHeight)
        )
        drawLine(
            color = ScannerOrange.copy(alpha = 0.85f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

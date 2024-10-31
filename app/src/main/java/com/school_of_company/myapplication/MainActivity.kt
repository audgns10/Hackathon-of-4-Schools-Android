package com.school_of_company.myapplication

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil3.compose.rememberAsyncImagePainter
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.school_of_company.myapplication.ui.theme.MyApplicationTheme
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

val CAMERA_PERMISSION_REQUEST_CODE = 1011

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Greeting()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
            } else {
                // 권한이 거부됨
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Greeting() {
    val context: Context = LocalContext.current

    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedImageUri = uri
        }

    val sharedPreferences = remember {
        context.getSharedPreferences(
            "MyPrefs", // Replace with your preferred name
            Context.MODE_PRIVATE
        )
    }

    val takePhotoFromCameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { takenPhoto ->
            if (takenPhoto != null) {
                val imageUri = takenPhoto.toUri(context)
                // 이후 처리 로직
                selectedImageUri = imageUri
            }
        }
    val upLoadTextState = rememberSaveable { mutableStateOf(true) }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한이 허용된 경우
            takePhotoFromCameraLauncher.launch()
            upLoadTextState.value = false
        } else {
            // 권한이 거부된 경우
            Toast.makeText(context, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Photism",
            style = TextStyle(
                fontSize = 30.sp,
                fontFamily = FontFamily(Font(R.font.racingsansoneregular)),
                fontWeight = FontWeight(400),
                color = Color(0xFFFF9429),
            ),
            modifier = Modifier.padding(top = 40.dp)
        )

        Spacer(modifier = Modifier.padding(top = 42.dp))

        Text(
            text = "이미지 업로드",
            style = TextStyle(
                fontSize = 24.sp,
                lineHeight = 26.4.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_semibold)),
                fontWeight = FontWeight(600),
                color = Color(0xFF1E1E1E),
            )
        )

        Spacer(modifier = Modifier.padding(top = 31.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(366.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = selectedImageUri,),
                contentDescription = "User Gallery Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                //CardonIcon(tint = Color(0xFFA7A7A7))
                Text(
                    text = if(selectedImageUri == null) "이미지 업로드" else "",
                    style = TextStyle(
                        fontSize = 24.sp,
                        lineHeight = 26.4.sp,
                        fontFamily = FontFamily(Font(R.font.pretendard_semibold)),
                        fontWeight = FontWeight(600),
                        color = Color(0xFFA7A7A7)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 10.dp))

        Row {
            Icon(
                painter = painterResource(id = R.drawable.carbon_warning),
                contentDescription = "Warning Icon",
                tint = Color(0xFFA7A7A7)
            )

            Spacer(modifier = Modifier.padding(end = 17.dp))

            Text(
                text = "이미지는 한개만 업로드 가능합니다.",
                color = Color(0xFFA7A7A7)
            )
        }

        Spacer(modifier = Modifier.padding(top = 10.dp))

        Text(
            text = "갤러리에서 이미지 선택하기",
            modifier = Modifier.clickable {
                launcher.launch("image/*")
                upLoadTextState.value = false
            },
            color = Color(0xFFA7A7A7),
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.padding(top = 10.dp))

        Text(
            text = "사진을 찍어 업로드 하기",
            modifier = Modifier.clickable {
                val activity = context as? Activity
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCameraLauncher.launch()
                    upLoadTextState.value = false
                } else {
                    requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            color = Color(0xFFA7A7A7),
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.weight(1f))

        PhotismButton(
            text = "사진 업로드",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 13.dp)
        ) {
            uploadImage(context, selectedImageUri)
            selectedImageUri = null
        }

        PhotismButton(
            text = "QR 생성하기",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            generateAndShowQRCode(context, selectedImageUri)
            selectedImageUri = null
        }
    }
}

private fun Bitmap.toUri(context: Context): Uri {
    val bytes = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, this, "Title", null)
    return Uri.parse(path)
}

@Composable
fun PhotismButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        modifier = modifier,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF9429),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(vertical = 16.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 20.sp,
                lineHeight = 26.4.sp,
                fontFamily = FontFamily(Font(R.font.pretendard_semibold)),
                fontWeight = FontWeight(600),
            )
        )
    }
}

private fun generateAndShowQRCode(context: Context, selectedImageUri: Uri?) {
    val imageUri = selectedImageUri?.toString() ?: ""  // URI를 문자열로 변환하여 QR 코드에 포함
    val qrCodeBitmap = encodeAsBitmap(imageUri)       // Base64 인코딩 대신 URI 사용

    // QR 코드 다이얼로그 표시
    val dialog = AlertDialog.Builder(context)
        .setTitle("QR 코드")
        .setMessage("QR 코드를 스캔해주세요")
        .setView(ImageView(context).apply { setImageBitmap(qrCodeBitmap) })
        .setPositiveButton("확인", null)
        .create()
    dialog.show()
//    val contents = selectedImageUri?.let { uri ->
//        val inputStream = context.contentResolver.openInputStream(uri)
//        val bytes = inputStream?.readBytes()
//        val base64Encoded = bytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) } ?: ""
//        base64Encoded
//    } ?: ""
//    val qrCodeBitmap = encodeAsBitmap(contents)
//
//
//
//    val dialog = AlertDialog.Builder(context)
//        .setTitle("QR 코드")
//        .setMessage("QR 코드를 스캔해주세요")
//        .setView(ImageView(context).apply { setImageBitmap(qrCodeBitmap) })
//        .setPositiveButton("확인", null)
//        .create()
//    dialog.show()
}

private fun encodeAsBitmap(contents: String): Bitmap {
    val writer = MultiFormatWriter()
    val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L)  // 오류 수정 수준 낮춤
    val bitMatrix = writer.encode(contents, BarcodeFormat.QR_CODE, 600, 600, hints)

    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.Black.toArgb() else Color.White.toArgb())
        }
    }
    return bitmap
//    val writer = MultiFormatWriter()
//    val bitMatrix = writer.encode(contents, BarcodeFormat.QR_CODE, 300, 300)
//    val width = bitMatrix.width
//    val height = bitMatrix.height
//    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
//    for (x in 0 until width) {
//        for (y in 0 until height) {
//            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.Black.toArgb() else Color.White.toArgb())
//        }
//    }
//    return bitmap
}

interface ApiService {
    @Multipart
    @POST("image-app") // API 경로를 설정합니다.
    fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>
}

object ApiClient {
    private const val BASE_URL = "https://d7a9-118-42-115-46.ngrok-free.app/" // 서버 URL로 변경하세요.

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

fun uploadImage(context: Context, imageUri: Uri?) {
    if (imageUri == null) {
        Log.e("Upload", "Image URI is null")
        return
    }

    val apiService = ApiClient.instance.create(ApiService::class.java)

    try {
        // Uri를 InputStream으로 변환
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val requestBody = inputStream?.let { input ->
            val bytes = input.readBytes()
            RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
        }

        // InputStream으로 만든 RequestBody를 MultipartBody.Part로 변환
        val body = requestBody?.let { reqBody ->
            MultipartBody.Part.createFormData("image", "uploaded_image.jpg", reqBody)
        }

        if (body != null) {
            // 이미지 업로드 요청 전송
            val call = apiService.uploadImage(body)
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Log.d("Upload", "Image uploaded successfully")
                    } else {
                        Log.d("Upload", "Failed to upload image: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("Upload error:", t.message ?: "Unknown error")
                }
            })
        } else {
            Log.e("Upload", "Failed to create MultipartBody.Part")
        }
    } catch (e: Exception) {
        Log.e("Upload error", e.message ?: "Unknown error")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting()
    }
}
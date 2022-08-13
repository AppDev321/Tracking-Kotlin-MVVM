package com.example.afjtracking.view.fragment.fileupload

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.afjtracking.BuildConfig
import com.example.afjtracking.R
import com.example.afjtracking.databinding.LayoutFileUploadBoxBinding
import com.example.afjtracking.room.model.TableUploadFile
import com.example.afjtracking.utils.AFJUtils
import com.example.afjtracking.view.activity.NavigationDrawerActivity
import com.example.afjtracking.view.fragment.fileupload.viewmodel.FileUploadModel
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream


class FileUploadDialog : DialogFragment(), FileUploadProgressListener {
    private lateinit var mBaseActivity: NavigationDrawerActivity
    private var _fileUploadViewModel: FileUploadModel? = null
    private val fileUploadVM get() = _fileUploadViewModel!!
    lateinit var progressBar: ProgressBar
    lateinit var containerUpload: LinearLayout
    lateinit var btnCancleReport: ImageView
    lateinit var btnDone: Button
    lateinit var txtFileName: TextView
    lateinit var txtPercentage: TextView
    lateinit var listnerUploadDialog: UploadDialogListener
    lateinit var uploadID: String
    lateinit var inpsectionType: String
    lateinit var fieldName: String
    var isDocumentPickShow: Boolean = true
    private val MULTIPLE_PERMISSIONS = 10
    lateinit var btnFileChoose: LinearLayout
    lateinit var btnCameraChoose: LinearLayout


    var permissions =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBaseActivity = context as NavigationDrawerActivity
    }


    companion object {
        fun newInstance(
            fileUploadListner: UploadDialogListener,
            uniqueFileId: String,
            inpsectionType: String,
            fieldName: String,
            isDocumentPickShow: Boolean
        ) =
            FileUploadDialog().apply {
                this.listnerUploadDialog = fileUploadListner
                this.uploadID = uniqueFileId
                this.inpsectionType = inpsectionType
                this.fieldName = fieldName
                this.isDocumentPickShow = isDocumentPickShow
            }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val dialogBinding: LayoutFileUploadBoxBinding = LayoutFileUploadBoxBinding.inflate(
            LayoutInflater.from(mBaseActivity),
            null, false
        )

        _fileUploadViewModel = ViewModelProvider(this).get(FileUploadModel::class.java)


        val mView: View = dialogBinding.root



        initViews(mView)
        setMyListeners()

        builder.setCancelable(false)
        builder.setView(mView)

        return builder.create()
    }

    private fun setMyListeners() {
        btnFileChoose.setOnClickListener {
            if (checkPermissions()) {
                showFileChooser()
            }
        }
        btnCameraChoose.setOnClickListener {
            if (checkPermissions()) {
                openCamera()
            }
        }


        btnCancleReport.setOnClickListener {

            if (fileUploadVM.uploadJob.isExecuted) {
                fileUploadVM.uploadJob.cancel()
                progressBar.progress = 0
                txtPercentage.text = "0%"
            }
            containerUpload.visibility = View.GONE
            btnDone.visibility = View.VISIBLE
            progressBar.progress = 0
        }


        btnDone.setOnClickListener {
            try {
                if (progressBar.progress == 0) {
                    dismiss()
                } else if (progressBar.progress > 1 && progressBar.progress == 100) {
                    //  ApplicationUtils.writeLogs("File uploadedddd........")
                    dismiss()
                } else {
                    mBaseActivity.toast("Please wait file is uploading...")
                }
            } catch (ex: java.lang.Exception) {
                mBaseActivity.toast(ex.toString())
            }
        }



        fileUploadVM.errorsMsg.observe(mBaseActivity)
        {
            mBaseActivity.showProgressDialog(false)
            if (it != null) {
                mBaseActivity.toast(it)
                btnDone.visibility = View.VISIBLE
                btnDone.text = "Close"
            }
        }
        fileUploadVM.getFileUploadStatus.observe(mBaseActivity)
        {
            if (it) {
                dismiss()

            } else {
                btnDone.visibility = View.VISIBLE
            }
        }


    }

    private fun initViews(mView: View) {
        try {
            btnFileChoose = mView.findViewById(R.id.btnAddReport)
            btnCameraChoose = mView.findViewById(R.id.btnAddCamera)

            containerUpload = mView.findViewById(R.id.container_upload)
            txtFileName = mView.findViewById(R.id.txtFilename)
            progressBar = mView.findViewById(R.id.progressBar)
            progressBar.max = 100
            progressBar.progress = 0

            btnCancleReport = mView.findViewById(R.id.btnCancelReport)

            txtPercentage = mView.findViewById(R.id.txtPercent)

            btnDone = mView.findViewById(R.id.btnDone)
            btnDone.text = "Close"


            if (isDocumentPickShow) {
                btnFileChoose.visibility = View.VISIBLE
            } else {
                btnFileChoose.visibility = View.GONE
            }

        } catch (ex: Exception) {
            mBaseActivity.toast(ex.toString())
        }
    }

    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(requireActivity(), p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MULTIPLE_PERMISSIONS
            )

            return false
        }
        return true
    }

    private fun showFileChooser() {
        try {
            openDocumentPicker()
        } catch (ex: java.lang.Exception) {
            mBaseActivity.toast(ex.toString())
        }
    }

    fun openDocumentPicker() {
        val intent = AFJUtils.getCustomFileChooserIntent(
            AFJUtils.IMAGE,
            AFJUtils.DOC,
            AFJUtils.DOCX,
            AFJUtils.XLS,
            AFJUtils.PDF,
            AFJUtils.TEXT
        )
        val i = Intent.createChooser(intent, "File")
        startActivityForResult(i, 2001)
    }

    private fun tryHandleOpenDocumentResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): OpenFileResult {
        return if (requestCode == 2001) {
            handleOpenDocumentResult(resultCode, data)
        } else OpenFileResult.DifferentResult
    }

    private fun handleOpenDocumentResult(resultCode: Int, data: Intent?): OpenFileResult {
        return if (resultCode == Activity.RESULT_OK && data != null) {
            val contentUri = data.data
            if (contentUri != null) {
                val stream =
                    try {
                        mBaseActivity.contentResolver.openInputStream(contentUri)
                    } catch (exception: FileNotFoundException) {
                        return OpenFileResult.ErrorOpeningFile
                    }

                val fileName =
                    getFileName(contentUri)

                if (stream != null && fileName != null) {
                    OpenFileResult.FileWasOpened(fileName, stream)
                } else OpenFileResult.ErrorOpeningFile
            } else {
                OpenFileResult.ErrorOpeningFile
            }
        } else {
            OpenFileResult.OpenFileWasCancelled
        }
    }

    @SuppressLint("Range")
    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor =
                mBaseActivity.contentResolver.query(uri, null, null, null, null)!!
            cursor.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result as String
    }

    private fun fileIsOpened(
        fileName: String,
        content: InputStream,
        isImageUpload: Boolean,
        originalFile :File?
    ) {
        try {
            val file = File(requireContext().cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            IOUtils.copy(content, outputStream)
            content.close()
            containerUpload.visibility = View.VISIBLE
            txtFileName.text = fileName
            // btnDone.visibility = View.GONE

            var filetype = "image"
            if (!isImageUpload) {
                filetype = "application"
                listnerUploadDialog.onFilePathReceived(file.path)
            }
            else
            {
                listnerUploadDialog.onFilePathReceived(file.path)
              //  listnerUploadDialog.onFilePathReceived(originalFile!!.path)
            }


            val uploadTableData = TableUploadFile(
                filetype,
                inpsectionType,
                file.extension, file.path,
                uploadID,
                fieldName,
                0,
                "",
                "",
                0,
                AFJUtils.getCurrentDateTime(),
                "",
                AFJUtils.getFileSizeInMB(file)

            )
            uploadTableData.apiRequestTime = AFJUtils.getCurrentDateTime()
            fileUploadVM.insertDataToTable(mBaseActivity, uploadTableData)


            fileUploadVM.fileUploadedSuccessfull.postValue(true)
            //Now call Background
           // AFJUtils.setPeriodicWorkRequest(mBaseActivity)


        } catch (ex: java.lang.Exception) {
            mBaseActivity.toast(ex.toString())

        }
    }

    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    private var openCameraActivityResultLauncher =
        registerForActivityResult(StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                try {

                    val uri = Uri.parse(currentPhotoPath)


                    val fileName =
                        getFileName(uri)

                    fileIsOpened(
                        fileName,
                        mBaseActivity.contentResolver.openInputStream(uri)!!, true, File(currentPhotoPath)
                    )

                } catch (ex: java.lang.Exception) {
                    mBaseActivity.toast(ex.toString())
                }
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            when (val result = tryHandleOpenDocumentResult(requestCode, resultCode, data)) {
                OpenFileResult.DifferentResult, OpenFileResult.OpenFileWasCancelled -> {
                }
                OpenFileResult.ErrorOpeningFile -> mBaseActivity.toast("error opening file")
                is OpenFileResult.FileWasOpened -> {
                    fileIsOpened(result.fileName, result.content, false,null)
                }
            }
            if (requestCode == 101) {

                val uri = Uri.parse(currentPhotoPath)
                val fileName =
                    getFileName(uri) //mBaseActivity.contentResolver.queryFileName(contentUri)

                fileIsOpened(
                    fileName,
                    mBaseActivity.contentResolver.openInputStream(uri)!!, true,File(currentPhotoPath)
                )

            }


        } catch (ex: java.lang.Exception) {
            mBaseActivity.toast(ex.toString())
        }
    }


    override fun onProgressUpdate(progress: Int) {

        AFJUtils.writeLogs("$progress")
        mBaseActivity.runOnUiThread {
            txtPercentage.text = "${progress}%"
            progressBar.progress = progress
            btnCancleReport.visibility = View.VISIBLE

        }
    }


    private fun openCamera() {
        try {
            val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val file: File = getImageFile()
            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                FileProvider.getUriForFile(
                    mBaseActivity,
                    mBaseActivity.packageName + ".provider",
                    file
                )
            else Uri.fromFile(file)
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                openCameraActivityResultLauncher.launch(pictureIntent)
            } else {
                startActivityForResult(pictureIntent, 101)
            }
        } catch (ex: java.lang.Exception) {
            mBaseActivity.toast(ex.toString())
        }
    }

    var currentPhotoPath = ""
    private fun getImageFile(): File {
        val imageFileName = "JPEG"
        val storageDir: File = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requireActivity().filesDir
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Camera"
            )
        }

        val file = File.createTempFile(imageFileName, ".jpg", storageDir)

        currentPhotoPath = "file:" + file.absolutePath

        return file
    }

    sealed class OpenFileResult {
        object OpenFileWasCancelled : OpenFileResult()
        data class FileWasOpened(val fileName: String, val content: InputStream) : OpenFileResult()
        object ErrorOpeningFile : OpenFileResult()
        object DifferentResult : OpenFileResult()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    val builder =
                        AlertDialog.Builder(mBaseActivity).apply {
                            setTitle("Permissions Required")
                            setMessage("Please allow permission to continue")
                            setCancelable(false)
                        }
                    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                        dialog.dismiss()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    builder.setNegativeButton(android.R.string.no)
                    { dialog, which ->
                        dialog.dismiss()
                    }
                    builder.show()
                }
                return
            }
        }
    }
}

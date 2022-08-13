package com.example.afjtracking.view.fragment.fileupload

import com.example.afjtracking.model.responses.UploadFileAPiResponse

interface FileUploadProgressListener {
    fun onProgressUpdate(progress: Int)
}


interface UploadDialogListener {
    fun onUploadCompleted(completedData: UploadFileAPiResponse)
    fun onFilePathReceived(path:  String)
}
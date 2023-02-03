package com.afjltd.tracking.view.fragment.fileupload

import com.afjltd.tracking.model.responses.UploadFileAPiResponse

interface FileUploadProgressListener {
    fun onProgressUpdate(progress: Int)
}


interface UploadDialogListener {
    fun onUploadCompleted(completedData: UploadFileAPiResponse)
    fun onFilePathReceived(path:  String)
}
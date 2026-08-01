package com.projectdreams.app.data.model

/**
 * Snapshot of what is on disk for a download session, used to offer
 * "Continue" when a previous download was interrupted.
 */
data class ResumeInfo(
    val fileCount: Int,
    val doneFiles: Int,
    val partialFiles: Int,
    val bytesOnDisk: Long
) {
    /** All manifest files are on disk and nothing is half-written. */
    val isComplete: Boolean
        get() = fileCount > 0 && doneFiles >= fileCount && partialFiles == 0

    /** Any download leftovers exist (resume possible). */
    val hasPartial: Boolean
        get() = doneFiles > 0 || partialFiles > 0 || bytesOnDisk > 0
}

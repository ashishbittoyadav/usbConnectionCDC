package com.example.usbapp.usb_connection

import android.os.Environment
import java.io.File

object ConstantHelper {

    const val DATA_DELIMITTER = "Y"
    const val KEY_ECG_POSITION = "KEY_ECG_POSITION"
    const val KEY_ECG_RECORD = "KEY_ECG_RECORD"
    const val KEY_ECG_POSITION_DATA = "KEY_ECG_POSITION_DATA"
    const val APP_VERSION = "1.0"
    const val IDENTIFIER = "SPDN_USB"
    const val JOB_GROUP_NAME = "SYNC_REPORT_GROUP"


    const val ACTION_TAKE_ECG = "in.sunfox.healthcare.spandanecg.TAKE_ECG_TEST"

    const val KEY_INTEGRATION_TOKEN = "KEY_INTEGRATION_TOKEN"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_AGE = "KEY_AGE"
    const val KEY_GENDER = "KEY_GENDER"
    const val KEY_PHONE_NUMBER = "KEY_PHONE_NUMBER"
    const val KEY_EXTRA_DATA = "KEY_EXTRA_DATA"


    object FTP {
        const val FTP_URL = "185.224.137.78"
        const val FTP_USERNAME = "u714355491.spandanapplication"
        const val FTP_PASSWORD = "WVLfvsLb"
    }

    object NotificationConstants {
        const val CHANNEL_ID_NORMAL = "CHANNEL_NORMAL"
        const val NOTIFICATION_TYPE_NORMAL = "NOTIFICATION_TYPE_NORMAL"
        const val NOTIFICATION_TYPE_BIG_IMAGE = "NOTIFICATION_TYPE_BIG_IMAGE"
        const val NOTIFICATION_TYPE_BIG_TEXT = "NOTIFICATION_TYPE_BIG_TEXT"
        const val KEY_LARGE_ICON = "KEY_LARGE_ICON"
        const val KEY_NOTIFICATION_TYPE = "KEY_NOTIFICATION_TYPE"
        const val KEY_TITLE_EXPANDED = "KEY_TITLE_EXPANDED"
        const val KEY_MESSAGE_EXPANDED = "KEY_MESSAGE_EXPANDED"
        const val KEY_SUMMARY = "KEY_SUMMARY"
        const val KEY_BIG_PICTURE = "KEY_BIG_PICTURE"
        const val KEY_NOTIFICATION_COLOR = "KEY_NOTIFICATION_COLOR"
    }


//    object HealthyHeartTips {
//        val tips = intArrayOf(
//            R.string.ecg_tip1,
//            R.string.ecg_tip2,
//            R.string.ecg_tip3,
//            R.string.ecg_tip4,
//            R.string.ecg_tip5,
//            R.string.ecg_tip6,
//            R.string.ecg_tip7,
//            R.string.ecg_tip8,
//            R.string.ecg_tip9,
//            R.string.ecg_tip10,
//            R.string.ecg_tip11,
//            R.string.ecg_tip12,
//            R.string.ecg_tip13,
//            R.string.ecg_tip14,
//            R.string.ecg_tip15
//        )
//    }


    object RecordType {
        const val ARRHYTHMIA = 0
        const val ECG = 1
        const val LIVE_MONITOR = 2
        const val HRV = 3
    }


    object FeaturesLimits {
        const val MIN_PR = 100
        const val MAX_PR = 200
        const val MIN_QRS = 60
        const val MAX_QRS = 100
        const val MIN_QT = 300
        const val MAX_QT = 450
        const val MIN_QTC = 300
        const val MAX_QTC = 450
        const val MIN_HEARTRATE = 60
        const val MAX_HEARTRATE = 100
    }

    object Languages {
        const val ENGLISH = "en"
        const val HINDI = "hi"
        const val BENGALI = "bn"
        const val GUJARATI = "mr"
    }

    object Fragments {
        const val TAG_BACKSTACK = "fragStackArrhythmia"
        const val TAG_FRAGMENT_ECG_RESULTS = "TAG_FRAGMENT_ECG_RESULTS"
        const val TAG_FRAGMENT_REAL_TIME_ECG = "TAG_FRAGMENT_REAL_TIME_ECG"
        const val TAG_FRAGMENT_COUNTDOWN_TO_START_ECG = "TAG_FRAGMENT_COUNTDOWN_TO_START_ECG"
        const val TAG_FRAGMENT_ELECTRODES_POSITION = "TAG_FRAGMENT_ELECTRODES_POSITION"
        const val TAG_FRAGMENT_ELECTRODES_CLIP = "TAG_FRAGMENT_ELECTRODES_CLIP"
        const val TAG_FRAGMENT_MULTIPLE_ECG_POSITION = "TAG_FRAGMENT_MULTIPLE_ECG_POSITION"
        const val TAG_FRAGMENT_LIVE_MONITOR = "TAG_FRAGMENT_LIVE_MONITOR"
    }

    object Links {
        const val SUNFOX_FB_LINK = "https://www.facebook.com/Sunfoxtechnologies/"
        const val SUNFOX_TWITTER_LINK = "https://twitter.com/SunfoxTech"
        const val SUNFOX_INSTAGRAM_LINK = "https://www.instagram.com/sunfoxt/"
        const val SUNFOX_LINKEDIN_LINK = "https://www.linkedin.com/company/13320385/"
        const val SUNFOX_GPLUS_LINK = "https://plus.google.com/100498809665736261890"
    }

    object SharedPreferences {
        const val KEY_MASTER_SHARED_PREF = "KEY_MASTER_SHARED_PREF"
        const val KEY_OPT_IN_FOR_HEALTHY_HEART_PROGRAM = "KEY_OPT_IN_FOR_HEALTHY_HEART_PROGRAM"
        const val NOT_FOUND_RESPONSE_DEFAULT = "DEFAULT"
        const val KEY_PATIENT_NAME = "KEY_PATIENT_NAME"
        const val KEY_PATIENT_AGE = "KEY_PATIENT_AGE"
        const val KEY_PATIENT_HEIGHT = "KEY_PATIENT_HEIGHT"
        const val KEY_PATIENT_WEIGHT = "KEY_PATIENT_WEIGHT"
        const val KEY_PATIENT_GENDER = "KEY_PATIENT_GENDER"
        const val KEY_SELECTED_LANGUAGE = "KEY_SELECTED_LANGUAGE"
        const val KEY_FIRST_APP_LOAD = "KEY_FIRST_APP_LOAD"
        const val KEY_SHOULD_SYNC_REPORTS = "KEY_SHOULD_SYNC_REPORTS"
    }

    object Intents {
        const val ACTION_USB_DISCONNECTED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
        const val ACTION_USB_CONNECTED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
        const val EXTRA_DATA_ECG_RECORD = "in.sunfox.healthcare.spandanecg.EXTRA_DATA_ECG_RECORD"
        const val EXTRA_PATH_RECORD = "in.sunfox.healthcare.spandanecg.EXTRA_PATH_RECORD"
        const val EXTRA_REPORT_SAVED = "in.sunfox.healthcare.spandanecg.EXTRA_REPORT_SAVED"
    }

    object Devices {
        const val STM302_VID = 1155
        const val STM302_PID = 22336
        const val STM103_VID = 0x1EAF
        const val STM103_PID = 0x0004
    }

    object DeviceRegistration {
        const val REPONSE_DEVICE_REGISTRATION_SUCCESSFUL = "101"
        const val REPONSE_DEVICE_ALREADY_REGISTERED = "102"
        const val REPONSE_DEVICE_NOT_AUTHENTIC = "104"
        const val REPONSE_DEVICE_CONNECTION_ERROR = "109"
    }

    object EcgPositions {
        const val POSITION_V1 = 0
        const val POSITION_V2 = 1
        const val POSITION_V3 = 2
        const val POSITION_V4 = 3
        const val POSITION_V5 = 4
        const val POSITION_V6 = 5
        const val POSITION_LEAD2 = 6
    }

    object UnitsSuffix {
        const val SUFFIX_UNIT_MILLISECOND = " ms"
        const val SUFFIX_UNIT_BPM = " BPM"
        const val SUFFIX_UNIT_AGE = " years"
        const val SUFFIX_UNIT_HEIGHT = " cms"
        const val SUFFIX_UNIT_WEIGHT = " kgs"
    }

    object Files {
        const val FILE_REPORTS_MAIN = "main.txt"
        const val FILE_REPORTS_V1 = "v1.txt"
        const val FILE_REPORTS_V2 = "v2.txt"
        const val FILE_REPORTS_V3 = "v3.txt"
        const val FILE_REPORTS_V4 = "v4.txt"
        const val FILE_REPORTS_V5 = "v5.txt"
        const val FILE_REPORTS_V6 = "v6.txt"
        const val FILE_REPORTS_LEAD2 = "lead2.txt"
        val ZIFFY_ECG_DIR_PATH = Environment.getExternalStorageDirectory()
            .toString() + File.separator + "ZiffyHealth/ZiffyECG/"
        val ZIFFY_ROOT_DIR_PATH =
            Environment.getExternalStorageDirectory().toString() + File.separator + "ZiffyHealth/"
        val PATH_PDF_ECG_TEMPLATE = Environment.getExternalStorageDirectory()
            .toString() + File.separator + ".Spandan/data/report_template_spandan.pdf"
        val PATH_PDF_HRV_TEMPLATE = Environment.getExternalStorageDirectory()
            .toString() + File.separator + ".Spandan/data/spandan_hrv_report_template.pdf"
        val PATH_PDF_ARRHYTHMIA_TEMPLATE = Environment.getExternalStorageDirectory()
            .toString() + File.separator + ".Spandan/data/spandan_arrhythmia_report_template.pdf"
        val PATH_SPANDAN_FOLDER =
            Environment.getExternalStorageDirectory().toString() + "/.Spandan/"
        val PATH_SPANDAN_DATA_FOLDER =
            Environment.getExternalStorageDirectory().toString() + "/.Spandan/data/"
        val PATH_SPANDAN_ARRHYTHMIA_DATA_FOLDER = Environment.getExternalStorageDirectory()
            .toString() + "/.Spandan/data/tests/arrhythmia/"
        val PATH_SPANDAN_HRV_DATA_FOLDER =
            Environment.getExternalStorageDirectory().toString() + "/.Spandan/data/tests/hrv/"
        val PATH_SPANDAN_ECG_DATA_FOLDER =
            Environment.getExternalStorageDirectory().toString() + "/.Spandan/data/tests/ecg/"
        val PATH_SPANDAN_DATA_LOGS_FOLDER =
            Environment.getExternalStorageDirectory().toString() + "/.Spandan/data/logs.info"
        val PATH_SPANDAN_PUSH_TO_SERVER_DATA =
            Environment.getExternalStorageDirectory().toString() + "/.Spandan/data/"
        const val FILE_NAME_SPANDAN_PUSH_TO_SERVER_DATA = "pushData.data"
    }

    object FragmentTags {
        const val TAG_FRAGMENT_REAL_TIME_ECG = "TAG_FRAGMENT_REAL_TIME_ECG"
    }
}
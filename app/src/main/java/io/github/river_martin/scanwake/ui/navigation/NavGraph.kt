package io.github.river_martin.scanwake.ui.navigation

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.river_martin.scanwake.alarm.AlarmScheduler
import io.github.river_martin.scanwake.alarm.BootAlarmStorage
import io.github.river_martin.scanwake.data.local.ScanWakeDatabase
import io.github.river_martin.scanwake.data.model.Alarm
import io.github.river_martin.scanwake.data.model.SavedBarcode
import io.github.river_martin.scanwake.data.repository.AlarmRepository
import io.github.river_martin.scanwake.ui.screens.AddEditAlarmScreen
import io.github.river_martin.scanwake.ui.screens.AlarmListScreen
import io.github.river_martin.scanwake.ui.screens.BarcodeScannerScreen
import io.github.river_martin.scanwake.ui.screens.NapAlarmScreen
import io.github.river_martin.scanwake.ui.screens.ReliabilityChecklistScreen
import kotlinx.coroutines.launch

object NavRoutes {
    const val ALARM_LIST = "alarm_list"
    const val ADD_EDIT_ALARM = "add_edit_alarm?alarmId={alarmId}"
    const val NAP_ALARM = "nap_alarm"
    const val BARCODE_SCANNER = "barcode_scanner"
    const val RELIABILITY_CHECKLIST = "reliability_checklist"

    fun addEditAlarmRoute(alarmId: Long? = null): String {
        return if (alarmId != null) "add_edit_alarm?alarmId=$alarmId" else "add_edit_alarm"
    }
}

@Composable
fun ScanWakeNavGraph(
    windowSizeClass: WindowSizeClass? = null
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("scanwake_prefs", Context.MODE_PRIVATE) }
    val isFirstLaunch = remember { prefs.getBoolean("is_first_launch", true) }
    val startDestination = if (isFirstLaunch) NavRoutes.RELIABILITY_CHECKLIST else NavRoutes.ALARM_LIST

    val database = remember { ScanWakeDatabase.getDatabase(context) }
    val repository = remember {
        AlarmRepository(
            database.alarmDao(),
            database.savedBarcodeDao()
        )
    }
    val scheduler = remember {
        AlarmScheduler(
            context
        )
    }

    val alarms by repository.allAlarms.collectAsState(initial = emptyList())
    val savedBarcodes by repository.allSavedBarcodes.collectAsState(initial = emptyList())

    // Self-healing startup check: ensure all enabled alarms are scheduled (handles force-stops / app kills)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val enabledAlarms = repository.getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    scheduler.schedule(alarm)
                    BootAlarmStorage.save(context, alarm)
                }
            } catch (e: Exception) {
                android.util.Log.e("ScanWakeBoot", "Error in startup alarm re-scheduling", e)
            }
        }
    }

    // Temporary holder for barcode scanned in AddEdit or Nap screen
    var tempScannedBarcode by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(220)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(220)) }
    ) {
        // Reliability Checklist Screen (First Launch & Manual Inspection)
        composable(NavRoutes.RELIABILITY_CHECKLIST) {
            ReliabilityChecklistScreen(
                windowSizeClass = windowSizeClass,
                onContinueClick = {
                    prefs.edit().putBoolean("is_first_launch", false).apply()
                    navController.navigate(NavRoutes.ALARM_LIST) {
                        popUpTo(NavRoutes.RELIABILITY_CHECKLIST) { inclusive = true }
                    }
                },
                onBackClick = if (isFirstLaunch) null else {
                    { navController.popBackStack() }
                }
            )
        }

        // Alarm List Screen
        composable(NavRoutes.ALARM_LIST) {
            AlarmListScreen(
                alarms = alarms,
                savedBarcodes = savedBarcodes,
                windowSizeClass = windowSizeClass,
                onReliabilityClick = {
                    navController.navigate(NavRoutes.RELIABILITY_CHECKLIST)
                },
                onAddAlarmClick = {
                    tempScannedBarcode = null
                    navController.navigate(NavRoutes.addEditAlarmRoute())
                },
                onAddNapAlarmClick = {
                    tempScannedBarcode = null
                    navController.navigate(NavRoutes.NAP_ALARM)
                },
                onEditAlarmClick = { alarm ->
                    tempScannedBarcode = null
                    navController.navigate(NavRoutes.addEditAlarmRoute(alarm.id))
                },
                onToggleAlarm = { alarm, isEnabled ->
                    scope.launch {
                        val updated = alarm.copy(isEnabled = isEnabled)
                        repository.updateAlarm(updated)
                        if (isEnabled) {
                            scheduler.schedule(updated)
                            BootAlarmStorage.save(
                                context,
                                updated
                            )
                        } else {
                            scheduler.cancel(updated)
                            BootAlarmStorage.remove(
                                context,
                                updated.id
                            )
                        }
                    }
                },
                onDeleteAlarm = { alarm ->
                    scope.launch {
                        scheduler.cancel(alarm)
                        BootAlarmStorage.remove(
                            context,
                            alarm.id
                        )
                        repository.deleteAlarm(alarm)
                    }
                }
            )
        }

        val saveAndScheduleAlarm: (Alarm) -> Unit = { alarmToSave ->
            scope.launch {
                if (alarmToSave.id == 0L) {
                    val newId = repository.insertAlarm(alarmToSave)
                    val newAlarm = alarmToSave.copy(id = newId)
                    scheduler.schedule(newAlarm)
                    BootAlarmStorage.save(context, newAlarm)
                } else {
                    repository.updateAlarm(alarmToSave)
                    scheduler.schedule(alarmToSave)
                    BootAlarmStorage.save(context, alarmToSave)
                }
                tempScannedBarcode = null
                navController.popBackStack()
            }
        }

        val openBarcodeScanner = {
            navController.navigate(NavRoutes.BARCODE_SCANNER)
        }

        val deleteBarcode: (SavedBarcode) -> Unit = { barcode -> scope.launch {
            repository.deleteSavedBarcode(barcode)
        }
        }

        val navBack: () -> Unit = {
            tempScannedBarcode = null
            navController.popBackStack()
        }

        // Add / Edit Normal Alarm Screen
        composable(
            route = NavRoutes.ADD_EDIT_ALARM,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            val initialAlarm = alarms.find { it.id == alarmId }

            AddEditAlarmScreen(
                initialAlarm = initialAlarm,
                savedBarcodes = savedBarcodes,
                scannedBarcode = tempScannedBarcode,
                windowSizeClass = windowSizeClass,
                onScanBarcodeClick = openBarcodeScanner,
                onSaveAlarm = saveAndScheduleAlarm,
                onDeleteBarcode = deleteBarcode,
                onBackClick = navBack
            )
        }

        // Dedicated Nap Alarm Screen
        composable(NavRoutes.NAP_ALARM) {
            NapAlarmScreen(
                savedBarcodes = savedBarcodes,
                scannedBarcode = tempScannedBarcode,
                windowSizeClass = windowSizeClass,
                onScanBarcodeClick = openBarcodeScanner,
                onSaveAlarm = saveAndScheduleAlarm,
                onDeleteBarcode = deleteBarcode,
                onBackClick = navBack
            )
        }

        // Barcode Scanner Screen
        composable(NavRoutes.BARCODE_SCANNER) {
            BarcodeScannerScreen(
                titleText = "Scan Alarm Barcode",
                instructionText = "Scan barcode on any item to set as alarm dismissal key",
                onBarcodeScanned = { scannedCode ->
                    tempScannedBarcode = scannedCode
                    navController.popBackStack()
                },
                onSaveAndUseBarcode = { barcodeVal, label ->
                    scope.launch {
                        repository.insertSavedBarcode(
                            SavedBarcode(
                                label = label,
                                barcodeValue = barcodeVal
                            )
                        )
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

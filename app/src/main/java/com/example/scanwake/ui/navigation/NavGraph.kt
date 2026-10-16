package com.example.scanwake.ui.navigation

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
import com.example.scanwake.alarm.AlarmScheduler
import com.example.scanwake.data.local.ScanWakeDatabase
import com.example.scanwake.data.repository.AlarmRepository
import com.example.scanwake.ui.screens.AddEditAlarmScreen
import com.example.scanwake.ui.screens.AlarmListScreen
import com.example.scanwake.ui.screens.BarcodeScannerScreen
import com.example.scanwake.ui.screens.NapAlarmScreen
import kotlinx.coroutines.launch

object NavRoutes {
    const val ALARM_LIST = "alarm_list"
    const val ADD_EDIT_ALARM = "add_edit_alarm?alarmId={alarmId}"
    const val NAP_ALARM = "nap_alarm"
    const val BARCODE_SCANNER = "barcode_scanner"

    fun addEditAlarmRoute(alarmId: Long? = null): String {
        return if (alarmId != null) "add_edit_alarm?alarmId=$alarmId" else "add_edit_alarm"
    }
}

@Composable
fun ScanWakeNavGraph() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val database = remember { ScanWakeDatabase.getDatabase(context) }
    val repository = remember { AlarmRepository(database.alarmDao()) }
    val scheduler = remember { AlarmScheduler(context) }

    val alarms by repository.allAlarms.collectAsState(initial = emptyList())

    // Temporary holder for barcode scanned in AddEdit or Nap screen
    var tempScannedBarcode by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.ALARM_LIST
    ) {
        // Alarm List Screen
        composable(NavRoutes.ALARM_LIST) {
            AlarmListScreen(
                alarms = alarms,
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
                        } else {
                            scheduler.cancel(updated)
                        }
                    }
                },
                onDeleteAlarm = { alarm ->
                    scope.launch {
                        scheduler.cancel(alarm)
                        repository.deleteAlarm(alarm)
                    }
                }
            )
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
                scannedBarcode = tempScannedBarcode,
                onScanBarcodeClick = {
                    navController.navigate(NavRoutes.BARCODE_SCANNER)
                },
                onSaveAlarm = { alarmToSave ->
                    scope.launch {
                        if (alarmToSave.id == 0L) {
                            val newId = repository.insertAlarm(alarmToSave)
                            scheduler.schedule(alarmToSave.copy(id = newId))
                        } else {
                            repository.updateAlarm(alarmToSave)
                            scheduler.schedule(alarmToSave)
                        }
                        tempScannedBarcode = null
                        navController.popBackStack()
                    }
                },
                onBackClick = {
                    tempScannedBarcode = null
                    navController.popBackStack()
                }
            )
        }

        // Dedicated Nap Alarm Screen
        composable(NavRoutes.NAP_ALARM) {
            NapAlarmScreen(
                scannedBarcode = tempScannedBarcode,
                onScanBarcodeClick = {
                    navController.navigate(NavRoutes.BARCODE_SCANNER)
                },
                onSaveAlarm = { napAlarm ->
                    scope.launch {
                        val newId = repository.insertAlarm(napAlarm)
                        scheduler.schedule(napAlarm.copy(id = newId))
                        tempScannedBarcode = null
                        navController.popBackStack()
                    }
                },
                onBackClick = {
                    tempScannedBarcode = null
                    navController.popBackStack()
                }
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
                }
            )
        }
    }
}

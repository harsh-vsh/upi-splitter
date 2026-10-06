package com.example.upisplitter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.upisplitter.data.repository.TransactionRepository
import com.example.upisplitter.ui.amount.AmountScreen
import com.example.upisplitter.ui.amount.AmountViewModel
import com.example.upisplitter.ui.breakdown.BreakdownScreen
import com.example.upisplitter.ui.breakdown.BreakdownViewModel
import com.example.upisplitter.ui.custom.CustomQrScreen
import com.example.upisplitter.ui.custom.CustomQrViewModel
import com.example.upisplitter.ui.details.DetailsScreen
import com.example.upisplitter.ui.details.DetailsViewModel
import com.example.upisplitter.ui.history.HistoryScreen
import com.example.upisplitter.ui.history.HistoryViewModel
import com.example.upisplitter.ui.home.HomeScreen
import com.example.upisplitter.ui.home.HomeViewModel
import com.example.upisplitter.ui.manual.ManualUpiScreen
import com.example.upisplitter.ui.manual.ManualUpiViewModel
import com.example.upisplitter.ui.manage.AddEditUpiScreen
import com.example.upisplitter.ui.manage.ManageUpiScreen
import com.example.upisplitter.ui.manage.ManageUpiViewModel
import com.example.upisplitter.ui.scanner.ScannerScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavGraph(repository: TransactionRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {
        composable("home") {
            val viewModel: HomeViewModel = viewModel {
                HomeViewModel(repository)
            }
            HomeScreen(
                viewModel = viewModel,
                onScanQrClick = { navController.navigate("scanner?mode=split") },
                onSplitByUpiClick = { navController.navigate("manual_upi") },
                onManageUpiClick = { navController.navigate("manage_upi") },
                onHistoryClick = { navController.navigate("history") },
                onCustomQrClick = { navController.navigate("custom_qr") },
                onMerchantClick = { _ ->
                    navController.navigate("custom_qr")
                },
                onTransactionClick = { transactionId ->
                    navController.navigate("details/$transactionId")
                }
            )
        }

        composable("custom_qr") {
            val viewModel: CustomQrViewModel = viewModel {
                CustomQrViewModel(repository)
            }
            CustomQrScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddMerchantClick = { navController.navigate("add_edit_upi") }
            )
        }

        composable(
            route = "scanner?mode={mode}&editingId={editingId}&name={name}&upi={upi}",
            arguments = listOf(
                navArgument("mode") { defaultValue = "split" },
                navArgument("editingId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("upi") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "split"
            val editingId = backStackEntry.arguments?.getLong("editingId") ?: -1L

            ScannerScreen(
                onQrValid = { merchantName, merchantUpiId, qrData ->
                    if (mode == "manage_edit") {
                        navController.previousBackStackEntry?.savedStateHandle?.set("scannedName", merchantName)
                        navController.previousBackStackEntry?.savedStateHandle?.set("scannedUpi", merchantUpiId)
                        navController.popBackStack()
                    } else if (mode == "manage") {
                        navController.previousBackStackEntry?.savedStateHandle?.set("scannedName", merchantName)
                        navController.previousBackStackEntry?.savedStateHandle?.set("scannedUpi", merchantUpiId)
                        navController.previousBackStackEntry?.savedStateHandle?.set("scannedEditingId", editingId)
                        navController.popBackStack()
                    } else {
                        val encodedName = URLEncoder.encode(merchantName, "UTF-8")
                        val encodedUpi = URLEncoder.encode(merchantUpiId, "UTF-8")
                        val encodedQr = URLEncoder.encode(qrData, "UTF-8")
                        navController.navigate("amount/$encodedName/$encodedUpi/$encodedQr/true") {
                            popUpTo("home")
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("manual_upi") {
            val viewModel: ManualUpiViewModel = viewModel {
                ManualUpiViewModel(repository)
            }
            ManualUpiScreen(
                viewModel = viewModel,
                onProceed = { merchantName, merchantUpiId ->
                    val encodedName = URLEncoder.encode(merchantName, "UTF-8")
                    val encodedUpi = URLEncoder.encode(merchantUpiId, "UTF-8")
                    val qrData = "upi://pay?pa=$merchantUpiId&pn=${URLEncoder.encode(merchantName, "UTF-8")}"
                    val encodedQr = URLEncoder.encode(qrData, "UTF-8")
                    navController.navigate("amount/$encodedName/$encodedUpi/$encodedQr/false") {
                        popUpTo("home")
                    }
                },
                onBack = { navController.popBackStack() },
                onAddMerchantClick = { navController.navigate("add_edit_upi") }
            )
        }

        composable("manage_upi") {
            val viewModel: ManageUpiViewModel = viewModel {
                ManageUpiViewModel(repository)
            }
            ManageUpiScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_edit_upi") },
                onEditClick = { item ->
                    val name = URLEncoder.encode(item.merchantName, "UTF-8")
                    val upi = URLEncoder.encode(item.upiId, "UTF-8")
                    navController.navigate("add_edit_upi?id=${item.id}&name=$name&upi=$upi")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_edit_upi?id={id}&name={name}&upi={upi}",
            arguments = listOf(
                navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                navArgument("upi") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val savedStateHandle = backStackEntry.savedStateHandle
            val id = backStackEntry.arguments?.getLong("id") ?: -1L
            val encodedName = backStackEntry.arguments?.getString("name") ?: ""
            val encodedUpi = backStackEntry.arguments?.getString("upi") ?: ""

            // Observe scannedName and scannedUpi as Flow State so UI updates on popBackStack
            val scannedNameState by savedStateHandle.getStateFlow<String?>("scannedName", null).collectAsState()
            val scannedUpiState by savedStateHandle.getStateFlow<String?>("scannedUpi", null).collectAsState()

            val decodedName = try { URLDecoder.decode(encodedName, "UTF-8") } catch (e: Exception) { "" }
            val decodedUpi = try { URLDecoder.decode(encodedUpi, "UTF-8") } catch (e: Exception) { "" }

            val initialName = if (!scannedNameState.isNullOrBlank()) scannedNameState!! else decodedName
            val initialUpi = if (!scannedUpiState.isNullOrBlank()) scannedUpiState!! else decodedUpi

            val viewModel: ManageUpiViewModel = viewModel {
                ManageUpiViewModel(repository)
            }

            AddEditUpiScreen(
                viewModel = viewModel,
                editingId = id,
                initialMerchantName = initialName,
                initialUpiId = initialUpi,
                onScanCameraClick = { editingId, currentName, currentUpi ->
                    val n = URLEncoder.encode(currentName, "UTF-8")
                    val u = URLEncoder.encode(currentUpi, "UTF-8")
                    navController.navigate("scanner?mode=manage_edit&editingId=$editingId&name=$n&upi=$u")
                },
                onSaveSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "amount/{merchantName}/{merchantUpiId}/{qrData}/{isEditable}",
            arguments = listOf(
                navArgument("merchantName") { type = NavType.StringType },
                navArgument("merchantUpiId") { type = NavType.StringType },
                navArgument("qrData") { type = NavType.StringType },
                navArgument("isEditable") { type = NavType.BoolType; defaultValue = true }
            )
        ) { backStackEntry ->
            val encodedName = backStackEntry.arguments?.getString("merchantName") ?: ""
            val encodedUpi = backStackEntry.arguments?.getString("merchantUpiId") ?: ""
            val encodedQr = backStackEntry.arguments?.getString("qrData") ?: ""
            val isEditable = backStackEntry.arguments?.getBoolean("isEditable") ?: true

            val merchantName = URLDecoder.decode(encodedName, "UTF-8")
            val merchantUpiId = URLDecoder.decode(encodedUpi, "UTF-8")
            val qrData = URLDecoder.decode(encodedQr, "UTF-8")

            val viewModel: AmountViewModel = viewModel {
                AmountViewModel(repository, merchantName, merchantUpiId, qrData, isEditable)
            }

            AmountScreen(
                merchantUpiId = merchantUpiId,
                viewModel = viewModel,
                onContinue = { transactionId ->
                    navController.navigate("breakdown/$transactionId") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "breakdown/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val viewModel: BreakdownViewModel = viewModel {
                BreakdownViewModel(repository, transactionId)
            }

            BreakdownScreen(
                viewModel = viewModel,
                onHomeClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("history") {
            val viewModel: HistoryViewModel = viewModel {
                HistoryViewModel(repository)
            }
            HistoryScreen(
                viewModel = viewModel,
                onTransactionClick = { transactionId ->
                    navController.navigate("details/$transactionId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "details/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val viewModel: DetailsViewModel = viewModel {
                DetailsViewModel(repository, transactionId)
            }

            DetailsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

package com.mihara.billio.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mihara.billio.R
import com.mihara.billio.data.model.InvoiceType
import com.mihara.billio.ui.client.ClientDetailScreen
import com.mihara.billio.ui.client.ClientFormScreen
import com.mihara.billio.ui.client.ClientListScreen
import com.mihara.billio.ui.dashboard.DashboardScreen
import com.mihara.billio.ui.invoice.InvoiceDetailScreen
import com.mihara.billio.ui.invoice.InvoiceFormScreen
import com.mihara.billio.ui.invoice.InvoiceListScreen
import com.mihara.billio.ui.navigation.Routes
import com.mihara.billio.ui.navigation.topLevelRoutes
import com.mihara.billio.ui.onboarding.OnboardingScreen
import com.mihara.billio.ui.settings.SettingsScreen

private data class Tab(val route: String, val labelRes: Int, val icon: ImageVector)

@Composable
fun BillioRoot(onboardingComplete: Boolean) {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab(Routes.DASHBOARD, R.string.nav_dashboard, Icons.Default.Dashboard),
        Tab(Routes.INVOICES, R.string.nav_invoices, Icons.AutoMirrored.Filled.ReceiptLong),
        Tab(Routes.QUOTES, R.string.nav_quotes, Icons.Default.RequestQuote),
        Tab(Routes.CLIENTS, R.string.nav_clients, Icons.Default.People),
        Tab(Routes.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
    )
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBars = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBars) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = if (onboardingComplete) Routes.DASHBOARD else Routes.ONBOARDING,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onDone = {
                    nav.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                })
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onNewInvoice = { nav.navigate(Routes.invoiceForm(type = "INVOICE")) },
                    onNewQuote = { nav.navigate(Routes.invoiceForm(type = "QUOTE")) },
                    onOpenInvoice = { nav.navigate(Routes.invoiceDetail(it)) },
                    onSeeAll = { nav.navigate(Routes.INVOICES) }
                )
            }
            composable(Routes.INVOICES) {
                InvoiceListScreen(
                    type = InvoiceType.INVOICE,
                    onOpen = { nav.navigate(Routes.invoiceDetail(it)) },
                    onCreate = { nav.navigate(Routes.invoiceForm(type = "INVOICE")) }
                )
            }
            composable(Routes.QUOTES) {
                InvoiceListScreen(
                    type = InvoiceType.QUOTE,
                    onOpen = { nav.navigate(Routes.invoiceDetail(it)) },
                    onCreate = { nav.navigate(Routes.invoiceForm(type = "QUOTE")) }
                )
            }
            composable(Routes.CLIENTS) {
                ClientListScreen(
                    onOpen = { nav.navigate(Routes.clientDetail(it)) },
                    onCreate = { nav.navigate(Routes.clientForm()) }
                )
            }
            composable(Routes.SETTINGS) { SettingsScreen() }

            composable(
                route = "${Routes.INVOICE_FORM}?id={id}&type={type}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("type") { type = NavType.StringType; defaultValue = "INVOICE" }
                )
            ) {
                InvoiceFormScreen(
                    onDone = { nav.popBackStack() },
                    onAddClient = { nav.navigate(Routes.clientForm()) }
                )
            }

            composable(
                route = "${Routes.INVOICE_DETAIL}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) {
                InvoiceDetailScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { id, type -> nav.navigate(Routes.invoiceForm(id, type)) },
                    onOpenInvoice = { id ->
                        nav.navigate(Routes.invoiceDetail(id)) { launchSingleTop = true }
                    }
                )
            }

            composable(
                route = "${Routes.CLIENT_FORM}?id={id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L })
            ) {
                ClientFormScreen(onDone = { nav.popBackStack() })
            }

            composable(
                route = "${Routes.CLIENT_DETAIL}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) {
                ClientDetailScreen(
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(Routes.clientForm(it)) },
                    onOpenInvoice = { nav.navigate(Routes.invoiceDetail(it)) }
                )
            }
        }
    }
}

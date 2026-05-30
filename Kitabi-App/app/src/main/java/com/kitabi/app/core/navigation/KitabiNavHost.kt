package com.kitabi.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kitabi.app.core.designsystem.KitabiTheme
import com.kitabi.app.feature.auth.AuthScreen
import com.kitabi.app.feature.bookmarks.BookmarksScreen
import com.kitabi.app.feature.chat.ChatRoomListScreen
import com.kitabi.app.feature.chat.ChatRoomScreen
import com.kitabi.app.feature.library.LibraryScreen
import com.kitabi.app.feature.onboarding.OnboardingScreen
import com.kitabi.app.feature.reader.ReaderScreen
import com.kitabi.app.feature.review.ReviewListScreen
import com.kitabi.app.feature.review.WriteReviewScreen
import com.kitabi.app.feature.search.SearchScreen
import com.kitabi.app.feature.settings.SettingsScreen
import com.kitabi.app.feature.stats.StatsScreen
import com.kitabi.app.feature.store.StoreScreen

/**
 * المضيف الرئيسي للتنقل في تطبيق كتابي
 * يحتوي على شريط التنقل السفلي ومسارات الشاشات
 * مع تأثيرات انتقالية متحركة تدعم RTL
 */
@Composable
fun KitabiNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // تحديد الشاشات التي تعرض شريط التنقل السفلي
    val bottomBarRoutes = BottomNavItem.entries.map { it.route }
    val showBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                KitabiBottomNavigationBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // تجنب تراكم الشاشات المتطابقة
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = KitabiTheme.colors.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Library.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(300)
                        )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) +
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300)
                        )
            }
        ) {
            // شاشة التعريف الأولي
            composable(Route.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate(Route.Library.route) {
                            popUpTo(Route.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // شاشة المصادقة
            composable(Route.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Route.Library.route) {
                            popUpTo(Route.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            // شاشة المكتبة
            composable(Route.Library.route) {
                LibraryScreen(
                    onNavigateToSearch = {
                        navController.navigate(Route.Search.route)
                    },
                    onNavigateToReader = { bookId ->
                        navController.navigate(Route.Reader.createRoute(bookId))
                    },
                    onImportBook = {
                        // سيتم الربط مع مستورد الملفات
                    }
                )
            }

            // شاشة المتجر
            composable(Route.Store.route) {
                StoreScreen(
                    onBookClick = { book ->
                        // الانتقال لتفاصيل الكتاب
                    },
                    onNavigateToSearch = {
                        navController.navigate(Route.Search.route)
                    }
                )
            }

            // شاشة البحث
            composable(Route.Search.route) {
                SearchScreen(
                    onBookClick = { bookId ->
                        navController.navigate(Route.Reader.createRoute(bookId))
                    },
                    onOnlineBookClick = { book ->
                        // الانتقال لتفاصيل الكتاب الإلكتروني
                    }
                )
            }

            // شاشة الإشارات المرجعية
            composable(Route.Bookmarks.route) {
                BookmarksScreen(
                    onBookmarkClick = { bookId, page ->
                        navController.navigate(Route.Reader.createRoute(bookId))
                    }
                )
            }

            // شاشة الإعدادات
            composable(Route.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onSignOut = {
                        navController.navigate(Route.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // شاشة الإحصائيات
            composable(Route.Stats.route) {
                StatsScreen()
            }

            // شاشة غرف المحادثة
            composable(Route.ChatRooms.route) {
                ChatRoomListScreen(
                    onRoomClick = { roomId ->
                        navController.navigate(Route.ChatRoom.createRoute(roomId))
                    }
                )
            }

            // شاشة غرفة المحادثة
            composable(
                route = Route.ChatRoom.route,
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                ChatRoomScreen(
                    roomId = roomId,
                    onBack = { navController.popBackStack() }
                )
            }

            // شاشة قراءة الكتاب
            composable(
                route = Route.Reader.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                ReaderScreen(
                    bookId = bookId,
                    onBack = { navController.popBackStack() }
                )
            }

            // شاشة مراجعات الكتاب
            composable(
                route = Route.Reviews.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                ReviewListScreen(
                    bookId = bookId,
                    onWriteReview = {
                        navController.navigate(Route.WriteReview.createRoute(bookId))
                    }
                )
            }

            // شاشة كتابة مراجعة
            composable(
                route = Route.WriteReview.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                WriteReviewScreen(
                    bookId = bookId,
                    onBack = { navController.popBackStack() },
                    onSubmitSuccess = { navController.popBackStack() }
                )
            }

            // شاشة مساعد الذكاء الاصطناعي
            composable(
                route = Route.AiAssistant.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                // يتم الوصول عبر الشاشة المنبثقة في القارئ
                PlaceholderScreen(title = "مساعد الذكاء الاصطناعي: $bookId")
            }
        }
    }
}

/**
 * شريط التنقل السفلي المخصص
 * يدعم RTL و Material 3 مع ألوان كتابي
 */
@Composable
private fun KitabiBottomNavigationBar(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = KitabiTheme.colors.surface,
        contentColor = KitabiTheme.colors.onSurface
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == item.route
            } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) {
                            KitabiTheme.colors.primary
                        } else {
                            KitabiTheme.colors.onSurfaceVariant
                        }
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            KitabiTheme.colors.primary
                        } else {
                            KitabiTheme.colors.onSurfaceVariant
                        }
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = KitabiTheme.colors.primaryContainer,
                    selectedIconColor = KitabiTheme.colors.onPrimaryContainer,
                    selectedTextColor = KitabiTheme.colors.onPrimaryContainer,
                    unselectedIconColor = KitabiTheme.colors.onSurfaceVariant,
                    unselectedTextColor = KitabiTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * شاشة مؤقتة للاختبار - سيتم استبدالها بالتنفيذ الكامل
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = KitabiTheme.colors.onBackground
        )
    }
}

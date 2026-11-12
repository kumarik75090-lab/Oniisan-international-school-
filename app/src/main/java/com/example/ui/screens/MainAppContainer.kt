package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SchoolRepository
import com.example.ui.theme.MyApplicationTheme

enum class ActiveScreen {
    SPLASH_LOGIN,
    SCHOOL_INFO,
    STUDENT_DASHBOARD,
    TEACHER_DASHBOARD,
    PARENT_DASHBOARD,
    ADMIN_DASHBOARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val brandingConfig by SchoolRepository.config.collectAsState()
    val students by SchoolRepository.students.collectAsState()
    val teachers by SchoolRepository.teachers.collectAsState()
    
    val context = LocalContext.current
    var currentActiveScreen by remember { mutableStateOf(ActiveScreen.SPLASH_LOGIN) }
    var appDarkThemeToggle by remember { mutableStateOf(false) }

    // User session states
    var selectedStudentSessionId by remember { mutableStateOf("std_01") }
    var showStudentPicker by remember { mutableStateOf(false) }
    var showParentPicker by remember { mutableStateOf(false) }

    MyApplicationTheme(darkTheme = appDarkThemeToggle, dynamicColor = false) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Global top bar with app title and theme customizer!
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, "Oniisan seal", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = brandingConfig.schoolName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            appDarkThemeToggle = !appDarkThemeToggle
                            val modeStr = if (appDarkThemeToggle) "Dark" else "Light"
                            Toast.makeText(context, "$modeStr Mode Activated!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                imageVector = if (appDarkThemeToggle) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Render screens based on state
                Crossfade(targetState = currentActiveScreen, label = "Screen transition") { screen ->
                    when (screen) {
                        ActiveScreen.SPLASH_LOGIN -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = 20.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                // Dynamic branding banner header
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(
                                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                                                        )
                                                    )
                                                )
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(65.dp)
                                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.School,
                                                    contentDescription = "Oniisan Logo",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(35.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                text = brandingConfig.schoolName,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = brandingConfig.motto,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(top = 4.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                // Interactive Quick Navigation card to generic school stats history
                                item {
                                    Button(
                                        onClick = { currentActiveScreen = ActiveScreen.SCHOOL_INFO },
                                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("button_school_info"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiary
                                        )
                                    ) {
                                        Icon(Icons.Default.MenuBook, "Book")
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Explore School Info & Admissions Details", fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                // 4 separate login boards
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Text(
                                            text = "Oniisan Smart Gateways Login",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.outline
                                        )

                                        // STUDENT PANEL ROUTE SWITCHER
                                        Card(
                                            modifier = Modifier.fillMaxWidth().testTag("student_login_card"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                                                    .padding(16.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.School, "Student", tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text("Student Gateway", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                        Text("Attendance, routines, results & AI Carey helper", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Dropdown Student selector to act as real records
                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                    val names = students.map { it.name }
                                                    val selectedStdName = students.find { it.id == selectedStudentSessionId }?.name ?: "Unknown"
                                                    OutlinedButton(
                                                        onClick = { showStudentPicker = true },
                                                        modifier = Modifier.fillMaxWidth().testTag("student_picker_btn"),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Logged in: $selectedStdName")
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(Icons.Default.ArrowDropDown, "down")
                                                    }
                                                    DropdownMenu(
                                                        expanded = showStudentPicker,
                                                        onDismissRequest = { showStudentPicker = false },
                                                        modifier = Modifier.fillMaxWidth(0.81f)
                                                    ) {
                                                        students.forEach { std ->
                                                            DropdownMenuItem(
                                                                text = { Text("${std.name} (${std.className})") },
                                                                onClick = {
                                                                    selectedStudentSessionId = std.id
                                                                    showStudentPicker = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        currentActiveScreen = ActiveScreen.STUDENT_DASHBOARD
                                                        Toast.makeText(context, "Log in verified successfully!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().testTag("student_dashboard_enter_btn")
                                                ) {
                                                    Text("Launch Student Terminal")
                                                }
                                            }
                                        }

                                        // PARENT PANEL GATEWAY
                                        Card(
                                            modifier = Modifier.fillMaxWidth().testTag("parent_login_card"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                                                    .padding(16.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.ChildCare, "Parent", tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text("Parent Gateway", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                        Text("Analyze childhood tracks, grades, consult bookings & payouts", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))

                                                Box(modifier = Modifier.fillMaxWidth()) {
                                                    val names = students.map { "Parent of ${it.name}" }
                                                    val currentChildName = students.find { it.id == selectedStudentSessionId }?.name ?: students.firstOrNull()?.name ?: ""
                                                    OutlinedButton(
                                                        onClick = { showParentPicker = true },
                                                        modifier = Modifier.fillMaxWidth().testTag("parent_picker_btn"),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text("Child: $currentChildName")
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(Icons.Default.ArrowDropDown, "down")
                                                    }
                                                    DropdownMenu(
                                                        expanded = showParentPicker,
                                                        onDismissRequest = { showParentPicker = false },
                                                        modifier = Modifier.fillMaxWidth(0.81f)
                                                    ) {
                                                        students.forEach { std ->
                                                            DropdownMenuItem(
                                                                text = { Text("Parent of ${std.name}") },
                                                                onClick = {
                                                                    selectedStudentSessionId = std.id
                                                                    showParentPicker = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        currentActiveScreen = ActiveScreen.PARENT_DASHBOARD
                                                        Toast.makeText(context, "Log in verified successfully!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().testTag("parent_dashboard_enter_btn")
                                                ) {
                                                    Text("Launch Parent Terminal")
                                                }
                                            }
                                        }

                                        // TEACHER PANEL GATEWAY
                                        Card(
                                            modifier = Modifier.fillMaxWidth().testTag("teacher_login_card"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                                                    .padding(16.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.SupervisedUserCircle, "Teacher", tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text("Teacher Gateway", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                        Text("Conduct routines, post assignments, mark logs & submit grades", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Button(
                                                    onClick = {
                                                        currentActiveScreen = ActiveScreen.TEACHER_DASHBOARD
                                                        Toast.makeText(context, "Logged in as Teacher Satoru Gojo!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().testTag("teacher_dashboard_enter_btn"),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Text("Faculty Portal")
                                                }
                                            }
                                        }

                                        // ADMIN GENERAL PANEL GATEWAY
                                        Card(
                                            modifier = Modifier.fillMaxWidth().testTag("admin_login_card"),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
                                                    .padding(16.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Shield, "Admin", tint = MaterialTheme.colorScheme.error)
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text("Admin Terminal Gateway", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                                                        Text("Manage faculty details, customize brand titles, backups, CCTV views", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(12.dp))
                                                Button(
                                                    onClick = {
                                                        currentActiveScreen = ActiveScreen.ADMIN_DASHBOARD
                                                        Toast.makeText(context, "Full school control authorized!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.fillMaxWidth().testTag("admin_dashboard_enter_btn"),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                ) {
                                                    Text("Establish Admin Session")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ActiveScreen.SCHOOL_INFO -> {
                            SchoolInfoScreen(
                                onBack = { currentActiveScreen = ActiveScreen.SPLASH_LOGIN }
                            )
                        }

                        ActiveScreen.STUDENT_DASHBOARD -> {
                            StudentPanel(
                                selectedStudentId = selectedStudentSessionId,
                                onLogout = { currentActiveScreen = ActiveScreen.SPLASH_LOGIN }
                            )
                        }

                        ActiveScreen.TEACHER_DASHBOARD -> {
                            TeacherPanel(
                                onLogout = { currentActiveScreen = ActiveScreen.SPLASH_LOGIN }
                            )
                        }

                        ActiveScreen.PARENT_DASHBOARD -> {
                            ParentPanel(
                                onLogout = { currentActiveScreen = ActiveScreen.SPLASH_LOGIN }
                            )
                        }

                        ActiveScreen.ADMIN_DASHBOARD -> {
                            AdminPanel(
                                onLogout = { currentActiveScreen = ActiveScreen.SPLASH_LOGIN }
                            )
                        }
                    }
                }
            }
        }
    }
}

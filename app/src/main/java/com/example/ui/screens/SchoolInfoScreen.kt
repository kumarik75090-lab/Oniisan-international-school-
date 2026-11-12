package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SchoolRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolInfoScreen(
    onBack: () -> Unit
) {
    val config by SchoolRepository.config.collectAsState()
    val context = LocalContext.current

    // Enquiry States
    var studentName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf(SchoolRepository.classesList.first()) }
    var parentName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Oniisan School", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Header with school brand name
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.White, CircleShape)
                                    .border(3.dp, MaterialTheme.colorScheme.tertiary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "School Seal Logo",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(45.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = config.schoolName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "“${config.motto}”",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(top = 4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Quick Info highlights
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoHighlightCard(
                        icon = Icons.Default.AccessTime,
                        title = "Timings",
                        subtitle = "8:00 AM - 3:00 PM",
                        modifier = Modifier.weight(1f)
                    )
                    InfoHighlightCard(
                        icon = Icons.Default.CalendarToday,
                        title = "Active Term",
                        subtitle = "2026 Academic Yr",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // History Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Our Rich History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(config.history, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }

            // Principal message
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = "Principal Message", tint = MaterialTheme.colorScheme.tertiary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Principal's Welcome", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Kyotaro Sensei", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "“${config.principalMessage}”",
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Mission and Vision tabs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vision & Core Mission", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Star, contentDescription = "Vision", tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Global Vision", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(config.vision, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Verified, contentDescription = "Mission", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Our Mission", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(config.mission, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            // Uniform details & rule guides
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Apparel & Rules Info", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Divider()
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Checkroom, contentDescription = "Uniform", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Uniform Guidelines", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(config.uniformDetails, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                        Divider()
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.List, contentDescription = "Rules", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Core Rules Code", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("1. Device usage is purely digital academic research.\n2. Dedication to critical logical deduction & active sport is encouraged.\n3. Attendance target: 80% minimum.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }

            // Interactive drawn Sakura Gardens Street campus layout!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Campus Maps & Location", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(config.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(bottom = 12.dp))
                        
                        // Vector Map Drawing
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            val roadColor = Color(0xFF94A3B8)
                            val grassColor = Color(0xFFCBD5E1) // stylized winter sakura map
                            val buildingColor = MaterialTheme.colorScheme.primaryContainer
                            val accentColor = MaterialTheme.colorScheme.tertiary

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw stylized grass background
                                drawRect(color = grassColor, size = size)

                                // Draw Roads
                                drawRect(
                                    color = roadColor,
                                    topLeft = Offset(0f, size.height * 0.4f),
                                    size = Size(size.width, size.height * 0.2f)
                                )
                                drawRect(
                                    color = roadColor,
                                    topLeft = Offset(size.width * 0.45f, 0f),
                                    size = Size(size.width * 0.12f, size.height)
                                )

                                // Green campus park
                                drawCircle(
                                    color = Color(0xFFA7F3D0),
                                    radius = 45f,
                                    center = Offset(size.width * 0.2f, size.height * 0.2f)
                                )

                                // Main School Block drawing
                                drawRect(
                                    color = buildingColor,
                                    topLeft = Offset(size.width * 0.65f, size.height * 0.1f),
                                    size = Size(size.width * 0.28f, size.height * 0.25f)
                                )
                                // Athletic Pitch drawing
                                drawRect(
                                    color = Color(0xFFFED7AA),
                                    topLeft = Offset(size.width * 0.1f, size.height * 0.67f),
                                    size = Size(size.width * 0.32f, size.height * 0.25f)
                                )

                                // Draw Pins
                                drawCircle(
                                    color = accentColor,
                                    radius = 12f,
                                    center = Offset(size.width * 0.79f, size.height * 0.22f)
                                )
                            }
                            
                            // Map Legends Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Oniisan Campus Main Gate", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }

            // Admission Enquiry Form Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("admission_enquiry_form"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Admissions Enquiry Desk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Submit the form below to register your interest for 2026 admissions. Our registrar office will contact you within 48 hours for evaluation slots.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Text(text = "Student Full Name *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            placeholder = { Text("Enter student name") },
                            modifier = Modifier.fillMaxWidth().testTag("student_name_input"),
                            singleLine = true
                        )

                        Text(text = "Class Applied For *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = className,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showClassDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Select class")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { showClassDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showClassDropdown,
                                onDismissRequest = { showClassDropdown = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                SchoolRepository.classesList.forEach { cls ->
                                    DropdownMenuItem(
                                        text = { Text(cls) },
                                        onClick = {
                                            className = cls
                                            showClassDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(text = "Parent/Guardian Name *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it },
                            placeholder = { Text("Parent's name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(text = "Contact Phone No. *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            placeholder = { Text("+81 90-XXXX-XXXX") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(text = "Email Address", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("parent@example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(text = "Additional Comments or Prior School Background", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Transfer info, specific requests etc...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            maxLines = 4
                        )

                        Button(
                            onClick = {
                                if (studentName.isBlank() || parentName.isBlank() || contactPhone.isBlank()) {
                                    Toast.makeText(context, "Please populate all mandated fields (*)", Toast.LENGTH_LONG).show()
                                } else {
                                    isSubmitted = true
                                    Toast.makeText(context, "Enquiry Record Received Successfully!", Toast.LENGTH_LONG).show()
                                    // Reset Fields
                                    studentName = ""
                                    parentName = ""
                                    contactPhone = ""
                                    email = ""
                                    notes = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("submit_enquiry_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Submit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit Interest Enquiry")
                        }

                        AnimatedVisibility(visible = isSubmitted) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, "Success", tint = Color(0xFF059669))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Interest Registered! Reference NIS-2026-619A received. Head Registrar evaluation desk will email you. Thank you!",
                                        fontSize = 12.sp,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoHighlightCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            Text(subtitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

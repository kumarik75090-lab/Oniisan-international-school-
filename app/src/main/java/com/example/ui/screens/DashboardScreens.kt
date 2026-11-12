package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.launch
import java.util.Locale

// ===================================
// STUDENT PANEL
// ===================================

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentPanel(
    selectedStudentId: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val students by SchoolRepository.students.collectAsState()
    val homeworkList by SchoolRepository.homeworkList.collectAsState()
    val notices by SchoolRepository.notices.collectAsState()
    val chatMessages by SchoolRepository.chatbotMessages.collectAsState()

    val currentStudent = students.find { it.id == selectedStudentId } ?: students.firstOrNull() ?: StudentProfile(
        id = "mock",
        name = "Guest Student",
        rollNo = "NIS-000",
        className = "Class 10-A",
        section = "A",
        photoUrl = "",
        attendancePercentage = 80f,
        feeStatus = "Pending"
    )

    // Interaction states
    var uploadedHomeworkIds by remember { mutableStateOf(setOf<String>()) }
    var userDoubtText by remember { mutableStateOf("") }
    var studentDoubtsList by remember { mutableStateOf(listOf("Why is space infinite?", "What is an imaginary number?")) }
    var doubtAnswersMap by remember {
        mutableStateOf(
            mapOf(
                "Why is space infinite?" to "Satoru Gojo: Space appears infinite to match gravity expansion limits!",
                "What is an imaginary number?" to "Koro Sensei: It represents a value that equals a negative when squared, i.e., i² = -1!"
            )
        )
    }

    var chatbotInput by remember { mutableStateOf("") }
    var isBotThinking by remember { mutableStateOf(false) }

    // TTS Setup
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
    }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, "Logout")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exit Student Panel")
                }
            }
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
            // Live Urgent marquee alert (Voice-activated)
            item {
                NoticeBannerWidget(
                    noticeText = "Urgent: Midterm evaluations start June 15. Standard uniforms mandated.",
                    onVoiceAnnounce = {
                        tts?.speak(
                            "Notice from school admin: Urgent, Mid-term evaluations start June 15, Standard uniforms mandated.",
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            null
                        )
                        Toast.makeText(context, "Voice Announcement Triggered", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Student profile view
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Cherry Blossom overlay representation
                        Canvas(modifier = Modifier.matchParentSize()) {
                            drawCircle(Color.White.copy(alpha = 0.04f), radius = 300f, center = Offset(size.width * 0.9f, 0f))
                            drawCircle(Color.White.copy(alpha = 0.05f), radius = 150f, center = Offset(0f, size.height))
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(65.dp)
                                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                        .border(2.dp, Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        "Profile",
                                        tint = Color.White,
                                        modifier = Modifier.size(35.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = currentStudent.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Roll: ${currentStudent.rollNo} | ${currentStudent.className} (${currentStudent.section})",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Attendance Status", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text("${currentStudent.attendancePercentage}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("Account Tuition", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text(
                                        text = currentStudent.feeStatus,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentStudent.feeStatus == "Paid") Color(0xFF81C784) else Color(0xFFFFB74D)
                                    )
                                }
                                Column {
                                    Text("Scholastic Badges", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text("${currentStudent.badges.size} Active", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Interactive student achievement badges row
            item {
                Column {
                    Text("Your Achievements Badges", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentStudent.badges) { badgeName ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EmojiEvents, "Badge", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(badgeName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Grid widgets
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ID badge simulator download
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                Toast.makeText(context, "ID Card PDF downloaded to simulated device storage!", Toast.LENGTH_LONG).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ContactPage, "ID Card", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ID Card Simulator", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Click to Export PDF", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    // Online Class launcher link
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                Toast.makeText(context, "Opening Simulated Live stream Zoom Classroom...", Toast.LENGTH_LONG).show()
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.VideoCameraFront, "Online zoom class", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Online Classes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Join Live Zoom Meeting", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // Subject-wise homework uploads
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AssignmentLate, "Homework", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Active Subject Homework", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val myClassHomework = homeworkList.filter { it.className == currentStudent.className || it.className == "All" }
                        if (myClassHomework.isEmpty()) {
                            Text("Hooray! No pending homework found.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        } else {
                            myClassHomework.forEach { hw ->
                                val isUploaded = uploadedHomeworkIds.contains(hw.id)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUploaded) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(hw.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                            Text("Due: ${hw.dueDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        }
                                        Text(hw.title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text(hw.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Button(
                                                onClick = {
                                                    uploadedHomeworkIds = uploadedHomeworkIds + hw.id
                                                    Toast.makeText(context, "PDF Report successfully uploaded!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.height(30.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isUploaded) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = if (isUploaded) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                                                    contentDescription = "Upload",
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isUploaded) "Uploaded" else "Upload Assignment", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bus tracking tracking GPS animation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBus, "Bus", tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("School Bus Live Tracker", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(currentStudent.transportRoute, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom drawn animated-style progression path
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw tracking line
                                drawLine(
                                    color = Color.LightGray,
                                    start = Offset(50f, size.height / 2f),
                                    end = Offset(size.width - 50f, size.height / 2f),
                                    strokeWidth = 6f
                                )
                                drawLine(
                                    color = Color(0xFFEF6C00),
                                    start = Offset(50f, size.height / 2f),
                                    end = Offset(size.width * 0.65f, size.height / 2f),
                                    strokeWidth = 6f
                                )

                                // Route Station dots
                                drawCircle(Color.Gray, radius = 8f, center = Offset(50f, size.height / 2f))
                                drawCircle(Color.Gray, radius = 8f, center = Offset(size.width * 0.4f, size.height / 2f))
                                drawCircle(Color.Gray, radius = 8f, center = Offset(size.width - 50f, size.height / 2f))

                                // Active Bus Node marker
                                drawCircle(Color(0xFFEF6C00), radius = 14f, center = Offset(size.width * 0.65f, size.height / 2f))
                            }
                            Text(
                                "Station B Express - 3 mins away",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            // Interactive doubts board
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("student_doubt_section"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("Doubt & Question Segment", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Ask your subject teachers direct academic questions.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            studentDoubtsList.forEach { doubt ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Q: $doubt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        val answer = doubtAnswersMap[doubt]
                                        Text(
                                            text = answer ?: "Waiting for teacher response...",
                                            fontSize = 11.sp,
                                            color = if (answer != null) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = userDoubtText,
                                onValueChange = { userDoubtText = it },
                                placeholder = { Text("Ask: Why does binary work?", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (userDoubtText.isNotBlank()) {
                                        studentDoubtsList = studentDoubtsList + userDoubtText
                                        userDoubtText = ""
                                        Toast.makeText(context, "Doubt registered!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ask")
                            }
                        }
                    }
                }
            }

            // Results summary card with performance analytics
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, "Ranks", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Current Term Marks Sheet", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Class: ${currentStudent.className} | Academic Evaluations", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Results table
                        SchoolRepository.studentResults.forEach { r ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.subject, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(r.remarks, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("${r.marksObtained}/${r.totalMarks}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (r.marksObtained >= 90) Color(0xFFC8E6C9) else Color(0xFFFFECB3)
                                        )
                                    ) {
                                        Text(
                                            text = r.grade,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = if (r.marksObtained >= 90) Color(0xFF2E7D32) else Color(0xFF5D4037)
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Performance breakdown (Analytical Graph)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Interactive Custom graphical indicator drawing
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val padding = 40f
                                val colWidth = (size.width - (padding * 2)) / 5f
                                val barMaxHeight = size.height - 20f

                                val marks = listOf(94f, 91f, 86f, 98f, 100f)
                                marks.forEachIndexed { idx, value ->
                                    val pct = value / 100f
                                    val barH = barMaxHeight * pct
                                    // draw stylized chart columns
                                    drawRect(
                                        color = if (pct >= 0.9f) Color(0xFF1565C0) else Color(0xFFEF6C00),
                                        topLeft = Offset(padding + (idx * colWidth) + 10f, barMaxHeight - barH + 5f),
                                        size = Size(colWidth - 20f, barH)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Help Chat Assistant! (direct REST-to-Gemini connection or school-brain backup)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("ai_chatbot_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartButton, "AI Assist", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Oniisan Guide AI Chatbot", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Query policies, routines, uniform guidelines or school rules directly.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Boxed chat stream list
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .verticalScroll(rememberScrollState())
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chatMessages.forEach { msg ->
                                val isModel = msg.role == "model"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isModel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(msg.senderName, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text(msg.message, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                            if (isBotThinking) {
                                Text("Thinking of response...", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatbotInput,
                                onValueChange = { chatbotInput = it },
                                placeholder = { Text("Ask: Uniform rules?", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f).testTag("chatbot_input_field"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (chatbotInput.isNotBlank()) {
                                        val prompt = chatbotInput
                                        chatbotInput = ""
                                        SchoolRepository.addChatMessage(ChatMessage("user", prompt, currentStudent.name))
                                        isBotThinking = true

                                        coroutineScope.launch {
                                            val response = SchoolRepository.getGeminiResponse(prompt)
                                            isBotThinking = false
                                            SchoolRepository.addChatMessage(ChatMessage("model", response, "School Bot"))
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("chatbot_send_btn")
                            ) {
                                Icon(Icons.Default.Send, "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Routine & Time-table schedule listings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("Daily Class Routine Slots", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(10.dp))

                        SchoolRepository.timetable.take(5).forEach { slot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(slot.time, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(slot.subject, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(slot.teacherName, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }

            // Library books & general schedules
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Library books & calendar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Book, "books", tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(currentStudent.libraryStatus, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}


// ===================================
// TEACHER PANEL
// ===================================

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TeacherPanel(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val teachers by SchoolRepository.teachers.collectAsState()
    val students by SchoolRepository.students.collectAsState()
    val homeworkList by SchoolRepository.homeworkList.collectAsState()
    val feedbacks by SchoolRepository.feedbacks.collectAsState()

    var selectedTeacherIndex by remember { mutableStateOf(0) }
    val activeTeacher = teachers.getOrNull(selectedTeacherIndex) ?: teachers.firstOrNull() ?: TeacherProfile(
        id = "t_mock", name = "Teacher", subject = "Gen", qualification = "Degree", experience = "5 Yrs", phone = "", photoUrl = ""
    )

    // Form states for creating homework
    var hwSubject by remember { mutableStateOf("") }
    var hwTitle by remember { mutableStateOf("") }
    var hwClassSelection by remember { mutableStateOf("Class 10-A") }
    var hwDetails by remember { mutableStateOf("") }
    var hwDueDate by remember { mutableStateOf("2026-06-10") }

    // Toggle states for student grading upload template
    var selectedStudentIndexResult by remember { mutableStateOf(0) }
    var editScoreInput by remember { mutableStateOf("95") }
    var editRemarks by remember { mutableStateOf("Splendid progress in physics models") }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, "Exit")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exit Teacher Panel")
                }
            }
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
            // Select active teacher profile simulation
            item {
                Column {
                    Text("Select Active Teacher Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(teachers.size) { idx ->
                            val teacher = teachers[idx]
                            ElevatedCard(
                                onClick = { selectedTeacherIndex = idx },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedTeacherIndex == idx) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.School, teacher.name, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(teacher.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Current Teacher Core Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SupervisedUserCircle, "Teacher Seal", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(activeTeacher.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Major: ${activeTeacher.subject} | exp: ${activeTeacher.experience}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Qualification: ${activeTeacher.qualification}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Ph: ${activeTeacher.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }

            // Student Attendance Marking sheet
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("teacher_attendance_marking"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("Active Attendance Marking Board", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Mark daily checks for Class 10-A students today.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(12.dp))

                        students.forEach { std ->
                            var isPresent by remember { mutableStateOf(true) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${std.name} (${std.rollNo})", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = isPresent,
                                    onCheckedChange = { isPresent = it }
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, "Attendance saved successfully to system logs!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Ledger")
                        }
                    }
                }
            }

            // Upload Homework Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("homework_upload_portal"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Upload New Homework Portfolio", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Text("Homework Title *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = hwTitle,
                            onValueChange = { hwTitle = it },
                            placeholder = { Text("Limits integration homework") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text("Subject *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = hwSubject,
                            onValueChange = { hwSubject = it },
                            placeholder = { Text("Mathematics") },
                            modifier = Modifier.fillMaxWidth().testTag("hw_subject_input"),
                            singleLine = true
                        )

                        Text("Target Class Standard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = hwClassSelection,
                                onValueChange = {},
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true
                            )
                        }

                        Text("Homework Tasks & Guidelines *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = hwDetails,
                            onValueChange = { hwDetails = it },
                            placeholder = { Text("Complete integration calculations...") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                if (hwTitle.isBlank() || hwSubject.isBlank() || hwDetails.isBlank()) {
                                    Toast.makeText(context, "Please complete homework requirements (*)", Toast.LENGTH_SHORT).show()
                                } else {
                                    SchoolRepository.addHomework(
                                        Homework(
                                            id = "hw_" + System.currentTimeMillis(),
                                            subject = hwSubject,
                                            title = hwTitle,
                                            className = hwClassSelection,
                                            description = hwDetails,
                                            dueDate = hwDueDate
                                        )
                                    )
                                    Toast.makeText(context, "Homework homework uploaded!", Toast.LENGTH_SHORT).show()
                                    // Reset fields
                                    hwTitle = ""
                                    hwSubject = ""
                                    hwDetails = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("teacher_hw_submit_btn")
                        ) {
                            Text("Post Student Assignment")
                        }
                    }
                }
            }

            // Grade uploading simulator template
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Online Term Grade Book Editor", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Text("Select Student Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        val stdNames = students.map { it.name }
                        var showDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = stdNames.getOrNull(selectedStudentIndexResult) ?: "",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Select student")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { showDropdown = true }
                            )
                            DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                                stdNames.forEachIndexed { idx, name ->
                                    DropdownMenuItem(text = { Text(name) }, onClick = {
                                        selectedStudentIndexResult = idx
                                        showDropdown = false
                                    })
                                }
                            }
                        }

                        Text("Marks Secured (Out of 100)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editScoreInput,
                            onValueChange = { editScoreInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text("Analytical Remarks", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editRemarks,
                            onValueChange = { editRemarks = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                Toast.makeText(context, "Calculated score successfully published!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileUpload, "Upload")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish Marks Ledger")
                        }
                    }
                }
            }

            // Parent Suggestions reader
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ForwardToInbox, "Inbox", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Feedback & Consultation Inbox", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (feedbacks.isEmpty()) {
                            Text("No complaints received yet.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            feedbacks.forEach { fb ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("From: ${fb.parentName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(fb.timestamp, fontSize = 9.sp, color = Color.Gray)
                                        }
                                        Text("Topic: ${fb.category}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        Text("Message: ${fb.message}", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ===================================
// PARENT PANEL
// ===================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPanel(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val students by SchoolRepository.students.collectAsState()
    val homeworkList by SchoolRepository.homeworkList.collectAsState()
    val notices by SchoolRepository.notices.collectAsState()

    var selectedChildIndex by remember { mutableStateOf(0) }
    val currentChild = students.getOrNull(selectedChildIndex) ?: students.firstOrNull() ?: StudentProfile(
        id = "mock", name = "No Student", rollNo = "0", className = "Nursery", section = "A", photoUrl = "", attendancePercentage = 80f, feeStatus = "Pending"
    )

    // Consult room booking states
    var ptConsultDate by remember { mutableStateOf("2026-06-12") }
    var ptConsultTeacher by remember { mutableStateOf("Gojo Satoru (Physics)") }
    var showPTTeacherDropdown by remember { mutableStateOf(false) }

    // Child leave request form states
    var leaveReason by remember { mutableStateOf("") }
    var leaveDaysRange by remember { mutableStateOf("2026-06-03 to 2026-06-05") }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, "Exit")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exit Parent Panel")
                }
            }
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
            // Child profile picker selector
            item {
                Column {
                    Text("Select Registered Child Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(students.size) { idx ->
                            val child = students[idx]
                            ElevatedCard(
                                onClick = { selectedChildIndex = idx },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedChildIndex == idx) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ChildCare, child.name, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(child.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Child Profile overview index card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("${currentChild.name} - Progress Tracking", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Class Standard", fontSize = 11.sp, color = Color.Gray)
                                Text(currentChild.className, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Register Pin", fontSize = 11.sp, color = Color.Gray)
                                Text(currentChild.rollNo, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Attendance Check", fontSize = 11.sp, color = Color.Gray)
                                Text("${currentChild.attendancePercentage}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Tuition Account Outstanding
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Term Balance Due", fontSize = 11.sp, color = Color.Gray)
                                Text("$${currentChild.feeAmount}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            if (currentChild.feeStatus == "Paid") {
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))) {
                                    Text("PAID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(6.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        SchoolRepository.payFeeForStudent(currentChild.id)
                                        Toast.makeText(context, "Simulated tuition payment successful! Secure ledger updated.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                    modifier = Modifier.testTag("parent_payment_btn")
                                ) {
                                    Text("PAY ONLINE", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Class Schedule and performance bar widget
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("Active Semester Achievements List", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        currentChild.badges.forEach { b ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(Icons.Default.Verified, "Verify", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(b, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Homework check and submission updates
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("Active Tasks & Homework Logs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))

                        val childHw = homeworkList.filter { it.className == currentChild.className || it.className == "All" }
                        if (childHw.isEmpty()) {
                            Text("No pending homework registered for child's class.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            childHw.forEach { ch ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(ch.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Subject: ${ch.subject}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text("Due: ${ch.dueDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }

            // Consult booking PT advisory Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("pt_meeting_booking_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Schedule Parent-Teacher Advisory Meet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Text("Select Target Teacher Advisors", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = ptConsultTeacher,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showPTTeacherDropdown = true }) {
                                        Icon(Icons.Default.ArrowDropDown, "Select advisor")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().clickable { showPTTeacherDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showPTTeacherDropdown,
                                onDismissRequest = { showPTTeacherDropdown = false }
                            ) {
                                DropdownMenuItem(text = { Text("Gojo Satoru (Physics)") }, onClick = {
                                    ptConsultTeacher = "Gojo Satoru (Physics)"
                                    showPTTeacherDropdown = false
                                })
                                DropdownMenuItem(text = { Text("Kakashi Hatake (English)") }, onClick = {
                                    ptConsultTeacher = "Kakashi Hatake (English)"
                                    showPTTeacherDropdown = false
                                })
                                DropdownMenuItem(text = { Text("Koro Sensei (Mathematics)") }, onClick = {
                                    ptConsultTeacher = "Koro Sensei (Mathematics)"
                                    showPTTeacherDropdown = false
                                })
                            }
                        }

                        Text("Select Meeting Date", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = ptConsultDate,
                            onValueChange = { ptConsultDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                Toast.makeText(context, "Consult slot registered with $ptConsultTeacher on $ptConsultDate!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirm Advisory Booking")
                        }
                    }
                }
            }

            // Child leave application form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Post Leave of Absence Form", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Text("Reason for Leave *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = leaveReason,
                            onValueChange = { leaveReason = it },
                            placeholder = { Text("Family health convention...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text("Select Dates of absence *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = leaveDaysRange,
                            onValueChange = { leaveDaysRange = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                if (leaveReason.isBlank() || leaveDaysRange.isBlank()) {
                                    Toast.makeText(context, "Please complete fields", Toast.LENGTH_SHORT).show()
                                } else {
                                    SchoolRepository.addFeedback(
                                        parentName = "Parent of ${currentChild.name}",
                                        childName = currentChild.name,
                                        category = "Leave Request",
                                        message = "Absence Request ($leaveDaysRange). Reason: $leaveReason"
                                    )
                                    Toast.makeText(context, "Leave form submitted!", Toast.LENGTH_SHORT).show()
                                    leaveReason = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Leave Form")
                        }
                    }
                }
            }
        }
    }
}


// ===================================
// ADMIN PANEL
// ===================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanel(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val students by SchoolRepository.students.collectAsState()
    val teachers by SchoolRepository.teachers.collectAsState()
    val visitors by SchoolRepository.visitors.collectAsState()
    val feedbackLogs by SchoolRepository.feedbacks.collectAsState()
    val brandingConfig by SchoolRepository.config.collectAsState()

    // Students registration form states
    var addStdName by remember { mutableStateOf("") }
    var addStdRoll by remember { mutableStateOf("") }
    var addStdClass by remember { mutableStateOf("Class 10-A") }
    var addStdSection by remember { mutableStateOf("A") }

    // Teachers creation form states
    var addTeacherName by remember { mutableStateOf("") }
    var addTeacherSubj by remember { mutableStateOf("") }
    var addTeacherQual by remember { mutableStateOf("") }
    var addTeacherExp by remember { mutableStateOf("") }
    var addTeacherPhone by remember { mutableStateOf("") }

    // Emergency announce alert banner state
    var adminEmergencyInput by remember { mutableStateOf("") }

    // App Branding states
    var editSchoolName by remember { mutableStateOf(brandingConfig.schoolName) }
    var editMotto by remember { mutableStateOf(brandingConfig.motto) }
    var editHistory by remember { mutableStateOf(brandingConfig.history) }

    // Visitor logging states
    var visitorNameInput by remember { mutableStateOf("") }
    var visitorPurposeInput by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, "Exit")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exit Admin Control Panel")
                }
            }
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
            // High fidelity statistics Analytics Graphs (Compose Canvas drawing)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("Oniisan Analytical Terminal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Active enrolment status metrics & attendance comparison.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom analytics split bar chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val barWidth = 60f
                                val spacing = 90f
                                val maxH = size.height - 20f

                                val data = listOf(
                                    Pair("Std Attend", 0.94f),
                                    Pair("Staff Pres", 0.98f),
                                    Pair("Fees Paid", 0.75f)
                                )

                                data.forEachIndexed { i, pair ->
                                    val barH = maxH * pair.second
                                    // columns
                                    drawRect(
                                        color = if (i == 0) Color(0xFF1565C0) else if (i == 1) Color(0xFF42A5F5) else Color(0xFFEF6C00),
                                        topLeft = Offset(i * (barWidth + spacing) + 40f, maxH - barH + 5f),
                                        size = Size(barWidth, barH)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // CCTV monitoring simulator grid thumbnails!
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("cctv_monitoring_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text("CCTV Live Feed Monitors", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Security system (16 channels locked feed layout).", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CctvCameraMock(cameraLabel = "Main Gate #1", modifier = Modifier.weight(1f))
                            CctvCameraMock(cameraLabel = "Science lab #3", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Broadcast priority scrolling emergency announcement
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("admin_emergency_section"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Priority Emergency Marquee Broadcast", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Row {
                            OutlinedTextField(
                                value = adminEmergencyInput,
                                onValueChange = { adminEmergencyInput = it },
                                placeholder = { Text("Emergency: Heavy snowfall tomorrow, closure announced.", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (adminEmergencyInput.isNotBlank()) {
                                        SchoolRepository.addNotice(
                                            Notice(
                                                id = "n_" + System.currentTimeMillis(),
                                                title = "Emergency: $adminEmergencyInput",
                                                category = "Emergency",
                                                date = "2026-05-29",
                                                content = adminEmergencyInput
                                            )
                                        )
                                        Toast.makeText(context, "Scrolling banner posted!", Toast.LENGTH_SHORT).show()
                                        adminEmergencyInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ALERT")
                            }
                        }
                    }
                }
            }

            // Application customization form (Editable layout settings)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("branding_customization_portal"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("School App Branding Customization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Customize default headings and mottos across the system.", fontSize = 11.sp, color = Color.Gray)

                        Text("School Brand Title *", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editSchoolName,
                            onValueChange = { editSchoolName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text("Educational Motto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editMotto,
                            onValueChange = { editMotto = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text("Campus historical notes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = editHistory,
                            onValueChange = { editHistory = it },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                SchoolRepository.updateConfig(
                                    brandingConfig.copy(
                                        schoolName = editSchoolName,
                                        motto = editMotto,
                                        history = editHistory
                                    )
                                )
                                Toast.makeText(context, "Dynamic credentials published successfully!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_settings_btn")
                        ) {
                            Text("Save Customized Configuration", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Student registry (CRUD list + add dialog form!)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("admin_student_ledger"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Active Student Roster Management", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        // Registration inputs
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Admit New Student Form", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = addStdName,
                                onValueChange = { addStdName = it },
                                placeholder = { Text("Zenitsu Agatsuma") },
                                label = { Text("Student Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = addStdRoll,
                                    onValueChange = { addStdRoll = it },
                                    placeholder = { Text("NIS-201") },
                                    label = { Text("Roll No") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = addStdClass,
                                    onValueChange = { addStdClass = it },
                                    placeholder = { Text("Class 10-A") },
                                    label = { Text("Class Standard") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Button(
                                onClick = {
                                    if (addStdName.isBlank() || addStdRoll.isBlank()) {
                                        Toast.makeText(context, "Populate all fields!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        SchoolRepository.addStudent(
                                            StudentProfile(
                                                id = "std_" + System.currentTimeMillis(),
                                                name = addStdName,
                                                rollNo = addStdRoll,
                                                className = addStdClass,
                                                section = addStdSection,
                                                photoUrl = "",
                                                attendancePercentage = 95f,
                                                feeStatus = "Pending"
                                            )
                                        )
                                        Toast.makeText(context, "Student roster updated!", Toast.LENGTH_SHORT).show()
                                        addStdName = ""
                                        addStdRoll = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Enrol Admittance")
                            }
                        }

                        // Current list of students with delete
                        Text("Registered Students Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        students.forEach { std ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(std.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Roll: ${std.rollNo} | ${std.className}", fontSize = 11.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { SchoolRepository.removeStudent(std.id) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }

            // Teacher registry (Teacher cards adding + qualification / qualification upload checklist)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("admin_teacher_ledger"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Active Faculty Roster Management", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Hire New Faculty Member", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = addTeacherName,
                                onValueChange = { addTeacherName = it },
                                placeholder = { Text("Kyojuro Rengoku") },
                                label = { Text("Teacher Name") },
                                modifier = Modifier.fillMaxWidth().testTag("add_teacher_name_input"),
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = addTeacherSubj,
                                    onValueChange = { addTeacherSubj = it },
                                    placeholder = { Text("Combustion & Energy") },
                                    label = { Text("Subject") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = addTeacherQual,
                                    onValueChange = { addTeacherQual = it },
                                    placeholder = { Text("M.Tech Thermal Labs") },
                                    label = { Text("Qualification") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = addTeacherExp,
                                    onValueChange = { addTeacherExp = it },
                                    placeholder = { Text("12 Years") },
                                    label = { Text("Experience") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = addTeacherPhone,
                                    onValueChange = { addTeacherPhone = it },
                                    placeholder = { Text("+81-2821") },
                                    label = { Text("Phone") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Button(
                                onClick = {
                                    if (addTeacherName.isBlank() || addTeacherSubj.isBlank() || addTeacherQual.isBlank()) {
                                        Toast.makeText(context, "Populate all mandated fields!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        SchoolRepository.addTeacher(
                                            TeacherProfile(
                                                id = "t_" + System.currentTimeMillis(),
                                                name = addTeacherName,
                                                subject = addTeacherSubj,
                                                qualification = addTeacherQual,
                                                experience = addTeacherExp,
                                                phone = addTeacherPhone,
                                                photoUrl = ""
                                            )
                                        )
                                        Toast.makeText(context, "New faculty card published!", Toast.LENGTH_SHORT).show()
                                        addTeacherName = ""
                                        addTeacherSubj = ""
                                        addTeacherQual = ""
                                        addTeacherExp = ""
                                        addTeacherPhone = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.End).testTag("add_teacher_btn")
                            ) {
                                Text("Approve Hiring")
                            }
                        }

                        // Listed teachers card deck with delete options
                        Text("Current Campus Faculty Roster", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        teachers.forEach { teacher ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(teacher.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Subj: ${teacher.subject} | Ph: ${teacher.phone}", fontSize = 11.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { SchoolRepository.removeTeacher(teacher.id) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }

            // Visitor management logs register
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("visitor_records_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Visitor Management Terminal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Keep trace of external personnel entering Sakura Gates campus grounds.", fontSize = 11.sp, color = Color.Gray)

                        OutlinedTextField(
                            value = visitorNameInput,
                            onValueChange = { visitorNameInput = it },
                            placeholder = { Text("E.g. Inosuke Hashibira") },
                            label = { Text("Visitor Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = visitorPurposeInput,
                            onValueChange = { visitorPurposeInput = it },
                            placeholder = { Text("Deliver lunch boxes etc...") },
                            label = { Text("Entry Purpose") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (visitorNameInput.isNotBlank() && visitorPurposeInput.isNotBlank()) {
                                    SchoolRepository.addVisitor(visitorNameInput, visitorPurposeInput)
                                    Toast.makeText(context, "Checked inside sakura gate!", Toast.LENGTH_SHORT).show()
                                    visitorNameInput = ""
                                    visitorPurposeInput = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Register Gate Pass Check-in")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Active Campus Visitors Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        visitors.forEach { v ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(v.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Reason: ${v.purpose}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(v.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Backup & Restore system modules
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("backup_restore_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Local Backup & Cloud Synced Restore", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Synchronize roster tables directly with secure offsite archives.", fontSize = 11.sp, color = Color.Gray)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Full Roster JSON Backup saved to temporary device memory!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Back-up DB JSON", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Full dynamic rosters restored from high-grade backup stack!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text("Restore Database", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper Widgets
@Composable
fun NoticeBannerWidget(
    noticeText: String,
    onVoiceAnnounce: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Campaign, "Notice Alert", tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = noticeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onVoiceAnnounce) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Voice announce notice statement",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CctvCameraMock(cameraLabel: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .background(Color.Black, RoundedCornerShape(8.dp))
            .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw retro radar grid
            drawLine(Color.White.copy(alpha = 0.07f), start = Offset(0f, 0f), end = Offset(size.width, size.height))
            drawLine(Color.White.copy(alpha = 0.07f), start = Offset(0f, size.height), end = Offset(size.width, 0f))
        }
        Text(
            cameraLabel,
            fontSize = 10.sp,
            color = Color.Green,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
        )
        Text(
            "REC ●",
            fontSize = 9.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
        )
    }
}

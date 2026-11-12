package com.example.data

import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ==========================================
// DATA MODELS
// ==========================================

data class SchoolConfig(
    val schoolName: String = "Oniisan International School",
    val motto: String = "Knowledge, Discipline, and Innovation",
    val logoUri: String? = null, // Custom image upload placeholder URI
    val bannerUri: String? = null,
    val primaryColorHex: String = "#1565C0",
    val secondaryColorHex: String = "#EF6C00",
    val history: String = "Established in 2015, Oniisan International School was founded with a vision to provide world-class bilingual education that fuses scientific excellence with cultural character.",
    val principalMessage: String = "Welcome to Oniisan! We are dedicated to delivering academic and technical education that inspires kids to tackle tomorrow's global challenges. Our smart management portal connects students, teachers, and parents seamlessly.",
    val vision: String = "To cultivate critical thinkers and compassionate global leaders who excel in research, logic, and interpersonal wisdom.",
    val mission: String = "To integrate advanced digital interfaces, experiential stem modules, and rich athletic and artistic curriculums into daily student routines.",
    val schoolTimings: String = "Monday to Friday: 8:00 AM - 3:00 PM | Saturday: 8:00 AM - 12:30 PM",
    val uniformDetails: String = "Summer: White short-sleeve shirt with royal blue collars, grey trousers/skirts, orange ties.\nWinter: Royal blue blazers with embroidered school crest, dark grey trousers.",
    val contactPhone: String = "+81 90-5555-0123",
    val contactEmail: String = "contact@oniisan-international.edu",
    val address: String = "12-4 Sakura Garden Avenue, Minato-ku, Tokyo, Japan",
    val cctvStatus: String = "Online - 16 Channels Active (Restricted Admin View Only)"
)

data class StudentProfile(
    val id: String,
    val name: String,
    val rollNo: String,
    val className: String, // e.g. "Class 10-A"
    val section: String, // A/B/C
    val photoUrl: String, // Decorative anime profiles or placeholders
    val attendancePercentage: Float,
    val feeStatus: String, // "Paid" or "Pending"
    val feeAmount: Double = 1250.00,
    val libraryStatus: String = "2 Books Borrowed (Return by next Monday)",
    val performanceProgress: Float = 0.88f, // 0.0 to 1.0
    val badges: List<String> = listOf("Perfect Attendance", "Math Wizard", "Ethics Award"),
    val transportRoute: String = "School Bus Route #5 - Station A Express"
)

data class TeacherProfile(
    val id: String,
    val name: String,
    val subject: String,
    val qualification: String,
    val experience: String,
    val phone: String,
    val photoUrl: String,
    val biography: String = "Dedicated educator specializing in modern interactive pedagogy.",
    val attendanceStatus: String = "Present"
)

data class Homework(
    val id: String,
    val subject: String,
    val title: String,
    val className: String,
    val description: String,
    val dueDate: String,
    val attachments: List<String> = emptyList()
)

data class TimetableSlot(
    val day: String, // "Monday", "Tuesday", etc.
    val time: String, // "08:00 - 09:00"
    val subject: String,
    val teacherName: String
)

data class Notice(
    val id: String,
    val title: String,
    val category: String, // "Holiday", "Exam", "Announcements", "Emergency"
    val date: String,
    val content: String,
    val author: String = "School Administration"
)

data class ExamScheduleItem(
    val subject: String,
    val examDate: String,
    val timing: String,
    val location: String = "Examination Hall North"
)

data class SubjectResult(
    val subject: String,
    val marksObtained: Int,
    val totalMarks: Int = 100,
    val grade: String,
    val remarks: String
)

data class VisitorRecord(
    val id: String,
    val name: String,
    val purpose: String,
    val timestamp: String
)

data class ParentFeedback(
    val id: String,
    val parentName: String,
    val childName: String,
    val category: String, // Complaint/Suggestion/Feedback
    val message: String,
    val timestamp: String
)

data class ChatMessage(
    val role: String, // "user" or "model"
    val message: String,
    val senderName: String,
    val timestamp: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

// ==========================================
// REACTIVE CENTRALIZED REPOSITORY (Editable)
// ==========================================

object SchoolRepository {

    // --- Configurations & App Customizations ---
    private val _config = MutableStateFlow(SchoolConfig())
    val config: StateFlow<SchoolConfig> = _config.asStateFlow()

    fun updateConfig(newConfig: SchoolConfig) {
        _config.value = newConfig
    }

    // --- Students List ---
    private val _students = MutableStateFlow<List<StudentProfile>>(emptyList())
    val students: StateFlow<List<StudentProfile>> = _students.asStateFlow()

    // --- Teachers List ---
    private val _teachers = MutableStateFlow<List<TeacherProfile>>(emptyList())
    val teachers: StateFlow<List<TeacherProfile>> = _teachers.asStateFlow()

    // --- Notice List ---
    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    // --- Homework List ---
    private val _homeworkList = MutableStateFlow<List<Homework>>(emptyList())
    val homeworkList: StateFlow<List<Homework>> = _homeworkList.asStateFlow()

    // --- Visitors Log ---
    private val _visitors = MutableStateFlow<List<VisitorRecord>>(emptyList())
    val visitors: StateFlow<List<VisitorRecord>> = _visitors.asStateFlow()

    // --- Feedbacks / Complaints ---
    private val _feedbacks = MutableStateFlow<List<ParentFeedback>>(emptyList())
    val feedbacks: StateFlow<List<ParentFeedback>> = _feedbacks.asStateFlow()

    // --- Classes list ---
    val classesList = listOf(
        "Nursery", "Kindergarten", "Class 1", "Class 2", "Class 3", 
        "Class 4", "Class 5", "Class 6", "Class 7", "Class 8", 
        "Class 9", "Class 10-A", "Class 11-A", "Class 12-A"
    )

    // --- Default Schedules by Class Name ---
    val timetable = listOf(
        TimetableSlot("Monday", "08:30 - 09:30", "Mathematics", "Gojo Satoru"),
        TimetableSlot("Monday", "09:30 - 10:30", "Physics", "Gojo Satoru"),
        TimetableSlot("Monday", "11:00 - 12:00", "English Literature", "Kakashi Hatake"),
        TimetableSlot("Monday", "13:00 - 14:00", "Bilingual Japanese", "Uchiha Itachi"),
        
        TimetableSlot("Tuesday", "08:30 - 09:30", "Chemistry", "Orochimaru Dr."),
        TimetableSlot("Tuesday", "09:30 - 10:30", "Mathematics", "Gojo Satoru"),
        TimetableSlot("Tuesday", "11:00 - 12:00", "Computer Science", "Koro Sensei"),
        
        TimetableSlot("Wednesday", "08:30 - 09:30", "Ninjutsu & Tactics", "Kakashi Hatake"),
        TimetableSlot("Wednesday", "11:00 - 12:00", "Art & Craft", "Deidara Clay"),
        TimetableSlot("Wednesday", "13:00 - 14:30", "Physical Education", "Guy Sensei"),
        
        TimetableSlot("Thursday", "08:30 - 09:30", "Physics", "Gojo Satoru"),
        TimetableSlot("Thursday", "09:30 - 10:30", "Mathematics", "Gojo Satoru"),
        TimetableSlot("Thursday", "11:00 - 12:00", "History", "Madara Uchiha"),
        
        TimetableSlot("Friday", "08:30 - 09:30", "Computer Science Labs", "Koro Sensei"),
        TimetableSlot("Friday", "11:00 - 12:00", "Ethics & Meditation", "Iruka Sensei")
    )

    val examSchedule = listOf(
        ExamScheduleItem("Mathematics", "2026-06-15", "09:00 AM - 12:00 PM"),
        ExamScheduleItem("Physics", "2026-06-17", "09:00 AM - 12:00 PM"),
        ExamScheduleItem("English Literature", "2026-06-19", "09:00 AM - 11:30 AM"),
        ExamScheduleItem("Bilingual Japanese", "2026-06-22", "09:00 AM - 12:00 PM"),
        ExamScheduleItem("Computer Science", "2026-06-24", "01:00 PM - 04:00 PM")
    )

    val studentResults = listOf(
        SubjectResult("Mathematics", 94, 100, "A+", "Exceptional problem-solving speed."),
        SubjectResult("Physics", 91, 100, "A", "Deep intuition in mechanical waves."),
        SubjectResult("English Literature", 86, 100, "B+", "Great creative analytical papers."),
        SubjectResult("Bilingual Japanese", 98, 100, "A+", "Flawless grammar and vocabulary usage."),
        SubjectResult("Computer Science", 100, 100, "A+", "Mastery of code recursion algorithms.")
    )

    // --- Holiday Calendar (Prepopulated) ---
    val holidaysList = mutableStateListOf(
        Notice("H1", "Summer Vacation 2026", "Holiday", "2026-07-10 to 22", "Annual school summer vacation for all standards."),
        Notice("H2", "Tanabata Stargazing Holiday", "Holiday", "2026-07-07", "Stargazing and bamboo wish-writing festival."),
        Notice("H3", "Golden Week Special Holiday", "Holiday", "2026-05-01 to 05", "National celebrations holiday."),
        Notice("H4", "Emergency Weather Shutdown", "Holiday", "2026-05-20", "Typhoon warning safety school closing notice.")
    )

    // --- Chat logs for Chatbot ---
    private val _chatbotMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("model", "Hello! Welcome to the Oniisan International School Helper AI. How can I assist you today? I know about uniform policies, history, routines, and can help query your marks or exam dates!", "Oniisan Care Bot")
    ))
    val chatbotMessages: StateFlow<List<ChatMessage>> = _chatbotMessages.asStateFlow()

    init {
        loadDemoData()
    }

    private fun loadDemoData() {
        // Preload Demo Students
        _students.value = listOf(
            StudentProfile(
                id = "std_01",
                name = "Naruto Uzumaki",
                rollNo = "NIS-001",
                className = "Class 10-A",
                section = "A",
                photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop",
                attendancePercentage = 84.5f,
                feeStatus = "Pending",
                feeAmount = 1480.00,
                badges = listOf("Ninja Grit Award", "Athletic Champion")
            ),
            StudentProfile(
                id = "std_02",
                name = "Sasuke Uchiha",
                rollNo = "NIS-002",
                className = "Class 10-A",
                section = "A",
                photoUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop",
                attendancePercentage = 98.2f,
                feeStatus = "Paid",
                feeAmount = 1550.00,
                badges = listOf("Top Scholar 2026", "Absolute Logic Master")
            ),
            StudentProfile(
                id = "std_03",
                name = "Sakura Haruno",
                rollNo = "NIS-003",
                className = "Class 10-A",
                section = "A",
                photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop",
                attendancePercentage = 95.0f,
                feeStatus = "Paid",
                feeAmount = 1250.00,
                badges = listOf("Perfect Attendance", "Biology Wonder")
            ),
            StudentProfile(
                id = "std_04",
                name = "Tanjiro Kamado",
                rollNo = "NIS-007",
                className = "Class 12-A",
                section = "A",
                photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop",
                attendancePercentage = 100.0f,
                feeStatus = "Paid",
                feeAmount = 1250.00,
                badges = listOf("Pure Ethics Award", "Community Hero")
            ),
            StudentProfile(
                id = "std_05",
                name = "Nezuko Kamado",
                rollNo = "NIS-008",
                className = "Class 11-A",
                section = "B",
                photoUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop",
                attendancePercentage = 90.5f,
                feeStatus = "Paid",
                feeAmount = 1250.00,
                badges = listOf("Art Prodigy")
            )
        )

        // Preload Demo Teachers (Admin-editable cards)
        _teachers.value = listOf(
            TeacherProfile(
                id = "t_01",
                name = "Gojo Satoru",
                subject = "Physics & Space",
                qualification = "PhD in Space-Time Astrophysics",
                experience = "10 Years",
                phone = "+81 90-8888-0000",
                photoUrl = "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop",
                biography = "Passionate physicist showing how concepts like gravity and atomic matrices apply to daily engineering. Enjoys pastries."
            ),
            TeacherProfile(
                id = "t_02",
                name = "Kakashi Hatake",
                subject = "English & Strategy",
                qualification = "MA in Tactical Literature",
                experience = "15 Years",
                phone = "+81 80-2222-3333",
                photoUrl = "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=150&auto=format&fit=crop",
                biography = "Former tactical leader specializing in rapid bilingual parsing and creative writing methods."
            ),
            TeacherProfile(
                id = "t_03",
                name = "Koro Sensei",
                subject = "Mathematics & Computing",
                qualification = "Cosmic Computing Master",
                experience = "8 Years",
                phone = "+81 70-5555-8888",
                photoUrl = "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&auto=format&fit=crop",
                biography = "Adapts mathematical calculations at lightning speeds to match student learning curves. Enthusiastic coder."
            )
        )

        // Preload Homework
        _homeworkList.value = listOf(
            Homework(
                id = "hw_01",
                subject = "Mathematics",
                title = "Calculus limits & recursion homework",
                className = "Class 10-A",
                description = "Solve problems 1-15 on Page 84. Pay clean attention to convergence properties of nested loops.",
                dueDate = "2026-06-03"
            ),
            Homework(
                id = "hw_02",
                subject = "Physics",
                title = "Newtonian fluid dynamics research",
                className = "Class 10-A",
                description = "Write a 300-word executive summary about shear stress configurations. Upload standard PDF format in portal.",
                dueDate = "2026-06-05"
            ),
            Homework(
                id = "hw_03",
                subject = "Bilingual Japanese",
                title = "Japanese classical poetry kanji analysis",
                className = "Class 10-A",
                description = "Analyze syntax and historical indicators of the Sakura Season collection.",
                dueDate = "2026-06-09"
            )
        )

        // Preload School Notices
        _notices.value = listOf(
            Notice(
                id = "n_01",
                title = "Welcome Message from Principal Kyoutaro",
                category = "Announcements",
                date = "2026-05-25",
                content = "We welcome all our students and parents to the 2026 Academic year! Oniisan International School is proud to integrate custom dynamic visual systems, comprehensive interactive dashboards, and cloud-simulation portals to empower learning."
            ),
            Notice(
                id = "n_02",
                title = "Summer Cultural Fair Schedule",
                category = "Exam",
                date = "2026-05-28",
                content = "The registration for the 2026 Annual Cultural Exhibition and Coding Carnival is now live. Open entry is available for all grades from elementary to advanced Class 12."
            ),
            Notice(
                id = "n_03",
                title = "Urgent: CCTV and Portal Software Updates",
                category = "Emergency",
                date = "2026-05-29",
                content = "The Oniisan Smart Portal software has been successfully upgraded to version 2.5, featuring deep adaptive gradients, direct speech synthesis text indicators, parent feedback channels, and simulated bus routing trackers.",
                author = "IT Admin Center"
            )
        )

        // Preload Visitors record
        _visitors.value = listOf(
            VisitorRecord("v_1", "Minato Namikaze", "Alumni Association Conference", "09:30 AM"),
            VisitorRecord("v_2", "Kushina Uzumaki", "Parent-Teacher Advisory Panel", "11:15 AM")
        )

        // Preload Complaints/Feedbacks
        _feedbacks.value = listOf(
            ParentFeedback("f_1", "Kushina Uzumaki", "Naruto Uzumaki", "Suggestion", "Please expand healthy ramen cafeteria options during final exams!", "2026-05-29 11:30 AM"),
            ParentFeedback("f_2", "Fugaku Uchiha", "Sasuke Uchiha", "Feedback", "Extremely pleased with Mr. Gojo's astrophysics teaching quality!", "2026-05-29 02:45 PM")
        )
    }

    // ==========================================
    // DATA EDITING / CREATION API (ADMIN/TEACHER)
    // ==========================================

    // Students CRUD
    fun addStudent(student: StudentProfile) {
        _students.value = _students.value + student
    }

    fun updateStudent(updated: StudentProfile) {
        _students.value = _students.value.map { if (it.id == updated.id) updated else it }
    }

    fun removeStudent(id: String) {
        _students.value = _students.value.filter { it.id != id }
    }

    // Teachers CRUD
    fun addTeacher(teacher: TeacherProfile) {
        _teachers.value = _teachers.value + teacher
    }

    fun updateTeacher(updated: TeacherProfile) {
        _teachers.value = _teachers.value.map { if (it.id == updated.id) updated else it }
    }

    fun removeTeacher(id: String) {
        _teachers.value = _teachers.value.filter { it.id != id }
    }

    // Notices CRUD
    fun addNotice(notice: Notice) {
        _notices.value = listOf(notice) + _notices.value
    }

    fun removeNotice(id: String) {
        _notices.value = _notices.value.filter { it.id != id }
    }

    // Homework CRUD
    fun addHomework(hw: Homework) {
        _homeworkList.value = listOf(hw) + _homeworkList.value
    }

    fun removeHomework(id: String) {
        _homeworkList.value = _homeworkList.value.filter { it.id != id }
    }

    // Visitors Logs
    fun addVisitor(name: String, purpose: String) {
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _visitors.value = _visitors.value + VisitorRecord(
            id = "v_" + System.currentTimeMillis(),
            name = name,
            purpose = purpose,
            timestamp = timeStr
        )
    }

    // Feedbacks
    fun addFeedback(parentName: String, childName: String, category: String, message: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
        _feedbacks.value = _feedbacks.value + ParentFeedback(
            id = "fb_" + System.currentTimeMillis(),
            parentName = parentName,
            childName = childName,
            category = category,
            message = message,
            timestamp = dateStr
        )
    }

    // Pay Fee method
    fun payFeeForStudent(studentId: String) {
        val list = _students.value.map {
            if (it.id == studentId) {
                it.copy(feeStatus = "Paid")
            } else it
        }
        _students.value = list
    }

    // Clear chatbot logs
    fun clearChat() {
        _chatbotMessages.value = listOf(
            ChatMessage("model", "Chat reset. How can I assist you today, Oniisan student?", "Oniisan Care Bot")
        )
    }

    // ==========================================
    // GEMINI Chatbot Integration
    // ==========================================
    suspend fun getGeminiResponse(userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            // Return high-quality, simulated smart backup matching context if api key is missing to guarantee pristine offline behavior
            return@withContext parseOfflineSchoolBrain(userPrompt)
        }

        // Setup Direct REST API Request using OkHttp (as mandated by DIRECT REST API option B of gemini-api skill)
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val jsonRequest = JSONObject()
        val contentsArray = JSONArray()
        val contentObject = JSONObject()
        val partsArray = JSONArray()
        val partTextObject = JSONObject()

        partTextObject.put("text", userPrompt)
        partsArray.put(partTextObject)
        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        jsonRequest.put("contents", contentsArray)

        // System Instruction to feed school details
        val systemInstructionObj = JSONObject()
        val sysPartsArr = JSONArray()
        val sysPartTextObj = JSONObject()
        sysPartTextObj.put("text", """
            You are the smart helper chatbot of Oniisan International School.
            The school history is: ${_config.value.history}
            The principal's message is: ${_config.value.principalMessage}
            The Uniform instructions are: ${_config.value.uniformDetails}
            The Timings are: ${_config.value.schoolTimings}
            Our address is: ${_config.value.address}.
            Be extremely encouraging, student-friendly, and polite. Give educational and school-supportive replies.
        """.trimIndent())
        sysPartsArr.put(sysPartTextObj)
        systemInstructionObj.put("parts", sysPartsArr)
        jsonRequest.put("systemInstruction", systemInstructionObj)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toString().toRequestBody(mediaType)

        // Request
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseString = response.body?.string() ?: ""
                val jsonObj = JSONObject(responseString)
                val candidates = jsonObj.getJSONArray("candidates")
                val firstCandidate = candidates.getJSONObject(0)
                val responseContent = firstCandidate.getJSONObject("content")
                val responseParts = responseContent.getJSONArray("parts")
                responseParts.getJSONObject(0).getString("text")
            } else {
                Log.e("OniisanChat", "Gemini HTTP failed with code: ${response.code}")
                // Fallback to offline rule-based dictionary
                parseOfflineSchoolBrain(userPrompt)
            }
        } catch (e: Exception) {
            Log.e("OniisanChat", "Gemini error: ${e.message}")
            parseOfflineSchoolBrain(userPrompt)
        }
    }

    private fun parseOfflineSchoolBrain(prompt: String): String {
        val p = prompt.lowercase(Locale.getDefault())
        return when {
            p.contains("uniform") -> "The Oniisan uniform is: ${_config.value.uniformDetails}"
            p.contains("timing") || p.contains("time") -> "School operates daily in these hours: ${_config.value.schoolTimings}"
            p.contains("principal") || p.contains("message") -> "Message from Principal Kyoutaro: '${_config.value.principalMessage}'"
            p.contains("history") || p.contains("establish") -> "About our heritage: ${_config.value.history}"
            p.contains("rule") -> "Standard Oniisan Rules:\n1. Maintain absolute research and tactical focus.\n2. Respect your upperclassmen ('Senpai').\n3. Device-usage is strictly meant for educational purposes."
            p.contains("admission") -> "Admission enquiries are open now! Fill our prompt enquiry form in the 'School Info' section to schedule an evaluation interview."
            p.contains("contact") || p.contains("email") -> "Contact us at ${_config.value.contactEmail} or call ${_config.value.contactPhone}."
            p.contains("math") || p.contains("calculator") -> "Our computational master, Koro Sensei, advises breaking larger algebraic expressions into simple iterative fragments!"
            p.contains("gojo") || p.contains("physics") -> "Mr. Satoru Gojo says: Study with an open mindset! The universe doesn't have borders, why should your thinking?"
            p.contains("fee") || p.contains("bill") -> "You can pay fees and check due dates in the Parent and Student Panels. Major debit and credit gateways are fully integrated!"
            p.contains("cctv") -> "CCTV monitoring placeholder is active. Admin controls show active feeds for classrooms 1 through 12."
            p.contains("hello") || p.contains("hi") || p.contains("hey") -> "Hello! I am the Oniisan International School Helper AI! You can ask me about Uniforms, Timelines, History, Rules, or Contact Details!"
            else -> "That is a great question! At Oniisan International School, we encourage exploring resources in the library, or consulting Satoru Gojo or Kakashi Sensei during free class periods. Let me know if you want details on timetables, rule sets, or holidays!"
        }
    }

    fun addChatMessage(msg: ChatMessage) {
        _chatbotMessages.value = _chatbotMessages.value + msg
    }
}

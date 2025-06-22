package com.example.clockinatwork

import AppDatabase
import WorkRecord
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clockinatwork.ui.theme.ClockInAtWorkTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 关键设置：内容延伸至状态栏，并设置透明背景
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.Transparent.toArgb() // 透明状态栏
        setContent {
            ClockInAtWorkTheme {
                val viewModel: WorkTimeViewModel = viewModel(
                    factory = WorkTimeViewModelFactory(
                        AppDatabase.getDatabase(this)
                    )
                )
                WorkTimeTrackerApp(viewModel)
//                Surface(modifier = Modifier.fillMaxSize()) {
//                    CalendarScreen()
//                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkTimeTrackerApp(viewModel: WorkTimeViewModel) {
    val records by viewModel.allRecords.collectAsState(initial = emptyList())
    val latestRecord by viewModel.latestRecord.collectAsState(initial = null)
    val minuteFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    minuteFormat.timeZone = timeZone
    dateFormat.timeZone = timeZone
    monthFormat.timeZone = timeZone
    val statusBarHeightPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarHeightDp)
            .background(Color.Black.copy(alpha = 0.5f)) // 半透明遮罩
    )

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // 模拟弹性阻尼效果（下拉/上拉时偏移量递减）
                    val offset = available.y * 0.3f // 阻尼系数（0~1，越小阻力越大）
                    return Offset(0f, offset)
                }
            }
        }

        // 记录列表
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .nestedScroll(nestedScrollConnection)
                .padding(bottom = 80.dp)
        ) {
            records.forEach { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,  // 使用主题定义的圆角（默认 4dp）
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,  // 卡片背景色
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 15.dp)
                ) {
                    RecordItem(record, minuteFormat)
                }
            }
        }

        // 固定在底部的按钮
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomEnd)
        ) {
            Button(
                onClick = {
                    viewModel.deleteAllRecord()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text(text = "清空数据", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(8.dp)) // 添加间距

            Button(
                onClick = {
                    val nowTime = Date()
                    if (latestRecord?.date == dateFormat.format(nowTime)) {
                        latestRecord!!.offDutyTime = nowTime.time
                        latestRecord!!.calculateWorkingHours()
                        viewModel.updateRecord(latestRecord!!)
                    } else {
                        viewModel.insertRecord(
                            WorkRecord(
                                date = dateFormat.format(nowTime),
                                monthDate = monthFormat.format(nowTime),
                                dutyTime = nowTime.time,
                                offDutyTime = null,
                                workingHours = null
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "打卡",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // 添加间距
        }
    }
}

@Composable
fun RecordItem(record: WorkRecord, timeFormat: SimpleDateFormat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = record.date,
            fontSize = 18.sp
        )
        Text(
            text = "上班时间：" + timeFormat.format(Date(record.dutyTime)),
            color = Color(0xFF4CAF50),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        record.offDutyTime?.let { time ->
            Text(
                text = "下班时间：" + timeFormat.format(Date(time)),
                color = Color(0xFFF44336),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        record.workingHours?.let { hours ->
            Text(
                text = "工作时长: $hours 小时",
                fontSize = 14.sp
            )
        }
    }
}

// ViewModel
class WorkTimeViewModel(private val db: AppDatabase) : ViewModel() {
    val allRecords: Flow<List<WorkRecord>> = db.workRecordDao().getAllRecords()
    val latestRecord: Flow<WorkRecord?> = db.workRecordDao().getLeastRecord()

    fun insertRecord(record: WorkRecord) {
        viewModelScope.launch {
            db.workRecordDao().insert(record)
        }
    }

    fun deleteAllRecord() {
        viewModelScope.launch {
            db.workRecordDao().deleteAllRecord()
        }
    }

    fun updateRecord(record: WorkRecord) {
        viewModelScope.launch {
            val isSuccess = db.workRecordDao().update(record) > 0
            if (isSuccess) {
                // dosomething
            } else {
                // dosomething
            }
        }
    }
}

// ViewModel Factory
class WorkTimeViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkTimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkTimeViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.clockinatwork.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CalendarScreen() {
    // 当前显示的月份
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // 控制月份选择对话框的显示
    var showDatePicker by remember { mutableStateOf(false) }

    // 无限滑动页面的初始位置（中间位置）
    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = initialPage)

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏（显示年月和跳转按钮）
        CalendarTitleBar(
            currentMonth = currentMonth,
            onPreviousClick = {
                currentMonth = currentMonth.minusMonths(1)
            },
            onNextClick = {
                currentMonth = currentMonth.plusMonths(1)
            },
            onTitleClick = { showDatePicker = true }
        )

        // 星期标题栏
        WeekDaysHeader()

        // 日历主体（横向滑动切换月份）
        HorizontalPager(
            count = Int.MAX_VALUE,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            // 计算当前页面对应的月份
            val monthOffset = page - initialPage
            val displayedMonth = YearMonth.now().plusMonths(monthOffset.toLong())

            // 当页面滑动停止时更新当前月份
            if (pagerState.currentPage == page) {
                currentMonth = displayedMonth
            }

            // 月份日历网格
            MonthCalendar(month = displayedMonth)
        }
    }

    // 日期选择对话框
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = currentMonth.atDay(1),
            onDateSelected = { date ->
                currentMonth = YearMonth.from(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun CalendarTitleBar(
    currentMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onTitleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 上个月按钮
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
        }

        // 月份标题（可点击）
        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .clickable { onTitleClick() }
                .padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold
        )

        // 下个月按钮
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@Composable
fun WeekDaysHeader() {
    val weekDays = remember {
        listOf("日", "一", "二", "三", "四", "五", "六")
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MonthCalendar(month: YearMonth) {
    // 获取该月的所有日期（包括上个月和下个月的部分日期）
    val daysInMonth = remember(month) {
        val firstDayOfMonth = month.atDay(1)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        val days = mutableListOf<LocalDate>()

        // 添加上个月的最后几天（如果需要）
        val firstVisibleDay = firstDayOfMonth.with(DayOfWeek.from(firstDayOfWeek))
        if (firstVisibleDay.isBefore(firstDayOfMonth)) {
            var day = firstVisibleDay
            while (day.isBefore(firstDayOfMonth)) {
                days.add(day)
                day = day.plusDays(1)
            }
        }

        // 添加当月的所有天
        var day = firstDayOfMonth
        while (day.month == month.month) {
            days.add(day)
            day = day.plusDays(1)
        }

        // 添加下个月的前几天（如果需要）
        val lastDayOfMonth = month.atEndOfMonth()
        val lastVisibleDay = lastDayOfMonth.with(DayOfWeek.from(firstDayOfWeek.plus(6)))
        if (lastVisibleDay.isAfter(lastDayOfMonth)) {
            day = lastDayOfMonth.plusDays(1)
            while (day.isBefore(lastVisibleDay.plusDays(1))) {
                days.add(day)
                day = day.plusDays(1)
            }
        }

        days
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(daysInMonth) { date ->
            DayItem(
                date = date,
                isCurrentMonth = date.month == month.month
            )
        }
    }
}

@Composable
fun DayItem(date: LocalDate, isCurrentMonth: Boolean) {
    val isToday = remember(date) { date == LocalDate.now() }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(CircleShape)
            .background(
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    !isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant
                    else -> Color.Transparent
                }
            )
            .clickable { /* 处理日期点击 */ },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = when {
                isToday -> MaterialTheme.colorScheme.onPrimary
                !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontSize = 16.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            // 年份选择
            var selectedYear by remember { mutableStateOf(selectedDate.year) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { selectedYear-- }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous Year")
                }

                Text(
                    text = "$selectedYear 年",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                IconButton(onClick = { selectedYear++ }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Next Year")
                }
            }

            // 月份选择
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp)
            ) {
                items(12) { month ->
                    val isSelected = selectedYear == selectedDate.year && month == selectedDate.monthValue

                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                selectedDate = LocalDate.of(selectedYear, month, 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$month 月",
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // 确认按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                TextButton(onClick = {
                    onDateSelected(selectedDate)
                    onDismiss()
                }) {
                    Text("确定")
                }
            }
        }
    }
}
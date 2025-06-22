import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToInt

@Entity(tableName = "work_records")
data class WorkRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // yyyy-MM-dd
    val monthDate: String, // yyyy-MM
    val dutyTime: Long,
    var offDutyTime: Long?,
    var workingHours: Double?
) {
    fun calculateWorkingHours() {
        val workingMinute = ((offDutyTime!! - dutyTime) / (1000 * 60)).toInt()
        workingHours = ((workingMinute * 100) / 60.0).roundToInt() / 100.0
    }
}
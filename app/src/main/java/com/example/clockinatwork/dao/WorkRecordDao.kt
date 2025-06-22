import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRecordDao {
    @Insert
    suspend fun insert(record: WorkRecord)

    @Update
    suspend fun update(record: WorkRecord): Int

    @Query("SELECT * FROM work_records ORDER BY id DESC LIMIT 1")
    fun getLeastRecord(): Flow<WorkRecord?>

    @Query("DELETE FROM work_records")
    suspend fun deleteAllRecord()

    @Query("SELECT * FROM work_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<WorkRecord>>
}
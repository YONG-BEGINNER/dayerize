package app.dayerize.data
import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName="assets") data class AssetRow(@PrimaryKey val id:String,val workspaceId:String,val payload:String)
@Entity(tableName="config") data class ConfigRow(@PrimaryKey val key:String,val value:String)
@Dao interface AssetDao {
    @Query("SELECT * FROM assets") fun observe(): Flow<List<AssetRow>>
    @Query("SELECT * FROM assets") suspend fun all(): List<AssetRow>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(row:AssetRow)
    @Query("DELETE FROM assets WHERE id=:id") suspend fun delete(id:String)
    @Query("DELETE FROM assets") suspend fun clear()
    @Query("SELECT * FROM config") fun observeConfig(): Flow<List<ConfigRow>>
    @Query("SELECT * FROM config") suspend fun config(): List<ConfigRow>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun setConfig(row:ConfigRow)
}
@Database(entities=[AssetRow::class,ConfigRow::class],version=1,exportSchema=true)
abstract class AppDatabase: RoomDatabase() {
    abstract fun assets(): AssetDao
    companion object { fun open(context:Context,name:String="dayerize.db"):AppDatabase = Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,name).build() }
}

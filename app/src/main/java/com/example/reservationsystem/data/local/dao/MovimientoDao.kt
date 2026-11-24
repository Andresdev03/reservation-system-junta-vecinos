package com.example.reservationsystem.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.reservationsystem.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {
    @Query(value = "SELECT * FROM movimientos ORDER BY id DESC")
    fun getAllMovimientos(): Flow<List<MovimientoEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMovimiento(movimiento: MovimientoEntity)

    @Query(value = "SELECT COALESCE(SUM(monto), 0) FROM movimientos WHERE tipo = 'INGRESO'")
    fun getTotalIngresos(): Flow<Double>

    @Query(value = "SELECT COALESCE(SUM(monto), 0) FROM movimientos WHERE tipo = 'GASTO'")
    fun getTotalGastos(): Flow<Double>

    @Delete
    suspend fun deleteMovimiento(
        movimiento: MovimientoEntity
    ): Int

    @Query(value = "SELECT * FROM movimientos WHERE id = :id LIMIT 1")
    suspend fun getMovimientoById(id: Int): MovimientoEntity?

    @Update
    suspend fun updateMovimiento(
        movimiento: MovimientoEntity
    ): Int
}
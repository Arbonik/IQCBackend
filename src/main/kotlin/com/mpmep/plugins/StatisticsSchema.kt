package com.mpmep.plugins

import com.mpmep.plugins.core.Operate
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*

@Serializable
data class Statistic(
    val age: Int,
    val gender: String,
    val time:Long,
    val difficulty:Int,
    val operator: Operate
)
object Statistics : Table() {
    val id = integer("id").autoIncrement()
    val gender = varchar("gender", length = 50)
    val age = integer("age")
    val time = long("time")
    val operator = enumeration<Operate>("operator")
    val difficulty = integer("difficulty")

    override val primaryKey = PrimaryKey(id)
}
object StatisticsService {
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(statistic: Statistic): Int = dbQuery {
        Statistics.insert {
            it[difficulty] = statistic.difficulty
            it[operator] = statistic.operator
            it[time] = statistic.time
            it[gender] = statistic.gender
            it[age] = statistic.age
        }[Statistics.id]
    }
    suspend fun readAll(): List<Statistic> {
        return dbQuery {
            Statistics.selectAll()
                .map {
                    Statistic(
                        it[Statistics.age],
                        it[Statistics.gender],
                        it[Statistics.time],
                        it[Statistics.difficulty],
                        it[Statistics.operator]
                    )
                }
        }
    }
}
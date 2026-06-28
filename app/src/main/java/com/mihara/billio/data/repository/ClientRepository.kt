package com.mihara.billio.data.repository

import com.mihara.billio.data.db.dao.ClientDao
import com.mihara.billio.data.db.entity.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val dao: ClientDao
) {
    fun observeAll(): Flow<List<Client>> = dao.observeAll()
    fun observe(id: Long): Flow<Client?> = dao.observe(id)
    suspend fun get(id: Long): Client? = dao.getById(id)
    suspend fun save(client: Client): Long = dao.upsert(client)
    suspend fun update(client: Client) = dao.update(client)

    /** Returns true if deleted, false if the client still has open invoices. */
    suspend fun delete(client: Client): Boolean {
        if (dao.openInvoiceCount(client.id) > 0) return false
        dao.delete(client)
        return true
    }
}

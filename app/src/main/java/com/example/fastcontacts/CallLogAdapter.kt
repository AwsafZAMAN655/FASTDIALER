package com.example.fastcontacts

import android.content.Context
import android.provider.CallLog
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** A small read-only model of a recent call. */
data class CallLogEntry(
    val name: String,
    val number: String,
    val dateMillis: Long,
    val type: Int,
    val durationSeconds: Long
)

object CallLogRepository {
    fun loadRecent(context: Context, limit: Int): List<CallLogEntry> {
        val result = ArrayList<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION
        )
        try {
            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                "${CallLog.Calls.DATE} DESC"
            )?.use { cursor ->
                val nameCol = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numberCol = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                val dateCol = cursor.getColumnIndex(CallLog.Calls.DATE)
                val typeCol = cursor.getColumnIndex(CallLog.Calls.TYPE)
                val durationCol = cursor.getColumnIndex(CallLog.Calls.DURATION)
                while (cursor.moveToNext() && result.size < limit) {
                    val number = if (numberCol >= 0) cursor.getString(numberCol).orEmpty() else ""
                    val name = if (nameCol >= 0) cursor.getString(nameCol).orEmpty() else ""
                    val date = if (dateCol >= 0) cursor.getLong(dateCol) else 0L
                    val type = if (typeCol >= 0) cursor.getInt(typeCol) else CallLog.Calls.MISSED_TYPE
                    val duration = if (durationCol >= 0) cursor.getLong(durationCol) else 0L
                    if (number.isNotBlank()) {
                        result.add(CallLogEntry(name.ifBlank { number }, number, date, type, duration))
                    }
                }
            }
        } catch (_: SecurityException) {
            // The user may deny call-log access; the Contacts and Dialpad features still work.
        } catch (_: IllegalArgumentException) {
            // A device with a non-standard call-log provider should not crash the app.
        }
        return result
    }
}

class CallLogAdapter(
    private val onClick: (CallLogEntry) -> Unit
) : RecyclerView.Adapter<CallLogAdapter.CallViewHolder>() {
    private val entries = ArrayList<CallLogEntry>()

    fun submit(items: List<CallLogEntry>) {
        entries.clear()
        entries.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_call_log, parent, false)
        return CallViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        holder.bind(entries[position], onClick)
    }

    override fun getItemCount(): Int = entries.size

    class CallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.callName)
        private val number: TextView = itemView.findViewById(R.id.callNumber)
        private val meta: TextView = itemView.findViewById(R.id.callMeta)

        fun bind(entry: CallLogEntry, onClick: (CallLogEntry) -> Unit) {
            name.text = entry.name
            number.text = if (entry.name == entry.number) "" else entry.number
            val context = itemView.context
            val type = when (entry.type) {
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                CallLog.Calls.REJECTED_TYPE -> "Rejected"
                CallLog.Calls.BLOCKED_TYPE -> "Blocked"
                else -> "Call"
            }
            val relative = if (entry.dateMillis > 0) {
                DateUtils.getRelativeTimeSpanString(
                    entry.dateMillis, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                ).toString()
            } else ""
            meta.text = listOf(type, relative).filter { it.isNotBlank() }.joinToString(" · ")
            itemView.setOnClickListener { onClick(entry) }
            itemView.contentDescription = "${entry.name}, $type, tap to call"
        }
    }
}

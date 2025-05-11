package com.simats.univalut

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class EventAdapter(private val eventList: MutableList<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    // ViewHolder for Event items
    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textViewEventTitle)
        val type: TextView = itemView.findViewById(R.id.textViewEventType)
        val startDate: TextView = itemView.findViewById(R.id.textViewEventStartDate)
        val endDate: TextView = itemView.findViewById(R.id.textViewEventEndDate)
        val description: TextView = itemView.findViewById(R.id.textViewEventDescription)
    }

    // Inflate the layout for the event
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(itemView)
    }

    // Bind the event data to the view
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.title.text = event.title
        holder.type.text = event.type
        holder.startDate.text = event.startDate.toString()
        holder.endDate.text = event.endDate.toString()
        holder.description.text = event.description
    }

    // Return the size of the event list
    override fun getItemCount(): Int {
        return eventList.size
    }

    // Method to update the event list dynamically
    fun updateEvents(events: List<Event>) {
        eventList.clear() // Clear the current list
        eventList.addAll(events) // Add the new events
        notifyDataSetChanged() // Notify the adapter to refresh the view
    }
}

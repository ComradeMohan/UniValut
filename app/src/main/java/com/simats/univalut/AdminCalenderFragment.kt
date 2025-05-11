package com.simats.univalut

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.*

class AdminCalenderFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val eventList = mutableListOf<Event>()
    private lateinit var startDateTextView: TextView
    private lateinit var endDateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_calender, container, false)

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewEvents)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventAdapter = EventAdapter(eventList)
        recyclerView.adapter = eventAdapter

        // Fetch events and update the RecyclerView
        fetchEvents()

        // Add Event Button
        val addEventButton: Button = view.findViewById(R.id.buttonAddEvent)
        addEventButton.setOnClickListener {
            showAddEventDialog()
        }

        return view
    }

    // Fetch events (could be from the server or database)
    private fun fetchEvents() {
        eventList.clear()
        eventList.add(
            Event(
                title = "Model Exam",
                type = "Exam",
                startDate = LocalDate.of(2025, 5, 10),
                endDate = LocalDate.of(2025, 5, 10),
                description = "End-of-semester model exam for all students"
            )
        )
        eventList.add(
            Event(
                title = "Workshop on AI",
                type = "Workshop",
                startDate = LocalDate.of(2025, 5, 15),
                endDate = LocalDate.of(2025, 5, 15),
                description = "A workshop on the latest trends in AI and machine learning."
            )
        )
        eventAdapter.notifyDataSetChanged()
    }

    // Dialog to add a new event
    private fun showAddEventDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_event, null)

        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Add New Event")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                // Get data from the EditTexts
                val title = dialogView.findViewById<EditText>(R.id.editTextTitle).text.toString()
                val type = dialogView.findViewById<EditText>(R.id.editTextType).text.toString().takeIf { it.isNotBlank() } ?: "" // Handle nullable event type
                val description = dialogView.findViewById<EditText>(R.id.editTextDescription).text.toString()
                val startDateStr = startDateTextView.text.toString()
                val endDateStr = endDateTextView.text.toString()

                try {
                    // Convert the dates (nullable LocalDate)
                    val startDate = if (startDateStr.isNotBlank()) LocalDate.parse(startDateStr) else null
                    val endDate = if (endDateStr.isNotBlank()) LocalDate.parse(endDateStr) else null

                    // Make sure startDate and endDate are valid (not null) before adding the event
                    if (startDate == null || endDate == null) {
                        Toast.makeText(requireContext(), "Please select both start and end dates.", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    // Create the new event object
                    val newEvent = Event(
                        title = title,
                        type = type,  // Event type can be empty, handled by defaulting to an empty string
                        startDate = startDate,
                        endDate = endDate,
                        description = description
                    )

                    // Add the event to the list and notify the adapter
                    eventList.add(newEvent)
                    eventAdapter.notifyItemInserted(eventList.size - 1)

                    // Show a success message
                    Toast.makeText(requireContext(), "Event added", Toast.LENGTH_SHORT).show()

                } catch (e: DateTimeParseException) {
                    Toast.makeText(requireContext(), "Invalid date format. Use yyyy-mm-dd", Toast.LENGTH_LONG).show()
                }

                // Dismiss the dialog
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }


        // Set up the TextViews for date pickers
        startDateTextView = dialogView.findViewById(R.id.textViewStartDate)
        endDateTextView = dialogView.findViewById(R.id.textViewEndDate)

        // Set listeners for the start date and end date TextViews
        startDateTextView.setOnClickListener {
            showDatePicker(startDateTextView)
        }

        endDateTextView.setOnClickListener {
            showDatePicker(endDateTextView)
        }

        builder.create().show()
    }


    // Function to show a date picker dialog
    private fun showDatePicker(dateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                dateTextView.text = selectedDate.toString() // Set the selected date in the TextView
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    companion object {
        // Static method to create a new instance of the fragment
        fun newInstance(s: String): Fragment {
            val fragment = AdminCalenderFragment()
            val args = Bundle()
            args.putString("param", s)
            fragment.arguments = args
            return fragment
        }
    }
}

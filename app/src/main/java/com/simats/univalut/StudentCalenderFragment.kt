package com.simats.univalut

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentCalenderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudentCalenderFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    private lateinit var recyclerView: RecyclerView
    private lateinit var eventAdapter: EventAdapter
    private val eventList = mutableListOf<Event>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_student_calender, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewEvents)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        eventAdapter = EventAdapter(eventList)
        recyclerView.adapter = eventAdapter

        fetchEvents()

        return view
    }
    private fun fetchEvents() {
        // TODO: Replace with actual backend/API call
        eventList.clear()

        // Simulated event data with start and end dates
        eventList.add(Event(
            title = "Model Exam",
            type = "Exam",
            startDate = LocalDate.of(2025, 5, 10),
            endDate = LocalDate.of(2025, 5, 10),
            description = "End-of-semester model exam for all students"
        ))

        eventList.add(Event(
            title = "Workshop on AI",
            type = "Workshop",
            startDate = LocalDate.of(2025, 5, 15),
            endDate = LocalDate.of(2025, 5, 15),
            description = "A workshop on the latest trends in AI and machine learning."
        ))

        eventAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(), "Events loaded", Toast.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudentCalenderFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentCalenderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
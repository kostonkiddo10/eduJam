package com.example.gdsc_hackathon.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gdsc_hackathon.R
import com.example.gdsc_hackathon.adapters.RecentLectureAdapter
import com.example.gdsc_hackathon.dataModel.RecentLectureModel

import com.google.firebase.auth.FirebaseAuth

import com.example.gdsc_hackathon.network.Api
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import com.example.gdsc_hackathon.extensions.copyToClipboard
import com.example.gdsc_hackathon.extensions.showSnackBarWithAction
import com.example.gdsc_hackathon.utils.NetworkUtils
import kotlin.collections.ArrayList
import kotlin.random.Random

class HomeFragment : Fragment() {
    private lateinit var syllabusLayout: LinearLayout
    private lateinit var weeklyTimeTableLayout: LinearLayout
    private lateinit var holidayLayout: LinearLayout
    private lateinit var examTimeConstraintLayout: LinearLayout
    private lateinit var practicalLayout: LinearLayout
    private lateinit var previousYearPapersLayout: LinearLayout
    private lateinit var academicCalendarLayout: LinearLayout
    private lateinit var moreLayout: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var quote: TextView
    private lateinit var quoteAuthor: TextView
    private lateinit var adapter: RecentLectureAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var quoteBannerLayout: RelativeLayout
    private lateinit var  quoteList: JsonObject

    private lateinit var mAuth : FirebaseAuth

    private lateinit var errorQuoteSaverList :ArrayList<String>
    private var  randomIndex :Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val rootView: View = inflater.inflate(R.layout.fragment_home, container, false)
        errorQuoteSaverList  = ArrayList()
        errorQuoteSaverList.add("\"Accept the things to which fate binds you and love the people with whom fate brings you together but do so with all your heart.\"")
        errorQuoteSaverList.add("\"Life shrinks or expands in proportion to one's courage.\"")
        errorQuoteSaverList.add("\"The ultimate promise of technology is to make us master of a world that we command by the push of a button.\"")
        errorQuoteSaverList.add("\"Well begun is half done.\"")
        errorQuoteSaverList.add("\"If I am not for myself, who will be for me? If I am not for others, what am I? And if not now, when?\"")

         randomIndex = Random.nextInt(errorQuoteSaverList.size);

        syllabusLayout = rootView.findViewById(R.id.syllabusLayout)
        syllabusLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.syllabusFragment)
        }

        weeklyTimeTableLayout = rootView.findViewById(R.id.weeklyTimeTableLayout)
        weeklyTimeTableLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.weeklyTimeTableFragment)
        }

        holidayLayout = rootView.findViewById(R.id.holidayLayout)
        holidayLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.holidayFragment)
        }

        examTimeConstraintLayout = rootView.findViewById(R.id.examTimeConstraintLayout)
        examTimeConstraintLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.examTimeConstraintFragment)
        }

        practicalLayout = rootView.findViewById(R.id.practicalLayout)
        practicalLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.practicalFragment)
        }

        previousYearPapersLayout = rootView.findViewById(R.id.previousYearPapersLayout)
        previousYearPapersLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.previousYearPapersFragment)
        }

        academicCalendarLayout = rootView.findViewById(R.id.academicCalendarLayout)
        academicCalendarLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.academicCalendarFragment)
        }

        moreLayout = rootView.findViewById(R.id.moreLayout)
        moreLayout.setOnClickListener {
            rootView.findNavController().navigate(R.id.moreFragment)
        }


        recyclerView = rootView.findViewById(R.id.recent_lectures_recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        val lectures = ArrayList<RecentLectureModel>()

        lectures.add(RecentLectureModel(R.drawable.ic_baseline_video_camera_front_24, "MATHS","25th December, 2021", "20:00"))
        lectures.add(RecentLectureModel(R.drawable.ic_baseline_video_camera_front_24, "PHYSICS","27th December, 2021","20:00"))
        for (i in 2..20) {
            lectures.add(RecentLectureModel(R.drawable.ic_baseline_video_camera_front_24, "Item $i", "1 JAN","TIME: 16:00" ))
        }

        adapter = RecentLectureAdapter(lectures)

        // Setting the Adapter with the recyclerview
        recyclerView.adapter = adapter

        quote = rootView.findViewById(R.id.quote)
        quoteAuthor = rootView.findViewById(R.id.quote_author)

        progressBar = rootView.findViewById(R.id.progress_bar)
        getQuotes()

        quoteBannerLayout = rootView.findViewById(R.id.quote_banner_layout)
        quoteBannerLayout.setOnClickListener {
            getQuotes()
        }
        quoteBannerLayout.setOnLongClickListener {
            copyQuote()
            true // <- set to true
        }
        return rootView
    }

    private fun copyQuote() {
        requireContext().copyToClipboard(quote.text.toString())
        showSnackBarWithAction(
            requireActivity(),
            "Quote Copied!",
            "Share Quote?",
            quote.text.toString()
        )
    }

    private fun getQuotes(){
        val dash = "-"
        if(!NetworkUtils.isNetworkAvailable(requireContext()))
        {
            quote.text = getString(R.string.no_internet_connection_quote_warning)
            quoteAuthor.text= dash.plus(getString(R.string.developers))
            return
        }
        val apiInterface = Api.create().getQuotes()
        progressBar.visibility =View.VISIBLE
        apiInterface.enqueue( object : Callback<JsonObject>{
            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {

                if(response?.body() != null){
                    quoteList = response.body()!!
                    progressBar.visibility =View.INVISIBLE
                    if(quoteList.get("length").asInt >150){
                        quote.text=   errorQuoteSaverList[randomIndex]
                        quoteAuthor.text = dash.plus("definitely not us")
                    }
                    else{
                        quote.text = quoteList.get("content").toString()
                        quoteAuthor.text = dash.plus(quoteList.get("author").toString().subSequence(1,quoteList.get("author").toString().length-1))
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                Log.w("MyTag", "requestFailed", t)
            }
        })
    }

}


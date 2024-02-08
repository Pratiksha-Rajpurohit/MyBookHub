package com.pratiksha.mybookkhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.pratiksha.mybookkhub.R
import com.pratiksha.mybookkhub.adapter.DashboardRecyclerAdapter
import com.pratiksha.mybookkhub.model.Book
import com.pratiksha.mybookkhub.util.ConnectionManager
import org.json.JSONException
import java.util.Collections


class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar



    lateinit var recyclerAdapter: DashboardRecyclerAdapter
     val bookInfoList = arrayListOf<Book>()

    // below variable is use to compare book rating here we use compareTo method to compare rating of book
    var ratingComparator = Comparator<Book>{book1,book2 ->
        if(book1.bookRating.compareTo(book2.bookRating,true) == 0){
            book1.bookName.compareTo(book2.bookName,true)
            }else{
                book1.bookRating.compareTo(book2.bookRating,true)
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        //below method is use to add fragment in menu
        setHasOptionsMenu(true)

        recyclerDashboard = view.findViewById(R.id.recycleDashboard)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout = view.findViewById(R.id.progressLayout)

        progressLayout.visibility = View.VISIBLE



        layoutManager = LinearLayoutManager(activity)



        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v1/books/fetch_books/"


        if(ConnectionManager().checkConnectivity(activity as Context)){

        val jsonObjectRequest = object: JsonObjectRequest(Method.GET, url, null, Response.Listener {
            // Here we will handel the Response

            try{

                progressLayout.visibility = View.GONE
           val success = it.getBoolean("success")

            if (success){

                val data = it.getJSONArray("data")
                for(i in 0 until data.length()){
                    val bookJsonObject = data.getJSONObject(i)
                    val bookObject = Book(
                        bookJsonObject.getString("book_id"),
                        bookJsonObject.getString("name"),
                        bookJsonObject.getString("author"),
                        bookJsonObject.getString("rating"),
                        bookJsonObject.getString("price"),
                        bookJsonObject.getString("image")
                    )
                    bookInfoList.add(bookObject)
                    recyclerAdapter = DashboardRecyclerAdapter(activity as Context,bookInfoList)

                    recyclerDashboard.adapter = recyclerAdapter
                    recyclerDashboard.layoutManager = layoutManager

//                    recyclerDashboard.addItemDecoration(
//                        DividerItemDecoration(
//                            recyclerDashboard.context,
//                            (layoutManager as LinearLayoutManager).orientation
//                        )
//                    )
                }
            } else{
                Toast.makeText(activity as Context,"Some Error Occurred",Toast.LENGTH_SHORT).show()
            }
            }catch(e: JSONException){
                Toast.makeText(activity as Context,"Some unexpected Error Occurred!!!",Toast.LENGTH_SHORT).show()
            }

        },Response.ErrorListener{
            // Here we will handel the Error
            Toast.makeText(activity as Context,"Volley Error Occurred!!!",Toast.LENGTH_SHORT).show()
        }){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String,String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "49deaa6e90baf9"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
        }else{
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Success")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings"){text,listener ->
                val settingIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Exit"){text,listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()
        }
        return view
    }

// using this method we can add menu item in tool bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        if(id == R.id.action_sort){
            Collections.sort(bookInfoList,ratingComparator)
            bookInfoList.reverse()
        }

        recyclerAdapter.notifyDataSetChanged()
        return super.onOptionsItemSelected(item)
    }

}
package com.materialcounter.app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.history)
        
        recyclerView = findViewById(R.id.recycler_view)
        tvEmpty = findViewById(R.id.tv_empty)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        loadHistory()
    }
    
    private fun loadHistory() {
        val prefs = getSharedPreferences("material_counter", Context.MODE_PRIVATE)
        val gson = Gson()
        
        val historyJson = prefs.getString("history", "[]")
        val type = object : TypeToken<List<CountRecord>>() {}.type
        val history: List<CountRecord> = gson.fromJson(historyJson, type)
        
        if (history.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            recyclerView.adapter = HistoryAdapter(history)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class HistoryAdapter(private val records: List<CountRecord>) : 
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvCount: TextView = view.findViewById(R.id.tv_count)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.tvTime.text = record.timestamp
        holder.tvCount.text = "数量: ${record.count}"
    }
    
    override fun getItemCount() = records.size
}

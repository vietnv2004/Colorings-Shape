package com.uef.coloring_app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AdminReportAdapter(private val reports: List<AdminReport>) : 
    RecyclerView.Adapter<AdminReportAdapter.ReportViewHolder>() {
    
    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val valueTextView: TextView = itemView.findViewById(R.id.valueTextView)
        val trendTextView: TextView = itemView.findViewById(R.id.trendTextView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_report, parent, false)
        return ReportViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        
        holder.titleTextView.text = report.title
        holder.descriptionTextView.text = report.description
        holder.valueTextView.text = report.value
        holder.trendTextView.text = report.trend
    }
    
    override fun getItemCount(): Int = reports.size
}

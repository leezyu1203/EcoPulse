package com.example.ecopulse.Guidance;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecopulse.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class WasteGuidanceAdapter extends RecyclerView.Adapter<WasteGuidanceAdapter.ViewHolder> {

    private List<String> wasteList;
    private Context context;
    private OnItemClickListener listener;

    // Interface to handle item clicks

    public WasteGuidanceAdapter(Context context, List<String> wasteList) {
        this.context=context;
        this.wasteList = wasteList;
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.guidance_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String wasteItem = wasteList.get(position);
        String[] wasteInfo = wasteItem.split("_"); // Splitting the waste name, type and desc

        // Display waste name and type in respective TextViews
        holder.wasteName.setText(wasteInfo[0].trim()); // Assuming name is the first part
        holder.wasteType.setText(wasteInfo[1].trim()); // Assuming type is the second part


        if(wasteInfo[2].trim().isEmpty()) // no desc stored in database
        {
            holder.wasteDesc.setText("N/A");
        }
        else  //got desc
        {
            holder.wasteDesc.setText(wasteInfo[2].trim()); // Assuming desc is the third part
        }

        //set the layout of waste desc
        float textSizeInSp = 16f;
        holder.wasteDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeInSp);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.wasteDesc.getLayoutParams();
        layoutParams.bottomMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,
                holder.itemView.getResources().getDisplayMetrics()
        );
        holder.wasteDesc.setLayoutParams(layoutParams);


        if(!wasteInfo[1].trim().isEmpty()) { //for search functionality (the waste type need to be showed)
            holder.wasteDesc.setVisibility(View.GONE);// waste desc initially invisible
            holder.wasteName.setTextColor(ContextCompat.getColor(this.context, R.color.dark_green));// set the default colour for waste name

            //change the text colour for different waste type(waste type text view only)
            if(wasteInfo[1].trim().equals("Recyclable Waste")){
                holder.wasteType.setTextColor(ContextCompat.getColor(this.context, R.color.recyclable));
            }
            else if(wasteInfo[1].trim().equals("Hazardous Waste")){
                holder.wasteType.setTextColor(ContextCompat.getColor(this.context, R.color.hazardous));
            }
            else if(wasteInfo[1].trim().equals("Household Food Waste")){
                holder.wasteType.setTextColor(ContextCompat.getColor(this.context, R.color.householdFood));
            }
            else if(wasteInfo[1].trim().equals("Residual Waste")){
                holder.wasteType.setTextColor(ContextCompat.getColor(this.context, R.color.residual));
            }
            else if(wasteInfo[1].trim().equals("Try another keyword"))
            {
                holder.wasteName.setTextColor(ContextCompat.getColor(this.context, R.color.error));
                holder.wasteType.setTextColor(ContextCompat.getColor(this.context, R.color.error));
            }
            else {//default
                holder.wasteName.setTextColor(ContextCompat.getColor(this.context, R.color.dark_green));
                holder.wasteType.setTextColor(ContextCompat.getColor(this.context, R.color.dark_green));
            }
        }



        if (wasteInfo[1].trim().isEmpty())//waste example list page (no waste type need to be showed as the waste type is showed as page title)
        {
            //set the layout of waste name
            ViewGroup.MarginLayoutParams layoutParams_wasteName = (ViewGroup.MarginLayoutParams) holder.wasteName.getLayoutParams();
            layoutParams_wasteName.width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    300,
                    holder.itemView.getResources().getDisplayMetrics()
            );
            holder.wasteName.setLayoutParams(layoutParams_wasteName);

            //set card colour as white
            holder.gudianceRecCardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            // change stroke width and stroke colour
            holder.gudianceRecCardView.setStrokeWidth(2);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(holder.getAdapterPosition());

                    // Toggle visibility of wasteDesc when RecyclerView item is clicked
                    if (holder.wasteDesc.getVisibility() == View.VISIBLE) {
                        holder.wasteDesc.setVisibility(View.GONE); // Hide wasteDesc
                    } else {
                        holder.wasteDesc.setVisibility(View.VISIBLE); // Show wasteDesc
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return wasteList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wasteName, wasteType,wasteDesc;
        MaterialCardView gudianceRecCardView;

        ViewHolder(View itemView) {
            super(itemView);
            wasteName = itemView.findViewById(R.id.guidanceRecWasteName);
            wasteType = itemView.findViewById(R.id.guidanceRecWasteType);
            wasteDesc = itemView.findViewById(R.id.guidanceRecDesc);
            gudianceRecCardView = itemView.findViewById(R.id.guidanceRecCard);
        }
    }
}
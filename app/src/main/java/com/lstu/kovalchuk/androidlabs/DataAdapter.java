package com.lstu.kovalchuk.androidlabs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<RequestData> requestDataList;

    public DataAdapter(Context context, List<RequestData> requestDataList) {
        this.requestDataList = requestDataList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder holder, int position) {
        RequestData requestData = requestDataList.get(position);
        //holder.ivExpand.setImageResource(R.drawable.ic_expand_more_green_24dp);
        holder.tvRqtId.setText(strFilter(String.valueOf(requestData.getRqt_id())));
        holder.tvUrl.setText(strFilter(requestData.getuRL()));
        holder.tvComment.setText(strFilter(requestData.getComment()));
        holder.tvImgsource.setText(strFilter(requestData.getImagesource()));
        holder.tvFullName.setText(strFilter(requestData.getFullName()));
        holder.tvEmail.setText(strFilter(requestData.getEmail()));
        holder.tvPhone.setText(strFilter(requestData.getPhone()));
        holder.tvAddress.setText(strFilter(requestData.getAddress()));
    }

    @Override
    public int getItemCount() {
        return requestDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivExpand;
        final TextView tvRqtId, tvUrl, tvComment, tvImgsource, tvFullName, tvEmail, tvPhone, tvAddress;
        final LinearLayout llCard, llDetail;
        ViewHolder(View view){
            super(view);
            ivExpand = view.findViewById(R.id.itemExpand);
            tvRqtId = view.findViewById(R.id.itemRqtId);
            tvUrl = view.findViewById(R.id.itemUrl);
            tvComment = view.findViewById(R.id.itemComment);
            tvImgsource = view.findViewById(R.id.itemImgsource);
            tvFullName = view.findViewById(R.id.itemFullName);
            tvEmail = view.findViewById(R.id.itemEmail);
            tvPhone = view.findViewById(R.id.itemPhone);
            tvAddress = view.findViewById(R.id.itemAddress);
            llDetail = view.findViewById(R.id.itemDetail);
            llCard = view.findViewById(R.id.itemCard);
            llCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(llDetail.getVisibility()==View.VISIBLE){
                        llDetail.setVisibility(View.GONE);
                        ivExpand.setImageResource(R.drawable.ic_expand_more_green_24dp);
                        return;
                    }
                    if(llDetail.getVisibility()==View.GONE){
                        llDetail.setVisibility(View.VISIBLE);
                        ivExpand.setImageResource(R.drawable.ic_expand_less_black_24dp);
                    }
                }
            });
        }
    }

    private String strFilter(String str){
        boolean isNull = false;
        try {
            str.equalsIgnoreCase(null);
        } catch (NullPointerException npe) {
            isNull = true;
        }
        if(isNull || str.equals("") || str.replace(" ", "").length() == 0){
            return "-";
        }else {
            return str;
        }
    }
}

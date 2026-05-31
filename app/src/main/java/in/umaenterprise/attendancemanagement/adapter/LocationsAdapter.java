package in.umaenterprise.attendancemanagement.adapter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.umaenterprise.attendancemanagement.R;
import in.umaenterprise.attendancemanagement.model.LocationModel;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<LocationModel> mList;
    private final OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(LocationModel model);
        void onEditClick(LocationModel model);
        void onDeleteClick(LocationModel model);
    }

    public LocationsAdapter(Context context, ArrayList<LocationModel> list, OnItemClickListener listener) {
        this.mContext = context;
        this.mList = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_view_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final LocationModel model = mList.get(position);
        
        holder.tvBranchCode.setText(model.getCode() != null && !model.getCode().isEmpty() ? model.getCode() : String.valueOf(position + 1));
        holder.tvBranchRadius.setText(String.format("%s M", (int) model.getRadius()));
        holder.tvBranchName.setText(model.getName());
        holder.tvBranchAddress.setText(model.getAddress());
        holder.tvBranchLatitude.setText(String.valueOf(model.getLatitude()));
        holder.tvBranchLongitude.setText(String.valueOf(model.getLongitude()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(model);
            }
        });
        
        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuItem edit = menu.add(Menu.NONE, 1, 1, mContext.getString(R.string.menu_edit));
                MenuItem delete = menu.add(Menu.NONE, 2, 2, mContext.getString(R.string.menu_delete));
                
                edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mListener.onEditClick(model);
                        return true;
                    }
                });
                
                delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mListener.onDeleteClick(model);
                        return true;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBranchCode;
        TextView tvBranchRadius;
        TextView tvBranchName;
        TextView tvBranchAddress;
        TextView tvBranchLatitude;
        TextView tvBranchLongitude;

        ViewHolder(View itemView) {
            super(itemView);
            tvBranchCode = itemView.findViewById(R.id.tv_branch_code);
            tvBranchRadius = itemView.findViewById(R.id.tv_branch_radius);
            tvBranchName = itemView.findViewById(R.id.tv_branch_name);
            tvBranchAddress = itemView.findViewById(R.id.tv_branch_address);
            tvBranchLatitude = itemView.findViewById(R.id.tv_branch_latitude);
            tvBranchLongitude = itemView.findViewById(R.id.tv_branch_longitude);
        }
    }
}

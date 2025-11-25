package com.luza.zippy.ui.sidebarList.minecraft.location;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.luza.zippy.R;

import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends ListAdapter<LocationAdapter.ListItem, RecyclerView.ViewHolder> {
    private OnLocationClickListener listener;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_LOCATION = 1;

    public LocationAdapter(DiffUtil.ItemCallback<ListItem> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_location_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_location, parent, false);
            return new LocationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((HeaderItem) item);
        } else if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).bind(((LocationItem) item).location);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) instanceof HeaderItem ? TYPE_HEADER : TYPE_LOCATION;
    }

    public void submitLocationList(List<LocationModel> locations) {
        List<ListItem> items = new ArrayList<>();
        
        // 添加地狱标题和位置
        boolean hasNether = false;
        for (LocationModel location : locations) {
            if ("地狱".equals(location.getDimension())) {
                if (!hasNether) {
                    items.add(new HeaderItem("地狱"));
                    hasNether = true;
                }
                items.add(new LocationItem(location));
            }
        }

        // 添加末地标题和位置
        boolean hasEnd = false;
        for (LocationModel location : locations) {
            if ("末地".equals(location.getDimension())) {
                if (!hasEnd) {
                    items.add(new HeaderItem("末地"));
                    hasEnd = true;
                }
                items.add(new LocationItem(location));
            }
        }

        submitList(items);
    }

    // ViewHolder for Headers
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvHeader;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }

        void bind(HeaderItem item) {
            tvHeader.setText(item.title);
        }
    }

    // ViewHolder for Locations
    class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvCoordinates, tvDimension, tvDescription;
        private ImageButton btnEdit, btnDelete;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            tvDimension = itemView.findViewById(R.id.tvDimension);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    LocationItem locationItem = (LocationItem) getItem(position);
                    listener.onEditClick(locationItem.location);
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    LocationItem locationItem = (LocationItem) getItem(position);
                    listener.onDeleteClick(locationItem.location);
                }
            });
        }

        public void bind(LocationModel location) {
            tvName.setText(location.getName());
            tvCoordinates.setText(String.format("(%d,%d)", location.getX(), location.getY()));
            tvDimension.setText(location.getConnectionType());
            if (location.getDescription() != null && !location.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(location.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }
        }
    }

    // Data classes for list items
    abstract static class ListItem {}

    static class HeaderItem extends ListItem {
        final String title;

        HeaderItem(String title) {
            this.title = title;
        }
    }

    static class LocationItem extends ListItem {
        final LocationModel location;

        LocationItem(LocationModel location) {
            this.location = location;
        }
    }

    // DiffUtil
    public static class LocationDiff extends DiffUtil.ItemCallback<ListItem> {
        @Override
        public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
                return ((HeaderItem) oldItem).title.equals(((HeaderItem) newItem).title);
            }
            if (oldItem instanceof LocationItem && newItem instanceof LocationItem) {
                return ((LocationItem) oldItem).location.getId() == 
                       ((LocationItem) newItem).location.getId();
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
            if (oldItem instanceof HeaderItem && newItem instanceof HeaderItem) {
                return ((HeaderItem) oldItem).title.equals(((HeaderItem) newItem).title);
            }
            if (oldItem instanceof LocationItem && newItem instanceof LocationItem) {
                LocationModel oldLocation = ((LocationItem) oldItem).location;
                LocationModel newLocation = ((LocationItem) newItem).location;
                return oldLocation.getName().equals(newLocation.getName()) &&
                        oldLocation.getX() == newLocation.getX() &&
                        oldLocation.getY() == newLocation.getY() &&
                        oldLocation.getDimension().equals(newLocation.getDimension()) &&
                        oldLocation.getConnectionType().equals(newLocation.getConnectionType()) &&
                        oldLocation.getDescription().equals(newLocation.getDescription());
            }
            return false;
        }
    }

    public interface OnLocationClickListener {
        void onEditClick(LocationModel location);
        void onDeleteClick(LocationModel location);
    }

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        this.listener = listener;
    }
} 
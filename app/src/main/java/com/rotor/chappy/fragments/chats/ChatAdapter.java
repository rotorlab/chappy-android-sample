package com.rotor.chappy.fragments.chats;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.maps.android.clustering.ClusterManager;
import com.rotor.chappy.R;
import com.rotor.chappy.model.Chat;
import com.rotor.chappy.model.map.PersonItem;
import com.rotor.chappy.model.map.PersonRender;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by efraespada on 17/06/2017.
 */

public abstract class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {

    private ChatsFragment fragment;

    public ChatAdapter(ChatsFragment fragment) {
        this.fragment = fragment;
    }

    public abstract void onChatClicked(Chat chat);

    @Override
    @NonNull
    public ChatAdapter.ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(com.rotor.chappy.R.layout.item_chat, parent, false);
        return new ChatHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ChatHolder holder, int position) {
        final Chat chat = (Chat) fragment.presenter().chats().values().toArray()[position];
        holder.resume();
        holder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChatClicked(chat);
            }
        });
        holder.name.setText(StringEscapeUtils.unescapeJava(chat.getName()));
    }

    @Override
    public int getItemCount() {
        return fragment.presenter().chats().size();
    }


    class ChatHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        GoogleMap map;
        private ClusterManager<PersonItem> mClusterManager;
        MapView mapView;
        LinearLayout content;
        TextView name;

        private ChatHolder(View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.chat_content);
            mapView = itemView.findViewById(R.id.map_view);
            mapView.onCreate(null);
            name = itemView.findViewById(R.id.group_name);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            map.getUiSettings().setMyLocationButtonEnabled(false);
            if (ActivityCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(fragment.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            map.setMyLocationEnabled(true);

            mClusterManager = new ClusterManager<PersonItem>(fragment.getActivity(), map);
            mClusterManager.setRenderer(new PersonRender((AppCompatActivity) fragment.getActivity(), map, mClusterManager));
            map.setOnCameraIdleListener(mClusterManager);
            map.setOnMarkerClickListener(mClusterManager);
            map.setOnInfoWindowClickListener(mClusterManager);
        }

        public void resume() {
            mapView.onResume();
            mapView.getMapAsync(this);
        }

        public void pause() {
            mapView.onPause();
        }
    }
}




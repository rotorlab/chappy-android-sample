package com.rotor.chappy.fragments.profile;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rotor.chappy.App;
import com.rotor.chappy.R;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.fragments.chat.ChatInterface;
import com.rotor.chappy.fragments.chat.ChatPresenter;
import com.rotor.chappy.fragments.chat.MessageAdapter;
import com.rotor.chappy.fragments.chats.ChatsFragment;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.chappy.model.Message;
import com.rotor.core.RFragment;
import com.rotor.core.Rotor;
import com.stringcare.library.SC;
import com.tapadoo.alerter.Alerter;

import net.glxn.qrgen.android.QRCode;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Date;

public class ProfileFragment extends RFragment implements Frag, ProfileInterface.View {

    public ProfilePresenter presenter;

    private RoundedImageView profile;
    private ImageView qr;
    private TextView name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@Nullable View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profile = view.findViewById(R.id.image);
        name = view.findViewById(R.id.name);
        qr = view.findViewById(R.id.user_qr);

        presenter = new ProfilePresenter(this);
    }

    @Override
    public void onResumeView() {
        presenter.start();
    }

    @Override
    public void onPauseView() {
        // nothing to do here
    }

    @Override
    public void onBackPressed() {
        getActivity().finish();
    }

    @Override
    public void connected() {
        Alerter.clearCurrent(getActivity());
    }

    @Override
    public void disconnected() {
        Alerter.create(getActivity()).setTitle("Device not connected")
                .setText("Trying to reconnect")
                .enableProgress(true)
                .disableOutsideTouch()
                .enableInfiniteDuration(true)
                .setProgressColorRes(R.color.primary)
                .show();
    }

    @Override
    public FragmentType type() {
        return FragmentType.CHAT;
    }

    @Override
    public String title() {
        return presenter.user().getName();
    }

    public static ProfileFragment instance() {
        return new ProfileFragment();
    }

    @Override
    public void userUpdated() {
        ImageLoader.getInstance().displayImage(presenter.user().getPhoto(), profile);
        name.setText(presenter.user().getName());
        Bitmap myBitmap = QRCode.from(SC.encryptString(presenter.user().getUid())).withColor(0xFF000000, 0x00FFFFFF).withSize(350, 350).bitmap();
        qr.setImageBitmap(myBitmap);
    }
}

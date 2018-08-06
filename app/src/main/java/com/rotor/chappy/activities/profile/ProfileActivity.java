package com.rotor.chappy.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.makeramen.roundedimageview.RoundedImageView;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rotor.chappy.R;
import com.rotor.chappy.activities.login.LoginGoogleActivity;
import com.rotor.chappy.activities.main.MainActivity;
import com.rotor.chappy.model.User;
import com.rotor.core.RAppCompatActivity;
import com.stringcare.library.SC;

import net.glxn.qrgen.android.QRCode;

/**
 * Created by efraespada on 27/02/2018.
 */

public class ProfileActivity extends RAppCompatActivity implements ProfileInterface.View {

    public static String TAG = ProfileActivity.class.getSimpleName();
    private ProfilePresenter presenter;
    private RoundedImageView profile;
    private ImageView qr;
    private TextView name;
    private User user;
    private Toolbar toolbar;
    private boolean uiReady;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(IconicsContextWrapper.wrap(context));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        profile = findViewById(R.id.image);
        name = findViewById(R.id.name);
        qr = findViewById(R.id.user_qr);

        presenter = new ProfilePresenter(this);

        uiReady = false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResumeView();
        presenter.start();
    }

    @Override
    protected void onPause() {
        presenter.onPauseView();
        super.onPause();
    }


    @Override
    public void onCreateUser() {
        // shouldn't be called
    }

    @Override
    public void onUserChanged(User user) {
        this.user = user;
        ImageLoader.getInstance().displayImage(user.getPhoto(), profile);
        name.setText(user.getName());
        Bitmap myBitmap = QRCode.from(SC.encryptString(user.getUid())).withColor(0xFF000000, 0x00FFFFFF).withSize(350, 350).bitmap();
        qr.setImageBitmap(myBitmap);

        if (!uiReady) {
            uiReady = true;

            AccountHeader headerResult = new AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.header)
                    .withSelectionListEnabledForSingleProfile(false)
                    .addProfiles(
                            new ProfileDrawerItem().withName(user.getName()).withEmail(user.getEmail()).withIcon(user.getPhoto())
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();

            PrimaryDrawerItem profileButton = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.menu_label_profile).withIcon(GoogleMaterial.Icon.gmd_account_circle);
            PrimaryDrawerItem chatsButton = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.menu_label_chats).withIcon(GoogleMaterial.Icon.gmd_message);
            SecondaryDrawerItem settingsButton = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.menu_label_settings).withIcon(GoogleMaterial.Icon.gmd_settings);
            SecondaryDrawerItem logoutButton = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.menu_label_sign_out).withIcon(GoogleMaterial.Icon.gmd_exit_to_app);

            DrawerBuilder builder = new DrawerBuilder()
                    .withActivity(this)
                    .withToolbar(toolbar)
                    .withAccountHeader(headerResult)
                    .addDrawerItems(
                            profileButton,
                            chatsButton,
                            new DividerDrawerItem(),
                            settingsButton,
                            logoutButton
                    )
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            // do something with the clicked item :D
                            int id = (int) drawerItem.getIdentifier();
                            switch (id) {
                                case 1:
                                    return true;
                                case 2:
                                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                    return true;
                                case 4:
                                    AuthUI.getInstance()
                                            .signOut(ProfileActivity.this)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Intent intent = new Intent(ProfileActivity.this, LoginGoogleActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            Drawer drawer = builder.build();

            drawer.setSelection(profileButton);
        }
    }

    @Override
    public User onUpdateUser() {
        return user;
    }

    @Override
    public void onDestroyUser() {
        user = null;
        finish();
    }

    @Override
    public void userProgress(int value) {
        // nothing to do here
    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }
}

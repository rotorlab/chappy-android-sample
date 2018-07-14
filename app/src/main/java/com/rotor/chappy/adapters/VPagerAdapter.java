package com.rotor.chappy.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.rotor.chappy.App;
import com.rotor.chappy.enums.FragmentType;
import com.rotor.chappy.fragments.chat.ChatFragment;
import com.rotor.chappy.fragments.chats.ChatsFragment;
import com.rotor.chappy.fragments.map.MapFragment;
import com.rotor.chappy.interfaces.Frag;
import com.rotor.core.RFragment;

public class VPagerAdapter extends FragmentStatePagerAdapter {


    private Fragment[] fragments = {
            ChatsFragment.instance(),
            ChatFragment.instance(),
            MapFragment.instance()
    };

    public VPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return (Fragment) fragments[i];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    public void setFragment(FragmentType type) {
        int i = 0;
        for (Fragment fragment : fragments) {
            Frag f = (Frag) fragment;
            if (type.equals(f.type())) {
                App.getPager().setCurrentItem(i);
                fragment.onResume();
                break;
            }
            i++;
        }
    }
}

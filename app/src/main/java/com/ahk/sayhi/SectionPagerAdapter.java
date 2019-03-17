package com.ahk.sayhi;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class SectionPagerAdapter extends FragmentPagerAdapter {
    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i){
            case 0:
                chatsFragment chatsfragment=new chatsFragment();
                return chatsfragment;
            case 1:
                friendsFragment friendsfragment=new friendsFragment();
                return friendsfragment;
            case 2:
                requestsFragment requestsfragment=new requestsFragment();
                return requestsfragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }


    public CharSequence getPageTitle(int i){
        switch (i){
            case 0:
                return "CHATS";
            case 1:
                return "FRIENDS";
            case 2:
                return "REQUESTS";
            default:
                return null;
        }
    }
}

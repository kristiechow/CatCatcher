package com.example.kristie.myapplication;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import java.util.ArrayList;

public class ActionTabsViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;

    public static final int PLAY = 0;
    public static final int HISTORY = 1;
    public static final int RANKING = 2;
    public static final int SETTINGS = 3;
    public static final String UI_TAB_PLAY = "Play";
    public static final String UI_TAB_HISTORY = "History";
    public static final String UI_TAB_RANKING = "Ranking";
    public static final String UI_TAB_SETTINGS = "Settings";

    public ActionTabsViewPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments){
        super(fm);
        this.fragments = fragments;
    }

    public Fragment getItem(int pos){
        return fragments.get(pos);
    }

    public int getCount(){
        return fragments.size();
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case PLAY:
                return UI_TAB_PLAY;
            case HISTORY:
                return UI_TAB_HISTORY;
            case RANKING:
                return UI_TAB_RANKING;
            case SETTINGS:
                return UI_TAB_SETTINGS;
            default:
                break;
        }
        return null;
    }
}


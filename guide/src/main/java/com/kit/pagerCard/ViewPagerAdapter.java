package com.kit.pagerCard;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * viewpager的适配器
 * @author libowu
 * @date 2019/09/27
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> list;

    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.list = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return list.get(i);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}

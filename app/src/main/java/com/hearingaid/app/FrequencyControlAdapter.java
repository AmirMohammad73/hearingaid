package com.hearingaid.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class FrequencyControlAdapter extends FragmentStateAdapter {
    private List<Fragment> fragments = new ArrayList<>();
    private List<String> titles = new ArrayList<>();
    
    public FrequencyControlAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    
    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }
    
    @Override
    public int getItemCount() {
        return fragments.size();
    }
}


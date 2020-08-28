package com.example.inventory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPager2Adapter extends FragmentStateAdapter {

    public ViewPager2Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){

            case 0:
                return new FragmentCategories();
            case 1:
                return new FragmentReports();
            default:
                return new FragmentInventory();

        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

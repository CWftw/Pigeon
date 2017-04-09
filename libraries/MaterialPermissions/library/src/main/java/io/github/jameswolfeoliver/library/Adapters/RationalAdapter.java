package io.github.jameswolfeoliver.library.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import io.github.jameswolfeoliver.library.Fragments.PagerFragment;
import io.github.jameswolfeoliver.library.Permission.Permission;

public class RationalAdapter extends FragmentPagerAdapter {

    private final List<Permission> permissions;

    public RationalAdapter(FragmentManager fm, List<Permission> permissions) {
        super(fm);
        this.permissions = permissions;
    }

    @Override
    public int getCount() {
        return this.permissions.size();
    }

    @Override
    public Fragment getItem(int position) {
        return new PagerFragment(permissions.get(position));
    }
}

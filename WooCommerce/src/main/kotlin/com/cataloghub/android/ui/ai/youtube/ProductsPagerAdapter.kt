package com.cataloghub.android.ui.ai.youtube

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cataloghub.android.model.AIProductStatus

class ProductsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    
    override fun getItemCount(): Int = 3
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProductsListFragment.newInstance(AIProductStatus.PENDING)
            1 -> ProductsListFragment.newInstance(AIProductStatus.APPROVED)
            2 -> ProductsListFragment.newInstance(AIProductStatus.REJECTED)
            else -> ProductsListFragment.newInstance(AIProductStatus.PENDING)
        }
    }
} 
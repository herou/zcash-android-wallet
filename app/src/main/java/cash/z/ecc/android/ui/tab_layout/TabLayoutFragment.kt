package cash.z.ecc.android.ui.tab_layout

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentTabLayoutBinding
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.profile.AwesomeFragment
import cash.z.ecc.android.ui.receive.ReceiveFragment
import com.google.android.material.tabs.TabLayout

class TabLayoutFragment: BaseFragment<FragmentTabLayoutBinding>() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager

    override fun inflate(inflater: LayoutInflater): FragmentTabLayoutBinding =
        FragmentTabLayoutBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayout)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        viewPager = view.findViewById(R.id.viewPager)

        val adapter = ViewPagerAdapter(activity?.supportFragmentManager)
        adapter.addFrag(ReceiveFragment(), "Shielded")
        adapter.addFrag(AwesomeFragment(), "Transparent")

        viewPager.adapter = adapter


        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position

                if(tab.position == 0){
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.zcashYellow))
                    tabLayout.setTabTextColors(ContextCompat.getColor(requireContext(), R.color.unselected_tab_grey), ContextCompat.getColor(requireContext(), R.color.zcashYellow))
                }

                if(tab.position == 1){
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.zcashBlueDark))
                    tabLayout.setTabTextColors(ContextCompat.getColor(requireContext(), R.color.unselected_tab_grey), ContextCompat.getColor(requireContext(), R.color.zcashBlueDark))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                Log.d("",""+tab)
            }
        })

        binding.backButtonHitArea.onClickNavBack() { tapped(Report.Tap.HOME_SCAN) }
    }


}
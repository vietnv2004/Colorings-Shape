package com.uef.coloring_app.ui.simple

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uef.coloring_app.R
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.ui.admin.AdminDashboardActivity

class SimpleManagementFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_management, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupClickListeners(view)
    }
    
    private fun setupClickListeners(view: View) {
        view.findViewById<View>(R.id.taskManagementButton).setOnClickListenerWithSound {
            HapticManager.buttonClick(requireContext())
            startActivity(Intent(requireContext(), com.uef.coloring_app.ui.admin.TaskManagementActivity::class.java))
        }
        
        view.findViewById<View>(R.id.achievementManagementButton).setOnClickListenerWithSound {
            HapticManager.buttonClick(requireContext())
            startActivity(Intent(requireContext(), com.uef.coloring_app.ui.admin.AchievementManagementActivity::class.java))
        }
        
        view.findViewById<View>(R.id.leaderboardManagementButton).setOnClickListenerWithSound {
            HapticManager.buttonClick(requireContext())
            startActivity(Intent(requireContext(), com.uef.coloring_app.ui.admin.LeaderboardManagementActivity::class.java))
        }
        
        view.findViewById<View>(R.id.userManagementButton).setOnClickListenerWithSound {
            HapticManager.buttonClick(requireContext())
            startActivity(Intent(requireContext(), com.uef.coloring_app.ui.admin.UserManagementActivity::class.java))
        }
        
        view.findViewById<View>(R.id.reportsButton).setOnClickListenerWithSound {
            HapticManager.buttonClick(requireContext())
            startActivity(Intent(requireContext(), com.uef.coloring_app.ui.admin.AdvancedAdminActivity::class.java))
        }
    }
}



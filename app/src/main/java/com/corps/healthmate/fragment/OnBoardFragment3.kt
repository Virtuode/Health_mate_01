package com.corps.healthmate.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.corps.healthmate.R

class OnBoardFragment3 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_on_board3, container, false)
        
        // Initialize Lottie animation
        val lottieView = view.findViewById<LottieAnimationView>(R.id.lottieAnimationView3)
        lottieView.playAnimation()
        
        return view
    }
}
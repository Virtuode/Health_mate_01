package com.corps.healthmate.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.corps.healthmate.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth

class VerificationBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var verificationStatusTextView: TextView
    private lateinit var verificationMessageTextView: TextView
    private val auth = FirebaseAuth.getInstance()
    private var verificationListener: VerificationListener? = null

    interface VerificationListener {
        fun onVerificationDone()
        fun onLoginRequested()
    }

    fun setVerificationListener(listener: VerificationListener) {
        this.verificationListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verification_bottom_sheet_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verificationStatusTextView = view.findViewById(R.id.verificationStatusTextView)
        verificationMessageTextView = view.findViewById(R.id.verificationMessageTextView)

        // Apply entry animations
        applyEntryAnimations(view)

        // Start checking verification status
        startVerificationCheck()
    }

    private fun applyEntryAnimations(view: View) {
        // Slide up animation
        val slideUp = ObjectAnimator.ofFloat(view, "translationY", 300f, 0f)
        slideUp.duration = 500

        // Fade in animation
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        fadeIn.duration = 500

        // Combine animations
        val animSet = AnimatorSet()
        animSet.playTogether(slideUp, fadeIn)
        animSet.start()
    }

    private fun startVerificationCheck() {
        auth.currentUser?.let { user ->
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (user.isEmailVerified) {
                        verificationStatusTextView.text = "Email Verified"
                        verificationStatusTextView.setTextColor(resources.getColor(R.color.logo_color, null))
                        verificationMessageTextView.text = "Your email has been verified successfully! Redirecting..."

                        // Notify listener and dismiss after a short delay
                        view?.postDelayed({
                            verificationListener?.onVerificationDone()
                            dismiss()
                        }, 1500)
                    } else {
                        verificationStatusTextView.text = "Email Not Verified"
                        verificationStatusTextView.setTextColor(resources.getColor(R.color.hint_color, null))
                        verificationMessageTextView.text = "Please verify your email to continue. Check your inbox."
                        // Check again after 3 seconds
                        view?.postDelayed({ startVerificationCheck() }, 3000)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // If verification is not complete when dialog is closed, redirect to login
        if (auth.currentUser?.isEmailVerified != true) {
            verificationListener?.onLoginRequested()
        }
    }

    companion object {
        const val TAG = "VerificationBottomSheet"
    }
}
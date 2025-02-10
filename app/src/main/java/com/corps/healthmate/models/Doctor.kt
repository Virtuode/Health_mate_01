package com.corps.healthmate.models

import android.os.Parcel
import android.os.Parcelable

@Suppress("DEPRECATION")
open class Doctor : Parcelable {
    // Getters and setters
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var phone: String? = null
    var specialization: String? = null
    private var documentUrl: String? = null
    private var isVerified = false
    private var verificationStatus: String? = null
    private var experience: String? = null
    var imageUrl: String? = null
    private var gender: String? = null
    private var consultationFee: Double = 0.0
    private var availableTimeSlots: List<String>? = null
    var biography: String? = null
    var rating: Float = 0f
    private var timeSlots: TimeSlotList? = null  // Change to TimeSlotList

    // Default constructor for Firebase
    constructor()

    constructor(
        id: String?,
        name: String?,
        email: String?,
        phone: String?,
        specialization: String?,
        documentUrl: String?,
        isVerified: Boolean,
        verificationStatus: String?,
        experience: String?,
        imageUrl: String?,
        gender: String?,
        consultationFee: Double,
        availableTimeSlots: List<String>?,
        biography: String?,
        rating: Float,
        timeSlots: TimeSlotList?  // Added timeSlots parameter
    ) {
        this.id = id
        this.name = name
        this.email = email
        this.phone = phone
        this.specialization = specialization
        this.documentUrl = documentUrl
        this.isVerified = isVerified
        this.verificationStatus = verificationStatus
        this.experience = experience
        this.imageUrl = imageUrl
        this.gender = gender
        this.consultationFee = consultationFee
        this.availableTimeSlots = availableTimeSlots
        this.biography = biography
        this.rating = rating
        this.timeSlots = timeSlots
    }

    // Parcelable implementation
    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        name = `in`.readString()
        email = `in`.readString()
        phone = `in`.readString()
        specialization = `in`.readString()
        documentUrl = `in`.readString()
        isVerified = `in`.readByte().toInt() != 0
        verificationStatus = `in`.readString()
        experience = `in`.readString()
        imageUrl = `in`.readString()
        gender = `in`.readString()
        consultationFee = `in`.readDouble()
        availableTimeSlots = `in`.createStringArrayList()
        biography = `in`.readString()
        rating = `in`.readFloat()
        timeSlots = `in`.readParcelable(TimeSlotList::class.java.classLoader) // Read TimeSlotList
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(email)
        dest.writeString(phone)
        dest.writeString(specialization)
        dest.writeString(documentUrl)
        dest.writeByte((if (isVerified) 1 else 0).toByte())
        dest.writeString(verificationStatus)
        dest.writeString(experience)
        dest.writeString(imageUrl)
        dest.writeString(gender)
        dest.writeDouble(consultationFee)
        dest.writeStringList(availableTimeSlots)
        dest.writeString(biography)
        dest.writeFloat(rating)
        dest.writeParcelable(timeSlots, flags) // Write TimeSlotList
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {


        @JvmField
        val CREATOR: Parcelable.Creator<Doctor> = object : Parcelable.Creator<Doctor> {
            override fun createFromParcel(`in`: Parcel): Doctor {
                return Doctor(`in`)
            }

            override fun newArray(size: Int): Array<Doctor?> {
                return arrayOfNulls(size)
            }
        }
    }
}

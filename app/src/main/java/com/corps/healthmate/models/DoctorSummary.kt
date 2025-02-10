package com.corps.healthmate.models

import android.os.Parcel
import android.os.Parcelable

class DoctorSummary : Parcelable {
    // Getters and setters
    var id: String? = null
    var name: String? = null
    var specialization: String? = null
    var experience: String? = null
    var imageUrl: String? = null
    var verificationStatus: String? = null
    var consultationFee: Double = 0.0
    var education: String? = null
    var availableTimeSlots: List<String>? = null

    // No-argument constructor required for Firebase
    constructor()

    // Constructor
    constructor(
        id: String?,
        name: String?,
        specialization: String?,
        experience: String?,
        imageUrl: String?,
        verificationStatus: String?,
        consultationFee: Double,
        availableTimeSlots: List<String>?,
        education: String?
    ) {
        this.id = id
        this.name = name
        this.specialization = specialization
        this.experience = experience
        this.imageUrl = imageUrl
        this.verificationStatus = verificationStatus
        this.consultationFee = consultationFee
        this.availableTimeSlots = availableTimeSlots
        this.education = education
    }

    // Parcelable implementation
    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        name = `in`.readString()
        specialization = `in`.readString()
        experience = `in`.readString()
        imageUrl = `in`.readString()
        verificationStatus = `in`.readString()
        consultationFee = `in`.readDouble()
        availableTimeSlots = `in`.createStringArrayList()
        education = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(specialization)
        dest.writeString(experience)
        dest.writeString(imageUrl)
        dest.writeString(verificationStatus)
        dest.writeDouble(consultationFee)
        dest.writeStringList(availableTimeSlots)
        dest.writeString(education)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DoctorSummary> = object : Parcelable.Creator<DoctorSummary> {
            override fun createFromParcel(parcel: Parcel): DoctorSummary {
                return DoctorSummary(parcel)
            }

            override fun newArray(size: Int): Array<DoctorSummary?> {
                return arrayOfNulls(size)
            }
        }
    }
}

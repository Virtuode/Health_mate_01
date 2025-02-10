package com.corps.healthmate.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class TimeSlot : Parcelable, Serializable {
    // Getters and setters
    var id: String? = null
    var startTime: String? = null
    var endTime: String? = null
    var totalSlots: Int = 0
    var availableSlots: Int = 0
    var date: String? = null

    constructor()

    constructor(id: String?, startTime: String?, endTime: String?, date: String?) {
        this.id = id
        this.startTime = startTime
        this.endTime = endTime
        this.date = date
        this.totalSlots = 2 // Maximum 2 patients per slot
        this.availableSlots = 2
    }

    val isAvailable: Boolean
        get() = availableSlots > 0

    val displayTime: String
        get() = "$startTime - $endTime"

    // Add Parcelable implementation
    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        startTime = `in`.readString()
        endTime = `in`.readString()
        totalSlots = `in`.readInt()
        availableSlots = `in`.readInt()
        date = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(startTime)
        dest.writeString(endTime)
        dest.writeInt(totalSlots)
        dest.writeInt(availableSlots)
        dest.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        private const val serialVersionUID = 1L

        @JvmField
        val CREATOR: Parcelable.Creator<TimeSlot> = object : Parcelable.Creator<TimeSlot> {
            override fun createFromParcel(parcel: Parcel): TimeSlot {
                return TimeSlot(parcel)
            }

            override fun newArray(size: Int): Array<TimeSlot?> {
                return arrayOfNulls(size)
            }
        }
    }
}
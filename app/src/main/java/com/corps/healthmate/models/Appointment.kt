package com.corps.healthmate.models

class Appointment {
    // Add getters and setters
    var id: String? = null
    private var doctorId: String? = null
    private var patientId: String? = null
    private var date: String? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private var patientName: String? = null
    private var patientPhone: String? = null
    private var patientEmail: String? = null
    var status: String? = null // Pending, Confirmed, Completed, Cancelled
    private var createdAt: Long = 0

    constructor()

    constructor(
        id: String?, doctorId: String?, patientId: String?, date: String?,
        startTime: String?, endTime: String?, patientName: String?,
        patientPhone: String?, patientEmail: String?, status: String?
    ) {
        this.id = id
        this.doctorId = doctorId
        this.patientId = patientId
        this.date = date
        this.startTime = startTime
        this.endTime = endTime
        this.patientName = patientName
        this.patientPhone = patientPhone
        this.patientEmail = patientEmail
        this.status = status
        this.createdAt = System.currentTimeMillis()
    }
}
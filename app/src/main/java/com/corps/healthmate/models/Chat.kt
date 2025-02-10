package com.corps.healthmate.models

/**
 * Model class representing a chat between a patient and a doctor.
 */
class Chat {
    var doctorName: String? = null
    var lastMessage: String? = null

    // Default constructor required for calls to DataSnapshot.getValue(Chat.class)
    constructor()

    constructor(doctorName: String?, lastMessage: String?) {
        this.doctorName = doctorName
        this.lastMessage = lastMessage
    }
}
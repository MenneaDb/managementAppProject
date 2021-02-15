package com.example.managementappproject.models

import android.os.Parcel
import android.os.Parcelable

// each board can have multiple tasks
data class Task (
    var title: String = "",
    val createdBy: String = "",
    var cards: ArrayList<Card> = ArrayList() // implementing cards to let the user add it to the taskList
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.createTypedArrayList(Card.CREATOR)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(title)
        writeString(createdBy)
        writeTypedList(cards)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}
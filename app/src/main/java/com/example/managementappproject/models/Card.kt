package com.example.managementappproject.models

import android.os.Parcel
import android.os.Parcelable

// data class because we don't want methods or other members, only the variables - IF IS PARCELABLE WE NEED TO STORE IT
data class Card (
        val name: String = "",
        val createdBy: String = "",
        val assignedTo: ArrayList<String> = ArrayList(), // Array because could be assigned from different people
        val labelColor: String = "" // to add the color selected from the user to a Card

        ): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.createStringArrayList()!!,
            parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeString(name)
        writeString(createdBy)
        writeStringList(assignedTo)
        writeString(labelColor)
    }

    override fun describeContents() = 0

    // creates the card and the newArray
    companion object {
        @JvmField
        val CREATOR : Parcelable.Creator<Card> = object : Parcelable.Creator<Card> {
            override fun createFromParcel(source: Parcel): Card = Card(source)
            override fun newArray(size: Int): Array<Card?> = arrayOfNulls(size)
        }
    }
}
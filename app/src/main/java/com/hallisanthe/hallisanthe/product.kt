package com.hallisanthe.hallisanthe

import android.os.Parcel
import android.os.Parcelable

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Long = 0,
    val imageUrl: String = "",
    val imageBase64: String = "",       // ADD THIS — for Firestore-only storage
    val category: String = "General",
    val artisanName: String = "Unknown Artisan",
    val villageName: String = "Unknown Village",
    val description: String = "",
    val wishlistedBy: List<String> = emptyList()
) : Parcelable {

    private constructor(parcel: Parcel) : this(
        id = parcel.readString().orEmpty(),
        name = parcel.readString().orEmpty(),
        price = parcel.readLong(),
        imageUrl = parcel.readString().orEmpty(),
        imageBase64 = parcel.readString().orEmpty(),
        category = parcel.readString().orEmpty(),
        artisanName = parcel.readString().orEmpty(),
        villageName = parcel.readString().orEmpty(),
        description = parcel.readString().orEmpty(),
        wishlistedBy = mutableListOf<String>().apply {
            parcel.readStringList(this)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeLong(price)
        parcel.writeString(imageUrl)
        parcel.writeString(imageBase64)
        parcel.writeString(category)
        parcel.writeString(artisanName)
        parcel.writeString(villageName)
        parcel.writeString(description)
        parcel.writeStringList(wishlistedBy)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product = Product(parcel)

        override fun newArray(size: Int): Array<Product?> = arrayOfNulls(size)
    }
}

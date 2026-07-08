package io.selimdawa.autoimageslider.View.draw.data

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class PositionSavedState : View.BaseSavedState {
    var selectedPosition = 0
    var selectingPosition = 0
    var lastSelectedPosition = 0

    constructor(superState: Parcelable?) : super(superState)
    private constructor(`in`: Parcel) : super(`in`) {
        selectedPosition = `in`.readInt(); selectingPosition =
            `in`.readInt(); lastSelectedPosition = `in`.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(selectedPosition); out.writeInt(selectingPosition); out.writeInt(
            lastSelectedPosition
        )
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PositionSavedState?> {
            override fun createFromParcel(`in`: Parcel) = PositionSavedState(`in`)
            override fun newArray(size: Int) = arrayOfNulls<PositionSavedState?>(size)
        }
    }
}
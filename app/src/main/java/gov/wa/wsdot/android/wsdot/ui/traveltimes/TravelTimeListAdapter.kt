package gov.wa.wsdot.android.wsdot.ui.traveltimes

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import gov.wa.wsdot.android.wsdot.R
import gov.wa.wsdot.android.wsdot.databinding.TravelTimeItemBinding
import gov.wa.wsdot.android.wsdot.db.traveltimes.TravelTime
import gov.wa.wsdot.android.wsdot.ui.common.recyclerview.DataBoundListAdapter
import gov.wa.wsdot.android.wsdot.util.AppExecutors

class TravelTimeListAdapter(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val favoriteClickCallback: ((TravelTime) -> Unit)?,
    private val viewMapClickCallback: ((TravelTime) -> Unit)?
) : DataBoundListAdapter<TravelTime, TravelTimeItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<TravelTime>() {
        override fun areItemsTheSame(oldItem: TravelTime, newItem: TravelTime): Boolean {
            return oldItem.travelTimeId == newItem.travelTimeId
        }
        override fun areContentsTheSame(oldItem: TravelTime, newItem: TravelTime): Boolean {
            return oldItem.currentTime == newItem.currentTime
                    && oldItem.localCacheDate == newItem.localCacheDate
                    && oldItem.favorite == newItem.favorite
        }
    }
) {

    override fun createBinding(parent: ViewGroup): TravelTimeItemBinding {

        val binding = DataBindingUtil.inflate<TravelTimeItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.travel_time_item,
            parent,
            false,
            dataBindingComponent
        )

        binding.root.findViewById<ImageButton>(R.id.favorite_button).setOnClickListener {
            binding.travelTime?.let {
                favoriteClickCallback?.invoke(it)
            }
        }

        binding.root.findViewById<Button>(R.id.show_map).setOnClickListener {
            binding.travelTime?.let {
                viewMapClickCallback?.invoke(it)
            }
        }


        return binding
    }

    override fun bind(binding: TravelTimeItemBinding, item: TravelTime, position: Int) {
        binding.travelTime = item
    }
}
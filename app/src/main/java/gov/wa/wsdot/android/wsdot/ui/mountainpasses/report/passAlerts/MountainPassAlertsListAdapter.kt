package gov.wa.wsdot.android.wsdot.ui.mountainpasses.report.passAlerts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import gov.wa.wsdot.android.wsdot.R
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassAlert
import gov.wa.wsdot.android.wsdot.databinding.MountainPassAlertItemBinding
import gov.wa.wsdot.android.wsdot.ui.common.recyclerview.DataBoundListAdapter
import gov.wa.wsdot.android.wsdot.util.AppExecutors

/**
 * A RecyclerView adapter for [] class.
 */
class MountainPassAlertsListAdapter(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val alertClickCallback: ((MountainPassAlert) -> Unit)? // ClickCallback for item in the adapter
) : DataBoundListAdapter<MountainPassAlert, MountainPassAlertItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<MountainPassAlert>() {
        override fun areItemsTheSame(oldItem: MountainPassAlert, newItem: MountainPassAlert): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: MountainPassAlert, newItem: MountainPassAlert): Boolean {
            return oldItem.headlineMessage == newItem.headlineMessage
        }
    }
) {

    var mObserver: RecyclerView.AdapterDataObserver? = null

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        mObserver = observer
        super.registerAdapterDataObserver(observer)

    }

    // This lets us add an observer that is dependant on the binding var
    // in this case we remove our auto scroll observer
    fun removeObserver() {
        mObserver?.let {
            unregisterAdapterDataObserver(it)
            mObserver = null
        }
    }


    override fun createBinding(parent: ViewGroup): MountainPassAlertItemBinding {

        val binding = DataBindingUtil.inflate<MountainPassAlertItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.mountain_pass_alert_item,
            parent,
            false,
            dataBindingComponent
        )

        binding.root.findViewById<View>(R.id.alert_view).setOnClickListener {
            binding.alert?.let {
                alertClickCallback?.invoke(it)
            }
        }

        return binding
    }



    override fun bind(binding: MountainPassAlertItemBinding, item: MountainPassAlert, position: Int) {
        binding.alert = item
    }

}
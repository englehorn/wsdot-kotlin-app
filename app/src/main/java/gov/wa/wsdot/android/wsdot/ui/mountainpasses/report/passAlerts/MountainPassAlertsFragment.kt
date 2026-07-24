package gov.wa.wsdot.android.wsdot.ui.mountainpasses.report.passAlerts

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dagger.android.support.DaggerFragment
import gov.wa.wsdot.android.wsdot.NavGraphDirections
import gov.wa.wsdot.android.wsdot.R
import gov.wa.wsdot.android.wsdot.databinding.MountainPassAlertsFragmentBinding
import gov.wa.wsdot.android.wsdot.db.mountainpass.MountainPassAlert
import gov.wa.wsdot.android.wsdot.db.travelerinfo.BridgeAlert
import gov.wa.wsdot.android.wsdot.di.Injectable
import gov.wa.wsdot.android.wsdot.ui.MainActivity
import gov.wa.wsdot.android.wsdot.ui.common.binding.FragmentDataBindingComponent
import gov.wa.wsdot.android.wsdot.ui.common.callback.RetryCallback
import gov.wa.wsdot.android.wsdot.util.AppExecutors
import gov.wa.wsdot.android.wsdot.util.autoCleared
import gov.wa.wsdot.android.wsdot.model.common.Status
import javax.inject.Inject

class MountainPassAlertsFragment : DaggerFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var mountainPassAlertsViewModel: MountainPassAlertsViewModel

    @Inject
    lateinit var appExecutors: AppExecutors

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<MountainPassAlertsFragmentBinding>()

    private var adapter by autoCleared<MountainPassAlertsListAdapter>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).setScreenName(this::class.java.simpleName)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mountainPassAlertsViewModel = activity?.run {
            ViewModelProvider(this, viewModelFactory).get(MountainPassAlertsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        val dataBinding = DataBindingUtil.inflate<MountainPassAlertsFragmentBinding>(
            inflater,
            R.layout.mountain_pass_alerts_fragment,
            container,
            false
        )

        mountainPassAlertsViewModel.refresh()

        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                mountainPassAlertsViewModel.refresh()
            }
        }

        dataBinding.viewModel = mountainPassAlertsViewModel

        binding = dataBinding

        // animation
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)

        return dataBinding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        // pass function to be called on adapter item tap and favorite
        val adapter = MountainPassAlertsListAdapter(dataBindingComponent, appExecutors)
        { alert -> navigateToAlert(alert) }

        this.adapter = adapter
        binding.passList.adapter = adapter

        postponeEnterTransition()
        binding.passList.viewTreeObserver
            .addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }

        mountainPassAlertsViewModel.passAlerts.observe(viewLifecycleOwner, Observer { alertsResource ->
            if (alertsResource?.data != null) {
                if (alertsResource.status != Status.ERROR && alertsResource.status != Status.LOADING) {

                    if (alertsResource.data.isEmpty()) {

                        binding.emptyListView.visibility = View.VISIBLE

                    } else {
                        binding.emptyListView.visibility = View.GONE
                    }

                    adapter.submitList(alertsResource.data)

                }

            } else {
                adapter.submitList(emptyList())
            }
        })
    }

    private fun navigateToAlert(alert: MountainPassAlert){
        val action = NavGraphDirections.actionGlobalNavMountainPassAlertFragment(
            alert.eventId, alert.passId, alert.mountainPass + " Alert"
        )
        findNavController().navigate(action)
    }

}
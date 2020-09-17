package dev.ronnie.spendingcalculator.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import dev.ronnie.spendingcalculator.R
import dev.ronnie.spendingcalculator.domain.SmsData
import dev.ronnie.spendingcalculator.databinding.FragmentPieChartBinding
import dev.ronnie.spendingcalculator.utils.EventObject
import dev.ronnie.spendingcalculator.utils.InjectorUtils
import dev.ronnie.spendingcalculator.utils.SMS_PERMISSION_REQUEST
import dev.ronnie.spendingcalculator.presentation.viewmodels.FragmentPieChartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentPieChart : Fragment() {

    private lateinit var binding: FragmentPieChartBinding
    private lateinit var smsData: SmsData
    private val viewModel: FragmentPieChartViewModel by viewModels {
        InjectorUtils.provideFragmentPieViewModelFactory(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        binding = FragmentPieChartBinding.inflate(inflater, container, false)
        context ?: return binding.root
        val toolbar: Toolbar = binding.toolbar as Toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar!!.title = "Transactions"


        getSmsPermission()


        return binding.root
    }

    private fun getSmsPermission() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_SMS),
                SMS_PERMISSION_REQUEST
            )

        } else {
            getMessages()


        }

        EventObject.message.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { message ->
                if (message == "Permission Granted") {
                    getMessages()
                } else {
                    Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun getMessages() {
        lifecycleScope.launch {
            viewModel.sms.observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    setPie(it)
                    smsData = it

                    Log.d("SmsHere", it.toString())
                } else {
                    Log.d("SmsHere", "Empty")
                }
            })
        }
    }

    private fun setPie(smsData: SmsData) {

        binding.anyChart.setProgressBar(binding.progress)
        binding.creditAmount.text = requireContext().getString(
            R.string.amount_debited_or_credited,
            viewModel.getfomartedAmount(smsData.TotalCreditedAmount)
        )
        binding.debitAmount.text = requireContext().getString(
            R.string.amount_debited_or_credited,
            viewModel.getfomartedAmount(smsData.totalDebitedAmount)
        )

        val data = ArrayList<DataEntry>()
        data.add(ValueDataEntry("Total Income", smsData.TotalCreditedAmount))
        data.add(ValueDataEntry("Total Expenses", smsData.totalDebitedAmount))
        val pie = AnyChart.pie()

        pie.setOnClickListener(object :
            ListenersInterface.OnClickListener(arrayOf("x", "value")) {
            override fun onClick(event: Event) {

                lifecycleScope.launch(Dispatchers.Main) {
                    delay(100)
                    openFragmentList(smsData, event)
                }
            }
        })
        pie.data(data)
        binding.anyChart.setChart(pie)

    }

    private fun openFragmentList(
        smsData: SmsData,
        event: Event
    ) {
        val name = event.data["x"].toString()

        val bundle = Bundle()

        if (name == "Total Income") {

            bundle.putParcelableArrayList(
                "list",
                smsData.creditSmsList as java.util.ArrayList<out Parcelable>
            )
            bundle.putString("type", "Credit Messages")

            binding.root.findNavController().navigate(R.id.toFragmentList, bundle)

        } else {
            bundle.putParcelableArrayList(
                "list",
                smsData.DebitSmsList as java.util.ArrayList<out Parcelable>
            )
            bundle.putString("type", "Debit Messages")

            binding.root.findNavController().navigate(R.id.toFragmentList, bundle)

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_search) {
            binding.root.findNavController().navigate(R.id.toFragmentSearch)
        }
        return super.onOptionsItemSelected(item)
    }


}

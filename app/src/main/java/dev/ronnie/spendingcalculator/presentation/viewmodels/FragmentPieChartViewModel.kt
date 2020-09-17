package dev.ronnie.spendingcalculator.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dev.ronnie.spendingcalculator.data.repository.SmsRepository
import java.text.DecimalFormat
import java.text.NumberFormat

class FragmentPieChartViewModel(private val smsRepository: SmsRepository) : ViewModel() {

    val sms = smsRepository.getMessages()


    fun getfomartedAmount(amount: Double) = formatCurrency(amount)

    private fun formatCurrency(number: Double): String {
        val formatter: NumberFormat = DecimalFormat("#,###")
        val formattedNumber: String = formatter.format(number)

        return "$formattedNumber.00"
    }
}
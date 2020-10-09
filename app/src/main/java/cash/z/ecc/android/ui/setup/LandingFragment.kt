package cash.z.ecc.android.ui.setup

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentLandingBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.locale
import cash.z.ecc.android.ext.toAppString
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.feedback.Report.Funnel.Restore
import cash.z.ecc.android.feedback.Report.Tap.*
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.WalletSetupState.SEED_WITHOUT_BACKUP
import cash.z.ecc.android.ui.setup.WalletSetupViewModel.WalletSetupState.SEED_WITH_BACKUP
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class LandingFragment : BaseFragment<FragmentLandingBinding>() {
    override val screen = Report.Screen.LANDING

    private val walletSetup: WalletSetupViewModel by activityViewModel(false)

    private var skipCount: Int = 0

    override fun inflate(inflater: LayoutInflater): FragmentLandingBinding =
        FragmentLandingBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonPositive.setOnClickListener {
            when (binding.buttonPositive.text.toString().toLowerCase(locale())) {
                R.string.landing_button_primary.toAppString(true) -> onNewWallet().also { tapped(LANDING_NEW) }
                R.string.landing_button_primary_create_success.toAppString(true) -> onBackupWallet().also { tapped(LANDING_BACKUP) }
            }
        }
        binding.buttonNegative.setOnLongClickListener {
            tapped(DEVELOPER_WALLET_PROMPT)
            if (binding.buttonNegative.text.toString().toLowerCase(locale()) == "restore") {
                MaterialAlertDialogBuilder(activity)
                    .setMessage("Would you like to import the dev wallet?\n\nIf so, please only send 0.0001 ZEC at a time and return some later so that the account remains funded.")
                    .setTitle("Import Dev Wallet?")
                    .setCancelable(true)
                    .setPositiveButton("Import") { dialog, _ ->
                        tapped(DEVELOPER_WALLET_IMPORT)
                        dialog.dismiss()
                        onUseDevWallet()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        tapped(DEVELOPER_WALLET_CANCEL)
                        dialog.dismiss()
                    }
                    .show()
                true
            } else {
                false
            }
        }
        binding.buttonNegative.setOnClickListener {
            when (binding.buttonNegative.text.toString().toLowerCase(locale())) {
                R.string.landing_button_secondary.toAppString(true) -> onRestoreWallet().also {
                    mainActivity?.reportFunnel(Restore.Initiated)
                    tapped(LANDING_RESTORE)
                }
                else -> onSkip(++skipCount)
            }
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)

        walletSetup.checkSeed().onEach {
            when(it) {
                SEED_WITHOUT_BACKUP, SEED_WITH_BACKUP -> {
                    mainActivity?.safeNavigate(R.id.nav_backup)
                }
                else -> {}
            }
        }.launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.hideKeyboard()
    }

    private fun onSkip(count: Int) {
        when (count) {
            1 -> {
                tapped(LANDING_BACKUP_SKIPPED_1)
                binding.textMessage.setText(R.string.landing_backup_skipped_message_1)
                binding.buttonNegative.setText(R.string.landing_button_backup_skipped_1)
            }
            2 -> {
                tapped(LANDING_BACKUP_SKIPPED_2)
                binding.textMessage.setText(R.string.landing_backup_skipped_message_2)
                binding.buttonNegative.setText(R.string.landing_button_backup_skipped_2)
            }
            else -> {
                tapped(LANDING_BACKUP_SKIPPED_3)
                onEnterWallet()
            }
        }
    }

    private fun onRestoreWallet() {
        mainActivity?.safeNavigate(R.id.action_nav_landing_to_nav_restore)
    }

    // AKA import wallet
    private fun onUseDevWallet() {
        val seedPhrase = "still champion voice habit trend flight survey between bitter process artefact blind carbon truly provide dizzy crush flush breeze blouse charge solid fish spread"
        val birthday = 663174 //991645
        mainActivity?.apply {
            lifecycleScope.launch {
                mainActivity?.startSync(walletSetup.importWallet(seedPhrase, birthday))
            }
            binding.buttonPositive.isEnabled = true
            binding.textMessage.setText(R.string.landing_import_success_message)
            binding.buttonNegative.setText(R.string.landing_button_secondary_import_success)
            binding.buttonPositive.setText(R.string.landing_import_success_primary_button)
            playSound("sound_receive_small.mp3")
            vibrateSuccess()
        }
    }

    private fun onNewWallet() {
        lifecycleScope.launch {
            binding.buttonPositive.setText(R.string.landing_button_progress_create)
            binding.buttonPositive.isEnabled = false

            try {
                val initializer = walletSetup.newWallet()
                if (!initializer.accountsCreated) {
                    binding.buttonPositive.isEnabled = true
                    binding.buttonPositive.setText(R.string.landing_button_primary)
                    throw IllegalStateException("New wallet should result in accounts table being created")
                }
                mainActivity?.startSync(initializer)

                binding.buttonPositive.isEnabled = true
                binding.textMessage.setText(R.string.landing_create_success_message)
                binding.buttonNegative.setText(R.string.landing_button_secondary_create_success)
                binding.buttonPositive.setText(R.string.landing_button_primary_create_success)
                mainActivity?.playSound("sound_receive_small.mp3")
                mainActivity?.vibrateSuccess()
            } catch (t: Throwable) {
                Toast.makeText(context, "Failed to create wallet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onBackupWallet() {
        skipCount = 0
        mainActivity?.safeNavigate(R.id.action_nav_landing_to_nav_backup)
    }

    private fun onEnterWallet() {
        skipCount = 0
        mainActivity?.navController?.popBackStack()
    }
}
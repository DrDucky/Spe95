package com.pomplarg.spe95

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.pomplarg.spe95.databinding.ActivityMainBinding
import com.pomplarg.spe95.signin.ui.SignInActivity
import com.pomplarg.spe95.utils.Constants
import com.pomplarg.spe95.utils.Constants.Companion.SHARED_PREF_FRAGMENT_KEY
import kotlinx.android.synthetic.main.nav_header.view.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ToolbarTitleListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var args: Bundle
    private lateinit var graph: NavGraph
    private lateinit var auth: FirebaseAuth
    val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    var updateType: String? = FLEXIBLE

    private val updateListener: InstallStateUpdatedListener? = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            showDialogForCompleteUpdate()
        }
    }

    private lateinit var binding: ActivityMainBinding
    private fun goToLoginActivity() {
        intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is signed in (non-null) and update UI accordingly.
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToLoginActivity()
        } else {

            //Check if app update is present
            val firebaseRemoteConfig = Firebase.remoteConfig
            firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            retrieveConfig(firebaseRemoteConfig)


            binding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)
            drawerLayout = binding.drawerLayout

            navController = findNavController(R.id.nav_host)

            // Set up ActionBar
            setSupportActionBar(binding.toolbar)

            val navInflater = navController.navInflater

            graph = navInflater.inflate(R.navigation.nav_main)
            graph.startDestination = R.id.speOperationFragment

            val pref: String? =
                PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(SHARED_PREF_FRAGMENT_KEY, "cyno")
            args = Bundle()
            args.putString("specialty", pref)

            navController.setGraph(graph, args)

            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.speOperationFragment,
                    R.id.agentFragment
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            // Set up navigation menu
            binding.navigationView.setupWithNavController(navController)
            binding.navigationView.setNavigationItemSelectedListener(this)
            binding.navigationView.setCheckedItem(R.id.cyno_fragment)
            binding.logout.setOnClickListener(::logout)
            binding.navigationView.getHeaderView(0).tv_header_username.text = currentUser.email
            binding.navigationView.getHeaderView(0).tv_header_version.text = getString(R.string.menu_version, BuildConfig.VERSION_NAME)

            //Open by default the menu to select the good specialty
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->

                when (updateType) {
                    FLEXIBLE  -> {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                            showDialogForCompleteUpdate()
                        }
                    }

                    IMMEDIATE -> {
                        if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                        ) {
                            // If an in-app update is already running, resume the update.
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.IMMEDIATE,
                                this,
                                IMMEDIATE_UPDATE_REQUEST_CODE
                            )
                        }
                    }
                }
            }
    }

    override fun onDestroy() {
        if (updateType == FLEXIBLE) {
            updateListener?.let {
                appUpdateManager.unregisterListener(it)
            }
        }
        super.onDestroy()
    }

    private fun logout(v: View) {
        AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Etes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton(
                android.R.string.yes
            ) { dialogInterface, which ->
                auth.signOut()
                goToLoginActivity()
            }
            .show()
    }

    private fun retrieveConfig(firebaseRemoteConfig: FirebaseRemoteConfig) {
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                updateType = firebaseRemoteConfig.getString(IN_APP_CONFIG_KEY)
                if (updateType == FLEXIBLE) {
                    checkForFlexibleInappUpdate()
                } else {
                    checkForImmediateAppUpdate()
                }
            }
        }
    }

    private fun checkForFlexibleInappUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                updateListener?.let {
                    appUpdateManager.registerListener(it)
                }
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.FLEXIBLE, this, FLEXIBLE_UPDATE_REQUEST_CODE)
            }
        }
    }

    private fun checkForImmediateAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, IMMEDIATE_UPDATE_REQUEST_CODE)
            }
        }
    }

    private fun showDialogForCompleteUpdate() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mise à jour de l'application")
        builder.setMessage("\nUne mise à jour vient d'être effectuée. Vous devez redémarrer l'application.")
        builder.setPositiveButton("Restart") { dialog, which ->
            appUpdateManager.completeUpdate()
        }
        builder.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cyno_fragment -> {
                args.putString("specialty", Constants.FIRESTORE_CYNO_DOCUMENT)
                navController.navigate(R.id.speOperationFragment, args)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.sd_fragment -> {
                args.putString("specialty", Constants.FIRESTORE_SD_DOCUMENT)
                navController.navigate(R.id.speOperationFragment, args)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.ra_fragment -> {
                args.putString("specialty", Constants.FIRESTORE_RA_DOCUMENT)
                navController.navigate(R.id.speOperationFragment, args)
                drawerLayout.closeDrawers()
                return true
            }
            else               -> {
                drawerLayout.closeDrawers()
                return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(
                    item
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.map_fragment -> {
                navController.navigate(R.id.mapFragment, args)
                true
            }
            R.id.stats_fragment -> {
                navController.navigate(R.id.statistiquesFragment, args)
                true
            }
            else                -> super.onOptionsItemSelected(item)
        }
    }

    override fun updateTitle(titleToolbar: String) {
        binding.toolbar.title = titleToolbar
    }

    companion object {
        const val TAG = "MainActivity"
        const val FLEXIBLE = "flexible"
        const val IMMEDIATE = "immediate"
        const val IN_APP_CONFIG_KEY = "in_app_update_type"
        const val FLEXIBLE_UPDATE_REQUEST_CODE = 1
        const val IMMEDIATE_UPDATE_REQUEST_CODE = 2
    }
}

interface ToolbarTitleListener {
    fun updateTitle(titleToolbar: String)
}
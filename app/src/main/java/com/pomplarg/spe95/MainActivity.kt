package com.pomplarg.spe95

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
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
            binding.navigationView.getHeaderView(0).tv_header_username.text = currentUser?.email

        }
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
}

interface ToolbarTitleListener {
    fun updateTitle(titleToolbar: String)
}
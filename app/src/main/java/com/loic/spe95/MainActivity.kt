package com.loic.spe95

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import com.google.firebase.auth.FirebaseUser
import com.loic.spe95.databinding.ActivityMainBinding
import com.loic.spe95.signin.ui.SignInActivity
import com.loic.spe95.utils.Constants.Companion.SHARED_PREF_FRAGMENT_KEY
import kotlinx.android.synthetic.main.nav_header.view.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var args: Bundle
    private lateinit var graph: NavGraph
    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityMainBinding


    public override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser == null) {
            intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        updateUi(currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout

        navController = findNavController(R.id.nav_host)

        // Set up ActionBar
        setSupportActionBar(binding.toolbar)

        val isTablet = resources.getBoolean(R.bool.isTablet)
        val navInflater = navController.navInflater

        when (isTablet) {
            false -> {
                graph = navInflater.inflate(R.navigation.nav_main)
                graph.startDestination = R.id.speOperationFragment
            }
            true  -> {
                graph = navInflater.inflate(R.navigation.nav_speoperation_tab)
                graph.startDestination = R.id.speOperationFragment

            }
        }


        val pref: Int =
            PreferenceManager.getDefaultSharedPreferences(this).getInt(SHARED_PREF_FRAGMENT_KEY, 1)
        args = Bundle()
        args.putInt("specialtyId", pref)

        navController.setGraph(graph, args)

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        // Set up navigation menu
        binding.navigationView.setupWithNavController(navController)
        binding.navigationView.setNavigationItemSelectedListener(this)
        binding.navigationView.setCheckedItem(R.id.cyno_fragment)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cyno_fragment -> {
                args.putInt("specialtyId", 1)
                navController.navigate(R.id.speOperationFragment, args)
                drawerLayout.closeDrawers()
                return true
            }
            R.id.sd_fragment   -> {
                args.putInt("specialtyId", 2)
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

    private fun updateUi(currentUser: FirebaseUser?) {
        binding.navigationView.getHeaderView(0).tv_header_username.text = currentUser?.email
    }
}
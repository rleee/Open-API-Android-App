package com.codingwithmitch.openapi.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.codingwithmitch.openapi.R
import com.codingwithmitch.openapi.ui.BaseActivity
import com.codingwithmitch.openapi.ui.auth.AuthActivity
import com.codingwithmitch.openapi.ui.main.account.ChangePasswordFragment
import com.codingwithmitch.openapi.ui.main.account.UpdateAccountFragment
import com.codingwithmitch.openapi.ui.main.blog.UpdateBlogFragment
import com.codingwithmitch.openapi.ui.main.blog.ViewBlogFragment
import com.codingwithmitch.openapi.util.BottomNavController
import com.codingwithmitch.openapi.util.setUpNavigation
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(),
    BottomNavController.NavGraphProvider,
    BottomNavController.OnNavigationGraphChanged,
    BottomNavController.OnNavigationReselectedListener {

    private lateinit var bottomNavigationView: BottomNavigationView
    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.nav_blog,
            this, this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.onNavigationItemSelected()
        }

        setupActionBar()
        subscribeObserver()
    }

    override fun displayProgressBar(boolean: Boolean) {
        if (boolean) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }

    private fun subscribeObserver() {
        sessionManager.cacheToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivity: subscribeObserver: AuthToken: $authToken")
            if (authToken == null || authToken.account_pk == -1 || authToken.token == null) {
                navAuthActivity()
            }
        })
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(tool_bar)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed() // android.R.id.home is The Up Button
        }
        return super.onOptionsItemSelected(item)
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    override fun onBackPressed() {
        bottomNavController.onBackPressed()
    }

    override fun getNavGraphId(itemId: Int): Int {
        return when (itemId) {
            R.id.nav_blog -> R.navigation.nav_blog
            R.id.nav_account -> R.navigation.nav_account
            R.id.nav_create_blog -> R.navigation.nav_create_blog
            else -> R.navigation.nav_blog
        }
    }

    override fun onGraphChange() {
        expandAppBar()
    }

    override fun onReselectNavItem(navController: NavController, fragment: Fragment) {
        when (fragment) {
            is ViewBlogFragment -> navController.navigate(R.id.action_viewBlogFragment_to_blogFragment)
            is UpdateBlogFragment -> navController.navigate(R.id.action_updateBlogFragment_to_blogFragment)
            is UpdateAccountFragment -> navController.navigate(R.id.action_updateAccountFragment_to_accountFragment)
            is ChangePasswordFragment -> navController.navigate(R.id.action_changePasswordFragment_to_accountFragment)
        }
    }
}
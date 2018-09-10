package com.google.firebase.quickstart.auth.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.firebase.quickstart.auth.R
import kotlinx.android.synthetic.main.activity_chooser.*

/**
 * Simple list-based Activity to redirect to one of the other Activities. This Activity does not
 * contain any useful code related to Firebase Authentication. You may want to start with
 * one of the following Files:
 *     {@link GoogleSignInActivity}
 *     {@link FacebookLoginActivity}
 *     {@link TwitterLoginActivity}
 *     {@link EmailPasswordActivity}
 *     {@link PasswordlessActivity}
 *     {@link PhoneAuthActivity}
 *     {@link AnonymousAuthActivity}
 *     {@link CustomAuthActivity}
 */
class ChooserActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)

        // Set up Adapter
        val adapter = MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES)
        adapter.setDescriptionIds(DESCRIPTION_IDS)

        listView.adapter = adapter
        listView.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val clicked = CLASSES[position]
        startActivity(Intent(this, clicked))
    }

    class MyArrayAdapter(private val mContext: Context, resource: Int, private val mClasses: Array<Class<*>>)
        : ArrayAdapter<Class<*>>(mContext, resource, mClasses) {
        private var mDescriptionIds: IntArray? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var view = convertView

            if (convertView == null) {
                val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(android.R.layout.simple_list_item_2, null)
            }

            //Android internal resource hence can't use synthetic binding
            view?.findViewById<TextView>(android.R.id.text1)?.text = mClasses[position].simpleName
            view?.findViewById<TextView>(android.R.id.text2)?.setText(mDescriptionIds!![position])

            return view
        }

        fun setDescriptionIds(descriptionIds: IntArray) {
            mDescriptionIds = descriptionIds
        }
    }

    companion object {
        private val CLASSES = arrayOf(GoogleSignInActivity::class.java,
                FacebookLoginActivity::class.java, TwitterLoginActivity::class.java,
                EmailPasswordActivity::class.java, PasswordlessActivity::class.java,
                PhoneAuthActivity::class.java, AnonymousAuthActivity::class.java,
                FirebaseUIActivity::class.java, CustomAuthActivity::class.java)
        private val DESCRIPTION_IDS = intArrayOf(R.string.desc_google_sign_in,
                R.string.desc_facebook_login, R.string.desc_twitter_login,
                R.string.desc_emailpassword, R.string.desc_passwordless,
                R.string.desc_phone_auth, R.string.desc_anonymous_auth,
                R.string.desc_firebase_ui, R.string.desc_custom_auth)
    }
}
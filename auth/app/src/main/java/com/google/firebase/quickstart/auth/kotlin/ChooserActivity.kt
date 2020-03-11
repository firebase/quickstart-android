package com.google.firebase.quickstart.auth.kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.quickstart.auth.R
import kotlinx.android.synthetic.main.activity_chooser.listView

/**
 * Simple list-based Activity to redirect to one of the other Activities. This Activity does not
 * contain any useful code related to Firebase Authentication. You may want to start with
 * one of the following Files:
 *     {@link GoogleSignInActivity}
 *     {@link FacebookLoginActivity}
 *     {@link EmailPasswordActivity}
 *     {@link PasswordlessActivity}
 *     {@link PhoneAuthActivity}
 *     {@link AnonymousAuthActivity}
 *     {@link CustomAuthActivity}
 *     {@link GenericIdpActivity}
 */
class ChooserActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)

        // Set up Adapter
        val adapter = MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES as Array<Class<*>>)
        adapter.setDescriptionIds(DESCRIPTION_IDS)

        listView.adapter = adapter
        listView.onItemClickListener = this
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val clicked = CLASSES[position]
        startActivity(Intent(this, clicked))
    }

    class MyArrayAdapter(
        private val ctx: Context,
        resource: Int,
        private val classes: Array<Class<*>>
    ) :
        ArrayAdapter<Class<*>>(ctx, resource, classes) {
        private var descriptionIds: IntArray? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView

            if (convertView == null) {
                val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(android.R.layout.simple_list_item_2, null)
            }

            // Android internal resource hence can't use synthetic binding
            view?.findViewById<TextView>(android.R.id.text1)?.text = classes[position].simpleName
            view?.findViewById<TextView>(android.R.id.text2)?.setText(descriptionIds!![position])

            return view!!
        }

        fun setDescriptionIds(descriptionIds: IntArray) {
            this.descriptionIds = descriptionIds
        }
    }

    companion object {
        private val CLASSES = arrayOf(
                GoogleSignInActivity::class.java,
                FacebookLoginActivity::class.java,
                EmailPasswordActivity::class.java,
                PasswordlessActivity::class.java,
                PhoneAuthActivity::class.java,
                AnonymousAuthActivity::class.java,
                FirebaseUIActivity::class.java,
                CustomAuthActivity::class.java,
                GenericIdpActivity::class.java
        )
        private val DESCRIPTION_IDS = intArrayOf(
                R.string.desc_google_sign_in,
                R.string.desc_facebook_login,
                R.string.desc_emailpassword,
                R.string.desc_passwordless,
                R.string.desc_phone_auth,
                R.string.desc_anonymous_auth,
                R.string.desc_firebase_ui,
                R.string.desc_custom_auth,
                R.string.desc_generic_idp
        )
    }
}

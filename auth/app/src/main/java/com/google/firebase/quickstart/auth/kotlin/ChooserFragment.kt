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
import androidx.fragment.app.Fragment
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.ActivityChooserBinding
import com.google.firebase.quickstart.auth.databinding.FragmentChooserBinding

/**
 * Simple list-based Fragment to redirect to one of the other Fragments. This Fragment does not
 * contain any useful code related to Firebase Authentication. You may want to start with
 * one of the following Files:
 *     {@link GoogleSignInFragment}
 *     {@link FacebookLoginFragment}
 *     {@link EmailPasswordFragment}
 *     {@link PasswordlessFragment}
 *     {@link PhoneAuthFragment}
 *     {@link AnonymousAuthFragment}
 *     {@link CustomAuthFragment}
 *     {@link GenericIdpFragment}
 *     {@link MultiFactorFragment}
 */
class ChooserFragment : Fragment() {

    private var _binding: FragmentChooserBinding? = null
    private val binding: FragmentChooserBinding
        get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Adapter
        val adapter = MyArrayAdapter(requireContext(), android.R.layout.simple_list_item_2, CLASSES as Array<Class<*>>)
        adapter.setDescriptionIds(DESCRIPTION_IDS)

        binding.listView.adapter = adapter
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val clicked = CLASSES[position]
            // TODO: navigate to another fragment instead
            startActivity(Intent(context, clicked))
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // TODO: Create an array of fragment destinations
        private val CLASSES = arrayOf(
                GoogleSignInActivity::class.java,
                FacebookLoginActivity::class.java,
                EmailPasswordActivity::class.java,
                PasswordlessActivity::class.java,
                PhoneAuthActivity::class.java,
                AnonymousAuthActivity::class.java,
                FirebaseUIActivity::class.java,
                CustomAuthActivity::class.java,
                GenericIdpActivity::class.java,
                MultiFactorActivity::class.java
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
                R.string.desc_generic_idp,
                R.string.desc_multi_factor
        )
    }
}

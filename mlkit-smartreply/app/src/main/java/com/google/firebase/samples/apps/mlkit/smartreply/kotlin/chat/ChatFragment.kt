package com.google.firebase.samples.apps.mlkit.smartreply.kotlin.chat

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.samples.apps.mlkit.smartreply.R
import com.google.firebase.samples.apps.mlkit.smartreply.databinding.ChatFragmentBinding
import com.google.firebase.samples.apps.mlkit.smartreply.kotlin.model.Message
import java.util.ArrayList
import java.util.Calendar

class ChatFragment : Fragment(), ReplyChipAdapter.ClickListener {

    private var _binding: ChatFragmentBinding? = null
    private val binding: ChatFragmentBinding get() = _binding!!
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: MessageListAdapter
    private lateinit var chipAdapter: ReplyChipAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

        // Set up recycler view for chat messages
        chatAdapter = MessageListAdapter()

        // Set up recycler view for smart replies
        val chipManager = LinearLayoutManager(context)
        chipManager.orientation = RecyclerView.HORIZONTAL
        chipAdapter = ReplyChipAdapter(this)

        with(binding) {
            chatHistory.layoutManager = LinearLayoutManager(context)
            smartRepliesRecycler.layoutManager = chipManager
            smartRepliesRecycler.adapter = chipAdapter

            chatHistory.adapter = chatAdapter
            chatHistory.setOnTouchListener { touchView, motionEvent ->
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(touchView.windowToken, 0)
                false
            }

            switchEmulatedUser.setOnClickListener {
                chatAdapter.emulatingRemoteUser = !chatAdapter.emulatingRemoteUser
                viewModel.switchUser()
            }

            sendButton.setOnClickListener(View.OnClickListener {
                val input = inputText.text.toString()
                if (TextUtils.isEmpty(input)) {
                    return@OnClickListener
                }

                viewModel.addMessage(input)
                inputText.setText("")
            })

            viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
                chatAdapter.setMessages(messages!!)
                if (chatAdapter.itemCount > 0) {
                    chatHistory.smoothScrollToPosition(chatAdapter.itemCount - 1)
                }
            })

            viewModel.getEmulatingRemoteUser().observe(viewLifecycleOwner, Observer { isEmulatingRemoteUser ->
                if (isEmulatingRemoteUser!!) {
                    switchText.setText(R.string.chatting_as_red)
                    switchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                } else {
                    switchText.setText(R.string.chatting_as_blue)
                    switchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
                }
            })
        }

        viewModel.getSuggestions().observe(viewLifecycleOwner, Observer { suggestions ->
            chipAdapter.setSuggestions(suggestions!!)
        })

        val messageList = ArrayList<Message>()
        messageList.add(Message("Hello. How are you?", false, System.currentTimeMillis()))
        viewModel.setMessages(messageList)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.chat_fragment_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.generateHistoryBasic -> {
                generateChatHistoryBasic()
                return true
            }
            R.id.generateHistorySensitive -> {
                generateChatHistoryWithSensitiveContent()
                return true
            }
            R.id.clearHistory -> {
                viewModel.setMessages(ArrayList())
                return true
            }
        }
        return false
    }

    override fun onChipClick(chipText: String) {
        binding.inputText.setText(chipText)
    }

    private fun generateChatHistoryBasic() {
        val messageList = ArrayList<Message>()
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DATE, -1)
        messageList.add(Message("Hello", true, calendar.timeInMillis))

        calendar.add(Calendar.MINUTE, 10)
        messageList.add(Message("Hey", false, calendar.timeInMillis))

        viewModel.setMessages(messageList)
    }

    private fun generateChatHistoryWithSensitiveContent() {
        val messageList = ArrayList<Message>()
        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DATE, -1)
        messageList.add(Message("Hi", false, calendar.timeInMillis))

        calendar.add(Calendar.MINUTE, 10)
        messageList.add(Message("How are you?", true, calendar.timeInMillis))

        calendar.add(Calendar.MINUTE, 10)
        messageList.add(Message("My cat died", false, calendar.timeInMillis))

        viewModel.setMessages(messageList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }
}

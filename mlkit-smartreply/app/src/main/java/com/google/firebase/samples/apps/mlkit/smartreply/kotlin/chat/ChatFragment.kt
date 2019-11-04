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
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.samples.apps.mlkit.smartreply.R
import com.google.firebase.samples.apps.mlkit.smartreply.kotlin.model.Message
import java.util.ArrayList
import java.util.Calendar

class ChatFragment : Fragment(), ReplyChipAdapter.ClickListener {

    private lateinit var viewModel: ChatViewModel
    private lateinit var inputText: TextView
    private lateinit var sendButton: Button
    private lateinit var switchUserButton: Button

    private lateinit var chatRecycler: RecyclerView
    private lateinit var chatAdapter: MessageListAdapter

    private lateinit var smartRepliesRecyler: RecyclerView
    private lateinit var chipAdapter: ReplyChipAdapter

    private lateinit var emulatedUserText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.chat_fragment, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ChatViewModel::class.java)

        chatRecycler = view.findViewById(R.id.chatHistory)
        emulatedUserText = view.findViewById(R.id.switchText)
        smartRepliesRecyler = view.findViewById(R.id.smartRepliesRecycler)
        inputText = view.findViewById(R.id.inputText)
        sendButton = view.findViewById(R.id.button)
        switchUserButton = view.findViewById(R.id.switchEmulatedUser)

        // Set up recycler view for chat messages
        val layoutManager = LinearLayoutManager(context)
        chatRecycler.layoutManager = layoutManager
        chatAdapter = MessageListAdapter()

        // Set up recycler view for smart replies
        val chipManager = LinearLayoutManager(context)
        chipManager.orientation = RecyclerView.HORIZONTAL
        chipAdapter = ReplyChipAdapter(this)
        smartRepliesRecyler.layoutManager = chipManager
        smartRepliesRecyler.adapter = chipAdapter

        chatRecycler.adapter = chatAdapter
        chatRecycler.setOnTouchListener { touchView, motionEvent ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(touchView.windowToken, 0)
            false
        }

        switchUserButton.setOnClickListener {
            chatAdapter.emulatingRemoteUser = !chatAdapter.emulatingRemoteUser
            viewModel.switchUser()
        }

        sendButton.setOnClickListener(View.OnClickListener {
            val input = inputText.text.toString()
            if (TextUtils.isEmpty(input)) {
                return@OnClickListener
            }

            viewModel.addMessage(input)
            inputText.text = ""
        })

        viewModel.getSuggestions().observe(this, Observer { suggestions -> chipAdapter.setSuggestions(suggestions!!) })

        viewModel.messages.observe(this, Observer { messages ->
            chatAdapter.setMessages(messages!!)
            if (chatAdapter.itemCount > 0) {
                chatRecycler.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        })

        viewModel.getEmulatingRemoteUser().observe(this, Observer { isEmulatingRemoteUser ->
            if (isEmulatingRemoteUser!!) {
                emulatedUserText.setText(R.string.chatting_as_red)
                emulatedUserText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            } else {
                emulatedUserText.setText(R.string.chatting_as_blue)
                emulatedUserText.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
            }
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
        inputText.text = chipText
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

    companion object {

        fun newInstance(): ChatFragment {
            return ChatFragment()
        }
    }
}

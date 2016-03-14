/*
 * Firebase UI Bindings Android Library
 *
 * Copyright © 2015 Firebase - All Rights Reserved
 * https://www.firebase.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binaryform must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY FIREBASE AS IS AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL FIREBASE BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.firebase.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This class is a generic way of backing an RecyclerView with a Firebase location.
 * It handles all of the child events at the given Firebase location. It marshals received data into the given
 * class type.
 *
 * To use this class in your app, subclass it passing in all required parameters and implement the
 * populateViewHolder method.
 *
 * <blockquote><pre>
 * {@code
 *     private static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
 *         TextView messageText;
 *         TextView nameText;
 *
 *         public ChatMessageViewHolder(View itemView) {
 *             super(itemView);
 *             nameText = (TextView)itemView.findViewById(android.R.id.text1);
 *             messageText = (TextView) itemView.findViewById(android.R.id.text2);
 *         }
 *     }
 *
 *     FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder> adapter;
 *     ref = new Firebase("https://<yourapp>.firebaseio.com");
 *
 *     RecyclerView recycler = (RecyclerView) findViewById(R.id.messages_recycler);
 *     recycler.setHasFixedSize(true);
 *     recycler.setLayoutManager(new LinearLayoutManager(this));
 *
 *     adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatMessageViewHolder>(ChatMessage.class, android.R.layout.two_line_list_item, ChatMessageViewHolder.class, mRef) {
 *         public void populateViewHolder(ChatMessageViewHolder chatMessageViewHolder, ChatMessage chatMessage, int position) {
 *             chatMessageViewHolder.nameText.setText(chatMessage.getName());
 *             chatMessageViewHolder.messageText.setText(chatMessage.getMessage());
 *         }
 *     };
 *     recycler.setAdapter(mAdapter);
 * }
 * </pre></blockquote>
 *
 * @param <T> The Java class that maps to the type of objects stored in the Firebase location.
 * @param <VH> The ViewHolder class that contains the Views in the layout that is shown for each object.
 */
public abstract class FirebaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    Class<T> mModelClass;
    protected int mModelLayout;
    Class<VH> mViewHolderClass;
    FirebaseArray mSnapshots;

    /**
     * @param modelClass Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref        The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                   combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>
     */
    public FirebaseRecyclerAdapter(Class<T> modelClass, int modelLayout, Class<VH> viewHolderClass, Query ref) {
        mModelClass = modelClass;
        mModelLayout = modelLayout;
        mViewHolderClass = viewHolderClass;
        mSnapshots = new FirebaseArray(ref);

        mSnapshots.setOnChangedListener(new FirebaseArray.OnChangedListener() {
            @Override
            public void onChanged(EventType type, int index, int oldIndex) {
                switch (type) {
                    case Added:
                        notifyItemInserted(index);
                        break;
                    case Changed:
                        notifyItemChanged(index);
                        break;
                    case Removed:
                        notifyItemRemoved(index);
                        break;
                    case Moved:
                        notifyItemMoved(oldIndex, index);
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }
        });
    }

    /**
     * @param modelClass Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref        The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                   combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>
     */
    public FirebaseRecyclerAdapter(Class<T> modelClass, int modelLayout, Class<VH> viewHolderClass, DatabaseReference ref) {
        this(modelClass, modelLayout, viewHolderClass, (Query) ref);
    }

    public void cleanup() {
        mSnapshots.cleanup();
    }

    @Override
    public int getItemCount() {
        return mSnapshots.getCount();
    }

    public T getItem(int position) {
        return parseSnapshot(mSnapshots.getItem(position));
    }

    /**
     * This method parses the DataSnapshot into the requested type. You can override it in subclasses
     * to do custom parsing.
     *
     * @param snapshot the DataSnapshot to extract the model from
     * @return the model extracted from the DataSnapshot
     */
    protected T parseSnapshot(DataSnapshot snapshot) {
        return snapshot.getValue(mModelClass);
    }

    public DatabaseReference getRef(int position) { return mSnapshots.getItem(position).getRef(); }

    @Override
    public long getItemId(int position) {
        // http://stackoverflow.com/questions/5100071/whats-the-purpose-of-item-ids-in-android-listview-adapter
        return mSnapshots.getItem(position).getKey().hashCode();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(mModelLayout, parent, false);
        try {
            Constructor<VH> constructor = mViewHolderClass.getConstructor(View.class);
            return constructor.newInstance(view);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        T model = getItem(position);
        populateViewHolder(viewHolder, model, position);
    }

    /**
     * Each time the data at the given Firebase location changes, this method will be called for each item that needs
     * to be displayed. The first two arguments correspond to the mLayout and mModelClass given to the constructor of
     * this class. The third argument is the item's position in the list.
     * <p>
     * Your implementation should populate the view using the data contained in the model.
     *
     * @param viewHolder The view to populate
     * @param model      The object containing the data used to populate the view
     * @param position  The position in the list of the view being populated
     */
    abstract protected void populateViewHolder(VH viewHolder, T model, int position);
}

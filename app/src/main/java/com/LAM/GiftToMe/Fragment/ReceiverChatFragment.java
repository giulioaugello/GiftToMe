package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.LAM.GiftToMe.Adapter.ReceiverChatAdapter;
import com.LAM.GiftToMe.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ReceiverChatFragment extends Fragment {

    private Context mContext;
    private RecyclerView recyclerView;
    private ReceiverChatAdapter receiverChatAdapterl;

    private String receiverName, giftName;

    public ReceiverChatFragment(String receiverName, String giftName){
        this.receiverName = receiverName;
        this.giftName = giftName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.receiver_chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        recyclerView = v.findViewById(R.id.message_recyclerview);

        TextView messageUsername = v.findViewById(R.id.tw_username);
        TextView messageGiftName = v.findViewById(R.id.mes_gift_name);

        messageUsername.setText(receiverName);
        messageGiftName.setText(giftName);

        return v;
    }
}

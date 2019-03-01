package com.example.asus.gxchat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.ViewHolder> {
    private Context context;
    private List<Msg> mMsgList;
    private String head1, head2;
    static class ViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        ImageView leftHead;
        ImageView rightHead;
        ViewHolder(@NonNull View view) {
            super(view);
            leftLayout = view.findViewById(R.id.left_layout);
            rightLayout = view.findViewById(R.id.right_layout);
            leftMsg = view.findViewById(R.id.left_msg);
            rightMsg = view.findViewById(R.id.right_msg);
            leftHead = view.findViewById(R.id.iv_left_head);
            rightHead = view.findViewById(R.id.iv_right_head);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.msg_item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Msg msg = mMsgList.get(i);
        if (msg.getType() == Msg.TYPE_RECEIVED){
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.leftMsg.setText(msg.getContent());
            Glide.with(context).load(head1).into(viewHolder.leftHead);
        }else if (msg.getType() == Msg.TYPE_SENT){
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.rightMsg.setText(msg.getContent());
            if (head2 != null){
                Glide.with(context).load(head2).into(viewHolder.rightHead);
            }else{
                Glide.with(context).load(R.drawable.timg).into(viewHolder.rightHead);
            }
        }
    }

    MsgAdapter(List<Msg> msgList, String head1, String head2, Context context){
        mMsgList = msgList;
        this.head1 = head1;
        this.head2 = head2;
        this.context = context;
    }
    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

}

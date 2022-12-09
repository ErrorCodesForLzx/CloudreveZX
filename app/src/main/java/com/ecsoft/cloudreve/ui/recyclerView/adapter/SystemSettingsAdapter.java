package com.ecsoft.cloudreve.ui.recyclerView.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ecsoft.cloudreve.R;
import com.ecsoft.cloudreve.ui.recyclerView.entity.SystemSettingPO;

import java.util.List;

public class SystemSettingsAdapter extends RecyclerView.Adapter<SystemSettingsAdapter.SystemSettingsViewHolder> {

    private Context context; // 上下文对象
    private List<SystemSettingPO> systemSettingPOS; // 实体类集合
    private OnSystemItemClickedListener onSystemItemClickedListener; // 监听器接口

    public SystemSettingsAdapter(Context context, List<SystemSettingPO> systemSettingPOS) {
        this.context = context;
        this.systemSettingPOS = systemSettingPOS;
    }

    @NonNull
    @Override
    public SystemSettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 创建一个ViewHolder对象
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_settings, null);
        return new SystemSettingsViewHolder(inflate);

    }

    @Override
    public void onBindViewHolder(@NonNull SystemSettingsViewHolder holder, int position) {
        // 当某个项目即将显示
        // 获取这个项目的实体对象
        SystemSettingPO currentEntity = systemSettingPOS.get(holder.getAdapterPosition());
        // 是否可以跳转
        if (currentEntity.isCanGoto()){
            // 能跳转，显示跳转>图标
            holder.ivGotoIcon.setVisibility(View.VISIBLE);
        } else {
            // 不能跳转，隐藏跳转>图标
            holder.ivGotoIcon.setVisibility(View.GONE);
        }
        // 显示设置图标
        holder.ivIcon.setImageResource(currentEntity.getIconDrawable());
        // 显示设置标题
        holder.tvTitle.setText(currentEntity.getSettingTitle());
        // 设置右标签
        holder.tvRightLabel.setText(currentEntity.getRightLabel());
        // 绑定事件
        holder.clSettingsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 当root视图被单击是触发事件
                // 判断自定义事件是否被绑定成功
                if (onSystemItemClickedListener != null){
                    // 自定义事件绑定成功
                    onSystemItemClickedListener.onItemClickedListener(currentEntity,holder.getAdapterPosition()); // 触发事件
                }
            }
        });
    }

    // 绑定监听器
    public void setOnSystemItemClickedListener(OnSystemItemClickedListener onSystemItemClickedListener) {
        this.onSystemItemClickedListener = onSystemItemClickedListener;
    }

    @Override
    public int getItemCount() {
        return systemSettingPOS.size();
    }



    // 监听接口
    public interface OnSystemItemClickedListener {
        void onItemClickedListener(SystemSettingPO clickedEntity,int position);
    }

    // 视图承载对象
    class SystemSettingsViewHolder extends RecyclerView.ViewHolder{

        public ConstraintLayout clSettingsItem;
        public ImageView ivIcon;
        public TextView tvTitle;
        public TextView tvRightLabel;
        public ImageView ivGotoIcon;

        public SystemSettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            // 加载组件
            clSettingsItem = itemView.findViewById(R.id.cl_settings_item);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvRightLabel = itemView.findViewById(R.id.tv_right_label);
            ivGotoIcon = itemView.findViewById(R.id.iv_goto_icon);
        }
    }
}

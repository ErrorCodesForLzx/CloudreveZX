package com.ecsoft.cloudreve.ui.recyclerView.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ecsoft.cloudreve.R;
import com.ecsoft.cloudreve.ui.fileType.FileTypeJudgeUtil;
import com.ecsoft.cloudreve.ui.recyclerView.entity.FileTreePO;

import java.util.List;

public class FileTreeAdapter extends RecyclerView.Adapter<FileTreeAdapter.FileTreeViewHolder> {

    private List<FileTreePO> fileTreePOData; // 数据源
    private Context context; // 上下文对象
    private OnFileClickListener onFileClickListener;

    public void setOnFileClickListener(OnFileClickListener onFileClickListener) {
        this.onFileClickListener = onFileClickListener;
    }

    public FileTreeAdapter(List<FileTreePO> fileTreePOData, Context context) {
        this.fileTreePOData = fileTreePOData;
        this.context = context;
    }

    @NonNull
    @Override
    public FileTreeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 获取视图解析器
        LayoutInflater inflater = LayoutInflater.from(context);
        View inflatedView = inflater.inflate(R.layout.item_file_tree_file, null); // 获取到解析的视图
        return new FileTreeViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull FileTreeViewHolder holder, int position) {
        // 在其中一个单项目被绑定的时候触发
        // 获取当前需要绑定到RV的数据源
        FileTreePO fileTreePO = fileTreePOData.get(position);
        // 判断当前FileTree的类型是文件夹还是文件
        if (!fileTreePO.getType().equals("file")){
            holder.ivTreeType.setImageResource(R.drawable.ic_net_dir);
        } else {
            holder.ivTreeType.setImageResource(FileTypeJudgeUtil.getImageResourceByFileName(fileTreePO.getName()));
        }
        // holder.ivTreeType.setImageResource(fileTreePO.getType().equals("file") ? R.drawable.ic_net_unkonow : R.drawable.ic_net_dir);
        // 修改名称
        holder.tvTreeName.setText(fileTreePO.getName());
        // 为根视图绑定单击事件
        holder.clFileTreeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 判断本适配器是否绑定了项目单击事件
                if (onFileClickListener != null){
                    // 如果不为空，代表已经绑定了单击事件
                    onFileClickListener.onClick(holder.getAdapterPosition(), fileTreePOData.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileTreePOData.size(); // 返回源数据的大小
    }

    // 设置监听接口
    public interface  OnFileClickListener{
        void onClick(Integer position,FileTreePO nowFileFree);
    }
    class FileTreeViewHolder extends RecyclerView.ViewHolder{

        // 声明需要用到的组件对象
        private TextView  tvTreeName;
        private ImageView ivTreeType;
        private ConstraintLayout clFileTreeItem;
        public FileTreeViewHolder(@NonNull View itemView) {
            super(itemView);
            clFileTreeItem = itemView.findViewById(R.id.cl_file_tree_item);
            tvTreeName     = itemView.findViewById(R.id.tv_tree_name);
            ivTreeType     = itemView.findViewById(R.id.iv_tree_type);
        }

    }

}

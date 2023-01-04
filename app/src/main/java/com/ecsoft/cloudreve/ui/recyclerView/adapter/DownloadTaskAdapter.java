package com.ecsoft.cloudreve.ui.recyclerView.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecsoft.cloudreve.R;
import com.ecsoft.cloudreve.download.entity.DownloadTaskPO;
import com.ecsoft.cloudreve.download.entity.DownloadTasks;
import com.ecsoft.cloudreve.storage.FileNameUtil;
import com.ecsoft.cloudreve.storage.FileStorageUtil;
import com.ecsoft.cloudreve.storage.entity.FileSizePO;
import com.ecsoft.cloudreve.ui.fileType.FileTypeJudgeUtil;

public class DownloadTaskAdapter extends RecyclerView.Adapter<DownloadTaskAdapter.DownloadTaskViewHolder> {

    private Context context;

    public DownloadTaskAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public DownloadTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载视图
        View inflate = LayoutInflater.from(context)
                .inflate(R.layout.item_download_task,null);
        return new DownloadTaskViewHolder(inflate);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DownloadTaskViewHolder holder, int position) {
        // 执行绑定
        DownloadTaskPO task = DownloadTasks.unfinishedTaskList.get(position);
        holder.tvDownloadFileName.setText(FileNameUtil.getReasonableFileName(task.getFileName()));
        holder.pbDownloadProgress.setMax(100);
        holder.pbDownloadProgress.setMin(0);
        int downloadProgress = (int) (task.getDownloadedSize()*100/task.getTotalSize());
        holder.pbDownloadProgress.setProgress(downloadProgress);
        holder.tvDownloadProgress.setText("已完成："+downloadProgress+"%");
        // 计算下载熟读 现在的 - 上一次的  *2
        long downloadSpeed = (task.getDownloadedSize() - task.getLastDownloadedSize()) * 2;
        FileSizePO reasonableFileSize = FileStorageUtil.getReasonableFileSize(downloadSpeed, 0);
        holder.tvDownloadSpeed.setText(reasonableFileSize.getSize()+" "+reasonableFileSize.getUnit()+"/S");
        holder.tvDownloadType.setText(task.downloadMessage);
        holder.ivFileIcon.setImageResource(FileTypeJudgeUtil.getImageResourceByFileName(task.getFileName()));
    }

    @Override
    public int getItemCount() {
        int a  = DownloadTasks.unfinishedTaskList.size();
        return DownloadTasks.unfinishedTaskList.size();
    }

    class DownloadTaskViewHolder extends RecyclerView.ViewHolder{

        TextView tvDownloadFileName;
        TextView tvDownloadType;
        ProgressBar pbDownloadProgress;
        ImageView ivFileIcon;
        TextView tvDownloadProgress;
        TextView tvDownloadSpeed;

        public DownloadTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // 加载视图组件
            tvDownloadFileName = itemView.findViewById(R.id.tv_download_file_name);
            tvDownloadType = itemView.findViewById(R.id.tv_download_type);
            pbDownloadProgress = itemView.findViewById(R.id.pb_download_progress);
            tvDownloadProgress = itemView.findViewById(R.id.tv_download_progress);
            ivFileIcon = itemView.findViewById(R.id.iv_file_icon);
            tvDownloadSpeed = itemView.findViewById(R.id.tv_download_speed);
        }
    }
}

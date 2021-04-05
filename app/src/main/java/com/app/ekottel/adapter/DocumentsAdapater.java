package com.app.ekottel.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.ekottel.R;
import com.app.ekottel.model.DocumentsModel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DocumentsAdapater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<DocumentsModel> docList = new ArrayList<>();
    private Context context;

    public DocumentsAdapater(ArrayList<DocumentsModel> docList, Context context) {
        this.docList = docList;
        this.context = context;
    }

    private class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView textView, docTypeTv, docSizeTv, docTimeTv;
        ImageView imageView;
        LinearLayout mDocumentsParentLayout;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.doc_file_name_tv);
            imageView = itemView.findViewById(R.id.doc_type_img);
            docTypeTv = itemView.findViewById(R.id.doc_type_tv);
            docSizeTv = itemView.findViewById(R.id.doc_file_size_tv);
            docTimeTv = itemView.findViewById(R.id.doc_file_datee_tv);
            mDocumentsParentLayout = itemView.findViewById(R.id.documents_parent_layout);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_documents, viewGroup, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof DocumentViewHolder) {
            final DocumentViewHolder documentViewHolder = (DocumentViewHolder) viewHolder;
            final DocumentsModel documentsModel = docList.get(i);
            documentViewHolder.textView.setText(documentsModel.getFileName());

            String extension = "";
            if (documentsModel.getFileName().contains(".")) {
                extension = documentsModel.getFileName().substring(documentsModel.getFileName().lastIndexOf("."));
            }
            extension = extension.replace(".", "").toUpperCase();
            if (extension != null && extension.length() > 0) {
                documentViewHolder.docTypeTv.setText(extension);
            } else {
                documentViewHolder.docTypeTv.setText("");
            }
            File file = new File(documentsModel.getFilePath());
            long file_size_in_kb = Long.parseLong(String.valueOf(file.length() / 1024));
            long file_size_in_mb = file_size_in_kb / 1024;
            if (file_size_in_kb > 999) {
                documentViewHolder.docSizeTv.setText(file_size_in_mb + " MB");
            } else {
                documentViewHolder.docSizeTv.setText(file_size_in_kb + " KB");
            }
            Date lastModDate = new Date(file.lastModified());
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            documentViewHolder.docTimeTv.setText(format.format(lastModDate));
            //   LOG.info("DocumentsAdapater", "File last modified @ : " + format.format(lastModDate));
            if (extension.equalsIgnoreCase("PDF")) {
                documentViewHolder.imageView.setImageResource(R.drawable.ic_pdf);
            } else if (extension.equalsIgnoreCase("APK")) {
                documentViewHolder.imageView.setImageResource(R.drawable.ic_default);
            } else if (extension.equalsIgnoreCase("DOC")) {
                documentViewHolder.imageView.setImageResource(R.drawable.ic_doc);
            } else if (extension.equalsIgnoreCase("RAR") || extension.equalsIgnoreCase("ZIP")) {
                documentViewHolder.imageView.setImageResource(R.drawable.ic_apk);
            } else if (extension.equalsIgnoreCase("PPTX")) {
                documentViewHolder.imageView.setImageResource(R.drawable.ic_excel);
            } else {
                documentViewHolder.imageView.setImageResource(R.drawable.ic_default);
            }

            documentViewHolder.mDocumentsParentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("DocumentsURL", documentsModel.getFilePath());
                    ((Activity) context).setResult(Activity.RESULT_OK, intent);
                    ((Activity) context).finish();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return docList.size();
    }

    /**
     * This method update the contacts data according user search filed.
     *
     * @param filteredArrayList
     */
    public void filterList(ArrayList<DocumentsModel> filteredArrayList) {
        docList = filteredArrayList;
        notifyDataSetChanged();
    }
}

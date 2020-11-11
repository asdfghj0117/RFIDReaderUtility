package com.favepc.reader.rfidreaderutility.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.favepc.reader.rfidreaderutility.R;
import com.favepc.reader.rfidreaderutility.object.Customize;

import java.util.ArrayList;

public class CustomizeListAdapter extends RecyclerView.Adapter<CustomizeListAdapter.ViewHolder> {

    private ArrayList<Customize> mCustomizes;
    private int mResourceId;
    private Context mContext;

    public CustomizeListAdapter(int resource, ArrayList<Customize> data) {
        this.mResourceId = resource;
        this.mCustomizes = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(mContext)
                .inflate(mResourceId, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Customize _customize = mCustomizes.get(position);
        holder.tvCommand.setText(_customize.Command());
        holder.tvName.setText(_customize.Name());


        holder.cbCheck.setChecked(_customize.Check());
        holder.cbCheck.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCustomizes.get(position).Check())
                    mCustomizes.get(position).Check(false);
                else
                    mCustomizes.get(position).Check(true);
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mIndex = position;
                LayoutInflater _inflater = LayoutInflater.from(mContext);
                View mView = _inflater.inflate(R.layout.fragment_customize_command_dialog, null);
                AlertDialog.Builder _alertDialogB = new AlertDialog.Builder(mContext);
                _alertDialogB.setView(mView);

                final EditText _editText = (EditText) mView.findViewById(R.id.fragment_customize_dialog_command);
                _editText.setText(mCustomizes.get(position).Command());
                final EditText _editTextName = (EditText) mView.findViewById(R.id.fragment_customize_dialog_name);
                _editTextName.setText(mCustomizes.get(position).Name());
                _alertDialogB
                        .setTitle("Edit Command:")
                        .setCancelable(false)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                // ToDo get user input here
                                Customize _c = mCustomizes.get(position);
                                _c.Command(_editText.getText().toString());
                                _c.Name(_editTextName.getText().toString());
                                mCustomizes.set(position, _c);
                                notifyDataSetChanged();
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog _alertDialog = _alertDialogB.create();
                _alertDialog.show();
                //return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCustomizes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvCommand;
        public CheckBox cbCheck;
        public TextView tvName;
        public ViewHolder(View v) {
            super(v);
            tvCommand = (TextView) v.findViewById(R.id.adapter_customize_command);
            cbCheck = (CheckBox) v.findViewById(R.id.adapter_customize_check);
            tvName = v.findViewById(R.id.adapter_customize_name);
        }
    }
}

/*public class CustomizeListAdapter extends BaseSwipeAdapter {

    private Context mContext;
    private int mResourceId;
    private ArrayList<Customize> mCustomizes = new ArrayList<Customize>();

    public CustomizeListAdapter(Context context, int resource, ArrayList<Customize> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResourceId = resource;
        this.mCustomizes = objects;
    }



    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.adapter_customize_item_swipe;
    }

    @Override
    public View generateView(final int position, final ViewGroup parent) {

        final Customize _customize = getItem(position);


        View v = LayoutInflater.from(mContext).inflate(mResourceId, null);
        return v;
    }

    @Override
    public void fillValues(final int position, View convertView) {
        //TextView t = (TextView)convertView.findViewById(R.id.position);
        //t.setText((position + 1) + ".");
        final Customize _customize = getItem(position);

        final SwipeLayout swipeLayout = (SwipeLayout)convertView.findViewById(R.id.adapter_customize_item_swipe);

        Button btDelete = (Button) convertView.findViewById(R.id.adapter_customize_delete);
        btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext, "click delete", Toast.LENGTH_SHORT).show();
                mCustomizes.remove(position);
                notifyDatasetChanged();
                swipeLayout.close();
            }
        });

        CheckBox cbCheck = (CheckBox) convertView.findViewById(R.id.adapter_customize_check);
        cbCheck.setChecked(_customize.Check());
        cbCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCustomizes.get(position).Check(isChecked);
                notifyDatasetChanged();
            }
        });

        TextView tvCommand = (TextView) convertView.findViewById(R.id.adapter_customize_command);
        tvCommand.setText(_customize.Command());
    }

    @Override
    public int getCount() {
        return mCustomizes.size();
    }

    @Override
    public Customize getItem(int position) {
        return mCustomizes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}*/

package com.example.ecopulse;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;

public class TimeslotAdapter extends ArrayAdapter<String> {
    private List<String> items;
    private String selectedDay;

    public TimeslotAdapter( Context context, List<String> items, String selectedDay) {
        super(context, 0, items);
        this.items = items;
        this.selectedDay = selectedDay;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    private View createCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeslot_list_item, parent, false);
        }

        TextView slot = convertView.findViewById(R.id.timeslot_item);
        AppCompatImageButton delete = convertView.findViewById(R.id.delete_btn);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = "";
        if (user != null) {
            userEmail = user.getEmail();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").whereEqualTo("email", userEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    QueryDocumentSnapshot document1 = task.getResult().iterator().next();

                    String userID = document1.getId();

                    db.collection("recycling_center_information").whereEqualTo("userID", userID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QueryDocumentSnapshot document2 = task.getResult().iterator().next();
                                String documentID = document2.getId();
                                String item = getItem(position);

                                if (item != null) {
                                    slot.setText(item);
                                    delete.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            items.remove(item);
                                            db.collection("recycling_center_information").document(documentID).update("timeslot", FieldValue.arrayRemove(selectedDay + ", " + item));
                                            notifyDataSetChanged();

                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });





        return convertView;
    }


}

package com.lazorcorn.simplefirestoreproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";   //usage for log messages

    private static final String KEY_QUOTE = "quote";    //access-key to database
    private static final String KEY_AUTHOR = "author";

    private EditText editTextQuote;
    private EditText editTextAuthor;
    private EditText editTextCategory;
    private int filter = 0;

    private TextView textViewData;
    private ScrollView scrollView;
    private FloatingActionButton fab;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef =  db.collection("Notebook");
    private DocumentReference entriesRef = db.collection("Notebook").document("The First Quotes"); //=database.reference to the collection.create reference to a document

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextQuote = findViewById(R.id.edit_entry_quote);
        editTextAuthor = findViewById(R.id.edit_entry_author);
        editTextCategory = findViewById(R.id.edit_entry_category);
        textViewData = findViewById(R.id.text_view_data);
        fab = findViewById(R.id.fab_page_up);
        scrollView = findViewById(R.id.scroll_view);
        fab.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        notebookRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if(e != null){return;}

                String dataBuff =  "";
                String categoryName = "";

                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                    Entry entry = documentSnapshot.toObject(Entry.class);
                    entry.setId(documentSnapshot.getId());

                    String id = entry.getId();
                    String quote = entry.getQuote();
                    String author = entry.getAuthor();
                    int category = entry.getCategory();
                    if(category == 1)
                    {categoryName = "Bright Side";}
                    else if(category == 2)
                    {categoryName = "Dark Side";}
                    else
                    {categoryName = "no Side";}

                    dataBuff += "''" + quote + "''\n\nAuthor: " + author +   "\nCategory: " + categoryName + "\n ▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰\n\n"; //"\nUploaded by: " + id +
                }
                textViewData.setText(dataBuff);
            }
        });


        //to auto load only one entry
        /*entriesRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) //if an exception occur
                {
                    Toast.makeText(MainActivity.this, "Error while loading", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,e.toString());
                    return;
                }
                if (documentSnapshot.exists())  //if an entry exists in db
                {
                    Entry entry = documentSnapshot.toObject(Entry.class); //auto recreate entry object and fill fields as long as all names fit

                    String quote = entry.getQuote();
                    String author = entry.getAuthor();

                    textViewData.setText("Quote: " + quote + "\n" + "Author: " + author);
                }else //if note is deleted
                {
                    textViewData.setText("");
                }
            }
        });*/
    }

    public void addQuote(View view){
        String quote = editTextQuote.getText().toString();
        String author = editTextAuthor.getText().toString();
        if(editTextCategory.length() == 0){
            editTextCategory.setText("0"); //un-categorized
        }

        int category = Integer.parseInt(editTextCategory.getText().toString());

        //put data in a container to save values related with keys to the database
        Entry entry = new Entry(quote, author, category);

        notebookRef.add(entry).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(MainActivity.this,"Quote Added!",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Failed to add note!",Toast.LENGTH_SHORT).show();
                Log.d(TAG,e.toString());
            }
        });

        editTextQuote.setText("");
        editTextAuthor.setText("");
        editTextCategory.setText("");
    }

    public void saveQuote(View view){
        String quote = editTextQuote.getText().toString();
        String author = editTextAuthor.getText().toString();
        if(editTextCategory.length() == 0){
            editTextCategory.setText("0"); //un-categorized
        }

        int category = Integer.parseInt(editTextCategory.getText().toString());

        //put data in a container to save values related with keys to the database
        Entry entry = new Entry(quote, author, category);

        entriesRef.set(entry). //pass new entry to firestore & set it as value for the first document
           //.save entry there
           //the same like: db.document("Notebook/MyFirstQuote").
            addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) { //callback method
                       Toast.makeText(MainActivity.this,"Saved!",Toast.LENGTH_SHORT).show();
                   }
            })
            .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this,"Failed to save!",Toast.LENGTH_SHORT).show();
                       Log.d(TAG,e.toString());
                   }
            });
        editTextQuote.setText("");
        editTextAuthor.setText("");
    }

    public void updateQuote(View view){
        String quote = editTextQuote.getText().toString();

        //Map<String, Object> entry = new HashMap<>();
        //entry.put(KEY_QUOTE, quote); //only override the quote

        //entriesRef.set(entry, SetOptions.merge()); //merge needed to keep all other values
        entriesRef.update(KEY_QUOTE, quote);
        editTextQuote.setText("");
    }

    public void deleteAuthor(View view){
        Map<String, Object> entry = new HashMap<>();
        entriesRef.update(KEY_AUTHOR, FieldValue.delete()); //Field Value deletes the Key-entry for "Author" in db
        editTextAuthor.setText("");
    }

    public void deleteEntry(View view){
        entriesRef.delete();
    }

    public void loadQuotation(View view){
            entriesRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        //String quote = documentSnapshot.getString(KEY_QUOTE);
                        //String author = documentSnapshot.getString(KEY_AUTHOR); //obsolete through below line
                        Entry entry = documentSnapshot.toObject(Entry.class); //auto recreate entry object and fill fields as long as all names fit
                        String quote = entry.getQuote();
                        String author = entry.getAuthor();

                        textViewData.setText("Quote: " + quote + "\n" + "Author: " + author);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Document doesn't exists!", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                }
            });
    }

    public void filterAuthor(View view){

    }

    public void loadQuotations(View view){
        Task task1 = notebookRef.whereLessThanOrEqualTo("category", 2)
                .orderBy("category", Query.Direction.DESCENDING)
                .get();
        /*Task task2 = notebookRef.whereEqualTo("category", 1)
                .orderBy("category")
                .orderBy("author")
                .get();
        Task task3 = notebookRef.whereEqualTo("category", 2)
                .orderBy("category")
                .orderBy("author")
                .get();
        Task task4 = notebookRef.whereEqualTo("category", 0)
                .orderBy("category")
                .orderBy("author")
                .get();
        Task task5 = notebookRef.whereGreaterThanOrEqualTo("category", 3)
                .orderBy("category")
                .orderBy("author")
                .get();*/


        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(task1); //QuerySnapshot is the return Type
        allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                String dataBuff = "";
                String categoryName = "";

                for(QuerySnapshot queryDocumentSnapshots : querySnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Entry entry = documentSnapshot.toObject(Entry.class);
                        entry.setId(documentSnapshot.getId());

                        String id = entry.getId();
                        String quote = entry.getQuote();
                        String author = entry.getAuthor();
                        int category = entry.getCategory();
                        if (category == 1) {
                            categoryName = "Bright Side";
                        } else if (category == 2) {
                            categoryName = "Dark Side";
                        } else {
                            categoryName = "no Side";
                        }

                        dataBuff += "''" + quote + "''\n\nAuthor: " + author + "\nCategory: " + categoryName + "\n ▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰\n\n"; //"\nUploaded by: " + id +
                    }
                }
                textViewData.setText(dataBuff);
            }
        });
        Toast.makeText(MainActivity.this, "Sorted by category", Toast.LENGTH_SHORT).show();
    }


}
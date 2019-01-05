package com.lazorcorn.simplefirestoreproject;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
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

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";   //usage for log messages

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle menuToggle;

    private static final String KEY_QUOTE = "quote";    //access-key to database
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_CATEGORY = "category";
    private int choice = 0;

    private EditText editTextQuote;
    private EditText editTextAuthor;
    private Spinner chooseCagegory;

    private TextView textViewData;
    private ScrollView scrollView;
    private FloatingActionButton fab;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Notebook");
    private DocumentReference entriesRef = db.collection("Notebook").document("The First Quotes"); //=database.reference to the collection.create reference to a document

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        menuToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(menuToggle);
        menuToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextQuote = findViewById(R.id.edit_entry_quote);
        editTextAuthor = findViewById(R.id.edit_entry_author);
        chooseCagegory = findViewById(R.id.spinner_category);
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.categories,
                android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        chooseCagegory.setAdapter(staticAdapter);

        textViewData = findViewById(R.id.text_view_data);
        fab = findViewById(R.id.fab_page_up);
        scrollView = findViewById(R.id.scroll_view);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
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
                if (e != null) {
                    return;
                }

                String dataBuff = "";

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Entry entry = documentSnapshot.toObject(Entry.class);
                    entry.setId(documentSnapshot.getId());

                    String id = entry.getId();
                    String quote = entry.getQuote();
                    String author = entry.getAuthor();
                    String category = entry.getCategory();
                    dataBuff += "\n" + "''" + quote + "''\n\nAuthor: " + author + "\nCategory: " + category + "\n\n▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰\n\n"; //"\nUploaded by: " + id +
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

    public void addQuote(View view) {
        SimpleDateFormat getTime = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = getTime.format(new Date());
        String quote = editTextQuote.getText().toString();
        quote = clearString(quote);
        if (quote.isEmpty()) {
            Toast.makeText(MainActivity.this, "No Quote to add!", Toast.LENGTH_SHORT).show();
            return;
        }

        String author = editTextAuthor.getText().toString();
        if (author.isEmpty()) {
            author = "unknown";
        }
        author = clearString(author);
        String category = chooseCagegory.getSelectedItem().toString();

        //put data in a container to save values related with keys to the database
        Entry entry = new Entry(timestamp, quote, author, category);

        notebookRef.add(entry).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(MainActivity.this, "Quote Added!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to add note!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.toString());
            }
        });

        editTextQuote.setText("");
        editTextAuthor.setText("");
    }

    private String clearString(String enteredString) {
        String cleanString = enteredString.replaceFirst("\"", "");
        if(cleanString.endsWith("\""))
        {
            cleanString = cleanString.substring(0,cleanString.length() - 1);
        }

        return cleanString;
    }

    /*
    public void saveQuote(View view) {
        SimpleDateFormat getTime = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = getTime.format(new Date());
        String quote = editTextQuote.getText().toString();
        String author = editTextAuthor.getText().toString();
        String category = chooseCagegory.getSelectedItem().toString();

        //put data in a container to save values related with keys to the database
        Entry entry = new Entry(timestamp, quote, author, category);

        entriesRef.set(entry). //pass new entry to firestore & set it as value for the first document
                //.save entry there
                //the same like: db.document("Notebook/MyFirstQuote").
                        addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) { //callback method
                        Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to save!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, e.toString());
                    }
                });
        editTextQuote.setText("");
        editTextAuthor.setText("");
    }

    public void updateQuote(View view) {
        String quote = editTextQuote.getText().toString();

        //Map<String, Object> entry = new HashMap<>();
        //entry.put(KEY_QUOTE, quote); //only override the quote

        //entriesRef.set(entry, SetOptions.merge()); //merge needed to keep all other values
        entriesRef.update(KEY_QUOTE, quote);
        editTextQuote.setText("");
    }

    public void deleteAuthor(View view) {
        Map<String, Object> entry = new HashMap<>();
        entriesRef.update(KEY_AUTHOR, FieldValue.delete()); //Field Value deletes the Key-entry for "Author" in db
        editTextAuthor.setText("");
    }

    public void deleteEntry(View view) {
        entriesRef.delete();
    }

    public void loadQuotation(View view) {
        entriesRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Entry entry = documentSnapshot.toObject(Entry.class); //auto recreate entry object and fill fields as long as all names fit
                    String quote = entry.getQuote();
                    String author = entry.getAuthor();
                    String timestamp = entry.getTimestamp();
                    String category = entry.getCategory();

                    textViewData.setText("Quote: " + quote + "\n" + "Author: " + author);
                } else {
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

    */

    public void loadQuotations(View view) {
        final String[] sorting = {"timestamp descending", "timestamp ascending", "category descending", "category ascending", "author descending", "author ascending"};

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by");
        builder.setItems(sorting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    choice = 0;
                    applyFilter();
                }
                else if (which == 1) {
                    choice = 1;
                    applyFilter();
                }
                else if (which == 2) {
                    choice = 2;
                    applyFilter();
                }
                else if (which == 3) {
                    choice = 3;
                    applyFilter();
                }
                else if (which == 4) {
                    choice = 4;
                    applyFilter();
                }
                else if (which == 5) {
                    choice = 5;
                    applyFilter();
                }
            }
        });
        builder.show();
    }

    private void applyFilter() {
        Task task = notebookRef
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
        String selectedFilter = "";

        if (choice == 0) {
            task = notebookRef
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get();
            selectedFilter = "timestamp";
        }
        else if (choice == 1) {
            task = notebookRef
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get();
            selectedFilter = "timestamp";
        }
        else if (choice == 2) {
            task = notebookRef.whereLessThanOrEqualTo("category", 2)
                    .orderBy("category", Query.Direction.DESCENDING)
                    .get();
            selectedFilter = "category";
        }
        else if (choice == 3) {
            task = notebookRef.whereLessThanOrEqualTo("category", 2)
                    .orderBy("category", Query.Direction.ASCENDING)
                    .get();
            selectedFilter = "category";
        }
        else if (choice == 4) {
            task = notebookRef
                    .orderBy("author", Query.Direction.DESCENDING)
                    .get();
            selectedFilter = "author";
        }
        else if (choice == 5) {
            task = notebookRef
                    .orderBy("author", Query.Direction.ASCENDING)
                    .get();
            selectedFilter = "author";
        }

        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(task); //QuerySnapshot is the return Type
        allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots) {
                String dataBuff = "";

                for (QuerySnapshot queryDocumentSnapshots : querySnapshots) {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Entry entry = documentSnapshot.toObject(Entry.class);
                        entry.setId(documentSnapshot.getId());

                        String id = entry.getId();
                        String quote = entry.getQuote();
                        String author = entry.getAuthor();
                        String timestamp = entry.getTimestamp();
                        String category = entry.getCategory();
                        dataBuff += "\n" + "''" + quote + "''\n\nAuthor: " + author + "\nCategory: " + category + "\n\n▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰▰\n\n"; //"\nUploaded by: " + id +
                    }
                }
                textViewData.setText(dataBuff);
            }
        });
        Toast.makeText(MainActivity.this, "Sorted by " + selectedFilter, Toast.LENGTH_SHORT).show();
    }

    public boolean onCreateOptionsmenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //menu item click handling:
        if (menuToggle.onOptionsItemSelected(item)) {
            Toast.makeText(this, "Menu open", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
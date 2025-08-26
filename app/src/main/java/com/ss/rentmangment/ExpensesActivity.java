package com.ss.rentmangment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ExpensesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private List<Expense> masterExpenseList = new ArrayList<>();
    private List<Expense> displayedExpenseList = new ArrayList<>();
    private FloatingActionButton fabAddExpense;
    private SearchView searchView;
    private DatabaseReference expensesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        setSupportActionBar(findViewById(R.id.toolbar_expenses));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewExpenses);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        searchView = findViewById(R.id.searchViewExpenses);

        adapter = new ExpenseAdapter(this, displayedExpenseList);
        recyclerView.setAdapter(adapter);

        String adminId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("mobile", "");
        expensesRef = FirebaseDatabase.getInstance().getReference("users").child(adminId).child("expenses");

        fabAddExpense.setOnClickListener(v ->
                startActivity(new Intent(this, AddExpenseActivity.class))
        );

        setupSearchView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterExpenses(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExpenses(newText);
                return true;
            }
        });
    }

    private void loadExpenses() {
        expensesRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                masterExpenseList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Expense expense = ds.getValue(Expense.class);
                    if (expense != null) {
                        masterExpenseList.add(expense);
                    }
                }
                // Show the most recent expenses first
                Collections.reverse(masterExpenseList);
                filterExpenses(searchView.getQuery().toString()); // Apply current search query
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ExpensesActivity.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterExpenses(String query) {
        displayedExpenseList.clear();
        String lowerCaseQuery = query.toLowerCase().trim();

        if (lowerCaseQuery.isEmpty()) {
            displayedExpenseList.addAll(masterExpenseList);
        } else {
            for (Expense expense : masterExpenseList) {
                boolean descMatches = expense.description != null && expense.description.toLowerCase().contains(lowerCaseQuery);
                boolean catMatches = expense.category != null && expense.category.toLowerCase().contains(lowerCaseQuery);
                boolean roomMatches = expense.roomName != null && expense.roomName.toLowerCase().contains(lowerCaseQuery);

                if (descMatches || catMatches || roomMatches) {
                    displayedExpenseList.add(expense);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

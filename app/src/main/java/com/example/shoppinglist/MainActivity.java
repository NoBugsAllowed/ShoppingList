package com.example.shoppinglist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String dataFileName = "shoppingListItems";
    private DrawerLayout mDrawerLayout;
    private RecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private List<ListItem> mItems;

    public CustomAdapter getAdapter() {
        return mAdapter;
    }

    public boolean isSortItems() {
        return sortItems;
    }

    private boolean sortItems;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("sortAlphabetically"))
            {
                sortItems = sharedPreferences.getBoolean(key,false);
                if(sortItems)
                    mAdapter.sortItems();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mRecyclerView = findViewById(R.id.recycler_view);

        mItems = new ArrayList<>();
        fillShoppingList(mItems);
        mAdapter = new CustomAdapter(this,mItems);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)).attachToRecyclerView(mRecyclerView);
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

        sortItems = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("sortAlphabetically",false);

        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.add_item_title);

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);

                builder.setView(input);
                builder.setPositiveButton(R.string.add_item_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mItems.add(new ListItem(input.getText().toString(),false));
                        if(sortItems)
                            mAdapter.sortItems();
                        mAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton(R.string.add_item_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CustomAdapter.CustomViewHolder) {
            String name = mItems.get(viewHolder.getAdapterPosition()).getText();

            final ListItem deletedItem = mItems.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            mAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar
                    .make(mDrawerLayout, name + " " + getResources().getString(R.string.snackbar_removed_text), Snackbar.LENGTH_LONG);
            snackbar.setAction(R.string.snackbar_undo, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.colorSecondary));
            snackbar.show();
        }
    }

    private void fillShoppingList(List<ListItem> items) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(getFilesDir() + "/" + dataFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            String line = br.readLine();
            while (line != null) {
                items.add(new ListItem(line, false));
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onPause() {
        super.onPause();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                File file = new File(getFilesDir() + "/" + dataFileName);
                FileWriter fw;
                try {
                    fw = new FileWriter(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                for(ListItem li : mItems) {
                    try {
                        fw.write(li.getText());
                        fw.write(System.getProperty("line.separator"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}

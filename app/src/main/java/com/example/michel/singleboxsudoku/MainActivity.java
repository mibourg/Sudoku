package com.example.michel.singleboxsudoku;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    int[][] sudokuGrid = new int[9][9];

    int[][] sudokuGridToBeTested = new int[9][9];

    EditText[][] editTextGrid = new EditText[9][9];

    TableLayout tableLayout;

    String currentPuzzleName;

    boolean inEditingMode = false;
    boolean inPlayingMode = true;
    boolean isTestingForSolvable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tableLayout = (TableLayout) findViewById(R.id.table_layout_for_sudoku_board);

        for (int i = 0; i < 9; i++) {
            if (i == 0 || i % 3 == 0) {
                View bigHorizontalDividerView = new View(getApplicationContext());
                bigHorizontalDividerView.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertDpToPx(2)));
                bigHorizontalDividerView.setPadding(0,0,0,0);
                bigHorizontalDividerView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.divider));
                tableLayout.addView(bigHorizontalDividerView);
            } else {
                View smallHorizontalDividerView = new View(getApplicationContext());
                smallHorizontalDividerView.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertDpToPx(1)));
                smallHorizontalDividerView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.divider));
                tableLayout.addView(smallHorizontalDividerView);
            }

            TableRow tableRow = new TableRow(getApplicationContext());
            tableRow.setGravity(Gravity.CENTER);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));

            for (int j = 0; j < 9; j++) {
                if (j == 0 || j % 3 == 0) {
                    View bigVerticalDividerView = new View(getApplicationContext());
                    bigVerticalDividerView.setLayoutParams(new TableRow.LayoutParams(convertDpToPx(2), ViewGroup.LayoutParams.MATCH_PARENT));
                    bigVerticalDividerView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.divider));
                    tableRow.addView(bigVerticalDividerView);
                } else {
                    View smallVerticalDividerView = new View(getApplicationContext());
                    smallVerticalDividerView.setLayoutParams(new TableRow.LayoutParams(convertDpToPx(1), ViewGroup.LayoutParams.MATCH_PARENT));
                    smallVerticalDividerView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.divider));
                    tableRow.addView(smallVerticalDividerView);
                }

                final EditText editText = new EditText(getApplicationContext());
                editText.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setGravity(Gravity.CENTER);
                editText.setBackgroundColor(Color.parseColor("#00000000"));
                editText.setTextSize(12.5f);
                editText.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        EditText callingEditText = (EditText) v;

                        Log.d("Keycode", String.valueOf(keyCode));

                        if (event.getAction() == KeyEvent.ACTION_UP && ((keyCode >= 8 && keyCode <= 16) || keyCode == 67 || event.getKeyCode() == KeyEvent.KEYCODE_DEL)) {
                            for (int i = 0; i < editTextGrid.length; i++) {
                                for (int j = 0; j < editTextGrid[i].length; j++) {
                                    if (editTextGrid[i][j].equals(callingEditText)) {
                                        if (!callingEditText.getText().toString().isEmpty()) {
                                            sudokuGrid[i][j] = Integer.valueOf(callingEditText.getText().toString());
                                        } else {
                                            sudokuGrid[i][j] = 0;
                                        }

                                        if (isFull(sudokuGrid) && inPlayingMode) {
                                            checkWin();
                                        }
                                        return true;
                                    }
                                }
                            }
                        }

                        return false;
                    }
                });
                editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(1)});
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = s.toString();
                        if (text.startsWith("0")) {
                            editText.setText("");
                        }
                    }
                });

                editTextGrid[i][j] = editText;
                tableRow.addView(editText);

                if (j == 8) {
                    View bigVerticalDividerView = new View(getApplicationContext());
                    bigVerticalDividerView.setLayoutParams(new TableRow.LayoutParams(convertDpToPx(2), ViewGroup.LayoutParams.MATCH_PARENT));
                    bigVerticalDividerView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.divider));
                    tableRow.addView(bigVerticalDividerView);
                }
            }

            tableLayout.addView(tableRow);

            if (i == 8) {
                View bigHorizontalDividerView = new View(getApplicationContext());
                bigHorizontalDividerView.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, convertDpToPx(2)));
                bigHorizontalDividerView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.divider));
                tableLayout.addView(bigHorizontalDividerView);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        if (inEditingMode) {
            menuInflater.inflate(R.menu.editing_menu, menu);
            return true;
        } else if (inPlayingMode) {
            menuInflater.inflate(R.menu.playing_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater menuInflater = getMenuInflater();
        if (inEditingMode) {
            menuInflater.inflate(R.menu.editing_menu, menu);
            return true;
        } else if (inPlayingMode) {
            menuInflater.inflate(R.menu.playing_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.load_puzzle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Puzzle Name");
            builder.setMessage("What is the name of the puzzle you'd like to solve?");

            clearBoard();

            final EditText puzzleNameEditText = new EditText(this);
            puzzleNameEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            builder.setView(puzzleNameEditText);
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String fileName = puzzleNameEditText.getText().toString();
                    currentPuzzleName = fileName;
                    loadPuzzle(fileName);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.create().show();

            return true;
        } else if (item.getItemId() == R.id.reset_puzzle) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    editTextGrid[i][j].setText("");
                }
            }
            loadPuzzle(currentPuzzleName);
            return true;
        } else if (item.getItemId() == R.id.enter_editing_mode) {
            inEditingMode = true;
            inPlayingMode = false;

            currentPuzzleName = "";

            Toast.makeText(this, R.string.now_in_editing_mode, Toast.LENGTH_SHORT).show();

            invalidateOptionsMenu();

            clearBoard();

            if (isTestingForSolvable) {
                sudokuGrid = sudokuGridToBeTested;
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (sudokuGrid[i][j] >= 1 && sudokuGrid[i][j] <= 9) {
                            editTextGrid[i][j].setText(String.valueOf(sudokuGrid[i][j]));
                            editTextGrid[i][j].setFocusableInTouchMode(true);
                            editTextGrid[i][j].setFocusable(true);
                        }
                    }
                }
            }

        } else if (item.getItemId() == R.id.save_puzzle) {
            Toast.makeText(this, R.string.must_solve_it, Toast.LENGTH_LONG).show();

            sudokuGridToBeTested = sudokuGrid;
            savePuzzle("temporary", sudokuGrid);

            inEditingMode = false;
            inPlayingMode = true;
            isTestingForSolvable = true;
            invalidateOptionsMenu();

            loadPuzzle("temporary");

            return true;
        } else if (item.getItemId() == R.id.enter_playing_mode) {
            inEditingMode = false;
            inPlayingMode = true;

            Toast.makeText(this, R.string.now_in_playing_mode, Toast.LENGTH_SHORT).show();

            invalidateOptionsMenu();
            return true;
        } else if (item.getItemId() == R.id.clear) {
            clearBoard();
        }

        return false;
    }

    private int convertDpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    private void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                editTextGrid[i][j].setText("");
                editTextGrid[i][j].setFocusable(true);
                editTextGrid[i][j].setFocusableInTouchMode(true);
                editTextGrid[i][j].setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                sudokuGrid[i][j] = 0;
            }
        }
    }

    private boolean isFull(int[][] sudokuGrid) {
        boolean isFull = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sudokuGrid[i][j] == 0 && editTextGrid[i][j].isFocusable()) {
                    isFull = false;
                }
            }
        }
        return isFull;
    }

    private boolean checkWin() {

        for (int i = 0; i < 9; i++) {
            int[] numbersInRow = Arrays.copyOf(sudokuGrid[i], 9);

            boolean containsDuplicate = false;
            Arrays.sort(numbersInRow);

            for (int j = 0; j < 9; j++) {
                if (j != 0 && numbersInRow[j - 1] == numbersInRow[j]) {
                    containsDuplicate = true;
                } else if (numbersInRow[j] == 0) {
                    Log.d("", "");
                    Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            Log.d("Duplicates in row?", containsDuplicate ? "Yes" : "No");

            if (containsDuplicate) {
                Log.d("", "");
                Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        for (int i = 0; i < 9; i++) {
            int[] numbersInColumn = new int[9];
            for (int j = 0; j < 9; j++) {
                numbersInColumn[j] = sudokuGrid[j][i];
            }

            boolean containsDuplicate = false;
            Arrays.sort(numbersInColumn);

            for (int j = 0; j < 9; j++) {
                if (j != 0 && numbersInColumn[j - 1] == numbersInColumn[j]) {
                    containsDuplicate = true;
                } else if (numbersInColumn[j] == 0) {
                    Log.d("", "");
                    Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

            Log.d("Duplicates in column?", containsDuplicate ? "Yes" : "No");

            if (containsDuplicate) {
                Log.d("", "");
                Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        ArrayList<Integer> square1 = new ArrayList<>();
        ArrayList<Integer> square2 = new ArrayList<>();
        ArrayList<Integer> square3 = new ArrayList<>();


        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (j < 3) {
                    square1.add(sudokuGrid[i][j]);
                } else if (j < 6) {
                    square2.add(sudokuGrid[i][j]);
                } else if (j < 9) {
                    square3.add(sudokuGrid[i][j]);
                }
            }

            if (i == 2 || i == 5 || i == 8) {
                Collections.sort(square1);
                Collections.sort(square2);
                Collections.sort(square3);

                boolean square1ContainsDuplicate = false;

                for (int j = 0; j < 9; j++) {
                    if (j != 0 && square1.get(j - 1).equals(square1.get(j))) {
                        square1ContainsDuplicate = true;
                    } else if (square1.get(j) == 0) {
                        Log.d("", "");
                        Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                boolean square2ContainsDuplicate = false;

                for (int j = 0; j < 9; j++) {
                    if (j != 0 && square2.get(j - 1).equals(square2.get(j))) {
                        square2ContainsDuplicate = true;
                    } else if (square2.get(j) == 0) {
                        Log.d("", "");
                        Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                boolean square3ContainsDuplicate = false;

                for (int j = 0; j < 9; j++) {
                    if (j != 0 && square3.get(j - 1).equals(square3.get(j))) {
                        square3ContainsDuplicate = true;
                    } else if (square3.get(j) == 0) {
                        Log.d("", "");
                        Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                Log.d("Duplicates in square 1?", square1ContainsDuplicate ? "Yes" : "No");
                Log.d("Duplicates in square 2?", square2ContainsDuplicate ? "Yes" : "No");
                Log.d("Duplicates in square 3?", square3ContainsDuplicate ? "Yes" : "No");

                if (square1ContainsDuplicate || square2ContainsDuplicate || square3ContainsDuplicate) {
                    Log.d("", "");
                    Toast.makeText(this, "Incorrect solution.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                square1.clear();
                square2.clear();
                square3.clear();
            }

        }

        Toast.makeText(this, "Correct solution!", Toast.LENGTH_LONG).show();

        if (isTestingForSolvable) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Puzzle was successfully solved!");
            builder.setMessage("What would you like to name this puzzle?");



            final EditText puzzleNameEditText = new EditText(this);
            puzzleNameEditText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            builder.setView(puzzleNameEditText);
            builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String fileName = puzzleNameEditText.getText().toString();
                    savePuzzle(fileName, sudokuGridToBeTested);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.create().show();

            isTestingForSolvable = false;
        }

        return true;
    }

    private void loadPuzzle(String puzzleFileName) {
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;

        if (currentPuzzleName != null) {
            try {
                fileInputStream = openFileInput(puzzleFileName);
                objectInputStream = new ObjectInputStream(fileInputStream);
                sudokuGrid = (int[][]) objectInputStream.readObject();
                currentPuzzleName = puzzleFileName;
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        if (sudokuGrid[i][j] >= 1 && sudokuGrid[i][j] <= 9) {
                            editTextGrid[i][j].setText(String.valueOf(sudokuGrid[i][j]));
                            editTextGrid[i][j].setFocusable(false);
                            editTextGrid[i][j].setFocusableInTouchMode(false);
                            editTextGrid[i][j].setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
                            editTextGrid[i][j].clearFocus();
                        } else {
                            editTextGrid[i][j].setFocusable(true);
                            editTextGrid[i][j].setFocusableInTouchMode(true);
                            editTextGrid[i][j].setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                            editTextGrid[i][j].clearFocus();
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                Log.e("FileNotFoundException", "The specified file was not found.");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("IOException", "There was a problem reading the file containing the puzzle.");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                Log.e("ClassNotFoundException", "There is no puzzle in the specified file.");
                e.printStackTrace();
            }
        }
    }

    private void savePuzzle(String puzzleFileName, int[][] sudokuGridToSave) {
        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream;

        try {
            fileOutputStream = openFileOutput(puzzleFileName, MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            Log.d("Writing", "File is about to be written to.");
            objectOutputStream.writeObject(sudokuGridToSave);
            Log.d("Writing", "Puzzle was written in the file.");
        } catch (FileNotFoundException e) {
            File file = new File(getApplicationContext().getFilesDir(), puzzleFileName);
        } catch (IOException e) {
            Log.e("IOException", "There was a problem writing to the file containing the puzzle.");
        }
    }
}

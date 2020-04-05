package com.vizalgo.gui;

import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.vizalgo.domain.problems.CheckableOption;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class DataModelTranslator {
    private SharedPreferences sharedPref;
    private String problemName;

    DataModelTranslator(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    public void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public void updateDataModelFromTextFields(LinearLayout layout, Object dataModel) {
        // TODO: Break out into method for each individual item and move to separate class.
        int index = 1;
        for (Field f : dataModel.getClass().getFields()) {
            if (f.getType() == CheckableOption.class) {
                // This is set in onOptionItemSelected.
                continue;
            }
            // Find the corresponding EditText.  Note that this assumes the field order is
            // preserved.
            // TODO: Create masked sequential id based on ordering of field in class.
            // TODO: Move to separate class
            EditText editText = (EditText) layout.getChildAt(index);
            if (editText == null) {
                // This may get called when the TextViews are being created.
                break;
            }
            index += 2;
            String textValue = editText.getText().toString();

            try {
                if (f.getType() == int.class || f.getType() == Integer.class) {
                    try {
                        f.set(dataModel, Integer.valueOf(textValue));
                    } catch (NumberFormatException n) {
                        // Oopsie-dasie, that is not a number.  Revert to saved.
                        editText.setText(new Integer(f.getInt(dataModel)).toString());
                    }
                } else if (f.getType() == String.class) {
                    f.set(dataModel, textValue);
                } else {
                    System.out.println(
                            String.format(
                                    "WARNING: Don't know what to do with field of type %s for %s",
                                    f.getType(), f.getName()));
                }
            } catch (IllegalAccessException e) {
                System.out.println(
                        String.format("IllegalAccessException reading view for: %s\n%s"
                                + f.getName(), e));
            }
        }
    }

    public void writeDataModel(Object dataModel) {
        // TODO: Add API To write individual option for efficiency
        SharedPreferences.Editor e = sharedPref.edit();
        for (Field f : dataModel.getClass().getFields()) {
            try {
                if (f.getType() == CheckableOption.class) {
                    CheckableOption option = (CheckableOption) f.get(dataModel);
                    e.putInt(getPref(f), option.Value);
                } else if (f.getType() == int.class || f.getType() == Integer.class) {
                    e.putInt(getPref(f), f.getInt(dataModel));
                } else if (f.getType() == String.class) {
                    e.putString(getPref(f), f.get(dataModel).toString());
                } else {
                    System.out.println(
                            String.format(
                                    "WARNING: Don't know what to do with field of type %s for %s",
                                    f.getType(), f.getName()));
                }
            } catch (IllegalAccessException ex) {
                System.out.println(
                        String.format("IllegalAccessException reading view for: %s\n%s"
                                + f.getName(), ex));
            }
        }
        e.apply();
    }

    public void readDataModel(Object dataModel, Consumer<Field> addTextViewConsumer) {
        for (Field f : dataModel.getClass().getFields()) {
            try {
                if (f.getType() == CheckableOption.class) {
                    CheckableOption option = (CheckableOption) f.get(dataModel);
                    option.Value = sharedPref.getInt(getPref(f), option.Value);
                } else if (f.getType() == int.class || f.getType() == Integer.class) {
                    f.setInt(dataModel, sharedPref.getInt(getPref(f), f.getInt(dataModel)));
                    addTextViewConsumer.accept(f);
                } else if (f.getType() == String.class) {
                    f.set(dataModel, sharedPref.getString(getPref(f), f.get(dataModel).toString()));
                    addTextViewConsumer.accept(f);
                } else {
                    System.out.println(
                            String.format(
                                    "WARNING: Don't know what to do with field of type %s for %s",
                                    f.getType(), f.getName()));
                }
            } catch (IllegalAccessException e) {
                System.out.println(
                        String.format("IllegalAccessException reading view for: %s\n%s"
                                + f.getName(), e));
            }
        }
    }

    public void setOptionValue(Object dataModel, String option) {
        // Search entire data model to find matching menu item.  This is inefficient and
        // fragile.  It will break with multiple menu items of the same id.
        // TODO: Fix this.
        for (Field f : dataModel.getClass().getFields()) {
            try {
                if (f.getType().equals(CheckableOption.class)) {
                    CheckableOption dropDown = (CheckableOption) f.get(dataModel);
                    int i = 0;
                    for (String mi : dropDown.getOptions()) {
                        if (mi.equals(option)) {
                            if (i != dropDown.Value) {
                                dropDown.Value = i;
                                writeDataModel(dataModel);
                            }
                            break;
                        }
                        i++;
                    }
                }
            } catch (IllegalAccessException e) {
                System.out.println(
                        String.format("IllegalAccessException setting menu for: %s\n%s"
                                + f.getName(), e));
            }
        }
    }

    private String getPref(Field field) {
        return problemName + "_" + field.getName();
    }
}

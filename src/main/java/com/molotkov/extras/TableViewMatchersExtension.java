package com.molotkov.extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.testfx.matcher.base.GeneralMatchers;

public class TableViewMatchersExtension {
    private static final String SELECTOR_TABLE_CELL = ".table-cell";

    private TableViewMatchersExtension() {
    }

    @Factory
    public static Matcher<TableView> containsRow(String[] row) {
        String descriptionText = "has row: " + Arrays.toString(row);
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, TableViewMatchersExtension::toText, (node) -> containsRow(node, row));
    }
    private static <T> boolean containsRow(TableView<T> tableView, String[] row) {
        if (tableView.getItems().isEmpty()) {
            return false;
        } else {
            Map<Integer, List<ObservableValue<?>>> rowValuesMap = new HashMap<>(tableView.getColumns().size());

            List rowValues;
            for(int j = 0; j < tableView.getItems().size(); ++j) {
                rowValues = getRowValues(tableView, j);
                rowValuesMap.put(j, rowValues);
            }

            // Custom bit
            List<List<String>> testList = new ArrayList<>();
            for(Map.Entry<Integer, List<ObservableValue<?>>> value : rowValuesMap.entrySet()) {
                List<String> entry = new ArrayList<>();
                for(ObservableValue<?> actualValue : value.getValue()) {
                    entry.add(actualValue.getValue().toString());
                }
                testList.add(entry);
            }
            List<String> entryRow = Arrays.asList(row);
            return testList.contains(entryRow);
            // End of custom bit
        }
    }

    @Factory
    public static Matcher<TableView> hasColumnWithID(String columnId) {
        String descriptionText = "has column title: " + columnId;
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, TableViewMatchersExtension::toText, (node) -> hasColumnWithID(node, columnId));
    }

    private static <T> boolean hasColumnWithID(TableView<T> tableView, String columnId) {
        for(TableColumn<?,?> column : tableView.getColumns()) {
            if(column.getId().equals(columnId)) return true;
        }
        return false;
    }

    private static List<ObservableValue<?>> getRowValues(TableView<?> tableView, int rowIndex) {
        Object rowObject = tableView.getItems().get(rowIndex);
        List<ObservableValue<?>> rowValues = new ArrayList(tableView.getColumns().size());

        for(int i = 0; i < tableView.getColumns().size(); ++i) {
            TableColumn<?, ?> column = (TableColumn)tableView.getColumns().get(i);
            CellDataFeatures cellDataFeatures = new CellDataFeatures(tableView, column, rowObject);
            rowValues.add(i, column.getCellValueFactory().call(cellDataFeatures));
        }

        return rowValues;
    }

    private static String toText(TableView<?> tableView) {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");

        for(int rowIndex = 0; rowIndex < tableView.getItems().size(); ++rowIndex) {
            joiner.add(toText(tableView, rowIndex));
        }

        return joiner.toString();
    }
    private static String toText(TableView<?> tableView, int rowIndex) {
        return '[' + (String)getRowValues(tableView, rowIndex).stream().map((observableValue) -> {
            return observableValue.getValue() == null ? "null" : observableValue.getValue().toString();
        }).collect(Collectors.joining(", ")) + ']';
    }
}

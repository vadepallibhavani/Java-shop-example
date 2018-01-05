package com.molotkov.extras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.testfx.api.FxAssert;
import org.testfx.matcher.base.GeneralMatchers;
import org.testfx.service.finder.NodeFinder;
import org.testfx.service.query.NodeQuery;


public class TableViewMatchersExtension {
    public static final int REPLACEMENT_VALUE = -1; // replaces NULL in getRowValues method

    private TableViewMatchersExtension() {
    }

    @Factory
    public static Matcher<TableView> hasNoTableCell(Object value) {
        String descriptionText = "has no table cell \"" + value + "\"";
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText,
                (tableView) -> toText(tableView) + "\nwhich does contain a cell with the given value",
                (node) -> hasNoTableCell(node, value));
    }

    private static boolean hasNoTableCell(TableView tableView, Object value) {
        NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
        NodeQuery nodeQuery = nodeFinder.from(new Node[]{tableView});
        return nodeQuery.lookup(".table-cell").match((cell) -> !hasCellValue((Cell) cell, value)).tryQuery().isPresent();
    }

    @Factory
    public static Matcher<TableView> containsRow(Object... row) {
        String descriptionText = "has row: " + Arrays.toString(row);
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, TableViewMatchersExtension::toText, (node) -> containsRow(node, row));
    }
    private static <T> boolean containsRow(TableView<T> tableView, Object... row) {
        if (tableView.getItems().isEmpty()) {
            return false;
        }

        Map<Integer, List<ObservableValue<?>>> rowValuesMap = new HashMap<>(tableView.getColumns().size());

        List rowValues;
        for(int j = 0; j < tableView.getItems().size(); ++j) {
            rowValues = getRowValues(tableView, j);
            rowValuesMap.put(j, rowValues);
        }

        List<List<Object>> testList = new ArrayList<>();
        for(Map.Entry<Integer, List<ObservableValue<?>>> value : rowValuesMap.entrySet()) {
            List<Object> entry = new ArrayList<>();
            for(ObservableValue<?> actualValue : value.getValue()) {
                entry.add(actualValue.getValue());
            }
            testList.add(entry);
        }
        List<Object> entryRow = Arrays.asList(row);
        return testList.contains(entryRow);
    }

    @Factory
    public static Matcher<TableView> hasColumnWithID(String columnId) {
        String descriptionText = "has column title(id): " + columnId;
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, (node) -> hasColumnWithID(node, columnId));
    }

    private static <T> boolean hasColumnWithID(TableView<T> tableView, String columnId) {
        for(TableColumn<?,?> column : tableView.getColumns()) {
            if(column.getId().equals(columnId)) return true;
        }
        return false;
    }

    @Factory
    public static Matcher<TableView> hasNoColumnWithID(String columnId) {
        String descriptionText = "has column title: " + columnId;
        return GeneralMatchers.typeSafeMatcher(TableView.class, descriptionText, (node) -> hasNoColumnWithID(node, columnId));
    }

    private static <T> boolean hasNoColumnWithID(TableView<T> tableView, String columnId) {
        for(TableColumn<?,?> column : tableView.getColumns()) {
            if(column.getId().equals(columnId)) return false;
        }
        return true;
    }

    private static List<ObservableValue<?>> getRowValues(TableView<?> tableView, int rowIndex) {
        Object rowObject = tableView.getItems().get(rowIndex);
        List<ObservableValue<?>> rowValues = new ArrayList(tableView.getColumns().size());

        for(int i = 0; i < tableView.getColumns().size(); ++i) {
            TableColumn<?, ?> column = tableView.getColumns().get(i);
            CellDataFeatures cellDataFeatures = new CellDataFeatures(tableView, column, rowObject);
            try {
                rowValues.add(i, column.getCellValueFactory().call(cellDataFeatures));
            } catch (NullPointerException ex) {
                final ObservableValue<Integer> replacement = new SimpleIntegerProperty(REPLACEMENT_VALUE).asObject();
                rowValues.add(i, replacement);
            }
        }

        return rowValues;
    }

    private static boolean hasCellValue(Cell cell, Object value) {
        return !cell.isEmpty() && hasItemValue(cell.getText(), value);
    }

    private static boolean hasItemValue(Object item, Object value) {
        if (item == null && value == null) {
            return true;
        } else if (item != null && value != null) {
            return Objects.equals(item, value) || Objects.equals(item.toString(), value) || value.toString() != null && Objects.equals(item.toString(), value.toString());
        } else {
            return false;
        }
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